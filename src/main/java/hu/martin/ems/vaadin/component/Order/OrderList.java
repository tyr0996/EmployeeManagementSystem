package hu.martin.ems.vaadin.component.Order;

import com.google.gson.Gson;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.IconProvider;
import hu.martin.ems.core.model.EmailAttachment;
import hu.martin.ems.core.model.EmailProperties;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.*;
import hu.martin.ems.model.Order;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.EmailSendingApi;
import hu.martin.ems.vaadin.api.OrderApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.core.IEmsOptionColumnBase;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.vaadin.klaudeta.PaginatedGrid;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "order/list", layout = MainView.class)
@RolesAllowed("ROLE_OrderMenuOpenPermission")
@NeedCleanCoding
public class OrderList extends EmsFilterableGridComponent implements IEmsOptionColumnBase<Order, OrderList.OrderVO> {

    @Getter
    private OrderApiClient apiClient = BeanProvider.getBean(OrderApiClient.class);

    private EmailSendingApi emailSendingApi = BeanProvider.getBean(EmailSendingApi.class);
    private boolean showDeleted = false;
    @Getter
    private PaginatedGrid<OrderVO, String> grid;

    private Gson gson = BeanProvider.getBean(Gson.class);
    List<OrderVO> orderVOS;
    List<Order> orderList;

    Grid.Column<OrderVO> customerOrSupplierColumn;
    Grid.Column<OrderVO> paymentTypeColumn;
    Grid.Column<OrderVO> stateColumn;
    Grid.Column<OrderVO> timeOfOrderColumn;
    Grid.Column<OrderVO> idColumn;
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Grid.Column<OrderVO> extraData;
    private TextFilteringHeaderCell idFilter;
    private TextFilteringHeaderCell customerOrSupplierFilter;
    private TextFilteringHeaderCell paymentTypeFilter;
    private TextFilteringHeaderCell stateFilter;
    private TextFilteringHeaderCell timeOfOrderFilter;
    private ExtraDataFilterField extraDataFilter;

    Logger logger = LoggerFactory.getLogger(Order.class);
    LinkedHashMap<String, List<String>> showDeletedCheckboxFilter;

    @Override
    public void editButtonClickEvent(OrderVO element){
        Map<String, List<String>> params = new HashMap<>();
        params.put("orderId", List.of(String.valueOf(element.id)));
        getUI().ifPresent(v -> v.navigate(element.customerOrSupplier.contains("(C) ") ? OrderCreateToCustomer.class : OrderFromSupplier.class, new QueryParameters(params)));
    }

