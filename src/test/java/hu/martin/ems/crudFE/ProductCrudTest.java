package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Listeners(UniversalVideoListener.class)
public class ProductCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    private static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = contentXpath + "/vaadin-grid";
    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-button";

    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.PRODUCT_SUBMENU;

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Product", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    @Video
    public void productCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
    }

    @Test
    @Video
    public void productReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
    }

    @Test
    @Video
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
    }

    @Test
    @Video
    public void productDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    @Video
    public void productUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
    }

    @Test
    @Video
    public void productRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
    @Video
    public void productPermanentlyDeleteTest() throws InterruptedException {
        //Azért nem lehet törölni, mert vannak olyan megrendelések, amikben benne van.
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
    }

    //@Test
    public void extraFilterInvalidValue() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }

    @Test
    @Video
    public void sellToCustomerTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        WebElement sellToCustomerButton = GridTestingUtil.getOptionButton(gridXpath, getRandomLocationFromGrid(gridXpath), 3);
        findVisibleElementWithXpath(gridXpath);
        WebElement sellButtonContainer = sellToCustomerButton.getShadowRoot().findElement(By.className("vaadin-button-container"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", sellButtonContainer);

        WebElement dialog = findVisibleElementWithXpath("//*[@id=\"overlay\"]");
        WebElement orderButton = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        crudTestingUtil.fillCreateOrUpdateForm(null);
        orderButton.click();
        checkNotificationText("Order element successfully paired to customer!");
    }

    @Test
    @Video
    public void orderFromSupplierTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        WebElement orderFromSupplierButton = GridTestingUtil.getOptionButton(gridXpath, getRandomLocationFromGrid(gridXpath), 4);
        findVisibleElementWithXpath(gridXpath);
        WebElement orderButtonContainer = orderFromSupplierButton.getShadowRoot().findElement(By.className("vaadin-button-container"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", orderButtonContainer);

        WebElement dialog = findVisibleElementWithXpath("//*[@id=\"overlay\"]");
        WebElement orderButton = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        crudTestingUtil.fillCreateOrUpdateForm(null);
        orderButton.click();
        checkNotificationText("Order element successfully paired to supplier!");
    }


//    @Test(enabled = false)
//    public void unexpcetedResponseCodeCreate() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        crudTestingUtil.createNotExpectedStatusCodeSave(spyProductApiClient, Product.class);
//    }
//
//    @Test(enabled = false)
//    public void unexpcetedResponseCodeUpdate() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        crudTestingUtil.updateNotExpectedStatusCode(spyProductApiClient, Product.class);
//    }

    @Test
    @Video
    public void databaseNotAvailableWhenModify() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyProductService).update(any(Product.class));
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 8);

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void nullResponseFromServiceWhenCreate() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyProductService).save(any(Product.class));
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 10);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "Product saving failed: Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void nullReturnWhenGettingAllCustomers() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCustomerService).findAll(false);
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        LinkedHashMap<String, String> failedData = new LinkedHashMap<>();
        failedData.put("Customer", "Error happened while getting customers");

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);

        WebElement sellToCustomerButton = GridTestingUtil.getOptionButton(gridXpath, getRandomLocationFromGrid(gridXpath), 3);
        findVisibleElementWithXpath(gridXpath);
        WebElement sellButtonContainer = sellToCustomerButton.getShadowRoot().findElement(By.className("vaadin-button-container"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", sellButtonContainer);

        WebElement dialog = findVisibleElementWithXpath("//*[@id=\"overlay\"]");
        WebElement orderButton = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        crudTestingUtil.fillCreateOrUpdateForm(null, failedData);
        assertEquals(false, GridTestingUtil.isEnabled(orderButton));
    }

    @Test
    @Video
    public void nullResponseWhenGettingAllSuppliers() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spySupplierService).findAll(false);
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        LinkedHashMap<String, String> failedData = new LinkedHashMap<>();
        failedData.put("Supplier", "Error happened while getting suppliers");

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        WebElement orderFromSupplierButton = GridTestingUtil.getOptionButton(gridXpath, getRandomLocationFromGrid(gridXpath), 4);
        findVisibleElementWithXpath(gridXpath);
        WebElement supplierButtonContainer = orderFromSupplierButton.getShadowRoot().findElement(By.className("vaadin-button-container"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", supplierButtonContainer);

        WebElement dialog = findVisibleElementWithXpath("//*[@id=\"overlay\"]");
        WebElement orderButton = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        crudTestingUtil.fillCreateOrUpdateForm(null, failedData);
        assertEquals(false, GridTestingUtil.isEnabled(orderButton));
    }

    @Test
    @Video
    public void unexpectedResponseCodeWhenGettingAllProducts() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyProductService).findAll(any(Boolean.class));
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyProductApiClient).findAllWithDeleted();
        mockDatabaseNotAvailableAfter(getClass(), spyDataSource, 2);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        checkNotificationText("Error happened while getting products");
        assertEquals(0, countVisibleGridDataRows(gridXpath));
        assertEquals(0, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }

    @Test
    @Video
    public void unexpectedResponseCodeWhenGettingCurrenciesNames() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.CURRENCIES_CODESTORE_ID); //id 1
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Buying price currency", "Error happened while getting currencies");
        failedFieldData.put("Selling price currency", "Error happened while getting currencies");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    @Video
    public void unexpectedResponseCodeWhenGettingTaxKeys() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.TAXKEYS_CODESTORE_ID); // id 4
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Tax key", "Error happened while getting tax keys");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    @Video
    public void unexpectedResponseCodeWhenGettingAmountUnits() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.AMOUNTUNITS_CODESTORE_ID); //id 2
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 5);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Amount unit", "Error happened while getting amount units");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    @Video
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 0);
    }
}