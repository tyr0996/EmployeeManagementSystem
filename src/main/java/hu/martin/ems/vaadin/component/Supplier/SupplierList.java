package hu.martin.ems.vaadin.component.Supplier;

import com.google.gson.Gson;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.IconProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.EmsFilterableGridComponent;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.Address;
import hu.martin.ems.model.Supplier;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.AddressApiClient;
import hu.martin.ems.vaadin.api.SupplierApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import hu.martin.ems.vaadin.core.EmsComboBox;
import hu.martin.ems.vaadin.core.EmsDialog;
import hu.martin.ems.vaadin.core.IEmsOptionColumnBaseDialogCreationForm;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route(value = "supplier/list", layout = MainView.class)
@CssImport("./styles/grid.css")
@RolesAllowed("ROLE_SupplierMenuOpenPermission")
@NeedCleanCoding
public class SupplierList extends EmsFilterableGridComponent implements Creatable<Supplier>, IEmsOptionColumnBaseDialogCreationForm<Supplier, SupplierList.SupplierVO> {

    @Getter
    private final SupplierApiClient apiClient = BeanProvider.getBean(SupplierApiClient.class);
    private final AddressApiClient addressApi = BeanProvider.getBean(AddressApiClient.class);
    private final Gson gson = BeanProvider.getBean(Gson.class);
    private boolean showDeleted = false;
    @Getter
    private PaginatedGrid<SupplierVO, String> grid;

    private List<Supplier> suppliers;
    private List<SupplierVO> supplierVOS;
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Grid.Column<SupplierVO> extraData;

    Grid.Column<SupplierVO> addressColumn;
    Grid.Column<SupplierVO> nameColumn;
    private TextFilteringHeaderCell addressFilter;
    private TextFilteringHeaderCell nameFilter;

    private Logger logger = LoggerFactory.getLogger(Supplier.class);
    List<Supplier> supplierList;
    List<Address> addressList;

    @Autowired

    //TODO nagyon ki kell javítani a update grid elements-et.
    public SupplierList(PaginationSetting paginationSetting) {

        SupplierVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(SupplierVO.class);
        grid.addClassName("styling");


        addressColumn = grid.addColumn(v -> v.address);
        nameColumn = grid.addColumn(v -> v.name);

        grid.setPartNameGenerator(supplierVO -> supplierVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());


        //region Options column
        extraData = this.grid.addComponentColumn(supplier -> {
            Button editButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).EDIT_ICON));
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).PERMANENTLY_DELETE_ICON));
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog dialog = getSaveOrUpdateDialog(supplier);
                dialog.open();
            });

            restoreButton.addClickListener(event -> {
                this.apiClient.restore(supplier.original);
                Notification.show("Supplier restored: " + supplier.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                EmsResponse resp = this.apiClient.delete(supplier.original);
                switch (resp.getCode()) {
                    case 200: {
                        Notification.show("Supplier deleted: " + supplier.original.getName())
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        updateGridItems();
                        break;
                    }
                    default: {
                        Notification.show(resp.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
                setEntities();
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.apiClient.permanentlyDelete(supplier.id);
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

        setEntities();
        List<SupplierVO> data = supplierList.stream().map(SupplierVO::new).collect(Collectors.toList());

        this.grid.setItems(data);
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

    public void setEntities() {
        EmsResponse response = apiClient.findAll();
        switch (response.getCode()) {
            case 200:
                supplierList = (List<Supplier>) response.getResponseData();
                break;
            default:
                supplierList = new ArrayList<>();
                logger.error("Supplier findAllByIds [currency]. Code: {}, Description: {}", response.getCode(), response.getDescription());
                Notification.show("EmsError happened while getting suppliers: " + response.getDescription())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
        }
    }

    private Stream<SupplierVO> getFilteredStream() {
        return supplierVOS.stream().filter(supplierVO ->
                filterField(addressFilter, supplierVO.address) &&
                filterField(nameFilter, supplierVO.name) &&
                supplierVO.filterExtraData()
        );
    }

    private void setFilteringHeaderRow() {
        addressFilter = new TextFilteringHeaderCell("Search address...", this);
        nameFilter = new TextFilteringHeaderCell("Search name...", this);

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if (extraDataFilter.getValue().isEmpty()) {
                SupplierVO.extraDataFilterMap.clear();
            } else {
                SupplierVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });


        HeaderRow filterRow = grid.appendHeaderRow();

        filterRow.getCell(addressColumn).setComponent(styleFilterField(addressFilter, "Address"));
        filterRow.getCell(nameColumn).setComponent(styleFilterField(nameFilter, "Name"));
        filterRow.getCell(extraData).setComponent(styleFilterField(extraDataFilter, ""));
    }


    public void updateGridItems() {
        EmsResponse response = apiClient.findAllWithDeleted();
        List<Supplier> suppliers;
        switch (response.getCode()) {
            case 200:
                suppliers = (List<Supplier>) response.getResponseData();
                break;
            default:
                Notification.show("EmsError happened while getting suppliers: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                suppliers = new ArrayList<>();
                break;
        }
        supplierVOS = suppliers.stream().map(SupplierVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
    }
    

    public EmsDialog getSaveOrUpdateDialog(SupplierVO entity) {
        Button saveButton = new Button("Save");
        EmsDialog createDialog = new EmsDialog((entity == null ? "Create" : "Modify") + " supplier");

        FormLayout formLayout = new FormLayout();

        TextField nameField = new TextField("Name");
        EmsComboBox<Address> addresses = new EmsComboBox<>("Address", this::setupAddresses, saveButton, "EmsError happened while getting addresses");

        if (entity != null) {
            nameField.setValue(entity.original.getName());
            addresses.setValue(entity.original.getAddress());
        }

        saveButton.addClickListener(event -> {
            Supplier supplier = entity == null ? new Supplier() : entity.original;
            supplier.setName(nameField.getValue());
            supplier.setAddress(addresses.getValue());
            supplier.setDeleted(0L);

            EmsResponse response = null;
            if (entity != null) {
                response = apiClient.update(supplier);
            } else {
                response = apiClient.save(supplier);
            }

            switch (response.getCode()) {
                case 200: {
                    Notification.show("Supplier " + (entity == null ? "saved: " : "updated: ") + ((Supplier) response.getResponseData()).getName())
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    break;
                }
                default: {
                    Notification.show("Supplier " + (entity == null ? "saving " : "modifying ") + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    createDialog.close();
                    updateGridItems();
                    return;
                }
            }

            nameField.clear();
            addresses.clear();
            createDialog.close();
            updateGridItems();
        });

        formLayout.add(nameField, addresses, saveButton);
        createDialog.add(formLayout);
        return createDialog;
    }

    private List<Address> setupAddresses() {
        EmsResponse response = addressApi.findAll();
        switch (response.getCode()) {
            case 200:
                addressList = (List<Address>) response.getResponseData();
                break;
            default:
                addressList = null;
                logger.error("Address findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
        return addressList;
    }

    @NeedCleanCoding
    public class SupplierVO extends BaseVO<Supplier> {
        private String address;
        private String name;

        public SupplierVO(Supplier supplier) {
            super(supplier.id, supplier.getDeleted(), supplier);
            this.id = supplier.getId();
            this.deleted = supplier.getDeleted();
            this.name = supplier.getName();
            this.address = supplier.getAddress().getName();
        }
    }
}
