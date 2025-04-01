package hu.martin.ems.crudFE;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.EmailProperties;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.sftp.SftpSender;
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
import hu.martin.ems.service.OrderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.mockito.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.testng.Assert.*;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@PrepareForTest(jakarta.mail.Transport.class)
//@RunWith(PowerMockRunner.class)
//@Listeners(UniversalVideoListener.class)
public class OrderCrudTest extends BaseCrudTest {

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

    @Mock
    public SftpSender sftpSender;


    @Spy
    public XDocReportRegistry spyRegistry;


//    private GridTestingUtil gridTestingUtil;
//
//
//    private OrderCreateTest orderCreateTest;

    @BeforeClass
    public void setup() {
//        gridTestingUtil = new GridTestingUtil(driver);
//        MockitoAnnotations.openMocks(this);
//        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
//        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, driver, "Order", showDeletedXpath, gridXpath, createButtonXpath);
        spyOrderService.setRegistry(spyRegistry);
//        orderCreateTest = new OrderCreateTest();
    }

    @Test
    public void generateODTFailedIOException() throws Exception {
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
        assertTrue(waitForDownload("order_[0-9]{1,}.pdf", 200, 10));
    }

    @Test
    public void sendSFTPFailedTest() throws InterruptedException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Error happened when sending with SFTP");
    }

    @Test
    public void sendSFTPSuccessTest() {
        BeanProvider.getBean(OrderService.class).setSender(sftpSender);
        Mockito.doReturn(true).when(sftpSender).send(any(byte[].class), any(String.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        page.getSendToAccountantSftpButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "SFTP sending is done");
    }




//    @Test
//    public void sendEmailSuccessTest() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(200, true, "Email sent!")).when(spyEmailSendingApi).send(Mockito.any(EmailProperties.class));
//
//        gridTestingUtil.loginWith(driver, port, "admin", "admin");
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
    public void sendEmailSuccessTest() throws MessagingException {
        Mockito.doNothing().when(spyEmailSendingService).transportSend(any(MimeMessage.class));

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
        assertEquals(notification.getText(), "Email sent!");
        notification.close();
    }

    @Test
    public void generateEmailFailedDueToCantGetOrderFromOrderId() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 2);
        sendEmailButton.click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Email generation failed");
        notification.close();

    }

    @Test
    public void sendEmailFailedTest() {
        EmptyLoggedInVaadinPage loggedInPage =
            (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
//        gridTestingUtil.loginWith(driver, port, "admin", "admin");
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
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
        assertEquals(notificationText == null ? "Order updated:" : notificationText, notification.getText());
        notification.close();

//        gridTestingUtil.checkNotificationContainsTexts(notificationText == null ? "Order updated:" : notificationText);
//
//        Thread.sleep(100);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
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
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);

        DoDeleteFailedTestData testResult = page.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Internal Server Error");
    }

    @Test
    public void deleteOrderElementWhatMemberOfAnOrder() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
//        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        oePage = new OrderElementPage(driver, port); //TODO lehet, hogy ez az újra inicializálás nem kell. Meg kell majd próbálni

        VaadinButtonComponent oeDeleteButtonCreatedOrder = oePage.getGrid().getDeleteButton(0);
        while(oeDeleteButtonCreatedOrder != null){
            oeDeleteButtonCreatedOrder.click();
            new VaadinNotificationComponent(driver).close();
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
        assertEquals(ocPage.getGrid().getTotalNonDeletedRowNumber(null), 0);
    }

    @Test
    public void restoreOrderTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage page = new OrderPage(driver, port);
        int originalDeleted = page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox());
        if(originalDeleted == 0){
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage ocPage = new OrderCreatePage(driver, port);
            ocPage.performCreate(null);
            new VaadinNotificationComponent(driver).close();
            ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
            page.performDelete();
            new VaadinNotificationComponent(driver).close();
//            originalVisibleRows = page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox());
//
//            orderCreateTest.setup();
//            orderCreateTest.createOrder();
//            deleteOrderTest();
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
//        gridTestingUtil.loginWith(driver, port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        int originalInvisibleRows = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedXpath);
//        if(originalInvisibleRows == 0) {
    }

    @Test
    public void permanentlyDeleteOrderTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
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
//        gridTestingUtil.loginWith(driver, port, "admin", "admin");
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
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage page = new OrderPage(driver, port);
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Error happened while getting orders");
        notificationComponent.close();
//        assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 0);


//        gridTestingUtil.mockDatabaseNotAvailableAfter(getClass(), spyDataSource, 2);
//        gridTestingUtil.loginWith(driver, port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.checkNotificationText("Error happened while getting orders");
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(gridXpath));
//        assertEquals(0, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedXpath));
//        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    private boolean waitForDownload(String fileNameRegex, int times, int padding) throws Exception {
        Pattern pattern = Pattern.compile(fileNameRegex);

        for (int i = 0; i < times; i++) {
            File dir = new File(StaticDatas.Selenium.downloadPath);
            File[] files = dir.listFiles();

            if (files != null) {
                for (File file : files) {
                    Matcher matcher = pattern.matcher(file.getName());
                    if (matcher.matches()) {
                        if (file.delete()) {
                            return true;
                        }
                    }
                }
            }
            Thread.sleep(padding); // Várakozás 1 másodpercig
        }
        return false;
    }
}
