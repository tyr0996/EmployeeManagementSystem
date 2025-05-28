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
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.auth.CustomUserDetailsService;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.User;
import hu.martin.ems.model.Role;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.UserApiClient;
import hu.martin.ems.vaadin.core.EmsDialog;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    Logger logger = LoggerFactory.getLogger(User.class);


    public LoginView() {
//        addAttachListener(event -> {
//            VaadinSession.getCurrent().setAttribute("csrfToken", getCsrfTokenFromCookie());
//        });

        NO_ROLE_USER.setTitle("Permission error");
        NO_ROLE_USER.setMessage("You have no permission to log in. Contact the administrator about your roles, and try again.");
        BAD_CREDIDENTALS.setTitle("Incorrect username or password");
        BAD_CREDIDENTALS.setMessage("Check that you have entered the correct username and password and try again.");

        Button register = new Button("Register");
        register.setClassName("register-button");

        register.addClickListener(event -> {
            EmsResponse response = roleApi.findById(-1L);
            switch (response.getCode()){
                case 200: {
                    Dialog registerDialog = getRegistrationDialog((Role) response.getResponseData());
                    registerDialog.open();
                    break;
                }
                default: {
                    Notification.show(response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    break;
                }
            }
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
            User user = getByUserName(userName);
            if(user != null && user.getRoleRole().getName().equals("NO_ROLE")){
                login.setErrorMessage(NO_ROLE_USER);
                loginOverlay.setI18n(login);
                loginOverlay.setError(true);
                loginOverlay.setEnabled(true);
            }
            else if(user != null){
                try {
                    UsernamePasswordAuthenticationToken authRequest =
                            new UsernamePasswordAuthenticationToken(userName, password);
                    Authentication auth = authenticationManager.authenticate(authRequest);

                    SecurityContextHolder.getContext().setAuthentication(auth);

                    VaadinServletRequest vaadinRequest = (VaadinServletRequest) VaadinRequest.getCurrent();
                    HttpServletRequest request = vaadinRequest.getHttpServletRequest();
                    request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

//                    HttpServletResponse response = VaadinServletResponse.getCurrent().getHttpServletResponse();
                    CustomUserDetailsService.getLoggedInUsername();
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
            else{
                login.setErrorMessage(BAD_CREDIDENTALS);
                loginOverlay.setI18n(login);
                loginOverlay.setError(true);
                loginOverlay.setEnabled(true);
            }
        });

        loginOverlay.addForgotPasswordListener(e -> {
            getForgotPasswordDialog().open();
        });
    }

    private User getByUserName(String username){
        EmsResponse emsResponse = userApi.findByUsername(username);
        switch (emsResponse.getCode()){
            case 200:
                return (User) emsResponse.getResponseData();
            default:
                logger.error("User findByUsernameError. Code: {}, Description: {}", emsResponse.getCode(), emsResponse.getDescription());
                Notification.show("EmsError happened while getting username")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return null;
        }
    }

    private Dialog getRegistrationDialog(Role noRole) {
        EmsDialog d = new EmsDialog("Registration");
        d.addCloseButton();

//        Button closeButton = new Button(new Icon("lumo", "cross"),
//                (e) -> d.close());
//        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
//        d.getHeader().add(closeButton);

        FormLayout form = new FormLayout();
        TextField userName = new TextField("Username");
        PasswordField password = new PasswordField("Password");
        PasswordField passwordAgain = new PasswordField("Password again");
        Button register = new Button("Register");

        form.add(userName, password, passwordAgain, register);
        d.add(form);

        register.addClickListener(event -> {
            User allreadyUser = getByUserName(userName.getValue());
            if(allreadyUser != null){
                Notification.show("Username already exists!").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            else{
                if(!password.getValue().equals(passwordAgain.getValue())){
                    Notification.show("The passwords doesn't match!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
                else{
                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                    User newUser = new User();
                    newUser.setPasswordHash(encoder.encode(password.getValue()));
                    newUser.setUsername(userName.getValue());
                    newUser.setDeleted(0L);
                    newUser.setRoleRole(noRole);
                    newUser.setEnabled(true);
                    userApi.save(newUser);
                    Notification.show("Registration successful!");
                    d.close();
                }
            }
        });
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
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Dialog d = new Dialog("Forgot password for " + userName);
        FormLayout form = new FormLayout();
        PasswordField pw1 = new PasswordField("Password");
        PasswordField pw2 = new PasswordField("Password again");
        Button submit = new Button("Submit");
        form.add(pw1, pw2, submit);
        d.add(form);
        submit.addClickListener(event -> {
            if(pw1.getValue().equals(pw2.getValue())){
                User user = getByUserName(userName);
                if(user == null){
                    Notification.show("User not found!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
                else{
                    user.setPasswordHash(encoder.encode(pw1.getValue()));
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

//
//    private String getCsrfTokenFromCookie() {
//        VaadinServletRequest vaadinRequest = (VaadinServletRequest) VaadinRequest.getCurrent();
//        Cookie[] cookies = vaadinRequest.getHttpServletRequest().getCookies();
//
//        for (Cookie cookie : cookies) {
//            if ("XSRF-TOKEN".equals(cookie.getName())) {
//                return cookie.getValue();
//            }
//        }
//        return null;
//    }
}
