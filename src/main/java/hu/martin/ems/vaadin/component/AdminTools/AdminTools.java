package hu.martin.ems.vaadin.component.AdminTools;

import com.google.gson.Gson;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.actuator.HealthStatusResponse;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.DownloadButton;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.AdminToolsApiClient;
import jakarta.annotation.security.RolesAllowed;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "adminTools", layout = MainView.class)
@NeedCleanCoding
@RolesAllowed("ROLE_AdminToolsMenuOpenPermission")
public class AdminTools extends VerticalLayout {
    private final AdminToolsApiClient adminToolsApiClient = BeanProvider.getBean(AdminToolsApiClient.class);
    private final Gson gson = BeanProvider.getBean(Gson.class);

    public AdminTools(PaginationSetting paginationSetting) {
        Button clearDatabaseButton = createClearDatabaseButton();
        DownloadButton exportEndpoints = new DownloadButton("Export APIs", "ems_apis.json", () -> adminToolsApiClient.exportApis());
        Div actuator = createActuatorDiv();

        add(clearDatabaseButton, exportEndpoints, actuator);
    }

    private Div createActuatorDiv() {
        EmsResponse status = adminToolsApiClient.healthStatus();
        Div actuator = new Div();
        HorizontalLayout health = new HorizontalLayout();
        health.add(new Paragraph("Health status"));
        String healthStatus = gson.fromJson(status.getResponseData().toString(), HealthStatusResponse.class).getStatus();
        Paragraph healthStatusParagraph = new Paragraph(healthStatus);
        healthStatusParagraph.getStyle().set("color", healthStatus.equals("UP") ? "green" : "red");
        health.add(healthStatusParagraph);
        actuator.add(health);
        return actuator;
    }

    private Button createClearDatabaseButton() {
        Button clearDatabaseButton = new Button("Clear database");

        clearDatabaseButton.addClickListener(v -> {
            EmsResponse response = adminToolsApiClient.clearDatabase();
            Notification.show(response.getDescription()).addThemeVariants(
                    response.getCode() == 200 ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR);
        });

        return clearDatabaseButton;
    }
}
