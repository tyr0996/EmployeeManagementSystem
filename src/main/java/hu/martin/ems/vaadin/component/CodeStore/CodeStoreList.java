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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CodeStoreApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import jakarta.annotation.security.RolesAllowed;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "codestore/list", layout = MainView.class)
@RolesAllowed("ROLE_CodeStoreMenuOpenPermission")
@NeedCleanCoding
public class CodeStoreList extends VerticalLayout implements Creatable<CodeStore> {

    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private Gson gson = BeanProvider.getBean(Gson.class);
    private boolean showDeleted = false;
    private boolean showOnlyDeletable = false;
    private PaginatedGrid<CodeStoreVO, ?> grid;
    private final PaginationSetting paginationSetting;

    private List<CodeStore> codeStores;
    private List<CodeStoreVO> codeStoreVOS;

    Grid.Column<CodeStoreVO> nameColumn;
    Grid.Column<CodeStoreVO> parentColumn;
    Grid.Column<CodeStoreVO> extraData;

    private static String nameColumnFilterText = "";
    private static String parentColumnFilterText = "";
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Logger logger = LoggerFactory.getLogger(CodeStore.class);
    private MainView mainView;

    @Autowired
    public CodeStoreList(PaginationSetting paginationSetting) {
        this.mainView = mainView;
        this.paginationSetting = paginationSetting;


        CodeStoreVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(CodeStoreVO.class);

        setupCodeStores();
        updateGridItems();

        nameColumn = this.grid.addColumn(v -> v.name);
        parentColumn = this.grid.addColumn(v -> v.parentName);
        grid.addClassName("styling");
        grid.setPartNameGenerator(codeStore -> codeStore.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());


        extraData = this.grid.addComponentColumn(codeStoreVO -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
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
                codeStoreApi.delete(codeStoreVO.original);
                Notification.show("CodeStore deleted: " + codeStoreVO.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setupCodeStores();
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                codeStoreApi.permanentlyDelete(codeStoreVO.id);
                Notification.show("CodeStore permanently deleted: " + codeStoreVO.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setupCodeStores();
                updateGridItems();
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

    private void updateGridItems() {
        if(codeStores == null){
            Notification.show("Error happened while getting codestores")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
        else{
            codeStoreVOS = codeStores.stream().map(CodeStoreVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        }
    }

    private Stream<CodeStoreVO> getFilteredStream() {
        return codeStoreVOS.stream().filter(codeStoreVO ->
                (nameColumnFilterText.isEmpty() || codeStoreVO.name.toLowerCase().equals(nameColumnFilterText.toLowerCase())) && //TODO: lehet, hogy erre érdemes lenne felhívni a felhasználó figyelmét, hogy az ebben történő szűrés teljes egyezést néz
                        (parentColumnFilterText.isEmpty() || codeStoreVO.parentName.toLowerCase().contains(parentColumnFilterText.toLowerCase())) &&
                        //(showDeleted ? (codeStoreVO.deleted == 0 || codeStoreVO.deleted == 1) : codeStoreVO.deleted == 0) &&
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
        TextField nameFilter = new TextField();
        nameFilter.setPlaceholder("Search name...");
        nameFilter.setClearButtonVisible(true);
        nameFilter.addValueChangeListener(event -> {
            nameColumnFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField parentColumnFilter = new TextField();
        parentColumnFilter.setPlaceholder("Search parent...");
        parentColumnFilter.setClearButtonVisible(true);
        parentColumnFilter.addValueChangeListener(event -> {
            parentColumnFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

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
        filterRow.getCell(parentColumn).setComponent(filterField(parentColumnFilter, "Parent"));
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
    }

    public Dialog getSaveOrUpdateDialog(CodeStore entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " codestore");
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
                    break;
                }
                default: {
                    Notification.show("CodeStore " + (entity == null ? "saving " : "modifying " ) + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
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
