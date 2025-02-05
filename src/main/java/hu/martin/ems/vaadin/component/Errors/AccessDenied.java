package hu.martin.ems.vaadin.component.Errors;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.vaadin.MainView;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@PermitAll
@Route(value = "access-denied", layout = MainView.class)
@NeedCleanCoding
public class AccessDenied extends VerticalLayout implements HasErrorParameter<AccessDeniedException> {
    public AccessDenied() {
        add(getCatImage(), getNormalMessage(), getPermissionMessage(), getCoffeMessage());

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    private Div getNormalMessage(){
        Div normalMessage = new Div();
        normalMessage.setText("You don't have permission to perform this action!");
        normalMessage.getStyle().set("color", "red")
                .set("font-size", "66px")
                .set("font-weight", "bold")
                .set("margin-top", "20px")
                .set("margin-right", "20px")
                .set("text-align", "center");
        return normalMessage;
    }

    private Div getPermissionMessage(){
        Div permissionMessage = new Div();
        permissionMessage.setText("Try to get permission from your boss!");
        permissionMessage.getStyle().set("color", "blue").set("font-size", "35px").set("font-style", "italic");
        return permissionMessage;
    }

    private Div getCoffeMessage(){
        Div coffeeMessage = new Div();
        coffeeMessage.setText("While you wait, take a coffee break.");
        coffeeMessage.getStyle().set("color", "blue").set("font-size", "35px").set("font-style", "italic");
        return coffeeMessage;
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<AccessDeniedException> parameter) {
        return HttpServletResponse.SC_FORBIDDEN;
    }

    private Image getCatImage(){
        Image catImage = new Image("https://i.pinimg.com/236x/b5/15/95/b51595de229aaa7712279a7653c307f0.jpg", "Angry cat");
        return catImage;
    }
}
