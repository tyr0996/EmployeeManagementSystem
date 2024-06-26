package hu.martin.ems.vaadin.component.Order;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.EmailAttachment;
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

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "order/list", layout = MainView.class)
public class OrderList extends VVerticalLayout {

    private final OrderService orderService;
    private final CurrencyService currencyService;
    private final EmailSendingService emailSendingService;
    private boolean showDeleted = false;
    private Grid<OrderVO> grid;

    @Autowired
    public OrderList(OrderService orderService,
                     CurrencyService currencyService,
                     EmailSendingService emailSendingService) {
        this.orderService = orderService;
        this.currencyService = currencyService;
        this.emailSendingService = emailSendingService;

        this.grid = new Grid<>(OrderVO.class);
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

        //region Options column
        this.grid.addComponentColumn(order -> {
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            Button restoreButton = new Button("Restore");
            Button permanentDeleteButton = new Button("Permanently Delete");
            DynamicFileDownloader odtDownload = new DynamicFileDownloader("Save to ODT", "order_" + order.getId() + ".odt",
                    out -> orderService.writeAsOdt(order.getOriginal(), out));
            DynamicFileDownloader pdfDownload = new DynamicFileDownloader("Save to PDF", "order_" + order.getId() + ".pdf",
                    out -> orderService.writeAsPdf(order.getOriginal(), out));
            pdfDownload.addComponentAsFirst(VaadinIcon.DOWNLOAD_ALT.create());

            Button sendEmail = new Button("Send email");
            sendEmail.addClickListener(event -> {
                emailSendingService.send(order.getOriginal().getCustomer().getEmailAddress(),
                        orderService.generateHTMLEmail(order.getOriginal()),
                        "Megrendelés visszaigazolás",
                        List.of(new EmailAttachment(
                                StaticDatas.ContentType.CONTENT_TYPE_APPLICATION_PDF,
                                orderService.writeAsPdf(order.getOriginal(), new ByteArrayOutputStream()),
                                "order_" + order.getId() + ".pdf")));
            });

            editButton.addClickListener(event -> {
                OrderCreate.o = order.getOriginal();
                UI.getCurrent().navigate("order/create");
            });

            restoreButton.addClickListener(event -> {
                this.orderService.restore(order.getOriginal());
                Notification.show("Order restored: " + order.getOriginal().getName());
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.orderService.delete(order.getOriginal());
                Notification.show("Order deleted: " + order.getOriginal().getName());
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.orderService.permanentlyDelete(order.getOriginal());
                Notification.show("Order permanently deleted: " + order.getOriginal().getName());
                updateGridItems();
            });

            VHorizontalLayout actions = new VHorizontalLayout();
            actions.add(odtDownload.asButton(), pdfDownload.asButton(), sendEmail);
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
