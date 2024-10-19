package hu.martin.ems.vaadin.component.Supplier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Address;
import hu.martin.ems.model.Supplier;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.AddressApiClient;
import hu.martin.ems.vaadin.api.SupplierApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@Route(value = "supplier/list", layout = MainView.class)
@CssImport("./styles/grid.css")
@AnonymousAllowed
@NeedCleanCoding
public class SupplierList extends VerticalLayout implements Creatable<Supplier> {

    private final SupplierApiClient supplierApi = BeanProvider.getBean(SupplierApiClient.class);
    private final AddressApiClient addressApi = BeanProvider.getBean(AddressApiClient.class);
    private boolean showDeleted = false;
    private PaginatedGrid<SupplierVO, String> grid;
    private final PaginationSetting paginationSetting;

    private List<Supplier> suppliers;
    private List<SupplierVO> supplierVOS;
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Grid.Column<SupplierVO> extraData;

    Grid.Column<SupplierVO> addressColumn;
    Grid.Column<SupplierVO> nameColumn;
    private static String addressFilterText = "";
    private static String nameFilterText = "";


    @Autowired
    public SupplierList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;

        SupplierVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(SupplierVO.class);
        grid.addClassName("styling");
        List<Supplier> suppliers = supplierApi.findAll();
        List<SupplierVO> data = suppliers.stream().map(SupplierVO::new).collect(Collectors.toList());

        this.grid.setItems(data);

        addressColumn = grid.addColumn(v -> v.address);
        nameColumn = grid.addColumn(v -> v.name);

        grid.setPartNameGenerator(supplierVO -> supplierVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());


        //region Options column
        extraData = this.grid.addComponentColumn(supplier -> {
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
                this.supplierApi.restore(supplier.original);
                Notification.show("Supplier restored: " + supplier.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.supplierApi.delete(supplier.original);
                Notification.show("Supplier deleted: " + supplier.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.supplierApi.permanentlyDelete(supplier.id);
                Notification.show("Supplier permanently deleted: " + supplier.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (supplier.deleted == 0) {
                actions.add(editButton, deleteButton);
            } else {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        });

        setFilteringHeaderRow();

        //endregion

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            SupplierVO.showDeletedCheckboxFilter.replace("deleted", newValue);

            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);


        add(hl, grid);
    }

    private Stream<SupplierVO> getFilteredStream() {
        return supplierVOS.stream().filter(supplierVO ->
                        (addressFilterText.isEmpty() || supplierVO.address.toLowerCase().contains(addressFilterText.toLowerCase())) &&
                        (nameFilterText.isEmpty() || supplierVO.name.toLowerCase().contains(nameFilterText.toLowerCase())) &&
                        supplierVO.filterExtraData()
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
        TextField addressFilter = new TextField();
        addressFilter.setPlaceholder("Search address...");
        addressFilter.setClearButtonVisible(true);
        addressFilter.addValueChangeListener(event -> {
            addressFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField nameFilter = new TextField();
        nameFilter.setPlaceholder("Search name...");
        nameFilter.setClearButtonVisible(true);
        nameFilter.addValueChangeListener(event -> {
            nameFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                SupplierVO.extraDataFilterMap.clear();
            }
            else{
                try {
                    SupplierVO.extraDataFilterMap = new ObjectMapper().readValue(extraDataFilter.getValue().trim(), new TypeReference<LinkedHashMap<String, List<String>>>() {});
                } catch (JsonProcessingException ex) {
                    Notification.show("Invalid json in extra data filter field!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();;
        filterRow.getCell(addressColumn).setComponent(filterField(addressFilter, "Address"));
        filterRow.getCell(nameColumn).setComponent(filterField(nameFilter, "Name"));
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
    }


    private void updateGridItems() {
        List<Supplier> suppliers = supplierApi.findAllWithDeleted();
        supplierVOS = suppliers.stream().map(SupplierVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
    }

    public Dialog getSaveOrUpdateDialog(Supplier entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " supplier");
        FormLayout formLayout = new FormLayout();

        TextField nameField = new TextField("Name");

        ComboBox<Address> addresses = new ComboBox<>("Address");
        ComboBox.ItemFilter<Address> addressFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        addresses.setItems(addressFilter, addressApi.findAll());
        addresses.setItemLabelGenerator(Address::getName);

        Button saveButton = new Button("Save");

        if (entity != null) {
            nameField.setValue(entity.getName());
            addresses.setValue(entity.getAddress());
        }

        saveButton.addClickListener(event -> {
            Supplier supplier = Objects.requireNonNullElseGet(entity, Supplier::new);
            supplier.setName(nameField.getValue());
            supplier.setAddress(addresses.getValue());
            supplier.setDeleted(0L);

            if(entity != null){
                supplierApi.update(supplier);
            }
            else{
                supplierApi.save(supplier);
            }


            Notification.show("Supplier " + (entity == null ? "saved: " : "updated: ") + supplier)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            nameField.clear();
            addresses.clear();
            createDialog.close();
            updateGridItems();
        });

        formLayout.add(nameField, addresses, saveButton);
        createDialog.add(formLayout);
        return createDialog;
    }

    @NeedCleanCoding
public class SupplierVO extends BaseVO {
        private Supplier original;
        private String address;
        private String name;

        public SupplierVO(Supplier supplier) {
            super(supplier.id, supplier.getDeleted());
            this.original = supplier;
            this.id = supplier.getId();
            this.deleted = supplier.getDeleted();
            this.name = original.getName();
            this.address = original.getAddress().getName();
        }
    }
}
