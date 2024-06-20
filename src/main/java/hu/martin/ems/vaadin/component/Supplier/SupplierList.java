package hu.martin.ems.vaadin.component.Supplier;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Supplier;
import hu.martin.ems.service.SupplierService;
import hu.martin.ems.vaadin.MainView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "supplier/list", layout = MainView.class)
public class SupplierList extends VerticalLayout {

    private final SupplierService supplierService;
    private boolean showDeleted = false;
    private Grid<SupplierVO> grid;

    @Autowired
    public SupplierList(SupplierService supplierService) {
        this.supplierService = supplierService;

        this.grid = new Grid<>(SupplierVO.class);
        List<Supplier> suppliers = supplierService.findAll(false);
        List<SupplierVO> data = suppliers.stream().map(SupplierVO::new).collect(Collectors.toList());
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");
        this.grid.removeColumnByKey("deleted");
        this.grid.removeColumnByKey("id");

        //region Options column
        this.grid.addComponentColumn(supplier -> {
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            Button restoreButton = new Button("Restore");
            Button permanentDeleteButton = new Button("Permanently Delete");

            editButton.addClickListener(event -> {
                SupplierCreate.s = supplier.getOriginal();
                UI.getCurrent().navigate("supplier/create");
            });

            restoreButton.addClickListener(event -> {
                this.supplierService.restore(supplier.getOriginal());
                Notification.show("Supplier restored: " + supplier.getOriginal().getName());
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.supplierService.delete(supplier.getOriginal());
                Notification.show("Supplier deleted: " + supplier.getOriginal().getName());
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.supplierService.permanentlyDelete(supplier.getOriginal());
                Notification.show("Supplier permanently deleted: " + supplier.getOriginal().getName());
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (supplier.getOriginal().getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else if (supplier.getOriginal().getDeleted() == 1) {
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

        add(showDeletedCheckbox, grid);
    }

    private void updateGridItems() {
        List<Supplier> suppliers = this.supplierService.findAll(showDeleted);
        this.grid.setItems(suppliers.stream().map(SupplierVO::new).collect(Collectors.toList()));
    }

    @Getter
    public class SupplierVO {
        private Supplier original;
        private Long deleted;
        private Long id;
        private String address;
        private String name;

        public SupplierVO(Supplier supplier) {
            this.original = supplier;
            this.id = supplier.getId();
            this.deleted = supplier.getDeleted();
            this.name = original.getName();
            this.address = original.getAddress().getName();
        }
    }
}
