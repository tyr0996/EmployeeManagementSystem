package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.jcraft.jsch.*;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.core.config.JschConfig;
import hu.martin.ems.core.file.XLSX;
import hu.martin.ems.core.model.EmailAttachment;
import hu.martin.ems.core.model.EmailProperties;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.pages.*;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinDatePickerComponent;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.doTestData.DoDeleteFailedTestData;
import hu.martin.ems.pages.core.doTestData.DoDeleteTestData;
import hu.martin.ems.pages.core.doTestData.DoRestoreTestData;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@Listeners(UniversalVideoListener.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderCrudTest extends BaseCrudTest {
    @AfterClass
    public void destroy_2() throws IOException {
        resetDatabase();
        System.out.println("Database reseted");
    }
    @BeforeClass
    public void setup() {
        spyOrderService.setRegistry(spyRegistry);
    }

    @Test
    public void sendSFTPFailed_HostIsNull() {
        String originalHost = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpHost");
        ReflectionTestUtils.setField(spyJschConfig, "sftpHost", null);

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "EmsError happened when sending with SFTP");
        notification.close();

        ReflectionTestUtils.setField(spyJschConfig, "sftpHost", originalHost);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sendSFTPFailed_NoPasswordProvided() {
        String sftpPassword = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpPassword");
        ReflectionTestUtils.setField(spyJschConfig, "sftpPassword", null);

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "EmsError happened when sending with SFTP");
        notification.close();
        ReflectionTestUtils.setField(spyJschConfig, "sftpPassword", sftpPassword);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sendSFTPFailed_UnknownError() throws JSchException {
        String sftpUserName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpUser");
        String sftpHostName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpHost");
        int sftpPort = Integer.parseInt(ReflectionTestUtils.getField(spyJschConfig, "sftpPort").toString());

        JSch jsch = mock(JSch.class);
        Session mockSession = mock(Session.class);
        ChannelSftp mockChannelSftp = mock(ChannelSftp.class);

        when(jsch.getSession(sftpUserName, sftpHostName, sftpPort)).thenReturn(mockSession);
        doThrow(new JSchException("Unknown error")).when(mockSession).connect();
        when(mockSession.openChannel("sftp")).thenReturn(mockChannelSftp);
        when(mockSession.getUserName()).thenReturn(sftpUserName);

        spyJschConfig.jsch = jsch;

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "EmsError happened when sending with SFTP");
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sendSFTPFailed_ChannelOpeningError() throws JSchException {
        JSch originalJsch = JschConfig.jsch;
        String sftpUserName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpUser");
        String sftpHostName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpHost");
        int sftpPort = Integer.parseInt(ReflectionTestUtils.getField(spyJschConfig, "sftpPort").toString());

        JSch jsch = mock(JSch.class);
        Session mockSession = mock(Session.class);

        when(jsch.getSession(sftpUserName, sftpHostName, sftpPort)).thenReturn(mockSession);
        doNothing().when(mockSession).connect();
        doThrow(JSchException.class).when(mockSession).openChannel("sftp");
        when(mockSession.getUserName()).thenReturn(sftpUserName);

        JschConfig.jsch = jsch;

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "EmsError happened when sending with SFTP");
        notification.close();

        JschConfig.jsch = originalJsch;
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sendSFTPFailed_AuthFail() throws JSchException {
        JSch originalJSch = JschConfig.jsch;
        String sftpUserName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpUser");
        String sftpHostName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpHost");
        int sftpPort = Integer.parseInt(ReflectionTestUtils.getField(spyJschConfig, "sftpPort").toString());

        JSch jsch = mock(JSch.class);
        Session mockSession = mock(Session.class);
        ChannelSftp mockChannelSftp = mock(ChannelSftp.class);

        when(jsch.getSession(sftpUserName, sftpHostName, sftpPort)).thenReturn(mockSession);
        doThrow(new JSchException("Auth fail")).when(mockSession).connect();
        when(mockSession.openChannel("sftp")).thenReturn(mockChannelSftp);
        when(mockSession.getUserName()).thenReturn(sftpUserName);

        spyJschConfig.jsch = jsch;

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "EmsError happened when sending with SFTP");
        notification.close();

        JschConfig.jsch = originalJSch;
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sendSFTPFailed_ConnectionRefused() throws JSchException {
        String sftpUserName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpUser");
        String sftpHostName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpHost");
        int sftpPort = Integer.parseInt(ReflectionTestUtils.getField(spyJschConfig, "sftpPort").toString());
        JSch originalJSch = JschConfig.jsch;

        JSch jsch = mock(JSch.class);
        Session mockSession = mock(Session.class);
        ChannelSftp mockChannelSftp = mock(ChannelSftp.class);

        when(jsch.getSession(sftpUserName, sftpHostName, sftpPort)).thenReturn(mockSession);
        doThrow(new JSchException("connect error", new ConnectException("Connection refused"))).when(mockSession).connect();
        when(mockSession.openChannel("sftp")).thenReturn(mockChannelSftp);
        when(mockSession.getUserName()).thenReturn(sftpUserName);

        spyJschConfig.jsch = jsch;

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "EmsError happened when sending with SFTP");
        notification.close();
        JschConfig.jsch = originalJSch;
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }



    @Test
    public void generateODTFailedCurrencyException() throws Exception {
        clearCurrencyDatabaseTable();
        Mockito.doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error")).when(spyRestTemplate).getForObject(Mockito.eq(fetchingCurrencyApiUrl + baseCurrency), Mockito.any(Class.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }
        VaadinButtonComponent odtButton = page.getGrid().getOptionAnchorButton(rowLocation, 1);
        odtButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), EmsResponse.Description.DOCUMENT_GENERATION_FAILED_CURRENCY_CONVERT_FAILED);
        notification.close();
        assertFalse(waitForDownload("order_[0-9]{1,}.odt", 200, 10));
        Mockito.reset(spyRegistry);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void generateODTFailed_IOException() throws Exception {
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }
        VaadinButtonComponent odtButton = page.getGrid().getOptionAnchorButton(rowLocation, 1);
        odtButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("Document generation failed. Missing template file.", notification.getText());
        notification.close();
        assertFalse(waitForDownload("order_[0-9]{1,}.odt", 200, 10));

        Mockito.reset(spyRegistry);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void generateODTFailedXDocReportException() throws Exception {
        Mockito.doThrow(XDocReportException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }
        VaadinButtonComponent odtButton = page.getGrid().getOptionAnchorButton(rowLocation, 1);
        odtButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("Document generation failed. Not supported file type", notification.getText());
        notification.close();
        assertFalse(waitForDownload("order_[0-9]{1,}.odt", 200, 10));
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }



    @Test
    public void generatePDFFailedCurrencyException() throws Exception {
        Mockito.doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error")).when(spyRestTemplate).getForObject(Mockito.eq(fetchingCurrencyApiUrl + baseCurrency), Mockito.any(Class.class));
        clearCurrencyDatabaseTable();
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }
        VaadinButtonComponent pdfButton = page.getGrid().getOptionAnchorButton(rowLocation, 2);
        pdfButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(EmsResponse.Description.DOCUMENT_GENERATION_FAILED_CURRENCY_CONVERT_FAILED, notification.getText());
        notification.close();
        assertFalse(waitForDownload("order_[0-9]{1,}.pdf", 200, 10));

        Mockito.reset(spyRegistry);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void generatePDFFailedIOException() throws Exception {
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }
        VaadinButtonComponent pdfButton = page.getGrid().getOptionAnchorButton(rowLocation, 2);
        pdfButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("Document generation failed. Missing template file.", notification.getText());
        notification.close();
        assertFalse(waitForDownload("order_[0-9]{1,}.pdf", 200, 10));

        Mockito.reset(spyRegistry);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void generatePDFFailedXDocReportException() throws Exception {
        Mockito.doThrow(XDocReportException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }
        VaadinButtonComponent pdfButton = page.getGrid().getOptionAnchorButton(rowLocation, 2);
        pdfButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("Document generation failed. Not supported file type", notification.getText());
        notification.close();
        assertFalse(waitForDownload("order_[0-9]{1,}.pdf", 200, 10));
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }


    @Test
    public void generateODTTest() throws Exception {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }
        VaadinButtonComponent odtButton = page.getGrid().getOptionAnchorButton(rowLocation, 1);
        odtButton.click();
        assertTrue(waitForDownload("order_[0-9]{1,}.odt", 200, 10));
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void generatePDFTest() throws Exception {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }
        VaadinButtonComponent odtButton = page.getGrid().getOptionAnchorButton(rowLocation, 2);
        odtButton.click();
        assertTrue(waitForDownload("order_[0-9]{1,}.pdf", 200, 10), "Download not happened!");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sendSFTPFailedXLSGenerationTest() throws IOException {
        XLSX spyXlsx = spy(XLSX.class);
        MockitoAnnotations.openMocks(this);

        Mockito.doThrow(IOException.class).when(spyXlsx).createExcelFile(any(), any());

        spyOrderService.setXlsx(spyXlsx);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "XLS generation failed");
        notification.close();
        Mockito.clearInvocations(spyOrderService.getXlsx());
        Mockito.reset(spyOrderService.getXlsx());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sendSFTPFailed_uploadFailed() throws JSchException, SftpException {
        JSch originalJsch = JschConfig.jsch;
        ChannelSftp originalChannelSftp = spyJschConfig.getChannelSftp();
        Session originalSession = spyJschConfig.getSession();
        String sftpUserName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpUser");
        String sftpHostName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpHost");
        int sftpPort = Integer.parseInt(ReflectionTestUtils.getField(spyJschConfig, "sftpPort").toString());

        JSch jsch = mock(JSch.class);
        Session mockSession = mock(Session.class);
        ChannelSftp mockChannelSftp = mock(ChannelSftp.class);

        when(jsch.getSession(sftpUserName, sftpHostName, sftpPort)).thenReturn(mockSession);
        doNothing().when(mockSession).connect();
        doNothing().when(mockChannelSftp).connect();
        doThrow(SftpException.class).when(mockChannelSftp).put(any(InputStream.class), anyString());
        when(mockSession.getUserName()).thenReturn(sftpUserName);

        spyJschConfig.setChannelSftp(mockChannelSftp);
        spyJschConfig.setSession(mockSession);
        when(spyJschConfig.getChannelSftp().isConnected()).thenReturn(true);

        spyJschConfig.jsch = jsch;

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "EmsError happened when sending with SFTP");
        notification.close();

        Mockito.reset(mockSession);
        Mockito.reset(jsch);
        Mockito.reset(mockChannelSftp);
        Mockito.clearInvocations(mockSession);
        Mockito.clearInvocations(jsch);
        Mockito.clearInvocations(mockChannelSftp);
        JschConfig.jsch = originalJsch;
        spyJschConfig.setSession(originalSession);
        spyJschConfig.setChannelSftp(originalChannelSftp);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }



    @Test
    public void sendSFTPSuccessTest() throws JSchException, SftpException {
        String sftpUserName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpUser");
        String sftpHostName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpHost");
        int sftpPort = Integer.parseInt(ReflectionTestUtils.getField(spyJschConfig, "sftpPort").toString());

        JSch originalJsch = JschConfig.jsch;

        JSch jsch = mock(JSch.class);
        Session mockSession = mock(Session.class);
        ChannelSftp mockChannelSftp = mock(ChannelSftp.class);

        when(jsch.getSession(sftpUserName, sftpHostName, sftpPort)).thenReturn(mockSession);
        doNothing().when(mockSession).connect();
        when(mockSession.openChannel("sftp")).thenReturn(mockChannelSftp);
        when(mockSession.getUserName()).thenReturn(sftpUserName);
        when(spyJschConfig.getChannelSftp()).thenReturn(mockChannelSftp);
        when(spyJschConfig.getChannelSftp().isConnected()).thenReturn(true);
        doNothing().when(mockChannelSftp).put(any(InputStream.class), anyString());

        JschConfig.jsch = jsch;

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "SFTP sending is done");
        notification.close();
        JschConfig.jsch = originalJsch;

        Mockito.reset(mockSession);
        Mockito.reset(jsch);
        Mockito.reset(mockChannelSftp);
        Mockito.clearInvocations(mockSession);
        Mockito.clearInvocations(jsch);
        Mockito.clearInvocations(mockChannelSftp);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sendEmailSuccessTest() throws MessagingException {
        Mockito.doNothing().when(spyEmailSendingService).transportSend(any(MimeMessage.class));

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }

        page = new OrderPage(driver, port);
        VaadinButtonComponent sendEmailButton = page.getGrid().getOptionColumnButton(rowLocation, 3);

        sendEmailButton.click();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Email sent!");
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }



    @Test
    public void generateEmailFailedDueToCantGetOrderFromOrderId() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            new VaadinNotificationComponent(driver).close();
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
            page.initWebElements();
        }

        VaadinButtonComponent sendEmailButton = page.getGrid().getOptionColumnButton(rowLocation, 3);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 2);
        sendEmailButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver, Duration.ofMillis(20000));
        assertEquals(notification.getText(), "Email generation failed");
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));

    }

    @Test
    public void sendEmailCreateAttachmentBodyPartFailedTest() throws MessagingException {
        Mockito.doThrow(MessagingException.class).when(spyEmailSendingService).createAttachmentBodyPart(any(EmailAttachment.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }

        VaadinButtonComponent sendEmailButton = page.getGrid().getOptionColumnButton(rowLocation, 3);
        sendEmailButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Email sending failed");
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sendEmailCreateMimeMessageFailedTest() throws MessagingException {
        Mockito.doThrow(MessagingException.class).when(spyEmailSendingService).createMimeMessage(any(jakarta.mail.Session.class), any(EmailProperties.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }

        VaadinButtonComponent sendEmailButton = page.getGrid().getOptionColumnButton(rowLocation, 3);
        sendEmailButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Email sending failed");
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sendEmailFailedTest() {
        EmptyLoggedInVaadinPage loggedInPage =
            (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }

        VaadinButtonComponent sendEmailButton = page.getGrid().getOptionColumnButton(rowLocation, 3);
        sendEmailButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Email sending failed");
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sendEmailPDFGenerationFailedIOException() throws IOException, XDocReportException {
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }

        VaadinButtonComponent sendEmailButton = page.getGrid().getOptionColumnButton(rowLocation, 3);
        sendEmailButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Email sending failed: " + EmsResponse.Description.DOCUMENT_GENERATION_FAILED_MISSING_TEMPLATE);
        notification.close();
        Mockito.reset(spyRegistry);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sendEmailPDFGenerationFailedCurrencyException() {
        clearCurrencyDatabaseTable();
        Mockito.doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error")).when(spyRestTemplate).getForObject(Mockito.eq(fetchingCurrencyApiUrl + baseCurrency), Mockito.any(Class.class));

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }

        VaadinButtonComponent sendEmailButton = page.getGrid().getOptionColumnButton(rowLocation, 3);
        sendEmailButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Email sending failed: " + EmsResponse.Description.DOCUMENT_GENERATION_FAILED_CURRENCY_CONVERT_FAILED);
        notification.close();
        Mockito.reset(spyRegistry);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sendEmailPDFGenerationFailedXDocReportException() throws IOException, XDocReportException {
        Mockito.doThrow(XDocReportException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }

        VaadinButtonComponent sendEmailButton = page.getGrid().getOptionColumnButton(rowLocation, 3);
        sendEmailButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Email sending failed: " + EmsResponse.Description.DOCUMENT_GENERATION_FAILED_NOT_SUPPORTED_FILE_TYPE);
        notification.close();
        Mockito.reset(spyDataSource);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }



    @Test
    public void deleteOrderTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);
        DoDeleteTestData testResult = page.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Order deleted: ");

        page.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void modifyOrderTest() {
        modifyOrder(null, true);

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    public void modifyOrder(String notificationText, Boolean needSuccess) {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }

        int original = page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox());
        String[] originalData = page.getGrid().getDataFromRowLocation(rowLocation, true);

        page.getGrid().goToPage(rowLocation.getPageNumber());
        VaadinButtonComponent modifyButton = page.getGrid().getModifyButton(rowLocation.getRowIndex());
        modifyButton.click();

        OrderCreateToCustomerPage createPage = new OrderCreateToCustomerPage(driver, port);

        createPage.getGrid().selectElements(3);
        createPage.getShowPreviouslyOrderedElementsCheckBox().setStatus(true);
        createPage.getCurrencyComboBox().fillWithRandom();
        createPage.getPaymentTypeComboBox().fillWithRandom();
        createPage.getCreateOrderButton().click();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertThat(notification.getText()).contains(notificationText == null ? "Order updated:" : notificationText);

        notification.close();

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        page.initWebElements();

        page.getGrid().applyFilter(originalData);
        assertEquals(needSuccess ? 0 : 1, page.getGrid().getTotalRowNumber());
        page.getGrid().resetFilter();
        assertEquals(original, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }



    @Test
    public void databaseNotAvailableWhileDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);

        DoDeleteFailedTestData testResult = page.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Database error");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }


    @Test
    public void deleteOrderElementWhatMemberOfAnOrder() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        //region orders menuben ha nincs elem, akkor létrehozunk egyet.
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        int originalVisibleRows = page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox());
        if(originalVisibleRows == 0) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
            originalVisibleRows = page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox());
        }
        VaadinButtonComponent deleteButton = page.getGrid().getDeleteButton(0);
        //endregion
        //region az orders menu-ben kitörölünk mindent (deleted:2)
        while(deleteButton != null && !deleteButton.isNull()){
            deleteButton.click();
            new VaadinNotificationComponent(driver).close();
            deleteButton = page.getGrid().getDeleteButton(0);
        }
        page.getShowDeletedCheckBox().setStatus(true);
        VaadinButtonComponent permanentlyDeleteButton = page.getGrid().getPermanentlyDeleteButton(0);
        while(permanentlyDeleteButton != null && !permanentlyDeleteButton.isNull()){
            permanentlyDeleteButton.click();
            new VaadinNotificationComponent(driver).close();
            permanentlyDeleteButton = page.getGrid().getPermanentlyDeleteButton(0);
        }
        //endregion
        //region az order element menu-ben is kitörlünk mindent (deleted:2)
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
        OrderElementPage oePage = new OrderElementPage(driver, port);

        VaadinButtonComponent oeDeleteButton = oePage.getGrid().getDeleteButton(0);
        while(oeDeleteButton != null){
            oeDeleteButton.click();
            new VaadinNotificationComponent(driver).close();
            oeDeleteButton = oePage.getGrid().getDeleteButton(0);
        }
        oePage.getShowDeletedCheckBox().setStatus(true);

        VaadinButtonComponent oePermanentlyDeleteButton = oePage.getGrid().getPermanentlyDeleteButton(0);
        while(oePermanentlyDeleteButton != null && !oePermanentlyDeleteButton.isNull()){
            oePermanentlyDeleteButton.click();
            new VaadinNotificationComponent(driver).close();
            oePermanentlyDeleteButton = oePage.getGrid().getPermanentlyDeleteButton(0);
        }
        //endregion
        //region létrehozunk pár darab order elementet Erdei Róbert névre
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Customer", "Erdei Róbert");
        oePage.performCreate(withData);
        oePage.performCreate(withData);
        oePage.performCreate(withData);
        oePage.performCreate(withData);
        //endregion
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
        OrderCreateToCustomerPage createPage = new OrderCreateToCustomerPage(driver, port);
        createPage.performCreate("Erdei Róbert");
        VaadinNotificationComponent.closeAll(driver);

        createPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
        oePage.initWebElements();
        VaadinButtonComponent oeDeleteButtonCreatedOrder = oePage.getGrid().getDeleteButton(0);
        while(oeDeleteButtonCreatedOrder != null){
            oeDeleteButtonCreatedOrder.click();
            new VaadinNotificationComponent(driver).close();
            oePage.initWebElements();
            oeDeleteButtonCreatedOrder = oePage.getGrid().getDeleteButton(0);
        }

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        page.initWebElements();
        page.getGrid().getModifyButton(0).click(); //TODO megcsinálni, hogy előtte nézze meg a szerkesztésben, hogy látja-e őket.
        OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
        assertEquals(ocPage.getGrid().getTotalNonDeletedRowNumber(null), 0);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void restoreOrderTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);
        int originalDeleted = page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox());
        if(originalDeleted == 0){
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            new VaadinNotificationComponent(driver).close();
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
            page.initWebElements();
            page.performDelete();
        }

        DoRestoreTestData testResult = page.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Order restored: ");

        page.getGrid().applyFilter(testResult.getResult().getRestoredData());
        page.getGrid().waitForRefresh();
        assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
