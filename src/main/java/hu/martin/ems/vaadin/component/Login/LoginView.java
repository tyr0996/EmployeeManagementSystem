package hu.martin.ems.vaadin.component.Login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("login")
public class LoginView extends VerticalLayout {
    public LoginView() {
        // Form layout létrehozása
        FormLayout formLayout = new FormLayout();

        // LoginForm létrehozása
        LoginForm loginForm = new LoginForm();
        loginForm.setAction("login");

        // Login event kezelés
        loginForm.addLoginListener(e -> {
            String username = e.getUsername();
            String password = e.getPassword();

            if (authenticate(username, password)) {
                Notification.show("Sikeres bejelentkezés");
                // További teendők sikeres bejelentkezés után
            } else {
                Notification.show("Sikertelen bejelentkezés");
                loginForm.setError(true);
            }
        });

        // Cím hozzáadása
        H1 title = new H1("Bejelentkezés");

        // Komponensek hozzáadása a form layouthoz
        formLayout.add(title, loginForm);

        // A form layout hozzáadása a fő layouthoz
        add(formLayout);
    }

    // Egyszerű autentikációs függvény
    private boolean authenticate(String username, String password) {
        return "felhasznalo".equals(username) && "jelszo".equals(password);
    }
}
