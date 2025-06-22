package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.ProductPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.dialog.Product_OrderingDialog;
import hu.martin.ems.pages.core.dialog.Product_SellingDialog;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.ProductSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.sql.SQLException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Listeners(UniversalVideoListener.class)
public class ProductCrudTest extends BaseCrudTest {

    @Test
    public void productCreateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        DoCreateTestData testResult = productPage.doCreateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Product saved: ");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void productReadTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        DoReadTestData testResult = productPage.doReadTest(true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        DoDeleteFailedTestData testResult = productPage.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Database error");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void productDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        DoDeleteTestData testResult = productPage.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Product deleted: ");

        productPage.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, productPage.getGrid().getTotalDeletedRowNumber(productPage.getShowDeletedSwitch()));
        assertEquals(0, productPage.getGrid().getTotalNonDeletedRowNumber(productPage.getShowDeletedSwitch()));
        productPage.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void productUpdateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        DoUpdateTestData testResult = productPage.doUpdateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Product updated: ");

        productPage.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, productPage.getGrid().getTotalDeletedRowNumber(productPage.getShowDeletedSwitch()));
        assertEquals(0, productPage.getGrid().getTotalNonDeletedRowNumber(productPage.getShowDeletedSwitch()));
        productPage.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void productRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        DoRestoreTestData testResult = productPage.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Product restored: ");

        productPage = new ProductPage(driver, port);
        productPage.getGrid().applyFilter(testResult.getResult().getRestoredData());
        productPage.getGrid().waitForRefresh();
        assertEquals(productPage.getGrid().getTotalDeletedRowNumber(productPage.getShowDeletedSwitch()), 0);
        assertEquals(productPage.getGrid().getTotalNonDeletedRowNumber(productPage.getShowDeletedSwitch()), 1);
        productPage.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void productPermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        DoPermanentlyDeleteTestData testResult = productPage.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("Product permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        productPage.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, productPage.getGrid().getTotalDeletedRowNumber(productPage.getShowDeletedSwitch()));
        assertEquals(0, productPage.getGrid().getTotalNonDeletedRowNumber(productPage.getShowDeletedSwitch()));
        productPage.getGrid().resetFilter();

        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Clearing database was successful");
        notificationComponent.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }


    @Test
    public void sellToCustomerTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);
        ProductPage page = new ProductPage(driver, port);

        VaadinButtonComponent orderFromSupplierButton = page.getGrid().getOptionColumnButton(page.getGrid().getRandomLocation(), 3);
        orderFromSupplierButton.click();
        Product_OrderingDialog dialog = new Product_OrderingDialog(driver);
        dialog.initWebElements();

        dialog.fill(null);
        dialog.getOrderButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Order element successfully paired to customer!");
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void orderFromSupplierTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);
        ProductPage page = new ProductPage(driver, port);

        VaadinButtonComponent orderFromSupplierButton = page.getGrid().getOptionColumnButton(page.getGrid().getRandomLocation(), 4);
        orderFromSupplierButton.click();
        Product_OrderingDialog dialog = new Product_OrderingDialog(driver);
        dialog.initWebElements();

        dialog.fill(null);
        dialog.getOrderButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Order element successfully paired to supplier!");
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        DoUpdateFailedTestData testResult = productPage.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("Product modifying failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void nullResponseFromServiceWhenCreate() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        DoCreateFailedTestData testResult = productPage.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Product saving failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void nullReturnWhenGettingAllCustomers() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 3);

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);
        ProductPage page = new ProductPage(driver, port);

        VaadinButtonComponent sellToCustomerButton = page.getGrid().getOptionColumnButton(page.getGrid().getRandomLocation(), 3);
        sellToCustomerButton.click();
        Product_SellingDialog dialog = new Product_SellingDialog(driver);
        dialog.initWebElements();

        assertEquals(dialog.getFailedComponents().size(), 1);
        assertEquals(dialog.getCustomerComboBox().getErrorMessage(), "EmsError happened while getting customers");
        assertFalse(dialog.getSellButton().isEnabled());
        assertFalse(dialog.getCustomerComboBox().isEnabled());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void nullResponseWhenGettingAllSuppliers() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);
        ProductPage page = new ProductPage(driver, port);

        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        VaadinButtonComponent orderFromSupplierButton = page.getGrid().getOptionColumnButton(page.getGrid().getRandomLocation(), 4);
        orderFromSupplierButton.click();
        Product_OrderingDialog dialog = new Product_OrderingDialog(driver);
        dialog.initWebElements();

        assertEquals(dialog.getFailedComponents().size(), 1);
        assertEquals(dialog.getSupplierComboBox().getErrorMessage(), "EmsError happened while getting suppliers");
        assertFalse(dialog.getOrderButton().isEnabled());
        assertFalse(dialog.getSupplierComboBox().isEnabled());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void unexpectedResponseCodeWhenGettingAllProducts() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(0, 1, 2));
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        SoftAssert sa = new SoftAssert();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "EmsError happened while getting products: Database error", "1");
        notification.close();

        assertEquals(productPage.getGrid().getTotalDeletedRowNumber(productPage.getShowDeletedSwitch()), 0);
        VaadinNotificationComponent notification2 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification2.getText(), "EmsError happened while getting products: Database error", "2");
        notification2.close();

        assertEquals(productPage.getGrid().getTotalNonDeletedRowNumber(productPage.getShowDeletedSwitch()), 0);
        VaadinNotificationComponent notification3 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification3.getText(), "EmsError happened while getting products: Database error", "3");
        notification3.close();

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void unexpectedResponseCodeWhenGettingCurrenciesNames() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(3, 6));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        productPage.getCreateButton().click();

        ProductSaveOrUpdateDialog dialog = new ProductSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        assertEquals(dialog.getFailedComponents().size(), 2);
        SoftAssert sa = new SoftAssert();
        sa.assertFalse(dialog.getSellingPriceCurrencyComponent().isEnabled());
        sa.assertFalse(dialog.getBuyingPriceCurrencyComponent().isEnabled());
        sa.assertEquals(dialog.getSellingPriceCurrencyComponent().getErrorMessage(), "EmsError happened while getting currencies");
        sa.assertEquals(dialog.getBuyingPriceCurrencyComponent().getErrorMessage(), "EmsError happened while getting currencies");
        sa.assertFalse(dialog.getSaveButton().isEnabled());

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void unexpectedResponseCodeWhenGettingTaxKeys() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 4);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        productPage.getCreateButton().click();

        ProductSaveOrUpdateDialog dialog = new ProductSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        assertEquals(dialog.getFailedComponents().size(), 1);
        SoftAssert sa = new SoftAssert();
        sa.assertFalse(dialog.getTaxKeyComponent().isEnabled());
        sa.assertEquals(dialog.getTaxKeyComponent().getErrorMessage(), "EmsError happened while getting tax keys");
        sa.assertFalse(dialog.getSaveButton().isEnabled());

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void unexpectedResponseCodeWhenGettingPackingUnits() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 5);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        productPage.getCreateButton().click();

        ProductSaveOrUpdateDialog dialog = new ProductSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        assertEquals(dialog.getFailedComponents().size(), 1);
        SoftAssert sa = new SoftAssert();
        sa.assertFalse(dialog.getPackingUnitComponent().isEnabled());
        sa.assertEquals(dialog.getPackingUnitComponent().getErrorMessage(), "EmsError happened while getting packing units");
        sa.assertFalse(dialog.getSaveButton().isEnabled());

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseUnavailableWhenSaving() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        DoCreateFailedTestData testResult = productPage.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Product saving failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }


    @Test
    public void closeCreateDialogTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);

        ProductPage productPage = new ProductPage(driver, port);
        productPage.getCreateButton().click();
        ProductSaveOrUpdateDialog dialog = new ProductSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        dialog.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void closeSellingDialogTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);
        ProductPage page = new ProductPage(driver, port);

        VaadinButtonComponent sellToCustomer = page.getGrid().getOptionColumnButton(page.getGrid().getRandomLocation(), 3);
        sellToCustomer.click();
        Product_SellingDialog dialog = new Product_SellingDialog(driver);
        dialog.initWebElements();
        dialog.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void closeOrderingDialogTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.PRODUCT_SUBMENU);
        ProductPage page = new ProductPage(driver, port);

        VaadinButtonComponent orderFromSupplierButton = page.getGrid().getOptionColumnButton(page.getGrid().getRandomLocation(), 4);
        orderFromSupplierButton.click();
        Product_OrderingDialog dialog = new Product_OrderingDialog(driver);
        dialog.initWebElements();
        dialog.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }
}