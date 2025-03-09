package hu.martin.ems.core.config;

import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class StaticDatas {
    public static final Long CURRENCIES_CODESTORE_ID = 1L;
    public static final Long AMOUNTUNITS_CODESTORE_ID = 2L;
    public static final Long ORDER_STATES_CODESTORE_ID = 3L;
    public static final Long TAXKEYS_CODESTORE_ID = 4L;
    public static final Long STREET_TYPES_CODESTORE_ID = 5L;
    public static final Long COUNTRIES_CODESTORE_ID = 6L;
    public static final Long PAYMENT_TYPES_CODESTORE_ID = 7L;

    public class FolderPaths{
        public static final String PROJECT_ROOT = System.getProperty("user.dir");
        public static final String GENERATED_SQL_FILES_PATH = PROJECT_ROOT + "\\src\\test\\java\\hu\\martin\\ems\\sql";
        public static final String STATIC_JSON_FOLDER_PATH = PROJECT_ROOT + "\\src\\main\\resources\\static";
    }


public class ContentType{
//        public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
//        public static final String CONTENT_TYPE_TEXT_HTML = "text/html";
        //public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
//        public static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";
//        public static final String CONTENT_TYPE_IMAGE_JPEG = "image/jpeg";
//        public static final String CONTENT_TYPE_IMAGE_PNG = "image/png";
//        public static final String CONTENT_TYPE_VIDEO_MP4 = "video/mp4";
//        public static final String CONTENT_TYPE_AUDIO_MP3 = "audio/mp3";
        public static final String CONTENT_TYPE_APPLICATION_PDF = "application/pdf";
//        public static final String CONTENT_TYPE_APPLICATION_ZIP = "application/zip";
//        public static final String CONTENT_TYPE_APPLICATION_MSWORD = "application/msword";
//        public static final String CONTENT_TYPE_APPLICATION_RTF = "application/rtf";
//        public static final String CONTENT_TYPE_APPLICATION_EXCEL = "application/excel";
    }


public class Produces{
        public static final String JSON = "application/json;charset=UTF-8";
    }


public class Consumes{
        public static final String JSON = "application/json;charset=UTF8";
    }

    public class Selenium{
        public static final String downloadPath = "D:\\Fejleszto\\selenium\\_downloadForSelenium";

    }

    public enum Icons {
        PDF_FILE("pdf-file"),
        ODT_FILE("odt-file"),
        XLSX_FILE("xlsx-file"),
        PERMANENTLY_DELETE("clear"),
        EDIT("edit");


        private String svgPath;

        Icons(String svgPath) {
            this.svgPath = svgPath;
        }

        public SvgIcon create() {
            try {
                byte[] iconData = Files.readAllBytes(Paths.get("src/main/resources/frontend/icon/" + svgPath + ".svg"));
                StreamResource resource = new StreamResource(this.svgPath, () -> new ByteArrayInputStream(iconData));
                return new SvgIcon(resource);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
