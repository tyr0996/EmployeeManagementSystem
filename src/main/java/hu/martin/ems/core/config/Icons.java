package hu.martin.ems.core.config;

import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.StreamResource;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public enum Icons {
    PDF_FILE("pdf-file"),
    ODT_FILE("odt-file"),
    XLSX_FILE("xlsx-file"),
    PERMANENTLY_DELETE("clear"),
    EDIT("edit");


    @Getter
    private String svgPath;

    Icons(String svgPath) {
        this.svgPath = svgPath;
    }

    public SvgIcon create() {
        try {
            byte[] iconData = Files.readAllBytes(Paths.get("src/main/resources/frontend/icon/" + getSvgPath() + ".svg"));
            StreamResource resource = new StreamResource(getSvgPath(), () -> new ByteArrayInputStream(iconData));
            return new SvgIcon(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
