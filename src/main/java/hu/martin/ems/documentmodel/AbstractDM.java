package hu.martin.ems.documentmodel;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.util.FileUtil;

@Getter
@Setter
public abstract class AbstractDM {

    public static final String TEMPLATE_DIRECTORY = "src/main/resources/templates/";

    private byte[] template;
    private final Logger logger;

    public AbstractDM(String template) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        if (template != null) {
            this.template = FileUtil.readAllBytes(TEMPLATE_DIRECTORY + template);
        }
    }
}
