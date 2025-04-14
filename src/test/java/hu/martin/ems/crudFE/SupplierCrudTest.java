package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.SupplierPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.SupplierSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Listeners(UniversalVideoListener.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SupplierCrudTest extends BaseCrudTest {
//    private static CrudTestingUtil crudTestingUtil;
//    private static WebDriverWait notificationDisappearWait;
//
//    private static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
//    private static final String gridXpath = contentXpath + "/vaadin-grid";
//    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-button";
//    
//    private static final String mainMenu = UIXpaths.ADMIN_MENU;
//    private static final String subMenu = UIXpaths.SUPPLIER_SUBMENU;
//
//    private GridTestingUtil gridTestingUtil;
//
//    
//
//    @BeforeClass
//    public void setup() {
//        gridTestingUtil = new GridTestingUtil(driver);
//        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, driver, "Supplier", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
//        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
//    }

    @Test
    public void closeCreateDialogTest(){
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        supplierPage.getCreateButton().click();
        SupplierSaveOrUpdateDialog dialog = new SupplierSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        dialog.close();
    }

    @Test
    public void supplierCreateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoCreateTestData testResult = supplierPage.doCreateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Supplier saved: ");
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoDeleteFailedTestData testResult = supplierPage.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Internal Server Error");
    }

    @Test
    public void supplierReadTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoReadTestData testResult = supplierPage.doReadTest(null, true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
    }

    @Test
    public void supplierDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoDeleteTestData testResult = supplierPage.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Supplier deleted: ");

        supplierPage.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, supplierPage.getGrid().getTotalDeletedRowNumber(supplierPage.getShowDeletedCheckBox()));
        assertEquals(0, supplierPage.getGrid().getTotalNonDeletedRowNumber(supplierPage.getShowDeletedCheckBox()));
        supplierPage.getGrid().resetFilter();
    }

    @Test
    public void supplierUpdateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoUpdateTestData testResult = supplierPage.doUpdateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Supplier updated: ");

        supplierPage.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, supplierPage.getGrid().getTotalDeletedRowNumber(supplierPage.getShowDeletedCheckBox()));
        assertEquals(0, supplierPage.getGrid().getTotalNonDeletedRowNumber(supplierPage.getShowDeletedCheckBox()));
        supplierPage.getGrid().resetFilter();
    }

    @Test
    //@Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/addresses.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void supplierRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoRestoreTestData testResult = supplierPage.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Supplier restored: ");

        supplierPage = new SupplierPage(driver, port);
        supplierPage.getGrid().applyFilter(testResult.getResult().getRestoredData());
        supplierPage.getGrid().waitForRefresh();
        assertEquals(supplierPage.getGrid().getTotalDeletedRowNumber(supplierPage.getShowDeletedCheckBox()), 0);
        assertEquals(supplierPage.getGrid().getTotalNonDeletedRowNumber(supplierPage.getShowDeletedCheckBox()), 1);
        supplierPage.getGrid().resetFilter();
    }

    @Test
    public void supplierPermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoPermanentlyDeleteTestData testResult = supplierPage.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("Supplier permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        supplierPage.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, supplierPage.getGrid().getTotalDeletedRowNumber(supplierPage.getShowDeletedCheckBox()));
        assertEquals(0, supplierPage.getGrid().getTotalNonDeletedRowNumber(supplierPage.getShowDeletedCheckBox()));
        supplierPage.getGrid().resetFilter();


        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Clearing database was successful");
        notificationComponent.close();
    }
//
//    //@Test
//    public void extraFilterInvalidValue() {
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//         gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        NotificationCheck nc = new NotificationCheck();
//        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
//        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
//    }
    @Test
    
    public void databaseNotAvailableWhenModify() throws SQLException {
//        Mockito.doReturn(null).when(spySupplierService).update(any(Supplier.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoUpdateFailedTestData testResult = supplierPage.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("Supplier modifying failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    
    public void nullResponseFromServiceWhenCreate() throws SQLException {
//        Mockito.doReturn(null).when(spySupplierService).save(any(Supplier.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoCreateFailedTestData testResult = supplierPage.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Supplier saving failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    
    public void unexpectedResponseCodeWhenGettingAllSuppliers() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 0);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Error happened while getting suppliers");
        notification.close();

        SupplierPage supplierPage = new SupplierPage(driver, port);
        assertEquals(supplierPage.getGrid().getTotalNonDeletedRowNumber(supplierPage.getShowDeletedCheckBox()), 0);
        assertEquals(supplierPage.getGrid().getTotalDeletedRowNumber(supplierPage.getShowDeletedCheckBox()), 0);
    }

    @Test
    
    public void unexpectedResponseCodeWhenGettingAddresses() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        supplierPage.getCreateButton().click();

        SupplierSaveOrUpdateDialog dialog = new SupplierSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        List<FailedVaadinFillableComponent> failedComponents = dialog.getFailedComponents();
        assertEquals(failedComponents.size(), 1);
        assertEquals(failedComponents.get(0).getErrorMessage(), "Error happened while getting addresses");
    }
}
