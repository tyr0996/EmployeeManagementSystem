package hu.martin.ems.vaadin.component.User;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.model.User;
import hu.martin.ems.model.Role;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.UserApiClient;
import hu.martin.ems.vaadin.component.Creatable;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;

@CssImport("./styles/grid.css")
@Route(value = "user/list", layout = MainView.class)
@AnonymousAllowed
@NeedCleanCoding
public class UserList extends VerticalLayout implements Creatable<User> {
    private boolean showDeleted = false;
    private PaginatedGrid<User, String> grid;
    private final PaginationSetting paginationSetting;
    private HorizontalLayout buttonsLayout;
    private Button saveButton;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField passwordAgainField;
    private ComboBox<Role> roles;

    private User editableUser;

    private Dialog createOrModifyDialog;
    private FormLayout createOrModifyForm;
    List<User> users;

    private final UserApiClient userApi = BeanProvider.getBean(UserApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);

    public UserList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;
        this.users = new ArrayList<>();
        this.createOrModifyForm = new FormLayout();
        createUsersGrid();
        createLayout();
    }

    private void createUsersGrid(){
        this.grid = new PaginatedGrid<>(User.class);
        grid.addClassName("styling");
        grid.setPartNameGenerator(user -> user.getDeleted() != 0 ? "deleted" : null);

        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());
        setGridColumns();
        updateGridItems();
    }

    private void createLayout(){
        Button create = new Button("Create");
        create.addClickListener(event -> {
            generateSaveOrUpdateDialog();
            createOrModifyDialog.open();
        });
        buttonsLayout = new HorizontalLayout();
        buttonsLayout.add(create);
        buttonsLayout.setAlignSelf(Alignment.CENTER, create);

        add(buttonsLayout, grid);
    }

    public void generateSaveOrUpdateDialog() {
        createOrModifyDialog = new Dialog((editableUser == null ? "Create" : "Modify") + " user");
        createSaveOrUpdateForm();
        saveButton.addClickListener(event -> {
            User user = saveUser();
            if(user != null){
                Notification.show((editableUser == null ? "User saved: " : "User updated: ") + user.getUsername())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                usernameField.clear();
                passwordField.clear();
                passwordAgainField.clear();
                createOrModifyDialog.close();
                updateGridItems();
                createOrModifyDialog.close();
            }
        });

        createOrModifyDialog.add(this.createOrModifyForm);
    }

    private void createSaveOrUpdateForm(){
        this.createOrModifyForm.removeAll();
        usernameField = new TextField("Username");
        passwordField = new PasswordField("Password");
        passwordAgainField = new PasswordField("Password again");
        roles = new ComboBox<>("Role");
        ComboBox.ItemFilter<Role> filterUser = (role, filterString) ->
                role.getName().toLowerCase().contains(filterString.toLowerCase());
        List<Role> savedRoles = roleApi.findAll();
        roles.setItems(filterUser, savedRoles);
        roles.setItemLabelGenerator(Role::getName);

        saveButton = new Button("Save");

        if (editableUser != null) {
            usernameField.setValue(editableUser.getUsername());
            passwordField.setValue("");
            passwordAgainField.setValue("");
            roles.setValue(editableUser.getRoleRole());
        }
        createOrModifyForm.add(usernameField, passwordField, passwordAgainField, roles, saveButton);
    }

    private User saveUser(){
        User user = Objects.requireNonNullElseGet(editableUser, User::new);
        if(editableUser == null && userApi.findByUsername(usernameField.getValue()) != null){
            Notification.show("Username already exists!").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return null;
        }
        if(!passwordField.getValue().equals(passwordAgainField.getValue())){
            Notification.show("Passwords doesn't match!").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return null;
        }
        else if(editableUser != null && (passwordField.getValue() == "" || passwordField.getValue() == null)){
            user.setPassword(editableUser.getPassword());
        }
        else{
            user.setPassword(passwordField.getValue());
        }
        user.setDeleted(0L);
        user.setUsername(usernameField.getValue());
        user.setRoleRole(roles.getValue());

        if(editableUser != null){
            return userApi.update(user);
        }
        else{
            return userApi.save(user);
        }
    }



    private void setGridColumns(){
        this.grid.removeAllColumns();
        this.grid.addColumn(User::getUsername).setHeader("Username");
        this.grid.addColumn(User::getPassword).setHeader("Password hash");
        addOptionsColumn();
    }

    private void addOptionsColumn(){
        this.grid.addComponentColumn(entry -> {
            Button editButton = new Button(EDIT.create());
            editButton.addClickListener(event -> {
                editableUser = entry;
                generateSaveOrUpdateDialog();
                createOrModifyDialog.open();
            });
            HorizontalLayout actions = new HorizontalLayout();
            actions.add(editButton);
            return actions;
        }).setHeader("Options");
    }

    public void refreshGrid(){
        updateGridItems();
    }

    private void updateGridItems() {
        users.clear();
        setUsers();
        this.grid.setItems(users);
    }

    private void setUsers(){
        this.users = userApi.findAll();
    }
}

