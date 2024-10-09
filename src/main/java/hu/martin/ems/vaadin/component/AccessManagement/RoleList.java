package hu.martin.ems.vaadin.component.AccessManagement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.vaadin.api.PermissionApiClient;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.RoleXPermissionApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.City.CityList;
import hu.martin.ems.vaadin.component.Creatable;
import lombok.AllArgsConstructor;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/grid.css")
@AnonymousAllowed
@NeedCleanCoding
public class RoleList extends VerticalLayout implements Creatable<Role> {
    private boolean showDeleted = false;
    private PaginatedGrid<GroupedRoleXPermissionVO, String> grid;
    private final PaginationSetting paginationSetting;
    private HorizontalLayout buttonsLayout;
    private Button saveButton;
    private TextField nameField;
    private MultiSelectComboBox<Permission> permissions;
    private Role editableRole;

    private Dialog createOrModifyDialog;
    private FormLayout createOrModifyForm;
    private List<GroupedRoleXPermission> groupedRoleXPermissions;
    private List<GroupedRoleXPermissionVO> groupedRoleXPermissionVOS;

    private final PermissionApiClient permissionApi = BeanProvider.getBean(PermissionApiClient.class);
    private final RoleXPermissionApiClient roleXPermissionApi = BeanProvider.getBean(RoleXPermissionApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);

    private static String roleFilterText = "";
    private static String permissionsFilterText = "";
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Grid.Column<GroupedRoleXPermissionVO> extraData;
    private Grid.Column<GroupedRoleXPermissionVO> roleColumn;
    private Grid.Column<GroupedRoleXPermissionVO> permissionsColumn;

    public RoleList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;

        GroupedRoleXPermissionVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.groupedRoleXPermissions = new ArrayList<>();
        this.createOrModifyForm = new FormLayout();
        createRoleXPermissionGrid();
        createLayout();
    }

    private void createRoleXPermissionGrid(){
        this.grid = new PaginatedGrid<>(GroupedRoleXPermissionVO.class);
        grid.addClassName("styling");
        grid.setPartNameGenerator(roleXPermissionVO -> roleXPermissionVO.original.role.getDeleted() != 0 ? "deleted" : null);
        setGridColumns();
        setFilteringHeaderRow();

        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());
        updateGridItems();
    }

    private void createLayout(){
        Button create = new Button("Create");
        create.addClickListener(event -> {
            generateSaveOrUpdateDialog();
            createOrModifyDialog.open();
        });

        Checkbox withDeletedCheckbox = new Checkbox("Show deleted");
        withDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            GroupedRoleXPermissionVO.showDeletedCheckboxFilter.replace("deleted", newValue);

            updateGridItems();

        });

        buttonsLayout = new HorizontalLayout();
        buttonsLayout.add(create, withDeletedCheckbox);
        buttonsLayout.setAlignSelf(Alignment.CENTER, create);

        add(buttonsLayout, grid);
    }

    public void generateSaveOrUpdateDialog() {
        RoleXPermissionCreate rxpc = new RoleXPermissionCreate();
        createOrModifyDialog = new Dialog((editableRole == null ? "Create" : "Modify") + " role");
        createSaveOrUpdateForm();
        saveButton.addClickListener(event -> {
            GroupedRoleXPermission grxp = saveRoleWithPermissions();
            Notification.show("Role " + (editableRole == null ? "saved: " : "updated: ") + grxp.getName())
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            nameField.clear();
            createOrModifyDialog.close();
            updateGridItems();
        });

        createOrModifyDialog.add(this.createOrModifyForm);
    }

    private void createSaveOrUpdateForm(){
        this.createOrModifyForm.removeAll();
        nameField = new TextField("Name");
        saveButton = new Button("Save");
        //Button editRoleXPermissionButton = new Button("Edit permissions");

        permissions = new MultiSelectComboBox<>("Permission");
        ComboBox.ItemFilter<Permission> filterPermission = (permission, filterString) ->
                permission.getName().toLowerCase().contains(filterString.toLowerCase());
        List<Permission> savedPermissions = permissionApi.findAll();
        permissions.setItems(filterPermission, savedPermissions);
        permissions.setItemLabelGenerator(Permission::getName);

        if (editableRole != null) {
            nameField.setValue(editableRole.getName());
            List<Permission> editableRolePermissions = roleXPermissionApi.findAllPairedPermissionsTo(editableRole);
            permissions.setValue(editableRolePermissions);
        }
        createOrModifyForm.add(nameField, permissions, saveButton);
    }

    private GroupedRoleXPermission saveRoleWithPermissions(){
        Role role = Objects.requireNonNullElseGet(editableRole, Role::new);
        role.setName(nameField.getValue());
        role.setDeleted(0L);
        if(editableRole != null){
            role = roleApi.update(role);
        }
        else{
            role = roleApi.save(role);
        }
        roleXPermissionApi.removeAllPermissionsFrom(role);
        for(Permission p : permissions.getSelectedItems()){
            RoleXPermission rxp = new RoleXPermission(role, p);
            rxp.setDeleted(0L);
            roleXPermissionApi.save(rxp);
        }
        return new GroupedRoleXPermission(role, permissions.getSelectedItems().stream().toList());
    }

    private void setFilteringHeaderRow(){
        TextField roleFilter = new TextField();
        roleFilter.setPlaceholder("Search role...");
        roleFilter.setClearButtonVisible(true);
        roleFilter.addValueChangeListener(event -> {
            roleFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField permissionsFilter = new TextField();
        permissionsFilter.setPlaceholder("Permissions name...");
        permissionsFilter.setClearButtonVisible(true);
        permissionsFilter.addValueChangeListener(event -> {
            permissionsFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                GroupedRoleXPermissionVO.extraDataFilterMap.clear();
            }
            else{
                try {
                    GroupedRoleXPermissionVO.extraDataFilterMap = new ObjectMapper().readValue(extraDataFilter.getValue().trim(), new TypeReference<LinkedHashMap<String, List<String>>>() {});
                } catch (JsonProcessingException ex) {
                    Notification.show("Invalid json in extra data filter field!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(roleColumn).setComponent(filterField(roleFilter, "Role"));
        filterRow.getCell(permissionsColumn).setComponent(filterField(permissionsFilter, "Permissions"));
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
    }

    private void setGridColumns(){
        roleColumn = this.grid.addColumn(v -> v.role);
        permissionsColumn = this.grid.addColumn(v -> v.permissions);
        addOptionsColumn();
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

    private void addOptionsColumn(){
        extraData = this.grid.addComponentColumn(groupedRoleXPermissionVO -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                editableRole = groupedRoleXPermissionVO.original.role;
                generateSaveOrUpdateDialog();
                createOrModifyDialog.open();
            });

            restoreButton.addClickListener(event -> {
                roleApi.restore(groupedRoleXPermissionVO.original.role);
                List<RoleXPermission> rxps = roleXPermissionApi.findAlRoleXPermissionByRole(groupedRoleXPermissionVO.original.role);
                for(RoleXPermission rxp : rxps){
                    roleXPermissionApi.restore(rxp);
                }
                Notification.show("Role restored: " + groupedRoleXPermissionVO.role)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                roleApi.delete(groupedRoleXPermissionVO.original.role);
                List<RoleXPermission> rxps = roleXPermissionApi.findAlRoleXPermissionByRole(groupedRoleXPermissionVO.original.role);
                for(RoleXPermission rxp : rxps){
                    roleXPermissionApi.delete(rxp);
                }
                Notification.show("Role deleted: " + groupedRoleXPermissionVO.role)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                roleApi.permanentlyDelete(groupedRoleXPermissionVO.id);
                List<RoleXPermission> rxps = roleXPermissionApi.findAlRoleXPermissionByRole(groupedRoleXPermissionVO.original.role);
                for(RoleXPermission rxp : rxps){
                    roleXPermissionApi.permanentlyDelete(rxp.id);
                }
                Notification.show("Role permanently deleted: " + groupedRoleXPermissionVO.role)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (groupedRoleXPermissionVO.deleted == 0) {
                actions.add(editButton, deleteButton);
            } else if (groupedRoleXPermissionVO.deleted == 1) {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        });
    }

    private void updateGridItems() {
        groupedRoleXPermissions.clear();
        setGroupedRoleXPermissions();
        groupedRoleXPermissionVOS = groupedRoleXPermissions.stream().map(GroupedRoleXPermissionVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
    }

    private Stream<GroupedRoleXPermissionVO> getFilteredStream() {
        return groupedRoleXPermissionVOS.stream().filter(groupedRoleXPermissionVO ->
                (roleFilterText.isEmpty() || groupedRoleXPermissionVO.role.toLowerCase().contains(roleFilterText.toLowerCase())) &&
                        (permissionsFilterText.isEmpty() || groupedRoleXPermissionVO.permissions.toLowerCase().contains(permissionsFilterText.toLowerCase())) &&
                        groupedRoleXPermissionVO.filterExtraData()
        );
    }

    private void setGroupedRoleXPermissions(){
        List<RoleXPermission> rxps = roleXPermissionApi.findAllWithUnused(true);
        Map<Role, List<Permission>> gridData = new HashMap<>();
        for (RoleXPermission rxp : rxps) {
            Role role = rxp.getRole();
            addRoleIfNotExists(gridData, role);
        }
        gridData.forEach((role, permissionList) -> {
            this.groupedRoleXPermissions.add(new GroupedRoleXPermission(role, permissionList));
        });
    }

    private void addRoleIfNotExists(Map<Role, List<Permission>> gridData, Role role) {
        if (!gridData.containsKey(role)) {
            List<Permission> permissions = roleXPermissionApi.findAllPairedPermissionsTo(role);
            gridData.put(role, permissions);
        }
    }

    protected class GroupedRoleXPermissionVO extends BaseVO {
        private GroupedRoleXPermission original;

        private String role;
        private String permissions;

        public GroupedRoleXPermissionVO(GroupedRoleXPermission groupedRoleXPermission){
            super(groupedRoleXPermission.getId(), groupedRoleXPermission.getDeleted());
            this.original = groupedRoleXPermission;
            this.id = original.getId();
            this.deleted = original.getDeleted();
            this.role = groupedRoleXPermission.roleAsString();
            this.permissions = groupedRoleXPermission.permissionsAsString();
        }
    }


    @AllArgsConstructor
    protected class GroupedRoleXPermission {
        private Role role;
        private List<Permission> permissions;

        private String getName(){
            return this.role.getName();
        }

        private Long getDeleted(){
            return this.role.getDeleted();
        }

        private Long getId(){
            return this.role.getId();
        }

        public String permissionsAsString() {
            return permissions.stream()
                    .filter(Objects::nonNull)
                    .map(Permission::getName)
                    .collect(Collectors.joining(", "));
        }

        public String roleAsString() {
            return role.getName();
        }
    }
}