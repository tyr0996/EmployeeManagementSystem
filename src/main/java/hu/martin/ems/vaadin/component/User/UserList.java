package hu.martin.ems.vaadin.component.User;

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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.model.User;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.model.Role;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.UserApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/grid.css")
@Route(value = "user/list", layout = MainView.class)
@AnonymousAllowed
@NeedCleanCoding
public class UserList extends VerticalLayout implements Creatable<User> {
    private boolean showDeleted = false;
    private HorizontalLayout buttonsLayout;
    private PaginatedGrid<UserVO, String> grid;
    private final PaginationSetting paginationSetting;
    private Button saveButton;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField passwordAgainField;
    private ComboBox<Role> roles;

    private User editableUser;

    private Dialog createOrModifyDialog;
    private FormLayout createOrModifyForm;
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();

    Grid.Column<UserVO> userNameColumn;
    Grid.Column<UserVO> passwordHashColumn;

    private Grid.Column<UserVO> extraData;

    List<User> users;
    List<UserVO> userVOS;
    List<Role> roleList;


    private final UserApiClient userApi = BeanProvider.getBean(UserApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);
    private static String usernameFilterText = "";
    private static String passwordHashFilterText = "";

    private Logger logger = LoggerFactory.getLogger(UserList.class);

    public UserList(PaginationSetting paginationSetting) {
        UserVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));
        this.paginationSetting = paginationSetting;
        this.users = new ArrayList<>();
        this.createOrModifyForm = new FormLayout();
        createUsersGrid();
        createLayout();
    }

    private void createUsersGrid(){
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
        TextField usernameFilter = new TextField();
        usernameFilter.setPlaceholder("Search username...");
        usernameFilter.setClearButtonVisible(true);
        usernameFilter.addValueChangeListener(event -> {
            usernameFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField passwordHashFilter = new TextField();
        passwordHashFilter.setPlaceholder("Search password hash...");
        passwordHashFilter.setClearButtonVisible(true);
        passwordHashFilter.addValueChangeListener(event -> {
            passwordHashFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField extraDataFilter = new TextField();

        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                UserVO.extraDataFilterMap.clear();
            }
            else{
                try {
                    UserVO.extraDataFilterMap = new ObjectMapper().readValue(extraDataFilter.getValue().trim(), new TypeReference<LinkedHashMap<String, List<String>>>() {});
                } catch (JsonProcessingException ex) {
                    Notification.show("Invalid json in extra data filter field!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
        filterRow.getCell(userNameColumn).setComponent(filterField(usernameFilter, "Username"));
        filterRow.getCell(passwordHashColumn).setComponent(filterField(passwordHashFilter, "Password hash"));
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
            UserVO.showDeletedCheckboxFilter.replace("deleted", newValue);

            userVOS = users.stream().map(UserVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));

        });

        buttonsLayout = new HorizontalLayout();
        buttonsLayout.add(create, withDeletedCheckbox);
        buttonsLayout.setAlignSelf(Alignment.CENTER, create);

        add(buttonsLayout, grid);
    }

    public void generateSaveOrUpdateDialog() {
        createOrModifyDialog = new Dialog((editableUser == null ? "Create" : "Modify") + " user");
        createSaveOrUpdateForm();
        saveButton.addClickListener(event -> {
            EmsResponse response = saveUser();
            switch (response.getCode()){
                case 200:
                    Notification.show("User " + (editableUser == null ? "saved: " : "updated: ") + ((User) response.getResponseData()).getUsername())
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    usernameField.clear();
                    passwordField.clear();
                    passwordAgainField.clear();
                    createOrModifyDialog.close();
                    updateGridItems();
                    break;
                case 400:
                    Notification.show(response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    break;
                case 500:
                    logger.info("User " + (editableUser == null ? "saving" : "modifing") + " failed");
                    Notification.show("User " + (editableUser == null ? "saving" : "modifing") + " failed").addThemeVariants(NotificationVariant.LUMO_ERROR);
                    createOrModifyDialog.close();
                    break;
                default:
                    Notification.show("Not expected status-code in " + (editableUser == null ? "saving" : "modifying")).addThemeVariants(NotificationVariant.LUMO_WARNING);
                    logger.warn("Invalid status code in UserList: {}", response.getCode());
                    createOrModifyDialog.close();
            }
            editableUser = null;
        });
        createOrModifyDialog.add(this.createOrModifyForm);
    }

    private void createSaveOrUpdateForm(){
        this.createOrModifyForm.removeAll();
        saveButton = new Button("Save");
        usernameField = new TextField("Username");
        passwordField = new PasswordField("Password");
        passwordAgainField = new PasswordField("Password again");
        roles = new ComboBox<>("Role");
        ComboBox.ItemFilter<Role> filterUser = (role, filterString) ->
                role.getName().toLowerCase().contains(filterString.toLowerCase());
        setupRoles();
        if(roleList != null){
            roles.setItems(filterUser, roleList);
            roles.setItemLabelGenerator(Role::getName);
        }
        else{
            roles.setEnabled(false);
            roles.setInvalid(true);
            saveButton.setEnabled(false);
            roles.setErrorMessage("Error happened while getting roles");
        }

        if (editableUser != null) {
            usernameField.setValue(editableUser.getUsername());
            passwordField.setValue("");
            passwordAgainField.setValue("");
            roles.setValue(editableUser.getRoleRole());
        }
        createOrModifyForm.add(usernameField, passwordField, passwordAgainField, roles, saveButton);
    }

    private void setupRoles() {
        EmsResponse response = roleApi.findAll();
        switch (response.getCode()){
            case 200:
                roleList = (List<Role>) response.getResponseData();
                break;
            default:
                roleList = null;
                logger.error("Role findAll. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private EmsResponse saveUser(){
        User user = Objects.requireNonNullElseGet(editableUser, User::new);
//        if((editableUser == null || ) && usernameCheck(usernameField.getValue())){
        if(!usernameCheck(usernameField.getValue())){
            createOrModifyDialog.close();
            editableUser = null;
            return new EmsResponse(400, "Username already exists!");
        }

        if(!passwordField.getValue().equals(passwordAgainField.getValue())){
            //Notification.show("Passwords doesn't match!").addThemeVariants(NotificationVariant.LUMO_ERROR);
            createOrModifyDialog.close();
            editableUser = null;
            return new EmsResponse(400, "Passwords doesn't match!");
        }
        else if(passwordField.getValue() == "") {
            String passwordValue = passwordField.getValue();
            //Notification.show("Password is required!").addThemeVariants(NotificationVariant.LUMO_ERROR);
            createOrModifyDialog.close();
            editableUser = null;
            return new EmsResponse(400, "Password is required!");
        }
        else{
            user.setPassword(passwordField.getValue());
        }
        user.setDeleted(0L);
        user.setUsername(usernameField.getValue());
        user.setRoleRole(roles.getValue());
        EmsResponse response = null;
        if(editableUser != null){
            response = userApi.update(user);
        }
        else{
            response = userApi.save(user);
        }
        return response;
    }

    private Boolean usernameCheck(String username){
        EmsResponse response = userApi.userExists(username);
        if(response.getResponseData() != null && editableUser != null) {
            User u = (User) response.getResponseData();
            return u.getId() == editableUser.getId();
        }
        else if(response.getResponseData() != null){
            return false;
        }
        else if(editableUser != null){
            return true;
        }
        return true;
    }

    private void setGridColumns(){
        userNameColumn = this.grid.addColumn(v -> v.username);
        passwordHashColumn = this.grid.addColumn(v -> v.passwordHash);
        addOptionsColumn();
    }

    private void addOptionsColumn(){
        extraData = this.grid.addComponentColumn(entry -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                editableUser = entry.original;
                generateSaveOrUpdateDialog();
                createOrModifyDialog.open();
            });

            restoreButton.addClickListener(event -> {
                userApi.restore(entry.original);
                Notification.show("User restored: " + entry.username)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                userApi.delete(entry.original);
                Notification.show("User deleted: " + entry.username)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                userApi.permanentlyDelete(entry.id);
                Notification.show("User permanently deleted: " + entry.username)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (entry.deleted == 0) {
                actions.add(editButton, deleteButton);
            } else {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        });
    }

    private void updateGridItems() {
        setupUsers();
        if(users != null){
            userVOS = users.stream().map(UserVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        }
        else{
            Notification.show("Getting users failed").addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void setupUsers(){
        EmsResponse response = userApi.findAllWithDeleted();
        switch (response.getCode()){
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
                (usernameFilterText.isEmpty() || userVO.username.toLowerCase().contains(usernameFilterText.toLowerCase())) &&
                        (passwordHashFilterText.isEmpty() || userVO.passwordHash.toLowerCase().contains(passwordHashFilterText.toLowerCase())) &&
                        userVO.filterExtraData()
//                        (showDeleted ? (userVO.deleted == 0 || userVO.deleted == 1) : userVO.deleted == 0)
        );
    }

    protected class UserVO extends BaseVO {
        private User original;
        private String username;
        private String passwordHash;

        public UserVO(User user){
            super(user.id, user.getDeleted());
            this.original = user;
            this.id = user.getId();
            this.deleted = user.getDeleted();
            this.username = user.getUsername();
            this.passwordHash = user.getPassword();
        }
    }
}

