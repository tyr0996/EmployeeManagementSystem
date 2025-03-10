package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.automation.remarks.video.annotations.Video;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.JPAConfig;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.EmailProperties;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.sftp.SftpSender;
import hu.martin.ems.service.OrderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.mockito.*;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.any;
import static org.testng.AssertJUnit.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@PrepareForTest(jakarta.mail.Transport.class)
//@RunWith(PowerMockRunner.class)
@Listeners(UniversalVideoListener.class)
public class OrderCrudTest extends BaseCrudTest {

    private static WebDriverWait notificationDisappearWait;
    private static final String gridXpath = contentXpath + "/vaadin-grid";
    private static final String showDeletedXpath = contentXpath + "/vaadin-checkbox";
    private static final String createOrderGridXpathModify = contentXpath + "/vaadin-grid";
    private static final String sendReportViaSFTPButtonXpath = contentXpath + "/vaadin-button";
    private static final String fromDatePickerXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-date-picker[1]/input";
    private static final String toDatePickerXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-date-picker[2]/input";
    private static final String createOrderPaymentComboBox = OrderCreateTest.paymentMethodComboBoxXpath;
    private static final String createOrderCurrencyComboBox = OrderCreateTest.currencyComboBoxXpath;
    private static final String createOrderSaveOrderButton = OrderCreateTest.orderCreateOrderButtonXpath;
    private static CrudTestingUtil crudTestingUtil;

    private static final String createButtonXpath = null;

    private static final String mainMenu = UIXpaths.ORDERS_MENU;
    private static final String subMenu = UIXpaths.ORDER_SUBMENU;

    @Mock
    public SftpSender sftpSender;


    @Spy
    public XDocReportRegistry spyRegistry;


    private GridTestingUtil gridTestingUtil;

    
    private OrderCreateTest orderCreateTest;

    @BeforeClass
    public void setup() {
        gridTestingUtil = new GridTestingUtil(getDriver());
        MockitoAnnotations.openMocks(this);
        notificationDisappearWait = new WebDriverWait(getDriver(), Duration.ofMillis(5000));
        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, getDriver(), "Order", showDeletedXpath, gridXpath, createButtonXpath);
        spyOrderService.setRegistry(spyRegistry);
        orderCreateTest = new OrderCreateTest();
    }

    @Test
    @Video
    public void generateODTFailedIOException() throws Exception {
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));

        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement odtButton = gridTestingUtil.getOptionDownloadButton(gridXpath, rowLocation, 1);
        odtButton.click();
        Thread.sleep(100);
        gridTestingUtil.checkNotificationText("Document generation failed. Missing template file.");
        gridTestingUtil.checkNoMoreNotificationsVisible();
        assertEquals(false, waitForDownload("order_[0-9]{1,}.odt", 10));

        Mockito.reset(spyRegistry);
    }

    @Test
    @Video
    public void generateODTFailedXDocReportException() throws Exception {
        Mockito.doThrow(XDocReportException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));

        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement odtButton = gridTestingUtil.getOptionDownloadButton(gridXpath, rowLocation, 1);
        odtButton.click();
        Thread.sleep(100);
        gridTestingUtil.checkNotificationText("Document generation failed. Not supported file type");
        gridTestingUtil.checkNoMoreNotificationsVisible();
        assertEquals(false, waitForDownload("order_[0-9]{1,}.odt", 10));
        Mockito.reset(spyRegistry);
    }

    @Test
    @Video
    public void generatePDFFailedIOException() throws Exception {
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));

        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement pdfButton = gridTestingUtil.getOptionDownloadButton(gridXpath, rowLocation, 2);
        pdfButton.click();
        Thread.sleep(100);
        gridTestingUtil.checkNotificationText("Document generation failed. Missing template file.");
        gridTestingUtil.checkNoMoreNotificationsVisible();
        assertEquals(false, waitForDownload("order_[0-9]{1,}.pdf", 10));
        Mockito.reset(spyRegistry);
    }

    @Test
    @Video
    public void generatePDFFailedXDocReportException() throws Exception {
        Mockito.doThrow(XDocReportException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));

        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement pdfButton = gridTestingUtil.getOptionDownloadButton(gridXpath, rowLocation, 2);
        pdfButton.click();
        Thread.sleep(100);
        gridTestingUtil.checkNotificationText("Document generation failed. Not supported file type");
        gridTestingUtil.checkNoMoreNotificationsVisible();
        assertEquals(false, waitForDownload("order_[0-9]{1,}.pdf", 10));
        Mockito.reset(spyRegistry);
    }


    @Test
    @Video
    public void generateODTTest() throws Exception {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement odtButton = gridTestingUtil.getOptionDownloadButton(gridXpath, rowLocation, 1);
        odtButton.click();
        Thread.sleep(100);
        assertEquals(true, waitForDownload("order_[0-9]{1,}.odt", 10));
    }

    @Test
    @Video
    public void generatePDFTest() throws Exception {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement pdfButton = gridTestingUtil.getOptionDownloadButton(gridXpath, rowLocation, 2);
        pdfButton.click();
        Thread.sleep(100);
        assertEquals(true, waitForDownload("order_[0-9]{1,}.pdf", 10));
    }

    @Test
    @Video
    public void sendSFTPFailedTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);

        gridTestingUtil.findVisibleElementWithXpath(sendReportViaSFTPButtonXpath).click();
        gridTestingUtil.checkNotificationContainsTexts("Error happened when sending with SFTP", 30000);
    }

    @Test
    @Video
    public void sendSFTPSuccessTest() throws InterruptedException {
        BeanProvider.getBean(OrderService.class).setSender(sftpSender);
        Mockito.doReturn(true).when(sftpSender).send(any(byte[].class), any(String.class));
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);

        gridTestingUtil.findVisibleElementWithXpath(sendReportViaSFTPButtonXpath).click();
        Thread.sleep(100);
        gridTestingUtil.checkNotificationContainsTexts("SFTP sending is done", 30000);
    }