//        assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 1);
        page.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void permanentlyDeleteOrderTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);

        int originalDeleted = page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox());
        if(originalDeleted == 0){
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
            ocPage.performCreate(null);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
            page.initWebElements();
            page.performDelete();
            new VaadinNotificationComponent(driver).close();
        }

        page.performPermanentlyDelete();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertThat(notification.getText()).contains("Order permanently deleted: ");
        notification.close();
        //TODO ellenőrzést megcsinálni

        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Clearing database was successful");
        notificationComponent.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void fromToDateSelectorFromIsLaterThenTo() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        LocalDate a = LocalDate.of(2024, 1, 15);
        LocalDate b = LocalDate.of(2024, 1, 30);

        String dateFormat = "yyyy. MM. dd.";

        checkFromToDatePicker(page.getFromDatePicker(), page.getToDatePicker(), a, b, a, b, dateFormat);
        checkFromToDatePicker(page.getFromDatePicker(), page.getToDatePicker(), b, a, b, b, dateFormat);
        checkFromToDatePicker(page.getToDatePicker(), page.getFromDatePicker(), b, a, b, a, dateFormat);
        checkFromToDatePicker(page.getToDatePicker(), page.getFromDatePicker(), a, b, a, a, dateFormat);
        checkFromToDatePicker(page.getFromDatePicker(), page.getToDatePicker(), a, a, a, a, dateFormat);
        checkFromToDatePicker(page.getToDatePicker(), page.getFromDatePicker(), a, a, a, a, dateFormat);

        checkFromToDatePicker(page.getFromDatePicker(), page.getToDatePicker(), a, null, a, null, dateFormat);
        checkFromToDatePicker(page.getFromDatePicker(), page.getToDatePicker(), null, a, null, a, dateFormat);
        checkFromToDatePicker(page.getToDatePicker(), page.getFromDatePicker(), a, null, a, null, dateFormat);
        checkFromToDatePicker(page.getToDatePicker(), page.getFromDatePicker(), null, a, null, a, dateFormat);

        checkFromToDatePicker(page.getFromDatePicker(), page.getToDatePicker(), null, null, null, null, dateFormat);
        assertNull(VaadinNotificationComponent.hasNotification(driver));

    }

    private void checkFromToDatePicker(VaadinDatePickerComponent datePicker1, VaadinDatePickerComponent datePicker2, LocalDate date1, LocalDate date2, LocalDate expectedDate1, LocalDate expectedDate2, String dateFormat){
        datePicker1.selectDate(date1);
        datePicker2.selectDate(date2);
        assertEquals(expectedDate1, datePicker1.getDate());
        assertEquals(expectedDate2, datePicker2.getDate());
    }

    @Test
    public void gettingOrdersFailed() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(0, 1, 2));
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);
        SoftAssert sa = new SoftAssert();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "EmsError happened while getting orders");
        notification.close();

        sa.assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        VaadinNotificationComponent notification2 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification2.getText(), "EmsError happened while getting orders");
        notification2.close();

        sa.assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        VaadinNotificationComponent notification3 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification3.getText(), "EmsError happened while getting orders");
        notification3.close();

        sa.assertNull(VaadinNotificationComponent.hasNotification(driver));
        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }
}
