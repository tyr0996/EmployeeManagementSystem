package hu.martin.ems.crudFE;

import com.automation.remarks.video.annotations.Video;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.core.file.XLSX;
import hu.martin.ems.core.model.EmailProperties;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.OrderCreatePage;
import hu.martin.ems.pages.OrderElementPage;
import hu.martin.ems.pages.OrderPage;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@PrepareForTest(jakarta.mail.Transport.class)
//@RunWith(PowerMockRunner.class)
//@Listeners(UniversalVideoListener.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderCrudTest extends BaseCrudTest {


    @AfterClass
    public void destroy_2() throws IOException {
        resetDatabase();
        System.out.println("Database reseted");
    }
//    private static WebDriverWait notificationDisappearWait;
//    private static final String gridXpath = contentXpath + "/vaadin-grid";
//    private static final String showDeletedXpath = contentXpath + "/vaadin-checkbox";
//    private static final String createOrderGridXpathModify = contentXpath + "/vaadin-grid";
//    private static final String sendReportViaSFTPButtonXpath = contentXpath + "/vaadin-button";
//    private static final String fromDatePickerXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-date-picker[1]/input";
//    private static final String toDatePickerXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-date-picker[2]/input";
//    private static final String createOrderPaymentComboBox = OrderCreateTest.paymentMethodComboBoxXpath;
//    private static final String createOrderCurrencyComboBox = OrderCreateTest.currencyComboBoxXpath;
//    private static final String createOrderSaveOrderButton = OrderCreateTest.orderCreateOrderButtonXpath;
//    private static CrudTestingUtil crudTestingUtil;

//    private static final String createButtonXpath = null;
//
//    private static final String mainMenu = UIXpaths.ORDERS_MENU;
//    private static final String subMenu = UIXpaths.ORDER_SUBMENU;

//    private GridTestingUtil gridTestingUtil;
//
//
//    private OrderCreateTest orderCreateTest;
//
//
//
//    @Spy
//    public XDocReportRegistry spyRegistry;

    @BeforeClass
    public void setup() {
//        gridTestingUtil = new GridTestingUtil(driver);
//        MockitoAnnotations.openMocks(this);
//        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
//        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, driver, "Order", showDeletedXpath, gridXpath, createButtonXpath);
        spyOrderService.setRegistry(spyRegistry);
//        orderCreateTest = new OrderCreateTest();
    }
//
//    @AfterMethod
//    public void afterMethod() {
//        Mockito.reset(spyRegistry);
//        Mockito.clearInvocations(spyRegistry);
//    }

    @Test
    public void sendSFTPFailed_HostIsNull(){
        String originalHost = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpHost");
        ReflectionTestUtils.setField(spyJschConfig, "sftpHost", null);

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Error happened when sending with SFTP");
        notification.close();

        ReflectionTestUtils.setField(spyJschConfig, "sftpHost", originalHost);
    }

    @Test
    public void sendSFTPFailed_NoPasswordProvided() {
        String sftpPassword = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpPassword");
        ReflectionTestUtils.setField(spyJschConfig, "sftpPassword", null);

//        JSch jsch = mock(JSch.class);
//        Session mockSession = mock(Session.class);
//        ChannelSftp mockChannelSftp = mock(ChannelSftp.class);

//        when(jsch.getSession(sftpUserName, sftpHostName, sftpPort)).thenReturn(mockSession);
//        doThrow(new JSchException("Unknown error")).when(mockSession).connect();
//        when(mockSession.openChannel("sftp")).thenReturn(mockChannelSftp);
//        when(mockSession.getUserName()).thenReturn(sftpUserName);

//        spyJschConfig.jsch = jsch;

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Error happened when sending with SFTP");
        notification.close();
        ReflectionTestUtils.setField(spyJschConfig, "sftpPassword", sftpPassword);
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
        assertEquals(notification.getText(), "Error happened when sending with SFTP");
        notification.close();
    }

    @Test
    public void sendSFTPFailed_ChannelOpeningError() throws JSchException {
        String sftpUserName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpUser");
        String sftpHostName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpHost");
        int sftpPort = Integer.parseInt(ReflectionTestUtils.getField(spyJschConfig, "sftpPort").toString());

        JSch jsch = mock(JSch.class);
        Session mockSession = mock(Session.class);
//        ChannelSftp mockChannelSftp = mock(ChannelSftp.class);

        when(jsch.getSession(sftpUserName, sftpHostName, sftpPort)).thenReturn(mockSession);
        doNothing().when(mockSession).connect();
        doThrow(JSchException.class).when(mockSession).openChannel("sftp");
//        when(mockSession.openChannel("sftp")).thenReturn(mockChannelSftp);
        when(mockSession.getUserName()).thenReturn(sftpUserName);

        spyJschConfig.jsch = jsch;

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Error happened when sending with SFTP");
        notification.close();


    }


    @Test
    public void sendSFTPFailed_AuthFail() throws JSchException {
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
        assertEquals(notification.getText(), "Error happened when sending with SFTP");
        notification.close();
    }



    @Test
    public void sendSFTPFailed_ConnectionRefused() throws JSchException {
        String sftpUserName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpUser");
        String sftpHostName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpHost");
        int sftpPort = Integer.parseInt(ReflectionTestUtils.getField(spyJschConfig, "sftpPort").toString());

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
        assertEquals(notification.getText(), "Error happened when sending with SFTP");
        notification.close();
    }

    @Test
    public void generateODTFailedIOException() throws Exception {
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
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
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
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
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
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
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
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
    }


    @Test
    public void generateODTTest() throws Exception {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }
        VaadinButtonComponent odtButton = page.getGrid().getOptionAnchorButton(rowLocation, 1);
        odtButton.click();
        assertTrue(waitForDownload("order_[0-9]{1,}.odt", 200, 10));
    }

    @Test
    public void generatePDFTest() throws Exception {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }
        VaadinButtonComponent odtButton = page.getGrid().getOptionAnchorButton(rowLocation, 2);
        odtButton.click();
        assertTrue(waitForDownload("order_[0-9]{1,}.pdf", 200, 10), "Download not happened!");
    }

