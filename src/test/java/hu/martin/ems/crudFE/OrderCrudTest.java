package hu.martin.ems.crudFE;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.controller.OrderController;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.controller.EmailSendingController;
import hu.martin.ems.core.model.EmailData;
import hu.martin.ems.core.model.EmailProperties;
import hu.martin.ems.core.service.EmailSendingService;
import hu.martin.ems.core.sftp.SftpSender;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.service.OrderService;
import hu.martin.ems.vaadin.component.Order.OrderCreate;
import hu.martin.ems.vaadin.component.Order.OrderList;
import org.checkerframework.checker.units.qual.A;
import org.mockito.*;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
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

    @Mock
    public EmailSendingService emailSendingService;

    @Mock
    public SftpSender sftpSender;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.openMocks(this);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
        crudTestingUtil = new CrudTestingUtil(driver, "Order", showDeletedXpath, gridXpath, createButtonXpath);
        GridTestingUtil.driver = driver;
    }

    @Test
    public void generateODTTest() throws Exception {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);

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
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);

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
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        findVisibleElementWithXpath(gridXpath);

        findVisibleElementWithXpath(sendReportViaSFTPButtonXpath).click();
        checkNotificationContainsTexts("Error happened when sending with SFTP", 30000);
    }

    @Test
    public void
    sendSFTPFailedJsonTest() throws JsonProcessingException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
//        findVisibleElementWithXpath(gridXpath);
//        //Mockito.doThrow(JsonProcessingException.class).when(om).writeValueAsString(Mockito.any(Object.class));
//
//        findVisibleElementWithXpath(sendReportViaSFTPButtonXpath).click();
//        checkNotificationContainsTexts("Data processing error", 30000);
    }

    @Test
    public void sendSFTPSuccessTest() throws InterruptedException {
        BeanProvider.getBean(OrderService.class).setSender(sftpSender);
        Mockito.doReturn(true).when(sftpSender).send(any(byte[].class), any(String.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        findVisibleElementWithXpath(gridXpath);

        findVisibleElementWithXpath(sendReportViaSFTPButtonXpath).click();
        Thread.sleep(100);
        checkNotificationContainsTexts("SFTP sending is done", 30000);
    }


    @Test
    public void sendEmailSuccessTest() throws InterruptedException {
        BeanProvider.getBean(EmailSendingController.class).setService(emailSendingService);
        Mockito.doReturn(true).when(emailSendingService).send(Mockito.any(EmailProperties.class));

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);

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
        checkNotificationContainsTexts("Email sikeresen elküldve!", 5000);

    }

    @Test
    public void sendEmailFailedTest() throws InterruptedException {

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);

        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null) {
            OrderCreateTest.setupTest();
            OrderCreateTest.createOrder();
            rowLocation = new ElementLocation(1, 0);
        }

        WebElement sendEmailButton = getOptionColumnButton(gridXpath, rowLocation, 3);
        sendEmailButton.click();
        checkNotificationContainsTexts("Email küldése sikertelen", 5000);
    }

    @Test
    public void deleteOrderTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void modifyOrderTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);

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
        findClickableElementWithXpath(createOrderSaveOrderButton).click();
        checkNotificationContainsTexts("Order updated:");
        Thread.sleep(100);


        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        assertEquals(0, countElementResultsFromGridWithFilter(gridXpath, originalData));
        assertEquals(original, countVisibleGridDataRows(gridXpath));
    }

    @Test
    public void deleteOrderElementWhatMemberOfAnOrder() throws InterruptedException {



        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        int originalVisibleRows = countVisibleGridDataRows(gridXpath);

        WebElement deleteButton = getDeleteButton(gridXpath, 0);
        while(deleteButton != null){
            deleteButton.click();
            //removeNotification(); //TODO létrehozni egy ilyen függvényt, ami csak szimplán bezárja a notification-t
            Thread.sleep(5);
            deleteButton = getDeleteButton(gridXpath, 0);
        }
        setCheckboxStatus(showDeletedXpath, true);
        WebElement permanentlyDeleteButton = getPermanentlyDeleteButton(gridXpath, 0);
        while(permanentlyDeleteButton != null){
            permanentlyDeleteButton.click();
            //removeNotification(); //TODO létrehozni egy ilyen függvényt, ami csak szimplán bezárja a notification-t
            Thread.sleep(5);
            permanentlyDeleteButton = getPermanentlyDeleteButton(gridXpath, 0);
        }

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);

        String oeGridXpath = OrderElementCrudTest.gridXpath;
        String oeShowDeletedXpath = OrderElementCrudTest.showDeletedChecBoxXpath;
        WebElement oeDeleteButton = getDeleteButton(oeGridXpath, 0);
        while(oeDeleteButton != null){
            oeDeleteButton.click();
            //removeNotification(); //TODO létrehozni egy ilyen függvényt, ami csak szimplán bezárja a notification-t
            Thread.sleep(5);
            oeDeleteButton = getDeleteButton(oeGridXpath, 0);
        }
        setCheckboxStatus(oeShowDeletedXpath, true);
        WebElement oePermanentlyDeleteButton = getPermanentlyDeleteButton(oeGridXpath, 0);
        while(oePermanentlyDeleteButton != null){
            oePermanentlyDeleteButton.click();
            //removeNotification(); //TODO létrehozni egy ilyen függvényt, ami csak szimplán bezárja a notification-t
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

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(20);
        getModifyButton(gridXpath, 0).click(); //TODO megcsinálni, hogy előtte nézze meg a szerkesztésben, hogy látja-e őket.
        Thread.sleep(1000);
        assertEquals(0, countVisibleGridDataRows(OrderCreateTest.createOrderGridXpath));
    }

    @Test
    public void restoreOrderTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
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
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
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
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
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

    @Test
    public void extraFilterInvalidValue() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
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
