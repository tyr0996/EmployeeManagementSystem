package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.CityPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.CitySaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CityCrudTest extends BaseCrudTest {
    @Test
    public void cityCreateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoCreateTestData testResult = cityPage.doCreateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("City saved: ");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void cityReadTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoReadTestData testResult = cityPage.doReadTest(true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void cityDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoDeleteTestData testResult = cityPage.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);

        cityPage.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, cityPage.getGrid().getTotalDeletedRowNumber(cityPage.getShowDeletedSwitch()));
        assertEquals(0, cityPage.getGrid().getTotalNonDeletedRowNumber(cityPage.getShowDeletedSwitch()));
        cityPage.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void cityUpdateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoUpdateTestData testResult = cityPage.doUpdateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("City updated: ");

        cityPage.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, cityPage.getGrid().getTotalDeletedRowNumber(cityPage.getShowDeletedSwitch()));
        assertEquals(0, cityPage.getGrid().getTotalNonDeletedRowNumber(cityPage.getShowDeletedSwitch()));
        cityPage.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void cityRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoRestoreTestData testResult = cityPage.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("City restored: ");

        cityPage = new CityPage(driver, port);
        cityPage.getGrid().applyFilter(testResult.getResult().getRestoredData());
        cityPage.getGrid().waitForRefresh();
        assertEquals(cityPage.getGrid().getTotalDeletedRowNumber(cityPage.getShowDeletedSwitch()), 0);
        assertEquals(cityPage.getGrid().getTotalNonDeletedRowNumber(cityPage.getShowDeletedSwitch()), 1);
        cityPage.getGrid().resetFilter();

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void cityPermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoPermanentlyDeleteTestData testResult = cityPage.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("City permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        cityPage.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, cityPage.getGrid().getTotalDeletedRowNumber(cityPage.getShowDeletedSwitch()));
        assertEquals(0, cityPage.getGrid().getTotalNonDeletedRowNumber(cityPage.getShowDeletedSwitch()));
        cityPage.getGrid().resetFilter();

        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Clearing database was successful");
        notificationComponent.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void gettingCountryCodesFailed() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        cityPage.getCreateButton().click();

        CitySaveOrUpdateDialog dialog = new CitySaveOrUpdateDialog(driver);
        dialog.initWebElements();
        List<FailedVaadinFillableComponent> failedComponents = dialog.getFailedComponents();
        assertEquals(failedComponents.size(), 1);
        assertEquals(failedComponents.get(0).getErrorMessage(), "EmsError happened while getting countries");
        dialog.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoDeleteFailedTestData testResult = cityPage.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Database error");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhilePermanentlyDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoPermanentlyDeleteFailedTestData testResult = cityPage.doDatabaseNotAvailableWhenPermanentlyDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("City permanently deletion failed: Database error");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoUpdateFailedTestData testResult = cityPage.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("City modifying failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenCreate() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoCreateFailedTestData testResult = cityPage.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("City saving failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenSetupCities() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(0, 1, 2));
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);

        SoftAssert sa = new SoftAssert();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "Getting cities failed");
        notification.close();
        sa.assertEquals(cityPage.getGrid().getTotalDeletedRowNumber(cityPage.getShowDeletedSwitch()), 0);
        VaadinNotificationComponent notification2 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification2.getText(), "Getting cities failed");
        notification2.close();
        sa.assertEquals(cityPage.getGrid().getTotalNonDeletedRowNumber(cityPage.getShowDeletedSwitch()), 0);
        VaadinNotificationComponent notification3 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification3.getText(), "Getting cities failed");
        notification3.close();

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }
}
