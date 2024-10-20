package hu.martin.ems.vaadin.component.Login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.User;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.UserApiClient;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Base64;

@Route(value = "login")
@AnonymousAllowed
@NeedCleanCoding
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private LoginI18n login = LoginI18n.createDefault();
//    private LoginForm login = new LoginForm();

    private final UserApiClient userApi = BeanProvider.getBean(UserApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);

    private LoginI18n.ErrorMessage NO_ROLE_USER = new LoginI18n.ErrorMessage();
    private LoginI18n.ErrorMessage BAD_CREDIDENTALS = new LoginI18n.ErrorMessage();

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${rememberme.key}")
    private String key;


    public LoginView() {
        NO_ROLE_USER.setTitle("Permission error");
        NO_ROLE_USER.setMessage("You have no permission to log in. Contact the administrator about your roles, and try again.");
        BAD_CREDIDENTALS.setTitle("Incorrect username or password");
        BAD_CREDIDENTALS.setMessage("Check that you have entered the correct username and password and try again.");

        Button register = new Button("Register");
        register.setClassName("register-button");
        register.addClickListener(event -> {
            Dialog registerDialog = getRegistrationDialog();
            registerDialog.open();
        });

        LoginOverlay loginOverlay = new LoginOverlay();

        loginOverlay.setTitle("EMS");
        loginOverlay.setDescription("The Employee Management System.");

        LoginI18n.Form loginForm = login.getForm();
        loginForm.setTitle("Login");
        loginForm.setUsername("Username");
        loginForm.setPassword("Password");
        loginForm.setSubmit("Login");
        loginForm.setForgotPassword("Forgot password");
        login.setForm(loginForm);

        //loginOverlay.setAction("login"); //Azért kellett kivenni, mert különben újratölti a login formot, és nem jelennek meg a hibaüzenetek
        loginOverlay.setI18n(login);
        loginOverlay.getFooter().add(register);
        loginOverlay.setOpened(true);
        add(loginOverlay);

        loginOverlay.addLoginListener(e -> {
            String userName = e.getUsername();
            String password = e.getPassword();
            User user = userApi.findByUsername(userName);
            if(user != null && user.getRoleRole().getName().equals("NO_ROLE")){
                login.setErrorMessage(NO_ROLE_USER);
                loginOverlay.setI18n(login);
                loginOverlay.setError(true);
                loginOverlay.setEnabled(true);
            }
            else {
                try {
                    UsernamePasswordAuthenticationToken authRequest =
                            new UsernamePasswordAuthenticationToken(userName, password);
                    Authentication auth = authenticationManager.authenticate(authRequest);

                    SecurityContextHolder.getContext().setAuthentication(auth);

                    HttpServletResponse response = VaadinServletResponse.getCurrent().getHttpServletResponse();
                    createRememberMeCookie(response, userName);
//
                    loginOverlay.close();
                    getUI().ifPresent(ui -> ui.navigate("/"));
                }
                catch (AuthenticationException ex) {
                    login.setErrorMessage(BAD_CREDIDENTALS);
                    loginOverlay.setI18n(login);
                    loginOverlay.setError(true);
                    loginOverlay.setEnabled(true);
                }
            }
        });

        loginOverlay.addForgotPasswordListener(e -> {
            getForgotPasswordDialog().open();
        });
    }

    private Dialog getRegistrationDialog() {
        Dialog d = new Dialog("Registration");
        FormLayout form = new FormLayout();
        TextField userName = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        PasswordField passwordAgain = new PasswordField("Password again");
        Button register = new Button("Register");
        register.addClickListener(event -> {
            User allreadyUser = userApi.findByUsername(userName.getValue());
            if(allreadyUser != null){
                Notification.show("Username already exists!").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            else{
                if(!password.getValue().equals(passwordAgain.getValue())){
                    Notification.show("The passwords doesn't match!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
                else{
                    User newUser = new User();
                    newUser.setPassword(password.getValue());
                    newUser.setUsername(userName.getValue());
                    newUser.setDeleted(0L);
                    newUser.setRoleRole(roleApi.findByName("NO_ROLE"));
                    userApi.save(newUser);
                    Notification.show("Registration successful!");
                    d.close();
                }
            }
        });
        form.add(userName, password, passwordAgain, register);
        d.add(form);
        return d;
    }

    private Dialog getForgotPasswordDialog(){
        Dialog d = new Dialog("Forgot password");
        FormLayout form = new FormLayout();
        TextField userName = new TextField("Username");
        Button next = new Button("Next");
        next.addClickListener(event -> {
            Dialog passwordDialog = passwordDialog(d, userName.getValue());
            passwordDialog.open();
        });
        form.add(userName, next);
        d.add(form);
        return d;
    }

    private Dialog passwordDialog(Dialog parent, String userName) {
        Dialog d = new Dialog("Forgot password for " + userName);
        FormLayout form = new FormLayout();
        PasswordField pw1 = new PasswordField("Password");
        PasswordField pw2 = new PasswordField("Password again");
        Button submit = new Button("Submit");
        form.add(pw1, pw2, submit);
        d.add(form);
        submit.addClickListener(event -> {
            if(pw1.getValue().equals(pw2.getValue())){
                User user = userApi.findByUsername(userName);
                if(user == null){
                    Notification.show("User not found!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
                else{
                    user.setPassword(pw1.getValue());
                    userApi.update(user);
                    Notification.show("Password changed successfully!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    d.close();
                    parent.close();
                }
            }
            else{
                Notification.show("The passwords doesn't match!").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        return d;
    }


    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {}

    private void createRememberMeCookie(HttpServletResponse response, String username) {
        String tokenValue = Base64.getEncoder().encodeToString((username + ":" + key).getBytes());
        Cookie rememberMeCookie = new Cookie("rememberMe", tokenValue);

        rememberMeCookie.setMaxAge(60 * 60 * 24 * 7); // 7 nap
        rememberMeCookie.setHttpOnly(true);
        rememberMeCookie.setSecure(false);
        rememberMeCookie.setPath("/");

        response.addCookie(rememberMeCookie);
    }
}
