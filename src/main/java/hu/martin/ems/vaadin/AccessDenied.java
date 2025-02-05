package hu.martin.ems.vaadin;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.annotations.NeedCleanCoding;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@AnonymousAllowed
@Route(value = "access-denied", layout = MainView.class)
@NeedCleanCoding
public class AccessDenied extends VerticalLayout {
    public AccessDenied(){
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setSpacing(true);
        setPadding(true);

        H1 title = new H1("Access Denied");
        Paragraph message = new Paragraph("You have no permission to view this page!");

        add(title, message);
    }
}
