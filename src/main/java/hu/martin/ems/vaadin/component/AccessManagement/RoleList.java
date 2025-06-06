package hu.martin.ems.vaadin.component.AccessManagement;

import com.google.gson.Gson;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.auth.CustomUserDetailsService;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.IconProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.model.User;
import hu.martin.ems.core.vaadin.IEmsFilterableGridPage;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.PermissionApiClient;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.UserApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CssImport("./styles/grid.css")
@NeedCleanCoding
@RolesAllowed("ROLE_RoleMenuOpenPermission")
@Route(value = "/accessManagement/list/role", layout = MainView.class)
public class RoleList extends AccessManagement implements Creatable<Role>, IEmsFilterableGridPage {
    private boolean showDeleted = false;
    @Getter
    private PaginatedGrid<RoleVO, String> grid;
    private final PaginationSetting paginationSetting;
    private HorizontalLayout buttonsLayout;
    private Button saveButton;
    private TextField nameField;
    private MultiSelectComboBox<Permission> permissions;
    private Role editableRole;

    private Role loggedInUserRole;

    private Dialog createOrModifyDialog;
    private FormLayout createOrModifyForm;
    private List<Role> roles;
    private List<RoleVO> roleVOS;

    private final PermissionApiClient permissionApi = BeanProvider.getBean(PermissionApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);
    private final UserApiClient userApiClient = BeanProvider.getBean(UserApiClient.class);

    private TextFilteringHeaderCell roleFilter;
    private Set<Permission> permissionsFilterSet = new HashSet<>();
    private Grid.Column<RoleVO> extraData;
    private Grid.Column<RoleVO> roleColumn;
    private Grid.Column<RoleVO> permissionsColumn;

    private Logger logger = LoggerFactory.getLogger(RoleList.class);
    List<Permission> permissionList;
    private Gson gson = BeanProvider.getBean(Gson.class);


    private void appendCloseButton(Dialog d) {
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> d.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        d.getHeader().add(closeButton);
    }

    public RoleList(PaginationSetting paginationSetting) {
        super(paginationSetting);
        this.currentView = this.getClass();
        setLoggedInUserRole();
        this.paginationSetting = paginationSetting;

        RoleVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.roles = new ArrayList<>();
        this.createOrModifyForm = new FormLayout();

        createRoleXPermissionGrid();
        createLayout();
    }