//    @Test
//    public void sendSFTPFailedTest() {
//        EmptyLoggedInVaadinPage loggedInPage =
//                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
//        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
//        OrderPage page = new OrderPage(driver, port);
//        page.getSendToAccountantSftpButton().click();
//        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
//        assertEquals(notification.getText(), "Error happened when sending with SFTP");
//        notification.close();
//    }

    @Test
    public void sendSFTPFailedXLSGenerationTest() throws IOException {
        XLSX spyXlsx = spy(XLSX.class);
        MockitoAnnotations.openMocks(this);

        Mockito.doThrow(IOException.class).when(spyXlsx).createExcelFile(any(), any());
//        Mockito.when(spyXlsx.createExcelFile(any(), any())).thenThrow(IOException.class);
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
    }

    @Test
    public void sendSFTPSuccessTest() throws JSchException {
        String sftpUserName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpUser");
        String sftpHostName = (String) ReflectionTestUtils.getField(spyJschConfig, "sftpHost");
        int sftpPort = Integer.parseInt(ReflectionTestUtils.getField(spyJschConfig, "sftpPort").toString());

        JSch jsch = mock(JSch.class);
        Session mockSession = mock(Session.class);
        ChannelSftp mockChannelSftp = mock(ChannelSftp.class);

        when(jsch.getSession(sftpUserName, sftpHostName, sftpPort)).thenReturn(mockSession);
        doNothing().when(mockSession).connect();
        when(mockSession.openChannel("sftp")).thenReturn(mockChannelSftp);
        when(mockSession.getUserName()).thenReturn(sftpUserName);

        spyJschConfig.jsch = jsch;

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "SFTP sending is done");
        notification.close();
    }




//    @Test
//    public void sendEmailSuccessTest() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(200, true, "Email sent!")).when(spyEmailSendingApi).send(Mockito.any(EmailProperties.class));
//
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//
//        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
//        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
//        if (rowLocation == null) {
//            orderCreateTest.setup();
//            orderCreateTest.createOrder();
//            rowLocation = new ElementLocation(1, 0);
//        }
//
//        WebElement sendEmailButton = gridTestingUtil.getOptionColumnButton(gridXpath, rowLocation, 3);
//        sendEmailButton.click();
//        Thread.sleep(100);
//        gridTestingUtil.checkNotificationContainsTexts("Email sent!", 5000);
//    }

    @Captor
    ArgumentCaptor<EmailProperties> orderArgumentCaptor;

    @Test
    public void sendEmailSuccessTest() throws MessagingException, IOException, XDocReportException {
        Mockito.doNothing().when(spyEmailSendingService).transportSend(any(MimeMessage.class));

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }

        page = new OrderPage(driver, port);
//        VaadinButtonComponent sendEmailButton = page.getGrid().getOptionColumnButton(rowLocation, 3);
        VaadinButtonComponent sendEmailButton = page.getGrid().getOptionColumnButton(rowLocation, 3);

        sendEmailButton.click();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Email sent!");
        notification.close();
    }



    @Test
    public void generateEmailFailedDueToCantGetOrderFromOrderId() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

