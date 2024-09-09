package hu.martin.ems;

import hu.martin.ems.core.config.DataProvider;
import hu.martin.ems.core.config.StaticDatas;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FilenameFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class EmployeeManagementSystemApplicationTests {

    @BeforeAll
    static void setUp() {
        DataProvider.saveAllSqlsFromJsons();
    }

    @Test
    public void testSqlFileCount() {
        Integer sqlFileCount = fileCountInFolder(StaticDatas.FolderPaths.GENERATED_SQL_FILES_PATH, ".sql");
        Integer jsonFileCount = fileCountInFolder(StaticDatas.FolderPaths.STATIC_JSON_FOLDER_PATH, ".json");

        assertEquals(jsonFileCount, sqlFileCount, "The number of the sql files not equals with the number of json files.");
    }

    private Integer fileCountInFolder(String folderPath, String extension){
        File folderFile = new File(folderPath);
        if (!folderFile.exists() || !folderFile.isDirectory()) {
            throw new IllegalStateException("Az " + folderFile.getName() + " mappa nem létezik vagy nem mappa!");
        }
        FilenameFilter filter = (dir, name) -> name.toLowerCase().endsWith(extension);
        String[] files = folderFile.list(filter);
        return files != null ? files.length : 0;
    }

    @Test
    public void testSqlGenerationFromJson(){
        String generatedSql = DataProvider.generateSqlFromJson(new File(StaticDatas.FolderPaths.STATIC_JSON_FOLDER_PATH + "\\roles.json"));
        assertEquals("INSERT INTO Role (name, deleted) VALUES\n\t('Martin', '0'),\n\t('Robi', '0')",
                generatedSql,
                "The generated sql not equals with the excepted");
    }



}
