package hu.martin.ems.vaadin.component.Order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.EmailAttachment;
import hu.martin.ems.core.model.EmailProperties;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.model.Order;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.EmailSendingApi;
import hu.martin.ems.vaadin.api.OrderApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.firitin.components.DynamicFileDownloader;
import org.vaadin.firitin.components.orderedlayout.VHorizontalLayout;
import org.vaadin.firitin.components.orderedlayout.VVerticalLayout;
import org.vaadin.klaudeta.PaginatedGrid;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.*;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "order/list", layout = MainView.class)
@AnonymousAllowed
@NeedCleanCoding
public class OrderList extends VVerticalLayout {

    private OrderApiClient orderApi = BeanProvider.getBean(OrderApiClient.class);

    private EmailSendingApi emailSendingApi = BeanProvider.getBean(EmailSendingApi.class);
    private boolean showDeleted = false;
    private PaginatedGrid<OrderVO, String> grid;

    List<OrderVO> orderVOS;
    List<Order> orderList;

    Grid.Column<OrderVO> customerColumn;
    Grid.Column<OrderVO> paymentTypeColumn;
    Grid.Column<OrderVO> stateColumn;
    Grid.Column<OrderVO> timeOfOrderColumn;
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Grid.Column<OrderVO> extraData;
    private static String customerFilterText = "";
    private static String paymentTypeColumnFilterText = "";
    private static String stateFilterText = "";
    private static String timeOfOrderFilterText = "";

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
        updateGridItems();

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

        updateGridItems();

        customerColumn = grid.addColumn(v -> v.customer);
        paymentTypeColumn = grid.addColumn(v -> v.paymentType);
        stateColumn = grid.addColumn(v -> v.state);
        timeOfOrderColumn = grid.addColumn(v -> v.timeOfOrder);

        grid.addClassName("styling");
        grid.setPartNameGenerator(orderVo -> orderVo.deleted != 0 ? "deleted" : null);

        //region Options column
        extraData = this.grid.addComponentColumn(order -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            DynamicFileDownloader odtDownload = new DynamicFileDownloader("", "order_" + order.id + ".odt",
                    out -> orderApi.createDocumentAsODT(order.original, out)).asButton();
            odtDownload.getButton().setIcon(ODT_FILE.create());
            DynamicFileDownloader pdfDownload = new DynamicFileDownloader("", "order_" + order.id + ".pdf",
                    out -> orderApi.createDocumentAsPDF(order.original, out)).asButton();
            pdfDownload.getButton().setIcon(PDF_FILE.create());

            Button sendEmail = new Button("Send email");
            sendEmail.addClickListener(event -> {
                EmsResponse sendEmailResponse = emailSendingApi.send(
                        new EmailProperties(
                            order.original.getCustomer().getEmailAddress(),
                            "Megrendelés visszaigazolás",
                            orderApi.generateEmail(order.original),
                            List.of(new EmailAttachment(
                                    StaticDatas.ContentType.CONTENT_TYPE_APPLICATION_PDF,
                                    orderApi.createDocumentAsPDF(order.original, new ByteArrayOutputStream()),
                                    "order_" + order.id + ".pdf")
                            )
                        )
                );
                processEmailSendingResponse(sendEmailResponse);
            });

            editButton.addClickListener(event -> {
                OrderCreate.editObject = order.original;
                OrderCreate oc = new OrderCreate(paginationSetting);
                MainView.contentLayout.removeAll();
                MainView.contentLayout.add(oc);
            });

            restoreButton.addClickListener(event -> {
                this.orderApi.restore(order.original);
                Notification.show("Order restored: " + order.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setupOrderList();
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.orderApi.delete(order.original);
                Notification.show("Order deleted: " + order.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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

            VHorizontalLayout actions = new VHorizontalLayout();

            if (order.deleted == 0) {
                actions.add(editButton, deleteButton);
            } else {
                actions.add(permanentDeleteButton, restoreButton);
            }
            actions.add(odtDownload, pdfDownload, sendEmail);
            return actions;
        });

        setFilteringHeaderRow();

        //endregion

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = !showDeleted;
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            OrderVO.showDeletedCheckboxFilter.replace("deleted", newValue);

            updateGridItems();
        });

        add(sftpLayout, sendSftp, showDeletedCheckbox, grid);
    }

    private void processEmailSendingResponse(EmsResponse emailSendingResponse){
        Boolean success = emailSendingResponse.getCode() == 200;
        Boolean sendingSuccess = (Boolean) emailSendingResponse.getResponseData();
        if(success && sendingSuccess){
            Notification.show("Email sent!")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        else if(success){
            Notification.show("Email sending failed")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
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
                (customerFilterText.isEmpty() || orderVO.customer.toLowerCase().contains(customerFilterText.toLowerCase())) &&
                (paymentTypeColumnFilterText.isEmpty() || orderVO.paymentType.toLowerCase().contains(paymentTypeColumnFilterText.toLowerCase())) &&
                (stateFilterText.isEmpty() || orderVO.state.toLowerCase().contains(stateFilterText.toLowerCase())) &&
                (timeOfOrderFilterText.isEmpty() || orderVO.timeOfOrder.toLowerCase().contains(timeOfOrderFilterText.toLowerCase())) &&
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
        TextField customerFilter = new TextField();
        customerFilter.setPlaceholder("Search customer...");
        customerFilter.setClearButtonVisible(true);
        customerFilter.addValueChangeListener(event -> {
            customerFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField paymentTypeFilter = new TextField();
        paymentTypeFilter.setPlaceholder("Search payment type...");
        paymentTypeFilter.setClearButtonVisible(true);
        paymentTypeFilter.addValueChangeListener(event -> {
            paymentTypeColumnFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField stateFilter = new TextField();
        stateFilter.setPlaceholder("Search state...");
        stateFilter.setClearButtonVisible(true);
        stateFilter.addValueChangeListener(event -> {
            stateFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField timeOfOrderFilter = new TextField();
        timeOfOrderFilter.setPlaceholder("Search name...");
        timeOfOrderFilter.setClearButtonVisible(true);
        timeOfOrderFilter.addValueChangeListener(event -> {
            timeOfOrderFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                OrderVO.extraDataFilterMap.clear();
            }
            else{
                try {
                     OrderVO.extraDataFilterMap = new ObjectMapper().readValue(extraDataFilter.getValue().trim(), new TypeReference<LinkedHashMap<String, List<String>>>() {});
                } catch (JsonProcessingException ex) {
                    Notification.show("Invalid json in extra data filter field!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(customerColumn).setComponent(filterField(customerFilter, "Customer"));
        filterRow.getCell(paymentTypeColumn).setComponent(filterField(paymentTypeFilter, "Payment type"));
        filterRow.getCell(stateColumn).setComponent(filterField(stateFilter, "State"));
        filterRow.getCell(timeOfOrderColumn).setComponent(filterField(timeOfOrderFilter, "Time of order"));
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
    }


    private void updateGridItems() {
        if(orderList == null){
            Notification.show("Error happened while getting orders")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            orderList = new ArrayList<>();
        }
        orderVOS = orderList.stream().map(OrderVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().collect(Collectors.toList()));

    }


    @NeedCleanCoding
public class OrderVO extends BaseVO {
        private Order original;
        private String state;
        private String customer;
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
            this.customer = original.getCustomer().getName();
            this.paymentType = original.getPaymentType().getName();
            this.name = original.getName();
        }
    }
}
