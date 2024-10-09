package hu.martin.ems.crudFE;

import hu.martin.ems.core.config.DataProvider;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.User;
import hu.martin.ems.model.Role;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.User.UserList;
import org.apache.poi.ss.formula.functions.T;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
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
        assertEquals("INSERT INTO Role (name, deleted) VALUES\n\t('Martin', '0'),\n\t('Robi', '0'),\n\t('NO_ROLE', '0')",
                generatedSql,
                "The generated sql not equals with the excepted");
    }

    @Test
    public void mergeMapsTest() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        LinkedHashMap<String, List<String>> a = new LinkedHashMap<>();
        a.put("deleted", Arrays.asList("0", "1"));

        LinkedHashMap<String, List<String>> b = new LinkedHashMap<>();
        b.put("deleted", Arrays.asList("1"));
        b.put("username", Arrays.asList("developer"));
        Method mergeMaps = BaseVO.class.getDeclaredMethod("mergeMaps" , LinkedHashMap.class, LinkedHashMap.class);
        mergeMaps.setAccessible(true);
        Object o = new TestVO();
        LinkedHashMap<String, List<String>> aMergedB = (LinkedHashMap<String, List<String>>) mergeMaps.invoke(o, a, b);
        LinkedHashMap<String, List<String>> bMergedA = (LinkedHashMap<String, List<String>>) mergeMaps.invoke(o, b, a);
        mergeMaps.setAccessible(false);
        assertEquals(aMergedB, bMergedA);
    }

    protected class TestVO extends BaseVO{
        public TestVO(){
            super(1L, 0L);
        }
    }

}
