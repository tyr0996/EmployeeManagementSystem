package hu.martin.ems.vaadin.component.OrderElement;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.service.OrderElementService;
import hu.martin.ems.vaadin.MainView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "orderElement/list", layout = MainView.class)
public class OrderElementList extends VerticalLayout {

    private final OrderElementService orderElementService;
    private boolean showDeleted = false;
    private Grid<OrderElementVO> grid;

    @Autowired
    public OrderElementList(OrderElementService orderElementService) {
        this.orderElementService = orderElementService;

        this.grid = new Grid<>(OrderElementVO.class);
        List<OrderElement> orderElements = orderElementService.findAll(false);
        List<OrderElementVO> data = orderElements.stream().map(OrderElementVO::new).collect(Collectors.toList());
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");
        this.grid.removeColumnByKey("id");
        this.grid.removeColumnByKey("deleted");

        //region Options column
        this.grid.addComponentColumn(orderElement -> {
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            Button restoreButton = new Button("Restore");
            Button permanentDeleteButton = new Button("Permanently Delete");

            editButton.addClickListener(event -> {
                OrderElementCreate.o = orderElement.getOriginal();
                UI.getCurrent().navigate("orderElement/create");
            });

            restoreButton.addClickListener(event -> {
                this.orderElementService.restore(orderElement.getOriginal());
                Notification.show("OrderElement restored: " + orderElement.getOriginal().getName());
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.orderElementService.delete(orderElement.getOriginal());
                Notification.show("OrderElement deleted: " + orderElement.getOriginal().getName());
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.orderElementService.permanentlyDelete(orderElement.getOriginal());
                Notification.show("OrderElement permanently deleted: " + orderElement.getOriginal().getName());
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if(orderElement.getOriginal().getDeleted() == 0){ actions.add(editButton, deleteButton); }
            else if(orderElement.getOriginal().getDeleted() == 1){ actions.add(permanentDeleteButton, restoreButton); }
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
        List<OrderElement> orderElements = this.orderElementService.findAll(showDeleted);
        this.grid.setItems(orderElements.stream().map(OrderElementVO::new).collect(Collectors.toList()));
    }

    @Getter
    public class OrderElementVO {
        private OrderElement original;
        private Long deleted;
        private Long id;
        private String product;
        private String order;
        private String taxKey;
        private Integer unit;
        private Integer unitNetPrice;
        private Integer netPrice;
        private Integer grossPrice;

        public OrderElementVO(OrderElement orderElement) {
            this.original = orderElement;
            this.id = orderElement.getId();
            this.deleted = orderElement.getDeleted();
            this.product = original.getProduct().getName();
            this.order = original.getOrder() == null ? "" : original.getOrder().getName();
            this.unit = original.getUnit();
            this.unitNetPrice = original.getUnitNetPrice();
            this.taxKey = original.getTaxKey().getName() + "%";
            this.netPrice = original.getNetPrice();
            this.grossPrice = original.getGrossPrice();
        }
    }
}
