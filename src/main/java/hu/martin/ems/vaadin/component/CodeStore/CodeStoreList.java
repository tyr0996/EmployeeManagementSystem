package hu.martin.ems.vaadin.component.CodeStore;

import com.google.gson.Gson;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.IconProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.EmsFilterableGridComponent;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CodeStoreApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "codestore/list", layout = MainView.class)
@RolesAllowed("ROLE_CodeStoreMenuOpenPermission")
@NeedCleanCoding
public class CodeStoreList extends EmsFilterableGridComponent implements Creatable<CodeStore> {

    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private Gson gson = BeanProvider.getBean(Gson.class);
    private boolean showDeleted = false;
    private boolean showOnlyDeletable = false;

    @Getter
    private PaginatedGrid<CodeStoreVO, String> grid;
    private final PaginationSetting paginationSetting;

    private List<CodeStore> codeStores;
    private List<CodeStoreVO> codeStoreVOS;

    Grid.Column<CodeStoreVO> nameColumn;
    Grid.Column<CodeStoreVO> parentColumn;
    Grid.Column<CodeStoreVO> extraData;

    private TextFilteringHeaderCell nameFilter;
    private TextFilteringHeaderCell parentFilter;
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Logger logger = LoggerFactory.getLogger(CodeStore.class);
    private MainView mainView;

