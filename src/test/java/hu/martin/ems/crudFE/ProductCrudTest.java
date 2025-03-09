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
    private GridTestingUtil gridTestingUtil;

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, gridTestingUtil, "Product", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
        this.gridTestingUtil = new GridTestingUtil(driver);
    }

    @Test
    @Video
    public void productCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
    }

    @Test
    @Video
    public void productReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
    }

    @Test
    @Video
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
    }

    @Test
    @Video
    public void productDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    @Video
    public void productUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
    }

    @Test
    @Video
    public void productRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
    @Video
    public void productPermanentlyDeleteTest() throws InterruptedException {
        //Azért nem lehet törölni, mert vannak olyan megrendelések, amikben benne van.
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
    }

    //@Test
    public void extraFilterInvalidValue() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }

    @Test
    @Video
    public void sellToCustomerTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        WebElement sellToCustomerButton = gridTestingUtil.getOptionButton(gridXpath, gridTestingUtil.getRandomLocationFromGrid(gridXpath), 3);
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        WebElement sellButtonContainer = sellToCustomerButton.getShadowRoot().findElement(By.className("vaadin-button-container"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", sellButtonContainer);

        WebElement dialog = gridTestingUtil.findVisibleElementWithXpath("//*[@id=\"overlay\"]");
        WebElement orderButton = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        crudTestingUtil.fillCreateOrUpdateForm(null);
        orderButton.click();
        gridTestingUtil.checkNotificationText("Order element successfully paired to customer!");
    }

    @Test
    @Video
    public void orderFromSupplierTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        WebElement orderFromSupplierButton = gridTestingUtil.getOptionButton(gridXpath, gridTestingUtil.getRandomLocationFromGrid(gridXpath), 4);
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        WebElement orderButtonContainer = orderFromSupplierButton.getShadowRoot().findElement(By.className("vaadin-button-container"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", orderButtonContainer);

        WebElement dialog = gridTestingUtil.findVisibleElementWithXpath("//*[@id=\"overlay\"]");
        WebElement orderButton = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        crudTestingUtil.fillCreateOrUpdateForm(null);
        orderButton.click();
        gridTestingUtil.checkNotificationText("Order element successfully paired to supplier!");
    }


//    @Test(enabled = false)
//    public void unexpcetedResponseCodeCreate() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        crudTestingUtil.createNotExpectedStatusCodeSave(spyProductApiClient, Product.class);
//    }
//
//    @Test(enabled = false)
//    public void unexpcetedResponseCodeUpdate() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        crudTestingUtil.updateNotExpectedStatusCode(spyProductApiClient, Product.class);
//    }

    @Test
    @Video
    public void databaseNotAvailableWhenModify() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyProductService).update(any(Product.class));
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 8);

        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Internal server error", false);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void nullResponseFromServiceWhenCreate() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyProductService).save(any(Product.class));
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 10);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "Product saving failed: Internal server error", false);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void nullReturnWhenGettingAllCustomers() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCustomerService).findAll(false);
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        LinkedHashMap<String, String> failedData = new LinkedHashMap<>();
        failedData.put("Customer", "Error happened while getting customers");

        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

        WebElement sellToCustomerButton = gridTestingUtil.getOptionButton(gridXpath, gridTestingUtil.getRandomLocationFromGrid(gridXpath), 3);
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        WebElement sellButtonContainer = sellToCustomerButton.getShadowRoot().findElement(By.className("vaadin-button-container"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", sellButtonContainer);

        WebElement dialog = gridTestingUtil.findVisibleElementWithXpath("//*[@id=\"overlay\"]");
        WebElement orderButton = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        crudTestingUtil.fillCreateOrUpdateForm(null, failedData);
        assertEquals(false, gridTestingUtil.isEnabled(orderButton));
    }

    @Test
    @Video
    public void nullResponseWhenGettingAllSuppliers() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spySupplierService).findAll(false);
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        LinkedHashMap<String, String> failedData = new LinkedHashMap<>();
        failedData.put("Supplier", "Error happened while getting suppliers");

        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        WebElement orderFromSupplierButton = gridTestingUtil.getOptionButton(gridXpath, gridTestingUtil.getRandomLocationFromGrid(gridXpath), 4);
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        WebElement supplierButtonContainer = orderFromSupplierButton.getShadowRoot().findElement(By.className("vaadin-button-container"));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", supplierButtonContainer);

        WebElement dialog = gridTestingUtil.findVisibleElementWithXpath("//*[@id=\"overlay\"]");
        WebElement orderButton = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        crudTestingUtil.fillCreateOrUpdateForm(null, failedData);
        assertEquals(false, gridTestingUtil.isEnabled(orderButton));
    }

    @Test
    @Video
    public void unexpectedResponseCodeWhenGettingAllProducts() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyProductService).findAll(any(Boolean.class));
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyProductApiClient).findAllWithDeleted();
        gridTestingUtil.mockDatabaseNotAvailableAfter(getClass(), spyDataSource, 2);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        gridTestingUtil.checkNotificationText("Error happened while getting products");
        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(gridXpath));
        assertEquals(0, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }

    @Test
    @Video
    public void unexpectedResponseCodeWhenGettingCurrenciesNames() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.CURRENCIES_CODESTORE_ID); //id 1
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Buying price currency", "Error happened while getting currencies");
        failedFieldData.put("Selling price currency", "Error happened while getting currencies");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    @Video
    public void unexpectedResponseCodeWhenGettingTaxKeys() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.TAXKEYS_CODESTORE_ID); // id 4
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Tax key", "Error happened while getting tax keys");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    @Video
    public void unexpectedResponseCodeWhenGettingAmountUnits() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.AMOUNTUNITS_CODESTORE_ID); //id 2
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 5);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Amount unit", "Error happened while getting amount units");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    @Video
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 0);
    }
}