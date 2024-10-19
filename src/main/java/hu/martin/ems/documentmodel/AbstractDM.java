package hu.martin.ems.documentmodel;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.util.FileUtil;

@Getter
public abstract class AbstractDM {

    public static final String TEMPLATE_DIRECTORY = "src/main/resources/templates/";

    private byte[] template;

    public AbstractDM(String template) {
        if (template != null) {
            this.template = FileUtil.readAllBytes(TEMPLATE_DIRECTORY + template);
        }
    }
}
