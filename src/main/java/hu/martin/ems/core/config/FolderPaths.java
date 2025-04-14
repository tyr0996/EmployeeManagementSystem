package hu.martin.ems.core.config;

public interface FolderPaths {
    String PROJECT_ROOT = System.getProperty("user.dir");
    String GENERATED_SQL_FILES_PATH = PROJECT_ROOT + "\\src\\test\\resources\\sql";
    String STATIC_JSON_FOLDER_PATH = PROJECT_ROOT + "\\src\\main\\resources\\static";
}