//    @Test
//    public void sendEmailSuccessTest() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(200, true, "Email sent!")).when(spyEmailSendingApi).send(Mockito.any(EmailProperties.class));
//
//        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
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
    @Video
    public void sendEmailSuccessTest() throws InterruptedException, MessagingException {
        Mockito.doNothing().when(spyEmailSendingService).transportSend(any(MimeMessage.class));
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if (rowLocation == null) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement sendEmailButton = gridTestingUtil.getOptionColumnButton(gridXpath, rowLocation, 3);
        sendEmailButton.click();
        gridTestingUtil.checkNotificationContainsTexts("Email sent!", 5000);
    }

    @Test
    @Video
    public void generateEmailFailedDueToCantGetOrderFromOrderId() throws InterruptedException, SQLException {
        JPAConfig.resetCallIndex();
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");

        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement sendEmailButton = gridTestingUtil.getOptionColumnButton(gridXpath, rowLocation, 3);
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 2);
        sendEmailButton.click();
        Thread.sleep(2000);
        gridTestingUtil.checkNotificationText("Email generation failed");
    }

    @Test
    @Video
    public void sendEmailFailedTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement sendEmailButton = gridTestingUtil.getOptionColumnButton(gridXpath, rowLocation, 3);
        sendEmailButton.click();
        Thread.sleep(100);
        gridTestingUtil.checkNotificationText("Email sending failed");
    }

    @Test
    @Video
    public void sendEmailPDFGenerationFailedIOException() throws InterruptedException, IOException, XDocReportException {
        Mockito.doThrow(IOException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));
