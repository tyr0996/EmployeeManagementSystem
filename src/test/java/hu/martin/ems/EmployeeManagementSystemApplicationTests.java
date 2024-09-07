package hu.martin.ems;

import hu.martin.ems.core.config.DataProvider;
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
        String projectRoot = System.getProperty("user.dir");

        String sqlDirectoryPath = projectRoot + "\\src\\test\\java\\hu\\martin\\ems\\sql";
        String jsonDirectoryPath = projectRoot + "\\src\\main\\resources\\static";

        Integer sqlFileCount = fileCountInFolder(sqlDirectoryPath, ".sql");
        Integer jsonFileCount = fileCountInFolder(jsonDirectoryPath, ".json");

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

}
