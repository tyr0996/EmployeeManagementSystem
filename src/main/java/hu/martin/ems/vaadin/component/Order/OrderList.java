package hu.martin.ems.vaadin.component.Order;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Order;
import hu.martin.ems.service.OrderService;
import hu.martin.ems.vaadin.MainView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "order/list", layout = MainView.class)
public class OrderList extends VerticalLayout {

    private final OrderService orderService;
    private boolean showDeleted = false;
    private Grid<OrderVO> grid;

    @Autowired
    public OrderList(OrderService orderService) {
        this.orderService = orderService;

        this.grid = new Grid<>(OrderVO.class);
        List<Order> orders = orderService.findAll(false);
        List<OrderVO> data = orders.stream().map(OrderVO::new).toList();
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

            HorizontalLayout actions = new HorizontalLayout();
            if(order.getOriginal().getDeleted() == 0){ actions.add(editButton, deleteButton); }
            else if(order.getOriginal().getDeleted() == 1){ actions.add(permanentDeleteButton, restoreButton); }
            return actions;
        }).setHeader("Options");

        //endregion

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            updateGridItems();
        });

        add(showDeletedCheckbox, grid);
    }

    private void updateGridItems() {
        List<Order> orders = this.orderService.findAll(showDeleted);
        this.grid.setItems(orders.stream().map(OrderVO::new).toList());
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
