package hu.martin.ems.vaadin.component.Supplier;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Address;
import hu.martin.ems.model.Supplier;
import hu.martin.ems.service.SupplierService;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.component.Creatable;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@Route(value = "supplier/list", layout = MainView.class)
@CssImport("./styles/grid.css")
public class SupplierList extends VerticalLayout implements Creatable<Supplier> {

    private final SupplierService supplierService;
    private boolean showDeleted = false;
    private PaginatedGrid<SupplierVO, String> grid;
    private final PaginationSetting paginationSetting;

    @Autowired
    public SupplierList(SupplierService supplierService,
                        PaginationSetting paginationSetting) {
        this.supplierService = supplierService;
        this.paginationSetting = paginationSetting;

        this.grid = new PaginatedGrid<>(SupplierVO.class);
        grid.addClassName("styling");
        List<Supplier> suppliers = supplierService.findAll(false);
        List<SupplierVO> data = suppliers.stream().map(SupplierVO::new).collect(Collectors.toList());
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");
        this.grid.removeColumnByKey("deleted");
        this.grid.removeColumnByKey("id");
        grid.setPartNameGenerator(supplierVO -> supplierVO.getDeleted() != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        //region Options column
        this.grid.addComponentColumn(supplier -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
                        Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog dialog = getSaveOrUpdateDialog(supplier.original);
                dialog.open();
            });

            restoreButton.addClickListener(event -> {
                this.supplierService.restore(supplier.getOriginal());
                Notification.show("Supplier restored: " + supplier.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.supplierService.delete(supplier.getOriginal());
                Notification.show("Supplier deleted: " + supplier.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.supplierService.permanentlyDelete(supplier.getOriginal());
                Notification.show("Supplier permanently deleted: " + supplier.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);


        add(hl, grid);
    }

    private void updateGridItems() {
        List<Supplier> suppliers = this.supplierService.findAll(showDeleted);
        this.grid.setItems(suppliers.stream().map(SupplierVO::new).collect(Collectors.toList()));
    }

    @Override
    public Dialog getSaveOrUpdateDialog(Supplier entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " supplier");
        FormLayout formLayout = new FormLayout();

        TextField nameField = new TextField("Name");

        ComboBox<Address> addresss = new ComboBox<>("Address");
        ComboBox.ItemFilter<Address> addressFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        addresss.setItems(addressFilter);
        addresss.setItemLabelGenerator(Address::getName);

        Button saveButton = new Button("Save");

        if (entity != null) {
            nameField.setValue(entity.getName());
            addresss.setValue(entity.getAddress());
        }

        saveButton.addClickListener(event -> {
            Supplier supplier = Objects.requireNonNullElseGet(entity, Supplier::new);
            supplier.setName(nameField.getValue());
            supplier.setAddress(addresss.getValue());
            supplier.setDeleted(0L);
            this.supplierService.saveOrUpdate(supplier);

            Notification.show("Supplier saved: " + supplier)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            nameField.clear();
            addresss.clear();
            createDialog.close();
        });

        formLayout.add(nameField, addresss, saveButton);
        createDialog.add(formLayout);
        return createDialog;
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
