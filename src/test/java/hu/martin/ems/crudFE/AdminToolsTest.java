package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class AdminToolsTest extends BaseCrudTest {

    @Test
    public void clearDatabaseTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Clearing database was successful");
        notificationComponent.close();
    }

    @Test
    public void clearDatabaseExceptionsTestThanSuccess() throws Exception {
        Mockito.doThrow(new ClassNotFoundException())
               .doCallRealMethod()
        .when(spyAdminToolsService).clearAllDatabaseTable();

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponentFail = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponentFail.getText(), "Clearing database failed for one or more table");
        notificationComponentFail.close();

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponentSucc = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponentSucc.getText(), "Clearing database was successful");
        notificationComponentSucc.close();
    }
}
