package hu.martin.ems.documentmodel;

import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@NoArgsConstructor
public abstract class AbstractDM {
    public static final String TEMPLATE_DIRECTORY = "src/main/resources/templates/";

    private Path templatePath;

    public ByteArrayInputStream getTemplate() throws IOException {
        return new ByteArrayInputStream(Files.readAllBytes(templatePath));
    }

    public AbstractDM(@NotNull String template) {
        templatePath = Paths.get(TEMPLATE_DIRECTORY + template);
    }
}
