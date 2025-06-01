package hu.martin.ems.vaadin.component.Order;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.IconProvider;
import hu.martin.ems.core.model.EmailAttachment;
import hu.martin.ems.core.model.EmailProperties;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.DownloadButton;
import hu.martin.ems.core.vaadin.EmsFilterableGridComponent;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.Order;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.EmailSendingApi;
import hu.martin.ems.vaadin.api.OrderApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
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
public class OrderList extends EmsFilterableGridComponent {

    private OrderApiClient orderApi = BeanProvider.getBean(OrderApiClient.class);

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

    private final PaginationSetting paginationSetting;
    Logger logger = LoggerFactory.getLogger(Order.class);


    @Autowired
    public OrderList(PaginationSetting paginationSetting) {
        OrderVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));
        this.grid = new PaginatedGrid<>(OrderVO.class);
        this.paginationSetting = paginationSetting;
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        setupOrderList();

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
            if(v.getValue() != null && to.getValue() != null && v.getValue().toEpochDay() > to.getValue().toEpochDay()){
                from.setValue(to.getValue());
            } else{}
        });
        to.addValueChangeListener(v -> {
//            if(v.getValue() != null && from.getValue() != null && v.getValue().toEpochDay() < from.getValue().toEpochDay()){
            if(v.getValue() != null && from.getValue() != null && v.getValue().toEpochDay() < from.getValue().toEpochDay()){
                to.setValue(from.getValue());
            } else{}
        });

        Button sendSftp = new Button("Send report to accountant via SFTP");
        sendSftp.addClickListener(event -> {
            EmsResponse response = orderApi.sendReportSFTPToAccountant(from.getValue(), to.getValue());
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
            Button editButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).EDIT_ICON));
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).PERMANENTLY_DELETE_ICON));
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            DownloadButton odtDownload = new DownloadButton(BeanProvider.getBean(IconProvider.class).create(IconProvider.ODT_FILE_ICON), "order_" + order.id + ".odt", () -> orderApi.createDocumentAsODT(order.original));
            DownloadButton pdfDownload = new DownloadButton(BeanProvider.getBean(IconProvider.class).create(IconProvider.PDF_FILE_ICON_ICON), "order_" + order.id + ".pdf", () -> orderApi.createDocumentAsPDF(order.original));

            Button sendEmail = new Button("Send email");
            sendEmail.addClickListener(event -> {
                EmsResponse pdfDocumentResponse = orderApi.createDocumentAsPDF(order.original);
                switch (pdfDocumentResponse.getCode()){
                    case 200:
                        break;
                    default:
                        Notification.show("Email sending failed: " + pdfDocumentResponse.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                }
                EmsResponse emailGenerationResponse = orderApi.generateEmail(order.original);
                String email;
                switch (emailGenerationResponse.getCode()){
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

            editButton.addClickListener(event -> {
                Map<String, List<String>> params = new HashMap<>();
                params.put("orderId", List.of(String.valueOf(order.id)));
                getUI().ifPresent(v -> v.navigate(order.customerOrSupplier.contains("(C) ") ? OrderCreateToCustomer.class : OrderFromSupplier.class, new QueryParameters(params)));
//                mainView.getContentLayout().removeAll();
//                mainView.getContentLayout().add(oc);
//                MainView.contentLayout.removeAll();
//                MainView.contentLayout.add(oc);
            });

            restoreButton.addClickListener(event -> {
                this.orderApi.restore(order.original);
                Notification.show("Order restored: " + order.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setupOrderList();
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                EmsResponse resp = this.orderApi.delete(order.original);
                switch (resp.getCode()){
                    case 200: {
                        Notification.show("Order deleted: " + order.original.getName())
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        updateGridItems();
                        break;
                    }
                    default: {
                        Notification.show(resp.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
                setupOrderList();
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.orderApi.permanentlyDelete(order.id);
                Notification.show("Order permanently deleted: " + order.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setupOrderList();
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();

            if (order.deleted == 0) {
                actions.add(editButton, deleteButton);
            } else {
                actions.add(permanentDeleteButton, restoreButton);
            }
            actions.add(odtDownload, pdfDownload, sendEmail);
            return actions;
        }).setAutoWidth(true).setFlexGrow(0);



        //endregion

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = !showDeleted;
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            OrderVO.showDeletedCheckboxFilter.replace("deleted", newValue);

            setupOrderList();
            updateGridItems();
        });

        setFilteringHeaderRow();
        updateGridItems();

        add(sftpLayout, sendSftp, showDeletedCheckbox, grid);
    }

    private void processEmailSendingResponse(EmsResponse emailSendingResponse){
        if(emailSendingResponse.getCode() == 200){
            Notification.show(emailSendingResponse.getDescription()).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        else{
            Notification.show(emailSendingResponse.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void setupOrderList() {
        EmsResponse response = orderApi.findAllWithDeleted();
        switch (response.getCode()){
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
                (idFilter.isEmpty() || orderVO.id.toString().equals(idFilter.getFilterText())) &&
                (customerOrSupplierFilter.isEmpty() || orderVO.customerOrSupplier.toLowerCase().contains(customerOrSupplierFilter.getFilterText().toLowerCase())) &&
                (paymentTypeFilter.isEmpty() || orderVO.paymentType.toLowerCase().contains(paymentTypeFilter.getFilterText().toLowerCase())) &&
                (stateFilter.isEmpty() || orderVO.state.toLowerCase().contains(stateFilter.getFilterText().toLowerCase())) &&
                (timeOfOrderFilter.isEmpty() || orderVO.timeOfOrder.toLowerCase().contains(timeOfOrderFilter.getFilterText().toLowerCase())) &&
                orderVO.filterExtraData()
        );
    }


    private Component filterField(TextField filterField, String title){
        VerticalLayout res = new VerticalLayout();
        res.getStyle().set("padding", "0px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");
        filterField.getStyle().set("display", "flex").set("width", "100%");
        NativeLabel titleLabel = new NativeLabel(title);
        res.add(titleLabel, filterField);
        res.setClassName("vaadin-header-cell-content");
        return res;
    }

    private void setFilteringHeaderRow(){
        idFilter = new TextFilteringHeaderCell("Search id...", this);
        customerOrSupplierFilter = new TextFilteringHeaderCell("Search customerOrSupplier...", this);
        paymentTypeFilter = new TextFilteringHeaderCell("Search payment type...", this);
        stateFilter = new TextFilteringHeaderCell("Search state...", this);
        timeOfOrderFilter = new TextFilteringHeaderCell("Search time of order...", this);

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                OrderVO.extraDataFilterMap.clear();
            }
            else{
                 OrderVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(idColumn).setComponent(filterField(idFilter, "ID"));
        filterRow.getCell(customerOrSupplierColumn).setComponent(filterField(customerOrSupplierFilter, "Customer"));
        filterRow.getCell(paymentTypeColumn).setComponent(filterField(paymentTypeFilter, "Payment type"));
        filterRow.getCell(stateColumn).setComponent(filterField(stateFilter, "State"));
        filterRow.getCell(timeOfOrderColumn).setComponent(filterField(timeOfOrderFilter, "Time of order"));
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
    }


    public void updateGridItems() {
        if(orderList == null){
            Notification.show("EmsError happened while getting orders")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            orderList = new ArrayList<>();
        }
        orderVOS = orderList.stream().map(OrderVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
    }

    public class OrderVO extends BaseVO {
        private Order original;
        private String state;
        private String customerOrSupplier;
        private String paymentType;
        private String timeOfOrder;
        private String name;

        public OrderVO(Order order) {
            super(order.id, order.getDeleted());
            this.original = order;
            this.id = order.getId();
            this.deleted = order.getDeleted();
            this.state = original.getState().getName();
            this.timeOfOrder = original.getTimeOfOrder().format(DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm:ss"));
            this.customerOrSupplier = original.getCustomer() == null ? "(S) " + original.getSupplier().getName() : "(C) " + original.getCustomer().getName();
            this.paymentType = original.getPaymentType().getName();
            this.name = original.getName();
        }
    }
}
