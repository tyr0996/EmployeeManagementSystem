package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.pages.CodeStorePage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CodeStoreCrudTest extends BaseCrudTest {
    @Test
    public void codeStoreCreateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Deletable", true);
        DoCreateTestData testResult = codeStorePage.doCreateTest(withData);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("CodeStore saved: ");
    }

    @Test
    public void codeStoreReadTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        DoReadTestData testResult = codeStorePage.doReadTest(null, true);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
    }

    @Test
    public void codeStoreDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);

        LinkedHashMap<String, Object> withDataCreate = new LinkedHashMap<>();
        withDataCreate.put("Deletable", true);

        DoDeleteTestData testResult = codeStorePage.doDeleteTest();

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        Assert.assertEquals(testResult.getNonDeletedRowNumberOnlyDeletableAfterMethod(), testResult.getOriginalNonDeletedOnlyDeletable() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Codestore deleted: ");

        codeStorePage.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        Assert.assertEquals(1, codeStorePage.getGrid().getTotalDeletedRowNumber(codeStorePage.getShowDeletedCheckBox()));
        Assert.assertEquals(0, codeStorePage.getGrid().getTotalNonDeletedRowNumber(codeStorePage.getShowDeletedCheckBox()));
        codeStorePage.getGrid().resetFilter();
    }

    @Test
    public void codeStoreUpdateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Deletable", true);
        DoUpdateTestData testResult = codeStorePage.doUpdateTest(withData, withData);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("CodeStore updated: ");

        codeStorePage.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        Assert.assertEquals(0, codeStorePage.getGrid().getTotalDeletedRowNumber(codeStorePage.getShowDeletedCheckBox()));
        Assert.assertEquals(0, codeStorePage.getGrid().getTotalNonDeletedRowNumber(codeStorePage.getShowDeletedCheckBox()));
        codeStorePage.getGrid().resetFilter();
    }

    @Test
    public void codeStoreRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        DoRestoreTestData testResult = codeStorePage.doRestoreTest();

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("CodeStore restored: ");

        codeStorePage = new CodeStorePage(driver, port);
        codeStorePage.getGrid().applyFilter(testResult.getResult().getRestoredData());
        codeStorePage.getGrid().waitForRefresh();
        Assert.assertEquals(codeStorePage.getGrid().getTotalDeletedRowNumber(codeStorePage.getShowDeletedCheckBox()), 0);
        Assert.assertEquals(codeStorePage.getGrid().getTotalNonDeletedRowNumber(codeStorePage.getShowDeletedCheckBox()), 1);
        codeStorePage.getGrid().resetFilter();

    }

    @Test
    public void codeStorePermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        DoPermanentlyDeleteTestData testResult = codeStorePage.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("CodeStore permanently deleted: ");


        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        codeStorePage.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        Assert.assertEquals(0, codeStorePage.getGrid().getTotalDeletedRowNumber(codeStorePage.getShowDeletedCheckBox()));
        Assert.assertEquals(0, codeStorePage.getGrid().getTotalNonDeletedRowNumber(codeStorePage.getShowDeletedCheckBox()));
        codeStorePage.getGrid().resetFilter();
    }

//    @Test
//    public void gettingCountryCodesFailed() throws SQLException {
//        EmptyLoggedInVaadinPage loggedInPage =
//                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
//        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);
//
//        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
//        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 1);
//        codeStorePage.getCreateButton().click();
//
//        CodeStoreSaveOrUpdateDialog dialog = new CodeStoreSaveOrUpdateDialog(driver);
//        dialog.initWebElements();
//        List<FailedVaadinFillableComponent> failedComponents = dialog.getFailedComponents();
//        Assert.assertEquals(failedComponents.size(), 1);
//        Assert.assertEquals(failedComponents.get(0).getErrorMessage(), "Error happened while getting countries");
//    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        DoDeleteFailedTestData testResult = codeStorePage.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Internal Server Error");
    }

    @Test
    public void databaseNotAvailableWhilePermanentlyDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        DoPermanentlyDeleteFailedTestData testResult = codeStorePage.doDatabaseNotAvailableWhenPermanentlyDeleteTest(spyDataSource);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("CodeStore permanently deletion failed: Internal Server Error");
    }

    @Test
    public void databaseNotAvailableWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);

        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Deletable", true);
        DoUpdateFailedTestData testResult = codeStorePage.doDatabaseNotAvailableWhenUpdateTest(withData, null, spyDataSource, 0);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("CodeStore modifying failed: Internal Server Error");
        Assert.assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    public void databaseNotAvailableWhenCreate() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        DoCreateFailedTestData testResult = codeStorePage.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("CodeStore saving failed: Internal Server Error");
        Assert.assertEquals(0, testResult.getResult().getFailedFields().size());
    }
}
