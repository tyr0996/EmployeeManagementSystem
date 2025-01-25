package hu.martin.ems.crudFE;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmailProperties;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.service.EmailSendingService;
import hu.martin.ems.core.sftp.SftpSender;
import hu.martin.ems.service.OrderService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.mockito.ArgumentMatchers.any;
import static org.testng.AssertJUnit.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class OrderCrudTest extends BaseCrudTest {

    private static WebDriverWait notificationDisappearWait;


    private static final String gridXpath =                  "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";
    private static final String showDeletedXpath =           "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-checkbox";
    private static final String createOrderGridXpathModify = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";
    private static final String sendReportViaSFTPButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-button";
    private static final String fromDatePickerXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-date-picker[1]/input";
    private static final String toDatePickerXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-date-picker[2]/input";
    private static final String createOrderPaymentComboBox = OrderCreateTest.paymentMethodComboBoxXpath;
    private static final String createOrderCurrencyComboBox = OrderCreateTest.currencyComboBoxXpath;
    private static final String createOrderSaveOrderButton = OrderCreateTest.orderCreateOrderButtonXpath;
    private static CrudTestingUtil crudTestingUtil;

    private static final String createButtonXpath = null;

    private static final String mainMenu = UIXpaths.ORDERS_MENU;
    private static final String subMenu = UIXpaths.ORDER_SUBMENU;

    @Mock
    public EmailSendingService emailSendingService;

    @Mock
    public SftpSender sftpSender;

    @Spy
    public XDocReportRegistry spyRegistry;


    @BeforeMethod
    public void setup() {
        MockitoAnnotations.openMocks(this);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
        crudTestingUtil = new CrudTestingUtil(driver, "Order", showDeletedXpath, gridXpath, createButtonXpath);
        GridTestingUtil.driver = driver;
        spyOrderService.setRegistry(spyRegistry);
    }

    @Test
    public void generateODTFailedIOException() throws Exception {
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);

        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement odtButton = getOptionDownloadButton(gridXpath, rowLocation, 1);
        odtButton.click();
        Thread.sleep(100);
        checkNotificationText("Document generation failed. Missing template file.");
        checkNoMoreNotificationsVisible();
        assertEquals(false, waitForDownload("order_[0-9]{1,}.odt", 10));

        Mockito.reset(spyRegistry);
    }

    @Test
    public void generateODTFailedXDocReportException() throws Exception {
        Mockito.doThrow(XDocReportException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);

        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement odtButton = getOptionDownloadButton(gridXpath, rowLocation, 1);
        odtButton.click();
        Thread.sleep(100);
        checkNotificationText("Document generation failed. Not supported file type");
        checkNoMoreNotificationsVisible();
        assertEquals(false, waitForDownload("order_[0-9]{1,}.odt", 10));
        Mockito.reset(spyRegistry);
    }

    @Test
    public void generatePDFFailedIOException() throws Exception {
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);

        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement pdfButton = getOptionDownloadButton(gridXpath, rowLocation, 2);
        pdfButton.click();
        Thread.sleep(100);
        checkNotificationText("Document generation failed. Missing template file.");
        checkNoMoreNotificationsVisible();
        assertEquals(false, waitForDownload("order_[0-9]{1,}.pdf", 10));
        Mockito.reset(spyRegistry);
    }

    @Test
    public void generatePDFFailedXDocReportException() throws Exception {
        Mockito.doThrow(XDocReportException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);

        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement pdfButton = getOptionDownloadButton(gridXpath, rowLocation, 2);
        pdfButton.click();
        Thread.sleep(100);
        checkNotificationText("Document generation failed. Not supported file type");
        checkNoMoreNotificationsVisible();
        assertEquals(false, waitForDownload("order_[0-9]{1,}.pdf", 10));
        Mockito.reset(spyRegistry);
    }


    @Test
    public void generateODTTest() throws Exception {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);

        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement odtButton = getOptionDownloadButton(gridXpath, rowLocation, 1);
        odtButton.click();
        Thread.sleep(100);
        assertEquals(true, waitForDownload("order_[0-9]{1,}.odt", 10));
    }

    @Test
    public void generatePDFTest() throws Exception {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);

        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement pdfButton = getOptionDownloadButton(gridXpath, rowLocation, 2);
        pdfButton.click();
        Thread.sleep(100);
        assertEquals(true, waitForDownload("order_[0-9]{1,}.pdf", 10));
    }

    @Test
    public void sendSFTPFailedTest(){
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findVisibleElementWithXpath(gridXpath);

        findVisibleElementWithXpath(sendReportViaSFTPButtonXpath).click();
        checkNotificationContainsTexts("Error happened when sending with SFTP", 30000);
    }

    @Test
    public void sendSFTPSuccessTest() throws InterruptedException {
        BeanProvider.getBean(OrderService.class).setSender(sftpSender);
        Mockito.doReturn(true).when(sftpSender).send(any(byte[].class), any(String.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findVisibleElementWithXpath(gridXpath);

        findVisibleElementWithXpath(sendReportViaSFTPButtonXpath).click();
        Thread.sleep(100);
        checkNotificationContainsTexts("SFTP sending is done", 30000);
    }


    @Test
    public void sendEmailSuccessTest() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(200, true, "Email sent!")).when(spyEmailSendingApi).send(Mockito.any(EmailProperties.class));

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);

        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if (rowLocation == null) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement sendEmailButton = getOptionColumnButton(gridXpath, rowLocation, 3);
        sendEmailButton.click();
        Thread.sleep(100);
        checkNotificationContainsTexts("Email sent!", 5000);
    }


    @Test
    public void sendEmailFailedTest() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(500, "Email sending failed")).when(spyEmailSendingApi).send(Mockito.any(EmailProperties.class));

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);

        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement sendEmailButton = getOptionColumnButton(gridXpath, rowLocation, 3);
        sendEmailButton.click();
        Thread.sleep(100);
        checkNotificationText("Email sending failed");
    }

    @Test
    public void sendEmailPDFGenerationFailedIOException() throws InterruptedException, IOException, XDocReportException {
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));
//        Mockito.doReturn(new EmsResponse(500, "Email sending failed")).when(spyEmailSendingApi).send(Mockito.any(EmailProperties.class));

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);

        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement sendEmailButton = getOptionColumnButton(gridXpath, rowLocation, 3);
        sendEmailButton.click();
        Thread.sleep(100);
        checkNotificationText("Email sending failed: " + EmsResponse.Description.DOCUMENT_GENERATION_FAILED_MISSING_TEMPLATE);
        Mockito.reset(spyRegistry);
    }

    @Test
    public void sendEmailPDFGenerationFailedXDocReportException() throws InterruptedException, IOException, XDocReportException {
        Mockito.doThrow(XDocReportException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);

        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement sendEmailButton = getOptionColumnButton(gridXpath, rowLocation, 3);
        sendEmailButton.click();
        Thread.sleep(100);
        checkNotificationText("Email sending failed: " + EmsResponse.Description.DOCUMENT_GENERATION_FAILED_NOT_SUPPORTED_FILE_TYPE);
        Mockito.reset(spyDataSource);
    }

    @Test
    public void deleteOrderTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void modifyOrderTest() throws InterruptedException {
        modifyOrder(null, true);
    }

    public void modifyOrder(String notificationText, Boolean needSuccess) throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);

        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        int original = countVisibleGridDataRows(gridXpath);
        String[] originalData = getDataFromRowLocation(gridXpath, rowLocation);

        goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());
        Thread.sleep(200);
        WebElement modifyButton = getModifyButton(gridXpath, rowLocation.getRowIndex());
        Thread.sleep(200);
        modifyButton.click();
        findVisibleElementWithXpath(createOrderGridXpathModify);
        Thread.sleep(2000);
        selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpathModify, 3);

        Thread.sleep(200);
        //setCheckboxStatus(OrderCreateTest.previouslyOrderedCheckboxXpath, true);

        selectRandomFromComboBox(findVisibleElementWithXpath(createOrderCurrencyComboBox));
        selectRandomFromComboBox(findVisibleElementWithXpath(createOrderPaymentComboBox));
        findClickableElementWithXpathWithWaiting(createOrderSaveOrderButton).click();
        checkNotificationContainsTexts(notificationText == null ? "Order updated:" : notificationText);

        Thread.sleep(100);


        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        assertEquals(needSuccess ? 0 : 1, countElementResultsFromGridWithFilter(gridXpath, originalData));
        assertEquals(original, countVisibleGridDataRows(gridXpath));
    }

    @Test
    public void deleteOrderElementWhatMemberOfAnOrder() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        int originalVisibleRows = countVisibleGridDataRows(gridXpath);

        WebElement deleteButton = getDeleteButton(gridXpath, 0);
        while(deleteButton != null){
            deleteButton.click();
            closeNotification(200);
            deleteButton = getDeleteButton(gridXpath, 0);
        }
        setCheckboxStatus(showDeletedXpath, true);
        WebElement permanentlyDeleteButton = getPermanentlyDeleteButton(gridXpath, 0);
        while(permanentlyDeleteButton != null){
            permanentlyDeleteButton.click();
            closeNotification(200);
            permanentlyDeleteButton = getPermanentlyDeleteButton(gridXpath, 0);
        }

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);

        String oeGridXpath = OrderElementCrudTest.gridXpath;
        String oeShowDeletedXpath = OrderElementCrudTest.showDeletedChecBoxXpath;
        WebElement oeDeleteButton = getDeleteButton(oeGridXpath, 0);
        while(oeDeleteButton != null){
            oeDeleteButton.click();
            closeNotification(200);
            Thread.sleep(5);
            oeDeleteButton = getDeleteButton(oeGridXpath, 0);
        }
        setCheckboxStatus(oeShowDeletedXpath, true);
        WebElement oePermanentlyDeleteButton = getPermanentlyDeleteButton(oeGridXpath, 0);
        while(oePermanentlyDeleteButton != null){
            oePermanentlyDeleteButton.click();
            closeNotification(200);
            Thread.sleep(5);
            oePermanentlyDeleteButton = getPermanentlyDeleteButton(gridXpath, 0);
        }

        OrderCreateTest.setupTest();
        OrderCreateTest.createOrder();

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);

        WebElement oeDeleteButtonCreatedOrder = getDeleteButton(oeGridXpath, 0);
        while(oeDeleteButtonCreatedOrder != null){
            oeDeleteButtonCreatedOrder.click();
            //removeNotification(); //TODO létrehozni egy ilyen függvényt, ami csak szimplán bezárja a notification-t
            Thread.sleep(5);
            oeDeleteButtonCreatedOrder = getDeleteButton(oeGridXpath, 0);
        }

        navigateMenu(mainMenu, subMenu);
        Thread.sleep(20);
        getModifyButton(gridXpath, 0).click(); //TODO megcsinálni, hogy előtte nézze meg a szerkesztésben, hogy látja-e őket.
        Thread.sleep(1000);
        assertEquals(0, countVisibleGridDataRows(OrderCreateTest.createOrderGridXpath));
    }

    @Test
    public void restoreOrderTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        int originalInvisibleRows = countHiddenGridDataRows(gridXpath, showDeletedXpath);
        if(originalInvisibleRows == 0) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            deleteOrderTest();
        }
        crudTestingUtil.restoreTest();
    }

    @Test
    public void permanentlyDeleteOrderTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        int originalInvisibleRows = countHiddenGridDataRows(gridXpath, showDeletedXpath);
        if(originalInvisibleRows == 0) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            deleteOrderTest();
        }
        crudTestingUtil.permanentlyDeleteTest();
    }

    @Test
    public void fromToDateSelectorFromIsLaterThenTo(){
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        WebElement grid = findVisibleElementWithXpath(gridXpath);

        LocalDate a = LocalDate.of(2024, 1, 15);
        LocalDate b = LocalDate.of(2024, 1, 30);

        String dateFormat = "yyyy. MM. dd.";

        checkFromToDatePicker(fromDatePickerXpath, toDatePickerXpath, a, b, a, b, dateFormat);
        checkFromToDatePicker(fromDatePickerXpath, toDatePickerXpath, b, a, b, b, dateFormat);
        checkFromToDatePicker(toDatePickerXpath, fromDatePickerXpath, b, a, b, a, dateFormat);
        checkFromToDatePicker(toDatePickerXpath, fromDatePickerXpath, a, b, a, a, dateFormat);
        checkFromToDatePicker(fromDatePickerXpath, toDatePickerXpath, a, a, a, a, dateFormat);
        checkFromToDatePicker(toDatePickerXpath, fromDatePickerXpath, a, a, a, a, dateFormat);

        checkFromToDatePicker(fromDatePickerXpath, toDatePickerXpath, a, null, a, null, dateFormat);
        checkFromToDatePicker(fromDatePickerXpath, toDatePickerXpath, null, a, null, a, dateFormat);
        checkFromToDatePicker(toDatePickerXpath, fromDatePickerXpath, a, null, a, null, dateFormat);
        checkFromToDatePicker(toDatePickerXpath, fromDatePickerXpath, null, a, null, a, dateFormat);

        checkFromToDatePicker(fromDatePickerXpath, toDatePickerXpath, null, null, null, null, dateFormat);

    }

    private void checkFromToDatePicker(String datePicker1Xpath, String datePicker2Xpath, LocalDate date1, LocalDate date2, LocalDate expectedDate1, LocalDate expectedDate2, String dateFormat){
        selectDateFromDatePicker(datePicker1Xpath, date1);
        selectDateFromDatePicker(datePicker2Xpath, date2);
        assertEquals(expectedDate1, getDateFromDatePicker(datePicker1Xpath, dateFormat));
        assertEquals(expectedDate2, getDateFromDatePicker(datePicker2Xpath, dateFormat));
    }

    //@Test
    public void extraFilterInvalidValue() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");

        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
        }

        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }

    @Test
    public void gettingOrdersFailed() throws InterruptedException {
        Mockito.doReturn(null).when(spyOrderService).findAll(true); //ApiClintben .findAllWithDeleted();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        checkNotificationText("Error happened while getting orders");
        checkNoMoreNotificationsVisible();
        assertEquals(0, countVisibleGridDataRows(gridXpath));
        assertEquals(0, countHiddenGridDataRows(gridXpath, showDeletedXpath));
        checkNoMoreNotificationsVisible();
    }

    private boolean waitForDownload(String fileNameRegex, int timeOut) throws Exception {
        Pattern pattern = Pattern.compile(fileNameRegex);
        if(downloadPath == null){
            downloadPath = System.getenv("chrome.download.path");
        }

        for (int i = 0; i < timeOut; i++) {
            File dir = new File(downloadPath);
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
            Thread.sleep(1000); // Várakozás 1 másodpercig
        }
        return false;
    }
}