    private void createRoleXPermissionGrid() {
        setRoles();

        this.grid = new PaginatedGrid<>(RoleVO.class);
        grid.addClassName("styling");
        grid.setPartNameGenerator(roleVO -> roleVO.original.getDeleted() != 0 ? "deleted" : null);
        setGridColumns();
        setFilteringHeaderRow();

        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());
        updateGridItems();
    }

    private void createLayout() {
        Button create = new Button("Create");
        create.addClickListener(event -> {
            generateSaveOrUpdateDialog();
            createOrModifyDialog.open();
        });

        Checkbox withDeletedCheckbox = new Checkbox("Show deleted");
        withDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            RoleVO.showDeletedCheckboxFilter.replace("deleted", newValue);
            setRoles();
            updateGridItems();
        });

        buttonsLayout = new HorizontalLayout();
        buttonsLayout.add(create, withDeletedCheckbox);
        buttonsLayout.setAlignSelf(Alignment.CENTER, create);

        add(buttonsLayout, grid);
    }

    public void generateSaveOrUpdateDialog() {
        createOrModifyDialog = new Dialog((editableRole == null ? "Create" : "Modify") + " role");
        createSaveOrUpdateForm();
        saveButton.addClickListener(event -> {
            saveRoleWithPermissions();
            setRoles();
            nameField.clear();
            createOrModifyDialog.close();
            updateGridItems();
        });

        createOrModifyDialog.add(this.createOrModifyForm);
    }

    private void createSaveOrUpdateForm() {
        this.createOrModifyForm.removeAll();
        nameField = new TextField("Name");
        saveButton = new Button("Save");

        permissions = new MultiSelectComboBox<>("Permission");
        ComboBox.ItemFilter<Permission> filterPermission = (permission, filterString) ->
                permission.getName().toLowerCase().contains(filterString.toLowerCase());
        setupPermissions();
        if (permissionList == null) {
            permissions.setInvalid(true);
            permissions.setErrorMessage("EmsError happened while getting permissions");
            permissions.setEnabled(false);
            saveButton.setEnabled(false);
        } else {
            saveButton.setEnabled(true);
            permissions.setInvalid(false);
            permissions.setEnabled(true);
            permissions.setItems(filterPermission, permissionList);
            permissions.setItemLabelGenerator(Permission::getName);
            if (editableRole != null) {
                nameField.setValue(editableRole.getName());
                List<Permission> editableRolePermissions = editableRole.getPermissions().stream().toList();
                permissions.setValue(editableRolePermissions);

            }
        }
        appendCloseButton(createOrModifyDialog);
        createOrModifyForm.add(nameField, permissions, saveButton);
    }

    private void setupPermissions() {
        EmsResponse response = permissionApi.findAll();
        switch (response.getCode()) {
            case 200:
                permissionList = (List<Permission>) response.getResponseData();
                break;
            default:
                permissionList = null;
                logger.error("Permission findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private void saveRoleWithPermissions() {
        EmsResponse resp = null;
        if (editableRole == null) {
            Role r = new Role();
            r.setPermissions(permissions.getSelectedItems());
            r.setName(nameField.getValue());
            r.setDeleted(0L);
            resp = roleApi.save(r);
        } else {
            editableRole.setPermissions(permissions.getSelectedItems());
            editableRole.setName(nameField.getValue());
            resp = roleApi.update(editableRole);
        }

        updateGridItems();
        switch (resp.getCode()) {
            case 200: {
                Notification.show("Role " + (editableRole == null ? "saved: " : "updated: ") + nameField.getValue())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                break;
            }
            default: {
                Notification.show("Role " + (editableRole == null ? "saving " : "modifying ") + "failed: " + resp.getDescription())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
            }
        }
    }

    private void setFilteringHeaderRow() {
        roleFilter = new TextFilteringHeaderCell("Search role...", this);

        MultiSelectComboBox<Permission> permissionsFilter = new MultiSelectComboBox();
        ComboBox.ItemFilter<Permission> filterPermission = (permission, filterString) ->
                permission.getName().toLowerCase().contains(filterString.toLowerCase());
        permissionsFilter.setClearButtonVisible(true);
        setupPermissions();
        if (permissionList == null) {
            permissionsFilter.setInvalid(true);
            permissionsFilter.setErrorMessage("EmsError happened while getting permissions");
            permissionsFilter.setEnabled(false);
        } else {
            permissionsFilter.setInvalid(false);
            permissionsFilter.setEnabled(true);
            permissionsFilter.setItems(filterPermission, permissionList);
            permissionsFilter.setItemLabelGenerator(Permission::getName);
        }

        permissionsFilter.addSelectionListener(v -> {
            permissionsFilterSet = v.getAllSelectedItems();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if (extraDataFilter.getValue().isEmpty()) {
                RoleVO.extraDataFilterMap.clear();
            } else {
                RoleVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(roleColumn).setComponent(styleFilterField(roleFilter, "Role"));
        filterRow.getCell(permissionsColumn).setComponent(styleFilterField(permissionsFilter, "Permissions"));
        filterRow.getCell(extraData).setComponent(styleFilterField(extraDataFilter, ""));
    }

    private void setGridColumns() {
        roleColumn = this.grid.addColumn(v -> v.role);
        permissionsColumn = this.grid.addColumn(v -> v.permissions);
        addOptionsColumn();
    }

    private void addOptionsColumn() {
        extraData = this.grid.addComponentColumn(roleVO -> {
            Button editButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).EDIT_ICON));
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).PERMANENTLY_DELETE_ICON));
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            disableDeletingAndEditingForLoggedInUser(roleVO, deleteButton, editButton);

            editButton.addClickListener(event -> {
                editableRole = roleVO.original;
                generateSaveOrUpdateDialog();
                createOrModifyDialog.open();
            });

            restoreButton.addClickListener(event -> {
                roleApi.restore(roleVO.original);
                setRoles();
                updateGridItems();
                Notification.show("Role restored: " + roleVO.role)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });

            deleteButton.addClickListener(event -> {
                EmsResponse resp = this.roleApi.delete(roleVO.original);
                switch (resp.getCode()) {
                    case 200: {
                        Notification.show("Role deleted: " + roleVO.original.getName())
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        updateGridItems();
                        break;
                    }
                    default: {
                        Notification.show(resp.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
                setRoles();
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                roleApi.permanentlyDelete(roleVO.id);

                Notification.show("Role permanently deleted: " + roleVO.role)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setRoles();
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (roleVO.deleted == 0) {
                actions.add(editButton, deleteButton);
            } else {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        });
    }

    private void setLoggedInUserRole() {
        EmsResponse response = userApiClient.findByUsername(CustomUserDetailsService.getLoggedInUsername());
        switch (response.getCode()) {
            case 200: {
                loggedInUserRole = ((User) response.getResponseData()).getRoleRole();
                break;
            }
            default:
                Notification.show("Unable to get the current user. Deleting and editing roles are disabled").addThemeVariants(NotificationVariant.LUMO_ERROR);
                loggedInUserRole = null;
                break;
        }
    }

    private void disableDeletingAndEditingForLoggedInUser(RoleVO roleVO, Button deleteButton, Button editButton) {
        if (loggedInUserRole == null || roleVO.original.equals(loggedInUserRole)) {
            deleteButton.setEnabled(false);
            editButton.setEnabled(false);
        }
    }

    public void updateGridItems() {
        if (roles != null) {
            roleVOS = roles.stream().map(RoleVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        } else {
        }
    }

    private Stream<RoleVO> getFilteredStream() {
        return roleVOS.stream().filter(this::filterPredicate);
    }

    private boolean filterPredicate(RoleVO groupedRoleXPermissionVO) {
        boolean roleFilterResult = filterField(roleFilter, groupedRoleXPermissionVO.role);
        HashSet<String> rowPermissions = new HashSet<>(groupedRoleXPermissionVO.permissionSet.stream().map(Permission::getName).toList());
        boolean permissionFilterResult = permissionsFilterSet.isEmpty() || rowPermissions.containsAll(permissionsFilterSet.stream().map(Permission::getName).toList());
        return roleFilterResult && permissionFilterResult && groupedRoleXPermissionVO.filterExtraData();
    }

    private void setRoles() {
        List<Role> roles;
        EmsResponse response;
        if (showDeleted) {
            response = roleApi.findAllWithDeleted();
        } else {
            response = roleApi.findAll();
        }
        switch (response.getCode()) {
            case 200: {
                roles = (List<Role>) response.getResponseData();
                break;
            }
            default: {
                Notification.show("EmsError happened while getting roles").addThemeVariants(NotificationVariant.LUMO_ERROR);
                this.roles = null;
                return;
            }
        }
        this.roles.clear();
        this.roles.addAll(roles);
    }

    protected class RoleVO extends BaseVO {
        private Role original;
        private String role;
        private String permissions;

        private Set<Permission> permissionSet;

        public RoleVO(Role original) {
            super(original.getId(), original.getDeleted());
            this.original = original;
            this.role = original.getName();
            this.permissions = original.getPermissions().stream()
                    .filter(Objects::nonNull)
                    .map(Permission::getName)
                    .collect(Collectors.joining(", "));
            this.permissionSet = original.getPermissions();
        }
    }
}