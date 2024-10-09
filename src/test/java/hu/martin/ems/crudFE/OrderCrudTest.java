package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.model.Order;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.testng.AssertJUnit.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class OrderCrudTest extends BaseCrudTest {

    private static WebDriverWait notificationDisappearWait;

    private static final String gridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";
    private static final String showDeletedXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-checkbox";
    private static final String createOrderGridXpathModify = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-vertical-layout/vaadin-grid";

    //TODO: valamiért máshogy generálja, hogyha a modify-ra nyomok. Ezt ki kellene majd nyomozni
    private static final String createOrderPaymentComboBox = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-vertical-layout/vaadin-form-layout[2]/vaadin-combo-box[2]";
    private static final String createOrderCurrencyComboBox = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-vertical-layout/vaadin-form-layout[2]/vaadin-combo-box[1]";
    private static final String createOrderSaveOrderButton =  "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-vertical-layout/vaadin-form-layout[2]/vaadin-button";
    private static CrudTestingUtil crudTestingUtil;

    private static final String createButtonXpath = null;

    @Value("${chrome.download.path}")
    private String downloadPath;

    @BeforeMethod
    public void setup() {
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

        WebElement odtButton = getOptionDownloadButton(gridXpath, rowLocation, 2);
        odtButton.click();
        Thread.sleep(100);
        assertEquals(true, waitForDownload("order_[0-9]{1,}.pdf", 10));
    }

//    @Test
//    public void sendSFTPFailedTest(){
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
//        assertEquals(1, 0);
//    }
//
//    @Test
//    public void sendEmailFailedTest(){
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
//        assertEquals(1, 0);
//    }

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
        selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpathModify);

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

    private boolean waitForDownload(String fileNameRegex, int timeOut)
            throws Exception {
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