//        ElementLocation rowLocation = page.getGrid().getRandomLocation(); //TODO visszarakni
        ElementLocation rowLocation = null; //TODO kitörölni
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
            ocPage.performCreate(null);
            new VaadinNotificationComponent(driver).close();
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
            page.initWebElements();
        }

        VaadinButtonComponent sendEmailButton = page.getGrid().getOptionColumnButton(rowLocation, 3);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 2); //TODO Eredeti: 2
        sendEmailButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver, Duration.ofMillis(5000));
        assertEquals(notification.getText(), "Email generation failed");
        notification.close();

    }

    @Test
    public void sendEmailFailedTest() {
        EmptyLoggedInVaadinPage loggedInPage =
            (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }

        VaadinButtonComponent sendEmailButton = page.getGrid().getOptionColumnButton(rowLocation, 3);
        sendEmailButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Email sending failed");
        notification.close();
    }

    @Test
    public void sendEmailPDFGenerationFailedIOException() throws IOException, XDocReportException {
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));
//        Mockito.doReturn(new EmsResponse(500, "Email sending failed")).when(spyEmailSendingApi).send(Mockito.any(EmailProperties.class));

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }

        VaadinButtonComponent sendEmailButton = page.getGrid().getOptionColumnButton(rowLocation, 3);
        sendEmailButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Email sending failed: " + EmsResponse.Description.DOCUMENT_GENERATION_FAILED_MISSING_TEMPLATE);
        notification.close();


//
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//
//        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
//        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
//        if(rowLocation == null) {
//            orderCreateTest.setup();
//            orderCreateTest.createOrder();
//            rowLocation = new ElementLocation(1, 0);
//        }
//
//        WebElement sendEmailButton = gridTestingUtil.getOptionColumnButton(gridXpath, rowLocation, 3);
//        sendEmailButton.click();
//        Thread.sleep(100);
//        gridTestingUtil.checkNotificationText();
        Mockito.reset(spyRegistry);
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
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
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
    }

    @Test
    public void modifyOrderTest() {
        modifyOrder(null, true);
    }

    public void modifyOrder(String notificationText, Boolean needSuccess) {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);

        ElementLocation rowLocation = page.getGrid().getRandomLocation();
        if(rowLocation == null) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
            ocPage.performCreate(null);
            rowLocation = new ElementLocation(1, 0);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        }

        int original = page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox());
        String[] originalData = page.getGrid().getDataFromRowLocation(rowLocation, true);

        page.getGrid().goToPage(rowLocation.getPageNumber());
        VaadinButtonComponent modifyButton = page.getGrid().getModifyButton(rowLocation.getRowIndex());
        modifyButton.click();

//        gridTestingUtil.goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());
//        Thread.sleep(200);
//        WebElement modifyButton = gridTestingUtil.getModifyButton(gridXpath, rowLocation.getRowIndex());
//        Thread.sleep(200);
//        modifyButton.click();
        OrderCreatePage createPage = new OrderCreatePage(driver, port);
        if(createPage.getGrid().getTotalRowNumber() == 0){
            System.out.println("Itt ugyebár nem volt elem ebben az esetben a gridben");
        }
        createPage.getGrid().selectElements(3);
        createPage.getShowPreviouslyOrderedElementsCheckBox().setStatus(true);
        createPage.getCurrencyComboBox().fillWithRandom();
        createPage.getPaymentTypeComboBox().fillWithRandom();
        createPage.getCreateOrderButton().click();


//        gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpathModify);
//        Thread.sleep(2000);
//        gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpathModify, 3);
//
//        Thread.sleep(200);
//        //gridTestingUtil.setCheckboxStatus(orderCreateTest.previouslyOrderedCheckboxXpath, true);

//        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(createOrderCurrencyComboBox));
//        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(createOrderPaymentComboBox));
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(createOrderSaveOrderButton).click();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertThat(notification.getText()).contains(notificationText == null ? "Order updated:" : notificationText);
//        assertEquals(notificationText == null ? "Order updated:" : notificationText, notification.getText());
        notification.close();

//        gridTestingUtil.checkNotificationContainsTexts(notificationText == null ? "Order updated:" : notificationText);
//
//        Thread.sleep(100);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        page.initWebElements();
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        Thread.sleep(100);
        page.getGrid().applyFilter(originalData);
        assertEquals(needSuccess ? 0 : 1, page.getGrid().getTotalRowNumber());
        page.getGrid().resetFilter();
        assertEquals(original, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
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
        assertThat(testResult.getNotificationWhenPerform()).contains("Internal Server Error");
    }
