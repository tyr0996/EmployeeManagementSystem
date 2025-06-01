package hu.martin.ems.core.vaadin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.StreamResource;
import hu.martin.ems.core.model.EmsResponse;

import java.io.ByteArrayInputStream;
import java.util.function.Supplier;

public class DownloadButton extends Anchor {
    public DownloadButton(SvgIcon icon, String fileName, Supplier<EmsResponse> apiCall){
        Button downloadButton = new Button(icon);
        this.add(downloadButton);

        downloadButton.addClickListener(event -> {
            clickListenerEvent(fileName, apiCall);
        });
    }

    public DownloadButton(String text, String fileName, Supplier<EmsResponse> apiCall){
        Button downloadButton = new Button(text);
        this.add(downloadButton);

        downloadButton.addClickListener(event -> {
            clickListenerEvent(fileName, apiCall);
        });
    }

    private void clickListenerEvent(String fileName, Supplier<EmsResponse> apiCall){
        EmsResponse response = apiCall.get();
        if (response.getCode() == 200) {
            StreamResource resource = new StreamResource(fileName, () -> (ByteArrayInputStream) response.getResponseData());
            this.setHref(resource);
            this.getElement().setAttribute("download", fileName);
            this.getElement().callJsFunction("click");
        } else {
            this.setHref("");
            Notification.show(response.getDescription())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
