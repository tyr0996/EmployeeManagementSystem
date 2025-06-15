package hu.martin.ems.vaadin.component.User;

import com.google.gson.Gson;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.auth.SecurityService;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.model.User;
import hu.martin.ems.core.vaadin.EmsFilterableGridComponent;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.Role;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.UserApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import hu.martin.ems.vaadin.core.EmsComboBox;
import hu.martin.ems.vaadin.core.EmsDialog;
import hu.martin.ems.vaadin.core.GridButtonSettings;
import hu.martin.ems.vaadin.core.IEmsOptionColumnBaseDialogCreationForm;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CssImport("./styles/grid.css")
@Route(value = "user/list", layout = MainView.class)
@RolesAllowed("ROLE_UserMenuOpenPermission")
@NeedCleanCoding
public class UserList extends EmsFilterableGridComponent implements Creatable<User>, IEmsOptionColumnBaseDialogCreationForm<User, UserList.UserVO> {
    private boolean showDeleted = false;
    private HorizontalLayout buttonsLayout;
    @Getter
    private PaginatedGrid<UserVO, String> grid;
    private final PaginationSetting paginationSetting;
    private Button saveButton;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField passwordAgainField;
    private EmsComboBox<Role> roles;
    private FormLayout createOrModifyForm;
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();

    Grid.Column<UserVO> userNameColumn;
    Grid.Column<UserVO> passwordHashColumn;
    Grid.Column<UserVO> enabledColumn;
    Grid.Column<UserVO> roleColumn;

    private Grid.Column<UserVO> extraData;

    List<User> users;
    List<UserVO> userVOS;
    List<Role> roleList;

    private Gson gson = BeanProvider.getBean(Gson.class);

    @Getter
    private final UserApiClient apiClient = BeanProvider.getBean(UserApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);
    private final SecurityService securityService = BeanProvider.getBean(SecurityService.class);

    private TextFilteringHeaderCell usernameFilter;
    private TextFilteringHeaderCell passwordHashFilter;
    private TextFilteringHeaderCell enabledFilter;
    private String roleFilter = "";

    private Logger logger = LoggerFactory.getLogger(UserList.class);