//
//    @Test
//    public void asdf() throws SQLException, IOException {
//        while(true){
//            databaseNotAvailableWhileDeleteTest();
//            deleteOrderElementWhatMemberOfAnOrder();
//            resetDatabase();
//        }
//    }



    @Test
    @Video
    public void deleteOrderElementWhatMemberOfAnOrder() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        //region orders menuben ha nincs elem, akkor létrehozunk egyet.
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        int originalVisibleRows = page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox());
        if(originalVisibleRows == 0) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
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
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
        OrderCreatePage createPage = new OrderCreatePage(driver, port);
        createPage.performCreate("Erdei Róbert");

        createPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
//        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
//        oePage = new OrderElementPage(driver, port); //TODO lehet, hogy ez az újra inicializálás nem kell. Meg kell majd próbálni
        oePage.initWebElements();
        VaadinButtonComponent oeDeleteButtonCreatedOrder = oePage.getGrid().getDeleteButton(0);
        while(oeDeleteButtonCreatedOrder != null){
//            oePage.initWebElements();
            oeDeleteButtonCreatedOrder.click();
            new VaadinNotificationComponent(driver).close();
            oePage.initWebElements();
            oeDeleteButtonCreatedOrder = oePage.getGrid().getDeleteButton(0);
        }

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        Thread.sleep(20);
        page.initWebElements();
        page.getGrid().getModifyButton(0).click(); //TODO megcsinálni, hogy előtte nézze meg a szerkesztésben, hogy látja-e őket.
//        gridTestingUtil.getModifyButton(gridXpath, 0).click();
//        Thread.sleep(1000);
        OrderCreatePage ocPage = new OrderCreatePage(driver, port);
//        ocPage.getCustomerComboBox().fillWith("Erdei Róbert");
//        ocPage.getGrid().waitForRefresh();
        assertEquals(ocPage.getGrid().getTotalNonDeletedRowNumber(null), 0);
    }

    @Test
    public void restoreOrderTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);
        int originalDeleted = page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox());
        if(originalDeleted == 0){
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
            ocPage.performCreate(null);
            new VaadinNotificationComponent(driver).close();
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
            page.initWebElements();
            page.performDelete();
//            page.initWebElements();
//            new VaadinNotificationComponent(driver).close();

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


//
//
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        int originalInvisibleRows = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedXpath);
//        if(originalInvisibleRows == 0) {
    }

    @Test
    public void permanentlyDeleteOrderTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);

        int originalDeleted = page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox());
        if(originalDeleted == 0){
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
            ocPage.performCreate(null);
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
            page.performDelete();
            new VaadinNotificationComponent(driver).close();
//            originalVisibleRows = page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox());
//
//            orderCreateTest.setup();
//            orderCreateTest.createOrder();
//            deleteOrderTest();
        }
        page.performPermanentlyDelete();
        //TODO ellenőrzést megcsinálni
//        crudTestingUtil.permanentlyDeleteTest();
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

    }

    private void checkFromToDatePicker(VaadinDatePickerComponent datePicker1, VaadinDatePickerComponent datePicker2, LocalDate date1, LocalDate date2, LocalDate expectedDate1, LocalDate expectedDate2, String dateFormat){
        datePicker1.selectDate(date1);
        datePicker2.selectDate(date2);
        assertEquals(expectedDate1, datePicker1.getDate());
        assertEquals(expectedDate2, datePicker2.getDate());
    }

//    //@Test
//    public void extraFilterInvalidValue() throws InterruptedException {
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        NotificationCheck nc = new NotificationCheck();
//        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
//
//        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
//        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
//        if(rowLocation == null) {
//            orderCreateTest.setup();
//            orderCreateTest.createOrder();
//        }
//
//        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
//    }

    @Test
    public void gettingOrdersFailed() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 2);
//        Mockito.doReturn(null).when(spyOrderService).findAll(true); //ApiClintben .findAllWithDeleted();
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Error happened while getting orders");
        notificationComponent.close();
//        assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 0);


//        gridTestingUtil.mockDatabaseNotAvailableAfter(getClass(), spyDataSource, 2);
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.checkNotificationText("Error happened while getting orders");
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(gridXpath));
//        assertEquals(0, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedXpath));
//        gridTestingUtil.checkNoMoreNotificationsVisible();
    }
}
