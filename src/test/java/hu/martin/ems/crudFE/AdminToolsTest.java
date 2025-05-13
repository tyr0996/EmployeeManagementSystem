package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.OrderPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminToolsTest extends BaseCrudTest {

    @Test
    public void clearDatabaseTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
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
        .when(spyAdminToolsService).clearAllDatabaseTable(anyBoolean());

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
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

    @Test
    public void exportApisTest() throws Exception {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getExportApisButton().click();
        assertTrue(waitForDownload("ems_apis[0-9]*.json", 20, 1000));
    }

    @Test
    public void exportApisFailedTestFailedNoBean() throws Exception {
        when(spyEndpointController.getHandlerMapping()).thenThrow(new RuntimeException());
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getExportApisButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Internal Server Error");
        assertFalse(waitForDownload("ems_apis[0-9]*.json", 200, 10));
    }

//    @Test
//    public void clearDatabase_entityDeletingFailedTest(){
//        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
//        loggedIn.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
//
//        OrderPage oPage = new OrderPage(driver, port);
//        oPage.initWebElements();
//
//        oPage.performDelete();
//        oPage.performPermanentlyDelete();
//
//        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);
//        AdminToolsPage page = new AdminToolsPage(driver, port);
//        page.getClearDatabaseButton().click();
//    }
}
