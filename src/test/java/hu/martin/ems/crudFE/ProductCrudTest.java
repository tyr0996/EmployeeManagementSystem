package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.model.Product;
import org.mockito.Mockito;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.mockito.ArgumentMatchers.any;
import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ProductCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    private static final String showDeletedChecBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";
    private static final String createButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button";

    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.PRODUCT_SUBMENU;

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Product", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void productCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
    }

    @Test
    public void productReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
    }

    @Test
    public void productDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void productUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
    }

    @Test
    public void productRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
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
    public void nullResponseFromServiceWhenModify() throws InterruptedException {
        Mockito.doReturn(null).when(spyProductService).update(any(Product.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void nullResponseFromServiceWhenCreate() throws InterruptedException {
        Mockito.doReturn(null).when(spyProductService).save(any(Product.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "Product saving failed: Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void nullReturnWhenGettingAllCustomers() throws InterruptedException {
        Mockito.doReturn(null).when(spyCustomerService).findAll(false);
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
    public void nullResponseWhenGettingAllSuppliers() throws InterruptedException {
        Mockito.doReturn(null).when(spySupplierService).findAll(false);
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
    public void unexpectedResponseCodeWhenGettingAllProducts() throws InterruptedException {
        Mockito.doReturn(null).when(spyProductService).findAll(any(Boolean.class));
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyProductApiClient).findAllWithDeleted();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        checkNotificationText("Error happened while getting products");
        assertEquals(0, countVisibleGridDataRows(gridXpath));
        assertEquals(0, countHiddenGridDataRows(gridXpath, showDeletedChecBoxXpath));
    }

    @Test
    public void unexpectedResponseCodeWhenGettingCurrenciesNames() throws InterruptedException {
        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.CURRENCIES_CODESTORE_ID);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Buying price currency", "Error happened while getting currencies");
        failedFieldData.put("Selling price currency", "Error happened while getting currencies");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    public void unexpectedResponseCodeWhenGettingTaxKeys() throws InterruptedException {
        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.TAXKEYS_CODESTORE_ID);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Tax key", "Error happened while getting tax keys");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    public void unexpectedResponseCodeWhenGettingAmountUnits() throws InterruptedException {
        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.AMOUNTUNITS_CODESTORE_ID);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Amount unit", "Error happened while getting amount units");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 0);
    }
}