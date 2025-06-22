package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.CodeStorePage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.CodeStoreSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CodeStoreCrudTest extends BaseCrudTest {
    @Test
    public void codeStoreCreateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Deletable", true);
        DoCreateTestData testResult = codeStorePage.doCreateTest(withData);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("CodeStore saved: ");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void codeStoreReadTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        DoReadTestData testResult = codeStorePage.doReadTest(true);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void codeStoreDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);

        LinkedHashMap<String, Object> withDataCreate = new LinkedHashMap<>();
        withDataCreate.put("Deletable", true);

        DoDeleteTestData testResult = codeStorePage.doDeleteTest();

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        Assert.assertEquals(testResult.getNonDeletedRowNumberOnlyDeletableAfterMethod(), testResult.getOriginalNonDeletedOnlyDeletable() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("CodeStore deleted: ");

        codeStorePage.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        Assert.assertEquals(1, codeStorePage.getGrid().getTotalDeletedRowNumber(codeStorePage.getShowDeletedSwitch()));
        Assert.assertEquals(0, codeStorePage.getGrid().getTotalNonDeletedRowNumber(codeStorePage.getShowDeletedSwitch()));
        codeStorePage.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void codeStoreUpdateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Deletable", true);
        DoUpdateTestData testResult = codeStorePage.doUpdateTest(withData, withData);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("CodeStore updated: ");

        codeStorePage.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        Assert.assertEquals(0, codeStorePage.getGrid().getTotalDeletedRowNumber(codeStorePage.getShowDeletedSwitch()));
        Assert.assertEquals(0, codeStorePage.getGrid().getTotalNonDeletedRowNumber(codeStorePage.getShowDeletedSwitch()));
        codeStorePage.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void codeStoreRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        DoRestoreTestData testResult = codeStorePage.doRestoreTest();

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("CodeStore restored: ");

        codeStorePage = new CodeStorePage(driver, port);
        codeStorePage.getGrid().applyFilter(testResult.getResult().getRestoredData());
        codeStorePage.getGrid().waitForRefresh();
        Assert.assertEquals(codeStorePage.getGrid().getTotalDeletedRowNumber(codeStorePage.getShowDeletedSwitch()), 0);
        Assert.assertEquals(codeStorePage.getGrid().getTotalNonDeletedRowNumber(codeStorePage.getShowDeletedSwitch()), 1);
        codeStorePage.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));

    }

    @Test
    public void codeStorePermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        DoPermanentlyDeleteTestData testResult = codeStorePage.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("CodeStore permanently deleted: ");


        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        codeStorePage.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        Assert.assertEquals(0, codeStorePage.getGrid().getTotalDeletedRowNumber(codeStorePage.getShowDeletedSwitch()));
        Assert.assertEquals(0, codeStorePage.getGrid().getTotalNonDeletedRowNumber(codeStorePage.getShowDeletedSwitch()));
        codeStorePage.getGrid().resetFilter();

        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Clearing database was successful");
        notificationComponent.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        DoDeleteFailedTestData testResult = codeStorePage.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Database error");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhilePermanentlyDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        DoPermanentlyDeleteFailedTestData testResult = codeStorePage.doDatabaseNotAvailableWhenPermanentlyDeleteTest(spyDataSource);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("CodeStore permanently deletion failed: Database error");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);

        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Deletable", true);
        DoUpdateFailedTestData testResult = codeStorePage.doDatabaseNotAvailableWhenUpdateTest(withData, null, spyDataSource, 0);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("CodeStore modifying failed: Database error");
        Assert.assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenCreate() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        DoCreateFailedTestData testResult = codeStorePage.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        Assert.assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        Assert.assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("CodeStore saving failed: Database error");
        Assert.assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void dialogClosingTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);
        codeStorePage.getCreateButton().click();
        CodeStoreSaveOrUpdateDialog dialog = new CodeStoreSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        dialog.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void datahbaseNotAvailableWhenGettingAllCodeStore() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);

        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(0, 1, 2));
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CODESTORE_SUBMENU);

        CodeStorePage codeStorePage = new CodeStorePage(driver, port);

        SoftAssert sa = new SoftAssert();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "EmsError happened while getting codestores");
        notification.close();
        sa.assertEquals(codeStorePage.getGrid().getTotalDeletedRowNumber(codeStorePage.getShowDeletedSwitch()), 0);
        VaadinNotificationComponent notification2 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification2.getText(), "EmsError happened while getting codestores");
        notification2.close();
        sa.assertEquals(codeStorePage.getGrid().getTotalNonDeletedRowNumber(codeStorePage.getShowDeletedSwitch()), 0);
        VaadinNotificationComponent notification3 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification3.getText(), "EmsError happened while getting codestores");
        notification3.close();

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }
}
