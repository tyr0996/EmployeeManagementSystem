package hu.martin.ems.vaadin.component.Errors;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import hu.martin.ems.vaadin.MainView;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;

@PermitAll
@Route(value = "not-found", layout = MainView.class)
public class NotFound extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        return HttpServletResponse.SC_NOT_FOUND;
    }

    public NotFound() {
        Image catImage = new Image("https://www.dailypaws.com/thmb/ZpHbaUCcoEQ5FPvB5XizOwaJFLI=/750x0/filters:no_upscale():max_bytes(150000):strip_icc():format(webp)/cat-games-fetch-301741957-2000-55f0272d106b40d3852635754f06a2e9.jpg", "Sad cat");

        Div angryMessage = new Div();
        angryMessage.setText("The page cannot be found because the kitty above played it somewhere");
        angryMessage.getStyle().set("color", "red")
                .set("font-size", "66px")
                .set("font-weight", "bold")
                .set("margin-top", "20px")
                .set("margin-right", "20px")
                .set("text-align", "center");

        Div cuteMessage = new Div();
        cuteMessage.setText("Maybe try again later");
        cuteMessage.getStyle().set("color", "blue").set("font-size", "35px").set("font-style", "italic");

        add(catImage, angryMessage, cuteMessage);

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }
}