//        Mockito.doReturn(new EmsResponse(500, "Email sending failed")).when(spyEmailSendingApi).send(Mockito.any(EmailProperties.class));

        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement sendEmailButton = gridTestingUtil.getOptionColumnButton(gridXpath, rowLocation, 3);
        sendEmailButton.click();
        Thread.sleep(100);
        gridTestingUtil.checkNotificationText("Email sending failed: " + EmsResponse.Description.DOCUMENT_GENERATION_FAILED_MISSING_TEMPLATE);
        Mockito.reset(spyRegistry);
    }

    @Test
    @Video
    public void sendEmailPDFGenerationFailedXDocReportException() throws InterruptedException, IOException, XDocReportException {
        Mockito.doThrow(XDocReportException.class).when(spyRegistry).loadReport(any(InputStream.class), any(TemplateEngineKind.class));

        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement sendEmailButton = gridTestingUtil.getOptionColumnButton(gridXpath, rowLocation, 3);
        sendEmailButton.click();
        Thread.sleep(100);
        gridTestingUtil.checkNotificationText("Email sending failed: " + EmsResponse.Description.DOCUMENT_GENERATION_FAILED_NOT_SUPPORTED_FILE_TYPE);
        Mockito.reset(spyDataSource);
    }

    @Test
    @Video
    public void deleteOrderTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    @Video
    public void modifyOrderTest() throws InterruptedException {
        modifyOrder(null, true);
    }

    public void modifyOrder(String notificationText, Boolean needSuccess) throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        int original = gridTestingUtil.countVisibleGridDataRows(gridXpath);
        String[] originalData = gridTestingUtil.getDataFromRowLocation(gridXpath, rowLocation);

        gridTestingUtil.goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());
        Thread.sleep(200);
        WebElement modifyButton = gridTestingUtil.getModifyButton(gridXpath, rowLocation.getRowIndex());
        Thread.sleep(200);
        modifyButton.click();
        gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpathModify);
        Thread.sleep(2000);
        gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpathModify, 3);

        Thread.sleep(200);
        //gridTestingUtil.setCheckboxStatus(orderCreateTest.previouslyOrderedCheckboxXpath, true);

        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(createOrderCurrencyComboBox));
        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(createOrderPaymentComboBox));
        gridTestingUtil.findClickableElementWithXpathWithWaiting(createOrderSaveOrderButton).click();
        gridTestingUtil.checkNotificationContainsTexts(notificationText == null ? "Order updated:" : notificationText);

        Thread.sleep(100);


        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        assertEquals(needSuccess ? 0 : 1, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, originalData));
        assertEquals(original, gridTestingUtil.countVisibleGridDataRows(gridXpath));
    }

    @Test
    @Video
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
    }

    @Test
    @Video
    public void deleteOrderElementWhatMemberOfAnOrder() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        int originalVisibleRows = gridTestingUtil.countVisibleGridDataRows(gridXpath);

        WebElement deleteButton = gridTestingUtil.getDeleteButton(gridXpath, 0);
        while(deleteButton != null){
            deleteButton.click();
            gridTestingUtil.closeNotification(200);
            deleteButton = gridTestingUtil.getDeleteButton(gridXpath, 0);
        }
        gridTestingUtil.setCheckboxStatus(showDeletedXpath, true);
        WebElement permanentlyDeleteButton = gridTestingUtil.getPermanentlyDeleteButton(gridXpath, 0);
        while(permanentlyDeleteButton != null){
            permanentlyDeleteButton.click();
            gridTestingUtil.closeNotification(200);
            permanentlyDeleteButton = gridTestingUtil.getPermanentlyDeleteButton(gridXpath, 0);
        }

        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);

        String oeGridXpath = OrderElementCrudTest.gridXpath;
        String oeShowDeletedXpath = OrderElementCrudTest.showDeletedCheckBoxXpath;
        WebElement oeDeleteButton = gridTestingUtil.getDeleteButton(oeGridXpath, 0);
        while(oeDeleteButton != null){
            oeDeleteButton.click();
            gridTestingUtil.closeNotification(200);
            Thread.sleep(5);
            oeDeleteButton = gridTestingUtil.getDeleteButton(oeGridXpath, 0);
        }
        gridTestingUtil.setCheckboxStatus(oeShowDeletedXpath, true);
        WebElement oePermanentlyDeleteButton = gridTestingUtil.getPermanentlyDeleteButton(oeGridXpath, 0);
        while(oePermanentlyDeleteButton != null){
            oePermanentlyDeleteButton.click();
            gridTestingUtil.closeNotification(200);
            Thread.sleep(5);
            oePermanentlyDeleteButton = gridTestingUtil.getPermanentlyDeleteButton(gridXpath, 0);
        }

        orderCreateTest.setup();
        orderCreateTest.createOrder();

        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);

        WebElement oeDeleteButtonCreatedOrder = gridTestingUtil.getDeleteButton(oeGridXpath, 0);
        while(oeDeleteButtonCreatedOrder != null){
            oeDeleteButtonCreatedOrder.click();
            //removeNotification(); //TODO létrehozni egy ilyen függvényt, ami csak szimplán bezárja a notification-t
            Thread.sleep(100);
            oeDeleteButtonCreatedOrder = gridTestingUtil.getDeleteButton(oeGridXpath, 0);
        }

        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(20);
        gridTestingUtil.getModifyButton(gridXpath, 0).click(); //TODO megcsinálni, hogy előtte nézze meg a szerkesztésben, hogy látja-e őket.
        Thread.sleep(1000);
        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(orderCreateTest.createOrderGridXpath));
    }

    @Test
    @Video
    public void restoreOrderTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        int originalInvisibleRows = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedXpath);
        if(originalInvisibleRows == 0) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            deleteOrderTest();
        }
        crudTestingUtil.restoreTest();
    }

    @Test
    @Video
    public void permanentlyDeleteOrderTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        int originalInvisibleRows = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedXpath);
        if(originalInvisibleRows == 0) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
            deleteOrderTest();
        }
        crudTestingUtil.permanentlyDeleteTest();
    }

    @Test
    @Video
    public void fromToDateSelectorFromIsLaterThenTo() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);

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
        gridTestingUtil.selectDateFromDatePicker(datePicker1Xpath, date1);
        gridTestingUtil.selectDateFromDatePicker(datePicker2Xpath, date2);
        assertEquals(expectedDate1, gridTestingUtil.getDateFromDatePicker(datePicker1Xpath, dateFormat));
        assertEquals(expectedDate2, gridTestingUtil.getDateFromDatePicker(datePicker2Xpath, dateFormat));
    }

    //@Test
    public void extraFilterInvalidValue() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");

        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            orderCreateTest.setup();
            orderCreateTest.createOrder();
        }

        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }

    @Test
    @Video
    public void gettingOrdersFailed() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyOrderService).findAll(true); //ApiClintben .findAllWithDeleted();
        gridTestingUtil.mockDatabaseNotAvailableAfter(getClass(), spyDataSource, 2);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.checkNotificationText("Error happened while getting orders");
        gridTestingUtil.checkNoMoreNotificationsVisible();
        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(gridXpath));
        assertEquals(0, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedXpath));
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    private boolean waitForDownload(String fileNameRegex, int timeOut) throws Exception {
        Pattern pattern = Pattern.compile(fileNameRegex);

        for (int i = 0; i < timeOut; i++) {
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
            Thread.sleep(1000); // Várakozás 1 másodpercig
        }
        return false;
    }
}