    public UserList(PaginationSetting paginationSetting) {
        UserVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));
        this.paginationSetting = paginationSetting;
        this.users = new ArrayList<>();
        this.createOrModifyForm = new FormLayout();
        createUsersGrid();
        createLayout();
    }

    private void createUsersGrid() {
        this.grid = new PaginatedGrid<>(UserVO.class);
        grid.addClassName("styling");
        grid.setPartNameGenerator(user -> user.deleted != 0 ? "deleted" : null);
        setGridColumns();
        setFilteringHeaderRow();
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        updateGridItems();
    }

    private void setFilteringHeaderRow() throws RuntimeException {
        usernameFilter = new TextFilteringHeaderCell("Search username...", this);
        passwordHashFilter = new TextFilteringHeaderCell("Search password hash...", this);
        enabledFilter = new TextFilteringHeaderCell("Search enabled...", this);

        ComboBox<Role> rolesFilter = new ComboBox();
        ComboBox.ItemFilter<Role> filterPermission = (permission, filterString) ->
                permission.getName().toLowerCase().contains(filterString.toLowerCase());
        setupRoles();
        if (roleList == null) {
            rolesFilter.setInvalid(true);
            rolesFilter.setErrorMessage("EmsError happened while getting permissions");
            rolesFilter.setEnabled(false);
        } else {
            rolesFilter.setInvalid(false);
            rolesFilter.setEnabled(true);
            rolesFilter.setItems(filterPermission, roleList);
            rolesFilter.setItemLabelGenerator(Role::getName);
        }

        rolesFilter.addValueChangeListener(v -> {
            roleFilter = v.getValue() == null ? "" : v.getValue().getName();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField extraDataFilter = new TextField();

        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if (extraDataFilter.getValue().isEmpty()) {
                UserVO.extraDataFilterMap.clear();
            } else {
                UserVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(extraData).setComponent(styleFilterField(extraDataFilter, ""));
        filterRow.getCell(userNameColumn).setComponent(styleFilterField(usernameFilter, "Username"));
        filterRow.getCell(passwordHashColumn).setComponent(styleFilterField(passwordHashFilter, "Password hash"));
        filterRow.getCell(roleColumn).setComponent(styleFilterField(rolesFilter, "Role"));
        filterRow.getCell(enabledColumn).setComponent(styleFilterField(enabledFilter, "Enabled"));
    }

    private void createLayout() {
        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog dialog = getSaveOrUpdateDialog(null);
            dialog.open();
        });

        Checkbox withDeletedCheckbox = new Checkbox("Show deleted");
        withDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            UserVO.showDeletedCheckboxFilter.replace("deleted", newValue);
            userVOS = users.stream().map(UserVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        });

        buttonsLayout = new HorizontalLayout();
        buttonsLayout.add(create, withDeletedCheckbox);
        buttonsLayout.setAlignSelf(Alignment.CENTER, create);

        add(buttonsLayout, grid);
    }

    public EmsDialog getSaveOrUpdateDialog(UserVO editableUser) {
        EmsDialog createOrModifyDialog = new EmsDialog((editableUser == null ? "Create" : "Modify") + " user");

        createSaveOrUpdateForm(editableUser);
        saveButton.addClickListener(event -> {
            EmsResponse response = saveUser(createOrModifyDialog, editableUser);
            switch (response.getCode()) {
                case 200:
                    Notification.show("User " + (editableUser == null ? "saved: " : "updated: ") + ((User) response.getResponseData()).getUsername())
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    usernameField.clear();
                    passwordField.clear();
                    passwordAgainField.clear();
                    createOrModifyDialog.close();
                    updateGridItems();
                    break;
                case 400: {
                    Notification.show(response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    break;
                }
                default: {
                    Notification.show("User " + (editableUser == null ? "saving " : "modifying ") + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    createOrModifyDialog.close();
                    break;
                }
            }
        });
        createOrModifyDialog.add(this.createOrModifyForm);
        return createOrModifyDialog;
    }

    private void createSaveOrUpdateForm(UserVO editableUser) {
        this.createOrModifyForm.removeAll();
        saveButton = new Button("Save");
        usernameField = new TextField("Username");
        passwordField = new PasswordField("Password");
        passwordAgainField = new PasswordField("Password again");
        roles = new EmsComboBox<Role>("Role", this::setupRoles, saveButton, "EmsError happened while getting roles");
//        roles = new ComboBox<>("Role");
//        ComboBox.ItemFilter<Role> filterUser = (role, filterString) ->
//                role.getName().toLowerCase().contains(filterString.toLowerCase());
//        setupRoles();
//        if (roleList != null) {
//            roles.setItems(filterUser, roleList);
//            roles.setItemLabelGenerator(Role::getName);
//        } else {
//            roles.setEnabled(false);
//            roles.setInvalid(true);
//            saveButton.setEnabled(false);
//            roles.setErrorMessage("EmsError happened while getting roles");
//        }

        if (editableUser != null) {
            usernameField.setValue(editableUser.original.getUsername());
            passwordField.setValue("");
            passwordAgainField.setValue("");
            roles.setValue(editableUser.original.getRoleRole());
        }
        createOrModifyForm.add(usernameField, passwordField, passwordAgainField, roles, saveButton);
    }

    private List<Role> setupRoles() {
        EmsResponse response = roleApi.findAll();
        switch (response.getCode()) {
            case 200:
                roleList = (List<Role>) response.getResponseData();
                break;
            default:
                roleList = null;
                logger.error("Role findAllByIds. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
        return roleList;
    }

    private EmsResponse saveUser(Dialog dialog, UserVO editableUser) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = Optional.ofNullable(editableUser)
                .map(e -> e.original)
                .orElseGet(User::new);
        if (!usernameCheck(usernameField.getValue(), editableUser)) {
            dialog.close();
            return new EmsResponse(400, "Username already exists!");
        }

        if (!passwordField.getValue().equals(passwordAgainField.getValue())) {
            dialog.close();
            return new EmsResponse(400, "Passwords doesn't match!");
        } else if (passwordField.getValue() == "") {
            dialog.close();
            return new EmsResponse(400, "Password is required!");
        } else {
            user.setPasswordHash(encoder.encode(passwordField.getValue()));
        }
        user.setDeleted(0L);
        user.setUsername(usernameField.getValue());
        user.setRoleRole(roles.getValue());
        user.setEnabled(true);
        EmsResponse response = null;
        if (editableUser != null) {
            response = apiClient.update(user);
        } else {
            response = apiClient.save(user);
        }
        return response;
    }

    private Boolean usernameCheck(String username, UserVO editableUser) {
        EmsResponse response = apiClient.findByUsername(username); //TODO: mi történik, ha nem érhető el az adatbázis?
        if (editableUser != null && editableUser.original.getUsername().equals(username)) {
            return true;
        } else {
            return response.getResponseData() == null;
        }
    }

    private void setGridColumns() {
        userNameColumn = this.grid.addColumn(v -> v.username);
        passwordHashColumn = this.grid.addColumn(v -> v.passwordHash);
        enabledColumn = this.grid.addColumn(v -> v.enabled);
        roleColumn = this.grid.addColumn(v -> v.role);

        addOptionsColumn();
    }

    private void addOptionsColumn() {
        EmsResponse response = apiClient.findByUsername(securityService.getAuthenticatedUser().getUsername());
        final UserVO loggedInUserVO;
        switch (response.getCode()) {
            case 200: {
                loggedInUserVO = new UserVO((User) response.getResponseData());
                break;
            }
            default: {
                loggedInUserVO = null;
                Notification.show("EmsError happened while getting the logged in user. Deletion and modification is disabled");
                break;
            }
        }


        extraData = this.grid.addComponentColumn(entity -> {
            boolean editable = !(loggedInUserVO == null || loggedInUserVO.equals(entity));
            return createOptionColumn("User", entity, new GridButtonSettings(editable, editable));
        });
    }

    public void updateGridItems() {
        setEntities();
        if (users != null) {
            userVOS = users.stream().map(UserVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        } else {
            Notification.show("Getting users failed").addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public void setEntities() {
        EmsResponse response = apiClient.findAllWithDeleted();
        switch (response.getCode()) {
            case 200:
                users = (List<User>) response.getResponseData();
                break;
            default:
                logger.error("User findAllWithDeleted error in UserList");
                users = null;
                break;
        }
    }

    private Stream<UserVO> getFilteredStream() {
        return userVOS.stream().filter(userVO ->
                (usernameFilter.isEmpty() || userVO.username.toLowerCase().contains(usernameFilter.getFilterText().toLowerCase())) &&
                        (passwordHashFilter.isEmpty() || userVO.passwordHash.toLowerCase().contains(passwordHashFilter.getFilterText().toLowerCase())) &&
                        (enabledFilter.isEmpty() || userVO.enabled.toLowerCase().equals(enabledFilter.getFilterText().toLowerCase())) &&
                        (roleFilter.isEmpty() || userVO.role.toLowerCase().equals(roleFilter.toLowerCase())) &&
                        userVO.filterExtraData()
        );
    }

    protected class UserVO extends BaseVO<User> {
        private String username;
        private String passwordHash;
        private String enabled;
        private String role;

        public UserVO(User user) {
            super(user.id, user.getDeleted(), user);
            this.id = user.getId();
            this.deleted = user.getDeleted();
            this.username = user.getUsername();
            this.passwordHash = user.getPasswordHash();
            this.role = user.getRoleRole().getName();
            this.enabled = user.isEnabled() ? "true" : "false"; //TODO berakni a táblázatba, hogy benne legyen, hogy engedélyezve van-e, vagy sem.
        }
    }
}