    @Autowired
    public CodeStoreList(PaginationSetting paginationSetting) {
        this.mainView = mainView;
        this.paginationSetting = paginationSetting;


        CodeStoreVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(CodeStoreVO.class);
        this.grid.setId("page-grid");

        setupCodeStores();


        nameColumn = this.grid.addColumn(v -> v.name);
        parentColumn = this.grid.addColumn(v -> v.parentName);
        grid.addClassName("styling");
        grid.setPartNameGenerator(codeStore -> codeStore.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());


        extraData = this.grid.addComponentColumn(codeStoreVO -> {
            Button editButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).EDIT_ICON));
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).PERMANENTLY_DELETE_ICON));
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog dialog = getSaveOrUpdateDialog(codeStoreVO.original);
                dialog.open();
            });

            restoreButton.addClickListener(event -> {
                codeStoreApi.restore(codeStoreVO.original);
                Notification.show("CodeStore restored: " + codeStoreVO.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setupCodeStores();
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                EmsResponse resp = this.codeStoreApi.delete(codeStoreVO.original);
                switch (resp.getCode()){
                    case 200: {
                        Notification.show("Codestore deleted: " + codeStoreVO.original.getName())
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        updateGridItems();
                        break;
                    }
                    default: {
                        Notification.show(resp.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
                setupCodeStores();
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                EmsResponse response = codeStoreApi.permanentlyDelete(codeStoreVO.id);
                switch (response.getCode()) {
                    case 200: {
                        Notification.show("CodeStore permanently deleted: " + codeStoreVO.name)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        setupCodeStores();
                        updateGridItems();
                        break;
                    }
                    default:{
                        Notification.show("CodeStore permanently deletion failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
            });
            if (!codeStoreVO.deletable) {
                editButton.setEnabled(false);
                deleteButton.setEnabled(false);
                permanentDeleteButton.setEnabled(false);
                restoreButton.setEnabled(false);
            }

            HorizontalLayout actions = new HorizontalLayout();
            if (codeStoreVO.deleted == 0) {
                actions.add(editButton, deleteButton);
            } else {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        });

        setFilteringHeaderRow();
        updateGridItems();

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });
        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = !showDeleted;
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            CodeStoreVO.showDeletedCheckboxFilter.replace("deleted", newValue);
            updateGridItems();
        });
        Checkbox showOnlyDeletableCodeStores = new Checkbox("Show only deletable codestores");
        showOnlyDeletableCodeStores.addValueChangeListener(event -> {
            showOnlyDeletable = !showOnlyDeletable;
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, showOnlyDeletableCodeStores, grid);
    }

    private void setupCodeStores() {
        EmsResponse response = codeStoreApi.findAllWithDeleted();
        switch (response.getCode()){
            case 200:
                codeStores = (List<CodeStore>) response.getResponseData();
                break;
            default:
                codeStores = null;
                logger.error("CodeStore findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    public void updateGridItems() {
        if(codeStores == null){
            Notification.show("EmsError happened while getting codestores")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
        else{
            codeStoreVOS = codeStores.stream().map(CodeStoreVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        }
    }

    private Stream<CodeStoreVO> getFilteredStream() {
        return codeStoreVOS.stream().filter(codeStoreVO ->
                (nameFilter.isEmpty() || codeStoreVO.name.toLowerCase().equals(nameFilter.getFilterText().toLowerCase())) &&
                (parentFilter.isEmpty() || codeStoreVO.parentName.toLowerCase().contains(parentFilter.getFilterText().toLowerCase())) &&
                codeStoreVO.filterExtraData() &&
                (showOnlyDeletable ? codeStoreVO.deletable : true)
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
        nameFilter = new TextFilteringHeaderCell("Search name...", this);
        parentFilter = new TextFilteringHeaderCell("Search parent...", this);

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                CodeStoreVO.extraDataFilterMap.clear();
            }
            else{
                CodeStoreVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(nameColumn).setComponent(filterField(nameFilter, "Name"));
        filterRow.getCell(parentColumn).setComponent(filterField(parentFilter, "Parent"));
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
    }

    private void appendCloseButton(Dialog d){
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> d.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        d.getHeader().add(closeButton);
    }

    public Dialog getSaveOrUpdateDialog(CodeStore entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " codestore");

        appendCloseButton(createDialog);
        FormLayout formLayout = new FormLayout();

        TextField nameTextField = new TextField("Name");
        ComboBox<CodeStore> parentCodeStore = new ComboBox<>("Parent");
        ComboBox.ItemFilter<CodeStore> filter = (codeStore, filterString) ->
                codeStore.getName().toLowerCase().contains(filterString.toLowerCase());
        parentCodeStore.setItems(filter, codeStores);
        parentCodeStore.setItemLabelGenerator(CodeStore::getName);

        Checkbox deletable = new Checkbox("Deletable");

        if (entity != null) {
            nameTextField.setValue(entity.getName());
            parentCodeStore.setValue(entity.getParentCodeStore());
            deletable.setValue(entity.getDeletable());
        }

        Button saveButton = new Button("Save");

        saveButton.addClickListener(event -> {
            CodeStore codeStore = new CodeStore();
            codeStore.id = entity == null ? null : entity.getId();
            if(entity != null){
                codeStore.id = entity.getId();
            }
            codeStore.setLinkName(entity == null ? nameTextField.getValue() : entity.getLinkName());
            codeStore.setName(nameTextField.getValue());
            codeStore.setDeletable(deletable.getValue());
            codeStore.setDeleted(0L);
            codeStore.setParentCodeStore(parentCodeStore.getValue());
            EmsResponse response = null;
            if(entity == null){
                response = codeStoreApi.save(codeStore);
            }
            else{
                response = codeStoreApi.update(codeStore);
            }

            switch (response.getCode()){
                case 200:{
                    Notification.show("CodeStore " + (entity == null ? "saved: " : "updated: ") + ((CodeStore) response.getResponseData()).getName())
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    createDialog.close();
                    updateGridItems();
                    break;
                }
                default: {
                    Notification.show("CodeStore " + (entity == null ? "saving " : "modifying " ) + "failed: " + response.getDescription()).
                            addThemeVariants(NotificationVariant.LUMO_ERROR);
                    createDialog.close();
                    updateGridItems();
                    break;
                }
            }

            setupCodeStores();
            updateGridItems();

            nameTextField.clear();
            deletable.clear();
            parentCodeStore.clear();
            createDialog.close();
        });

        formLayout.add(nameTextField, parentCodeStore, deletable, saveButton);

        createDialog.add(formLayout);
        return createDialog;
    }

    public class CodeStoreVO extends BaseVO {
        private CodeStore original;
        private String name;
        private String parentName;
        private Boolean deletable;

        public CodeStoreVO(CodeStore codeStore){
            super(codeStore.id, codeStore.getDeleted());
            this.original = codeStore;
            this.name = codeStore.getName();
            this.parentName = codeStore.getParentCodeStore() == null ? "" : codeStore.getParentCodeStore().getName();
            this.deletable = codeStore.getDeletable();
        }
    }
}
