package hu.martin.ems.vaadin.component.CodeStore;

import com.google.gson.Gson;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.EmsFilterableGridComponent;
import hu.martin.ems.core.vaadin.ExtraDataFilterField;
import hu.martin.ems.core.vaadin.Switch;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CodeStoreApiClient;
import hu.martin.ems.vaadin.component.BaseVOWithDeletable;
import hu.martin.ems.vaadin.component.Creatable;
import hu.martin.ems.vaadin.core.EmsComboBox;
import hu.martin.ems.vaadin.core.EmsDialog;
import hu.martin.ems.vaadin.core.IEmsOptionColumnBaseDialogCreationFormDeletable;
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
public class CodeStoreList extends EmsFilterableGridComponent<CodeStoreList.CodeStoreVO> implements Creatable<CodeStore>, IEmsOptionColumnBaseDialogCreationFormDeletable<CodeStore, CodeStoreList.CodeStoreVO> {

    @Getter
    private final CodeStoreApiClient apiClient = BeanProvider.getBean(CodeStoreApiClient.class);
    private Gson gson = BeanProvider.getBean(Gson.class);
    private boolean showDeleted = false;
    private boolean showOnlyDeletable = false;

    @Getter
    public PaginatedGrid<CodeStoreVO, String> grid;
    private List<CodeStore> codeStores;
    private List<CodeStoreVO> codeStoreVOS;

    Grid.Column<CodeStoreVO> nameColumn;
    Grid.Column<CodeStoreVO> parentColumn;
    Grid.Column<CodeStoreVO> extraData;

    private TextFilteringHeaderCell nameFilter;
    private TextFilteringHeaderCell parentFilter;
    private ExtraDataFilterField extraDataFilter;
    private Logger logger = LoggerFactory.getLogger(CodeStore.class);

    private LinkedHashMap<String, List<String>> showDeletedCheckboxFilter;

    @Autowired
    public CodeStoreList(PaginationSetting paginationSetting) {
        showDeletedCheckboxFilter = new LinkedHashMap<>();
        showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(CodeStoreVO.class);
        this.grid.setId("page-grid");

        setEntities();

        nameColumn = this.grid.addColumn(v -> v.name);
        parentColumn = this.grid.addColumn(v -> v.parentName);
        grid.addClassName("styling");
        grid.setPartNameGenerator(codeStore -> codeStore.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        extraData = this.grid.addComponentColumn(codeStoreVO -> createOptionColumn("CodeStore", codeStoreVO));

        setFilteringHeaderRow();
        updateGridItems();

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });
        Switch showDeletedSwitch = new Switch("Show deleted");
        showDeletedSwitch.addClickListener(event -> {
//            showDeleted = event.getSource().getValue();
            showDeleted = !showDeleted;
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            showDeletedCheckboxFilter.replace("deleted", newValue);
            setEntities();
            updateGridItems();
        });
        Switch showOnlyDeletableCodeStores = new Switch("Show only deletable codestores");
        showOnlyDeletableCodeStores.addClickListener(event -> {
            showOnlyDeletable = !showOnlyDeletable;
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedSwitch, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedSwitch);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, showOnlyDeletableCodeStores, grid);
    }

    public void setEntities() {
        setCodeStores();
    }

    public List<CodeStore> setCodeStores(){
        EmsResponse response = apiClient.findAllWithDeleted();
        switch (response.getCode()) {
            case 200:
                codeStores = (List<CodeStore>) response.getResponseData();
                break;
            default:
                codeStores = null;
                logger.error("CodeStore findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
        return codeStores;
    }

    public void updateGridItems() {
        if (codeStores == null) {
            Notification.show("EmsError happened while getting codestores")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            codeStoreVOS = codeStores.stream().map(CodeStoreVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        }
    }

    private Stream<CodeStoreVO> getFilteredStream() {
        return codeStoreVOS.stream().filter(codeStoreVO ->
                filterField(nameFilter, codeStoreVO.name) &&
                filterField(parentFilter, codeStoreVO.parentName) &&
                filterExtraData(extraDataFilter, codeStoreVO, showDeletedCheckboxFilter) && (showOnlyDeletable ? codeStoreVO.deletable : true)
        );
    }

    private void setFilteringHeaderRow() {
        nameFilter = new TextFilteringHeaderCell("Search name...", this);
        parentFilter = new TextFilteringHeaderCell("Search parent...", this);
        extraDataFilter = new ExtraDataFilterField("", this);

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(nameColumn).setComponent(styleFilterField(nameFilter, "Name"));
        filterRow.getCell(parentColumn).setComponent(styleFilterField(parentFilter, "Parent"));
        filterRow.getCell(extraData).setComponent(styleFilterField(extraDataFilter, ""));
    }

    public EmsDialog getSaveOrUpdateDialog(CodeStoreVO entity) {
        EmsDialog createDialog = new EmsDialog((entity == null ? "Create" : "Modify") + " codestore");

        FormLayout formLayout = new FormLayout();
        Button saveButton = new Button("Save");

        TextField nameTextField = new TextField("Name");
        EmsComboBox<CodeStore> parentCodeStore = new EmsComboBox<>("Parent", this::setCodeStores, saveButton, "EmsError happened while getting codestores");

        Switch deletable = new Switch("Deletable");

        if (entity != null) {
            nameTextField.setValue(entity.original.getName());
            parentCodeStore.setValue(entity.original.getParentCodeStore());
            deletable.setValue(entity.original.getDeletable());
        }



        saveButton.addClickListener(event -> {
            CodeStore codeStore = new CodeStore();
            codeStore.id = entity == null ? null : entity.original.getId();
            codeStore.setLinkName(entity == null ? nameTextField.getValue() : entity.original.getLinkName());
            codeStore.setName(nameTextField.getValue());
            codeStore.setDeletable(deletable.getValue());
            codeStore.setDeleted(0L);
            codeStore.setParentCodeStore(parentCodeStore.getValue());
            EmsResponse response = null;
            if (entity == null) {
                response = apiClient.save(codeStore);
            } else {
                response = apiClient.update(codeStore);
            }

            switch (response.getCode()) {
                case 200: {
                    Notification.show("CodeStore " + (entity == null ? "saved: " : "updated: ") + ((CodeStore) response.getResponseData()).getName())
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    createDialog.close();
                    updateGridItems();
                    break;
                }
                default: {
                    Notification.show("CodeStore " + (entity == null ? "saving " : "modifying ") + "failed: " + response.getDescription()).
                            addThemeVariants(NotificationVariant.LUMO_ERROR);
                    createDialog.close();
                    updateGridItems();
                    break;
                }
            }

            setEntities();
            updateGridItems();

            nameTextField.clear();
            deletable.setValue(false);
            parentCodeStore.clear();
            createDialog.close();
        });

        formLayout.add(nameTextField, parentCodeStore, deletable, saveButton);

        createDialog.add(formLayout);
        return createDialog;
    }

    public class CodeStoreVO extends BaseVOWithDeletable<CodeStore> {
        private String name;
        private String parentName;

        public CodeStoreVO(CodeStore codeStore) {
            super(codeStore.id, codeStore.getDeleted(), codeStore.getDeletable(), codeStore);
            this.name = codeStore.getName();
            this.parentName = codeStore.getParentCodeStore() == null ? "" : codeStore.getParentCodeStore().getName();
        }
    }
}
