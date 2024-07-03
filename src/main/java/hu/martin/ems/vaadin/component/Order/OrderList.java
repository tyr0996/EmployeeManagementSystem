package hu.martin.ems.vaadin.component.Order;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.EmailAttachment;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.service.EmailSendingService;
import hu.martin.ems.model.Order;
import hu.martin.ems.service.CurrencyService;
import hu.martin.ems.service.OrderService;
import hu.martin.ems.vaadin.MainView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.firitin.components.DynamicFileDownloader;
import org.vaadin.firitin.components.orderedlayout.VHorizontalLayout;
import org.vaadin.firitin.components.orderedlayout.VVerticalLayout;
import org.vaadin.klaudeta.PaginatedGrid;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static hu.martin.ems.core.config.StaticDatas.Icons.*;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "order/list", layout = MainView.class)
public class OrderList extends VVerticalLayout {

    private final OrderService orderService;
    private final CurrencyService currencyService;
    private final EmailSendingService emailSendingService;
    private boolean showDeleted = false;
    private PaginatedGrid<OrderVO, String> grid;
    private final PaginationSetting paginationSetting;

    @Autowired
    public OrderList(OrderService orderService,
                     CurrencyService currencyService,
                     EmailSendingService emailSendingService,
                     PaginationSetting paginationSetting) {
        this.orderService = orderService;
        this.currencyService = currencyService;
        this.emailSendingService = emailSendingService;
        this.paginationSetting = paginationSetting;
        this.grid = new PaginatedGrid<>(OrderVO.class);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        List<Order> orders = orderService.findAll(false);
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

        Button sendSftp = new Button("Send report to accountant via SFTP");
        sendSftp.addClickListener(event -> {
            orderService.sendReport(from.getValue(), to.getValue());
        });
        HorizontalLayout sftpLayout = new HorizontalLayout();
        sftpLayout.add(sendSftp, from, to);

        List<OrderVO> data = orders.stream().map(OrderVO::new).collect(Collectors.toList());
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");
        this.grid.removeColumnByKey("id");
        this.grid.removeColumnByKey("deleted");
        grid.addClassName("styling");
        grid.setPartNameGenerator(orderVo -> orderVo.getDeleted() != 0 ? "deleted" : null);

        //region Options column
        this.grid.addComponentColumn(order -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            DynamicFileDownloader odtDownload = new DynamicFileDownloader("", "order_" + order.getId() + ".odt",
                    out -> orderService.writeAsOdt(order.getOriginal(), out)).asButton();
            odtDownload.getButton().setIcon(ODT_FILE.create());
            DynamicFileDownloader pdfDownload = new DynamicFileDownloader("", "order_" + order.getId() + ".pdf",
                    out -> orderService.writeAsPdf(order.getOriginal(), out)).asButton();
            pdfDownload.getButton().setIcon(PDF_FILE.create());

            Button sendEmail = new Button("Send email");
            sendEmail.addClickListener(event -> {
                boolean success = emailSendingService.send(order.getOriginal().getCustomer().getEmailAddress(),
                        orderService.generateHTMLEmail(order.getOriginal()),
                        "Megrendelés visszaigazolás",
                        List.of(new EmailAttachment(
                                StaticDatas.ContentType.CONTENT_TYPE_APPLICATION_PDF,
                                orderService.writeAsPdf(order.getOriginal(), new ByteArrayOutputStream()),
                                "order_" + order.getId() + ".pdf")));
                Notification.show(success ? "Email sikeresen elküldve!" : "Az email küldése sikertelen volt!")
                            .addThemeVariants(success ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR);
            });

            editButton.addClickListener(event -> {
                OrderCreate.o = order.getOriginal();
                UI.getCurrent().navigate("order/create");
            });

            restoreButton.addClickListener(event -> {
                this.orderService.restore(order.getOriginal());
                Notification.show("Order restored: " + order.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.orderService.delete(order.getOriginal());
                Notification.show("Order deleted: " + order.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.orderService.permanentlyDelete(order.getOriginal());
                Notification.show("Order permanently deleted: " + order.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            VHorizontalLayout actions = new VHorizontalLayout();

            actions.add(odtDownload, pdfDownload, sendEmail);
            if (order.getOriginal().getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else if (order.getOriginal().getDeleted() == 1) {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        }).setHeader("Options");

        //endregion

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            updateGridItems();
        });

        add(sftpLayout, sendSftp, grid);
    }

    private void updateGridItems() {
        List<Order> orders = this.orderService.findAll(showDeleted);
        this.grid.setItems(orders.stream().map(OrderVO::new).collect(Collectors.toList()));
    }

    @Getter
    public class OrderVO {
        private Order original;
        private Long deleted;
        private Long id;
        private String state;
        private String customer;
        private String paymentType;
        private String timeOfOrder;

        public OrderVO(Order order) {
            this.original = order;
            this.id = order.getId();
            this.deleted = order.getDeleted();
            this.state = original.getState().getName();
            this.timeOfOrder = original.getTimeOfOrder().format(DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm:ss"));
            this.customer = original.getCustomer().getName();
            this.paymentType = original.getPaymentType().getName();
        }
    }
}
