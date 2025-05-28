package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.core.config.DataProvider;
import hu.martin.ems.vaadin.component.BaseVO;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeManagementSystemApplicationTests extends BaseCrudTest {


    @Test(description = "It needs for coverage. The String-based switch statement compiles a very interesting byte code. I have to make a hashCode conflict")
    public void testDataProviderFixObjectName(){
        assertEquals("User".hashCode(), "UsfS".hashCode()); //This line needed to check that the User and the UsfS hashcode is equal.
        assertEquals(DataProvider.JsonFile.fixObjectName("User"), "loginuser");
        assertEquals(DataProvider.JsonFile.fixObjectName("Order"), "orders");
        assertEquals(DataProvider.JsonFile.fixObjectName("UsfS"), "UsfS");
        assertEquals(DataProvider.JsonFile.fixObjectName("OrdfS"), "OrdfS");
        assertEquals(DataProvider.JsonFile.fixObjectName("OthfS"), "OthfS");
        assertEquals(DataProvider.JsonFile.fixObjectName("Other"), "Other");
    }

    @Test
    public void testSqlFileCount() {
        Integer sqlFileCount = fileCountInFolder(dp.getGENERATED_SQL_FILES_PATH(), ".sql");
        Integer jsonFileCount = fileCountInFolder(dp.getSTATIC_JSON_FOLDER_PATH(), ".json");

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
    public void testSqlGenerationFromJson() throws IOException {
        String generatedSql = dp.generateSqlFromJson(new File(dp.getSTATIC_JSON_FOLDER_PATH() + "\\roles.json"));
        assertEquals(generatedSql,
                "INSERT INTO Role (name, deleted, id) VALUES\n\t('Martin', '0', '1'),\n\t('Robi', '0', '2'),\n\t('NO_ROLE', '0', '-1')",
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

//    @Test
//    public void entityForcePermanentlyDeleteViolatesForeignKeyConstraint() {
//        Role role = new Role();
//        role.setName("testRole");
//        role.setDeleted(0L);
//        Permission permission = new Permission();
//        permission.setDeleted(0L);
//        permission.setName("testPermission");
//        Set<Permission> permissionSet = new HashSet<>();
//        Long id = BeanProvider.getBean(PermissionRepository.class).customSave(permission).getId();
//        Permission savedPermission = BeanProvider.getBean(PermissionRepository.class).customFindById(id);
//        permissionSet.add(savedPermission);
//        role.setPermissions(permissionSet);
//        Role savedRole = BeanProvider.getBean(RoleRepository.class).customSave(role);
//        Long roleId = savedRole.getId();
//        BeanProvider.getBean(RoleRepository.class).customForcePermanentlyDelete(roleId);
//    }


//    @Test
//    public void copyEntityFailedTest() throws IllegalAccessException {
////        EntityUtil<TestVO> entityUtil = spy(EntityUtil.class);
//        EntityUtil<TestVO> realUtil = new EntityUtil<>();
//        EntityUtil<TestVO> entityUtil = spy(realUtil);
//
//        TestVO t1 = new TestVO(1, 0);
//        TestVO t2 = new TestVO(2, 0);
//
//        Mockito.doThrow(IllegalAccessException.class).when(entityUtil).copyField(any(), any(), any(Field.class));
//        assertThrows(RuntimeException.class, () -> {
//            entityUtil.copyEntity(t1, t2, TestVO.class);
//        });
//    }

//    @Test
//    public void oneOfTheIconsIOExceptionTest() throws Exception {
//        Icons icon = Icons.EDIT;
//
//        Field field = Icons.class.getDeclaredField("svgPath");
//        field.setAccessible(true);
//        field.set(icon, "definitely-nonexistent-icon");
//
//        EmptyLoggedInVaadinPage loggedInPage = (EmptyLoggedInVaadinPage)
//                LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
//        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);
//        InternalErrorNotification notification = new InternalErrorNotification(driver);
//        SoftAssert sa = new SoftAssert();
//        sa.assertEquals(notification.getCaption().getText(), "asdf");
//        sa.assertEquals(notification.getMessage().getText(), "asdfadsf");
//        sa.assertAll();
//
//        field.set(icon, "pdf-file");
//    }

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
