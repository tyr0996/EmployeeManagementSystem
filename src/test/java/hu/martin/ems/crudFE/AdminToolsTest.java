package hu.martin.ems.crudFE;

import com.google.gson.Gson;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.core.actuator.HealthStatusResponse;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.vaadin.core.EmsError;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.testng.annotations.Test;

import java.nio.charset.Charset;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.spy;
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
        assertNull(VaadinNotificationComponent.hasNotification(driver));
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
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void exportApisTest() throws Exception {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getExportApisButton().click();
        assertTrue(waitForDownload("ems_apis[0-9]*.json", 20, 1000));
        assertNull(VaadinNotificationComponent.hasNotification(driver));
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
        notification.close();
        assertFalse(waitForDownload("ems_apis[0-9]*.json", 200, 10));
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void healthStatusResponseUPTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        assertEquals(adminToolsPage.getHealthStatus().getText(), "UP");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void healthStatusResponseDOWNTest() {
        String mockResponse = BeanProvider.getBean(Gson.class).toJson(new HealthStatusResponse("DOWN"));
        MockingUtil.mockBaseUrlWebClientResponse(spyWebClientProvider, "actuator/health", mockResponse, String.class);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);
        assertEquals(adminToolsPage.getHealthStatus().getText(), "DOWN");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void healthStatusResponse500Test() {
        WebClient mockClient = spy(WebClient.class);
        WebClient.RequestHeadersUriSpec mockUriSpec = spy(WebClient.RequestHeadersUriSpec.class);
        EmsError bodyEntity = new EmsError(Instant.now().toEpochMilli(), 500, "Internal Server Error", "/actuator/health");
        byte[] responseBody = BeanProvider.getBean(Gson.class).toJson(bodyEntity).getBytes(Charset.defaultCharset());
        Mockito.when(mockUriSpec.uri("actuator/health")).thenThrow(new WebClientResponseException(500, "Internal Server Error", null, responseBody, Charset.defaultCharset()));
        Mockito.when(mockClient.get()).thenReturn(mockUriSpec);
        Mockito.doReturn(mockClient).when(spyWebClientProvider).initBaseUrlWebClient();

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        assertEquals(adminToolsPage.getHealthStatus().getText(), "Inaccessible");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }
}