    @Autowired
    public OrderList(PaginationSetting paginationSetting) {
        showDeletedCheckboxFilter = new LinkedHashMap<>();
        showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));
        this.grid = new PaginatedGrid<>(OrderVO.class);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        setEntities();

        DatePicker from = new DatePicker("from");
        DatePicker to = new DatePicker("to");
        to.setValue(LocalDate.now());
        from.setValue(LocalDate.now());
        to.setMax(LocalDate.now());
        to.setMin(from.getValue());
        from.setMax(to.getValue());
        to.addValueChangeListener(event -> {
            from.setMax(event.getValue());
        });
        from.addValueChangeListener(event -> {
            to.setMin(event.getValue());
        });

        from.addValueChangeListener(v -> {
            if (v.getValue() != null && to.getValue() != null && v.getValue().toEpochDay() > to.getValue().toEpochDay()) {
                from.setValue(to.getValue());
            } else {
            }
        });
        to.addValueChangeListener(v -> {
            if (v.getValue() != null && from.getValue() != null && v.getValue().toEpochDay() < from.getValue().toEpochDay()) {
                to.setValue(from.getValue());
            } else {
            }
        });

        Button sendSftp = new Button("Send report to accountant via SFTP");
        sendSftp.addClickListener(event -> {
            EmsResponse response = apiClient.sendReportSFTPToAccountant(from.getValue(), to.getValue());
            Notification.show(response.getDescription()).addThemeVariants(
                    response.getCode() == 200 ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR
            );
        });
        HorizontalLayout sftpLayout = new HorizontalLayout();
        sftpLayout.add(sendSftp, from, to);

        idColumn = grid.addColumn(v -> v.id);
        customerOrSupplierColumn = grid.addColumn(v -> v.customerOrSupplier);
        paymentTypeColumn = grid.addColumn(v -> v.paymentType);
        stateColumn = grid.addColumn(v -> v.state);
        timeOfOrderColumn = grid.addColumn(v -> v.timeOfOrder);


        grid.addClassName("styling");
        grid.setPartNameGenerator(orderVo -> orderVo.deleted != 0 ? "deleted" : null);

        //region Options column
        extraData = this.grid.addComponentColumn(order -> {

            DownloadButton odtDownload = new DownloadButton(BeanProvider.getBean(IconProvider.class).create(IconProvider.ODT_FILE_ICON), "order_" + order.id + ".odt", () -> apiClient.createDocumentAsODT(order.original));
            DownloadButton pdfDownload = new DownloadButton(BeanProvider.getBean(IconProvider.class).create(IconProvider.PDF_FILE_ICON_ICON), "order_" + order.id + ".pdf", () -> apiClient.createDocumentAsPDF(order.original));

            Button sendEmail = new Button("Send email");
            sendEmail.addClickListener(event -> {
                EmsResponse pdfDocumentResponse = apiClient.createDocumentAsPDF(order.original);
                switch (pdfDocumentResponse.getCode()) {
                    case 200:
                        break;
                    default:
                        Notification.show("Email sending failed: " + pdfDocumentResponse.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                }
                EmsResponse emailGenerationResponse = apiClient.generateEmail(order.original);
                String email;
                switch (emailGenerationResponse.getCode()) {
                    case 200:
                        email = new String(((String) emailGenerationResponse.getResponseData()));
                        break;
                    default:
                        Notification.show("Email generation failed" + emailGenerationResponse.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                }
                EmsResponse sendEmailResponse = emailSendingApi.send(
                        new EmailProperties(
                                order.original.getCustomer().getEmailAddress(),
                                "Megrendelés visszaigazolás",
                                email,
                                List.of(new EmailAttachment(
                                        MediaType.APPLICATION_PDF_VALUE,
                                        ((ByteArrayInputStream) pdfDocumentResponse.getResponseData()).readAllBytes(),
                                        "order_" + order.id + ".pdf")
                                )
                        )
                );
                processEmailSendingResponse(sendEmailResponse);

            });
            return createOptionColumn("Order", order, new DownloadButton[]{odtDownload, pdfDownload}, new Button[]{sendEmail});
        }).setAutoWidth(true).setFlexGrow(0);


        //endregion

        Switch showDeletedSwitch = new Switch("Show deleted");
        showDeletedSwitch.addClickListener(event -> {
            showDeleted = event.getSource().getValue();
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            showDeletedCheckboxFilter.replace("deleted", newValue);

            setEntities();
            updateGridItems();
        });

        setFilteringHeaderRow();
        updateGridItems();

        add(sftpLayout, sendSftp, showDeletedSwitch, grid);
    }

    private void processEmailSendingResponse(EmsResponse emailSendingResponse) {
        if (emailSendingResponse.getCode() == 200) {
            Notification.show(emailSendingResponse.getDescription()).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification.show(emailSendingResponse.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    @Override
    public void setEntities() {
        EmsResponse response = apiClient.findAllWithDeleted();
        switch (response.getCode()) {
            case 200:
                orderList = (List<Order>) response.getResponseData();
                break;
            default:
                orderList = null;
                logger.error("Order findAllWithDeletedError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }


    private Stream<OrderVO> getFilteredStream() {
        return orderVOS.stream().filter(orderVO ->
                filterField(idFilter, orderVO.id.toString()) &&
                        filterField(customerOrSupplierFilter, orderVO.customerOrSupplier) &&
                        filterField(paymentTypeFilter, orderVO.paymentType) &&
                        filterField(stateFilter, orderVO.state) &&
                        filterField(timeOfOrderFilter, orderVO.timeOfOrder) &&
                        filterExtraData(extraDataFilter, orderVO, showDeletedCheckboxFilter));
    }

    private void setFilteringHeaderRow() {
        idFilter = new TextFilteringHeaderCell("Search id...", this);
        customerOrSupplierFilter = new TextFilteringHeaderCell("Search customerOrSupplier...", this);
        paymentTypeFilter = new TextFilteringHeaderCell("Search payment type...", this);
        stateFilter = new TextFilteringHeaderCell("Search state...", this);
        timeOfOrderFilter = new TextFilteringHeaderCell("Search time of order...", this);
        extraDataFilter = new ExtraDataFilterField("", this);

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(idColumn).setComponent(styleFilterField(idFilter, "ID"));
        filterRow.getCell(customerOrSupplierColumn).setComponent(styleFilterField(customerOrSupplierFilter, "Customer"));
        filterRow.getCell(paymentTypeColumn).setComponent(styleFilterField(paymentTypeFilter, "Payment type"));
        filterRow.getCell(stateColumn).setComponent(styleFilterField(stateFilter, "State"));
        filterRow.getCell(timeOfOrderColumn).setComponent(styleFilterField(timeOfOrderFilter, "Time of order"));
        filterRow.getCell(extraData).setComponent(styleFilterField(extraDataFilter, ""));
    }

    public void updateGridItems() {
        if (orderList == null) {
            Notification.show("EmsError happened while getting orders")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            orderList = new ArrayList<>();
        }
        orderVOS = orderList.stream().map(OrderVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
    }

    public class OrderVO extends BaseVO<Order> {
        private String state;
        private String customerOrSupplier;
        private String paymentType;
        private String timeOfOrder;
        private String name;

        public OrderVO(Order order) {
            super(order.id, order.getDeleted(), order);
            this.state = original.getState().getName();
            this.timeOfOrder = original.getTimeOfOrder().format(DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm:ss"));
            this.customerOrSupplier = original.getCustomer() == null ? "(S) " + original.getSupplier().getName() : "(C) " + original.getCustomer().getName();
            this.paymentType = original.getPaymentType().getName();
            this.name = original.getName();
        }
    }
}
