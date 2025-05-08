package hu.martin.ems.core.config;

import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.StreamResource;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class IconProvider {
    public final static  String PDF_FILE_ICON_ICON = "pdf-file";
    public final static  String ODT_FILE_ICON = "odt-file";
    public final static  String PERMANENTLY_DELETE_ICON = "clear";
    public final static  String EDIT_ICON = "edit";

    public SvgIcon create(String icon) {
        try {
            byte[] iconData = readAllBytes(Paths.get("src/main/resources/frontend/icon/" + icon + ".svg"));
            StreamResource resource = new StreamResource(icon, () -> new ByteArrayInputStream(iconData));
            return new SvgIcon(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] readAllBytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }
}
