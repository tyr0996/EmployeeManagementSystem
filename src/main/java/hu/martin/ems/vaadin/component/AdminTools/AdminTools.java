package hu.martin.ems.vaadin.component.AdminTools;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.AdminToolsApiClient;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "adminTools", layout = MainView.class)
@AnonymousAllowed
@NeedCleanCoding
public class AdminTools extends VerticalLayout {
    private final AdminToolsApiClient adminToolsApiClient = BeanProvider.getBean(AdminToolsApiClient.class);

    public AdminTools(PaginationSetting paginationSetting) {
        Button b = new Button("Clear database");
        add(b);

        b.addClickListener(v -> {
            EmsResponse response = adminToolsApiClient.clearDatabase();
            Notification.show(response.getDescription()).addThemeVariants(
                    response.getCode() == 200 ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR);
        });
    }
}
