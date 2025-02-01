package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.model.Customer;
import org.mockito.Mockito;
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
public class CustomerCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    private static final String showDeletedChecBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";
    private static final String createButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button";
    
    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.CUSTOMER_SUBMENU;

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Customer", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void customerCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
    }

    @Test
    public void customerReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
    }

    @Test
    public void customerDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void customerUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
    }

    @Test
    public void customerRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
    public void customerPermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
    }

    @Test
    public void nullResponseFromServiceWhenModify() throws InterruptedException {
        Mockito.doReturn(null).when(spyCustomerService).update(any(Customer.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void nullResponseFromServiceWhenCreate() throws InterruptedException {
        Mockito.doReturn(null).when(spyCustomerService).save(any(Customer.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "Customer saving failed", false);
        checkNoMoreNotificationsVisible();
    }



    @Test
    public void unexpectedResponseCodeWhenFindAllCustomer() throws InterruptedException {
        Mockito.doReturn(null).when(spyCustomerService).findAll(true); //ApiClient .findAllWithDeleted();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(200);
        checkNotificationContainsTexts("Error happened while getting customers");
        checkNoMoreNotificationsVisible();
        assertEquals(countVisibleGridDataRows(gridXpath), 0);
        assertEquals(countHiddenGridDataRows(gridXpath, showDeletedChecBoxXpath), 0);
    }

    @Test
    public void unexpectedResponseCodeWhenFindAllAddress() throws InterruptedException {
        Mockito.doReturn(null).when(spyAddressService).findAll(false);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedData = new LinkedHashMap<>();
        failedData.put("Address", "Error happened while getting addresses");
        checkNoMoreNotificationsVisible();
        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedData);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 0);
    }
}
