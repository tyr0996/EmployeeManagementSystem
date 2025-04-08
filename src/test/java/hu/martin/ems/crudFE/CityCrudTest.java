package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.CityPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.saveOrUpdateDialog.CitySaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CityCrudTest extends BaseCrudTest {
    @Test
    public void cityCreateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoCreateTestData testResult = cityPage.doCreateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("City saved: ");
    }

    @Test
    public void cityReadTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoReadTestData testResult = cityPage.doReadTest(null, true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
    }

    @Test
    public void cityDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoDeleteTestData testResult = cityPage.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
//        assertThat(testResult.getNotificationWhenPerform()).contains("City deleted: ");

        cityPage.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, cityPage.getGrid().getTotalDeletedRowNumber(cityPage.getShowDeletedCheckBox()));
        assertEquals(0, cityPage.getGrid().getTotalNonDeletedRowNumber(cityPage.getShowDeletedCheckBox()));
        cityPage.getGrid().resetFilter();
    }

    @Test
    public void cityUpdateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoUpdateTestData testResult = cityPage.doUpdateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("City updated: ");

        cityPage.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, cityPage.getGrid().getTotalDeletedRowNumber(cityPage.getShowDeletedCheckBox()));
        assertEquals(0, cityPage.getGrid().getTotalNonDeletedRowNumber(cityPage.getShowDeletedCheckBox()));
        cityPage.getGrid().resetFilter();
    }

    @Test
    public void cityRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoRestoreTestData testResult = cityPage.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("City restored: ");

        cityPage = new CityPage(driver, port);
        cityPage.getGrid().applyFilter(testResult.getResult().getRestoredData());
        cityPage.getGrid().waitForRefresh();
        assertEquals(cityPage.getGrid().getTotalDeletedRowNumber(cityPage.getShowDeletedCheckBox()), 0);
        assertEquals(cityPage.getGrid().getTotalNonDeletedRowNumber(cityPage.getShowDeletedCheckBox()), 1);
        cityPage.getGrid().resetFilter();

    }

    @Test
    public void cityPermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoPermanentlyDeleteTestData testResult = cityPage.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("City permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        cityPage.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, cityPage.getGrid().getTotalDeletedRowNumber(cityPage.getShowDeletedCheckBox()));
        assertEquals(0, cityPage.getGrid().getTotalNonDeletedRowNumber(cityPage.getShowDeletedCheckBox()));
        cityPage.getGrid().resetFilter();
    }

    @Test
    public void gettingCountryCodesFailed() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        cityPage.getCreateButton().click();

        CitySaveOrUpdateDialog dialog = new CitySaveOrUpdateDialog(driver);
        dialog.initWebElements();
        List<FailedVaadinFillableComponent> failedComponents = dialog.getFailedComponents();
        assertEquals(failedComponents.size(), 1);
        assertEquals(failedComponents.get(0).getErrorMessage(), "Error happened while getting countries");
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoDeleteFailedTestData testResult = cityPage.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Internal Server Error");
    }

    @Test
    public void databaseNotAvailableWhilePermanentlyDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoPermanentlyDeleteFailedTestData testResult = cityPage.doDatabaseNotAvailableWhenPermanentlyDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("City permanently deletion failed: Internal Server Error");
    }

    @Test
    public void databaseNotAvailableWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoUpdateFailedTestData testResult = cityPage.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("City modifying failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    public void databaseNotAvailableWhenCreate() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CITY_SUBMENU);

        CityPage cityPage = new CityPage(driver, port);
        DoCreateFailedTestData testResult = cityPage.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("City saving failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }
}
