package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.core.config.DataProvider;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.vaadin.component.BaseVO;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class EmployeeManagementSystemApplicationTests extends BaseCrudTest {

    @BeforeClass
    public void setUp() {
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
    public void mergeMapsTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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

    @Test
    public void equalsTest(){
        TestVO a = new TestVO(2, 0);
        TestVO b = new TestVO(2, 1);
        TestVO2 a2 = new TestVO2(2, 0);
        TestVO2 b2 = new TestVO2(2, 1);

        assertEquals(true, a.equals(b));
        assertEquals(true, b.equals(a));
        assertEquals(false, b2.equals(a));
        assertEquals(false, a2.equals(a));
        assertEquals(false, b2.equals(b));
        assertEquals(false, a2.equals(b));
        assertEquals(true, b2.equals(a2));
        assertEquals(true, b2.equals(a2));
        assertEquals(true, b2.equals(b2));
        assertEquals(true, a2.equals(a2));
        assertEquals(true, b.equals(b));
        assertEquals(true, a.equals(a));
    }

    protected class TestVO extends BaseVO{
        public TestVO(){
            super(1L, 0L);
        }

        public TestVO(long id, long deleted) {
            super(id, deleted);
        }
    }

    protected class TestVO2 extends BaseVO {
        public TestVO2(){
            super(1L, 0L);
        }

        public TestVO2(long id, long deleted) {
            super(id, deleted);
        }
    }

}
