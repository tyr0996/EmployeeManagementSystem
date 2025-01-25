package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.model.Supplier;
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
public class SupplierCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    private static final String showDeletedChecBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";
    private static final String createButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button";
    
    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.SUPPLIER_SUBMENU;

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Supplier", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void supplierCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
    }

    @Test
    public void supplierReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
    }

    @Test
    public void supplierDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void supplierUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
    }

    @Test
    //@Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/addresses.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void supplierRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
    public void supplierPermanentlyDeleteTest() throws InterruptedException {
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
    public void nullResponseFromServiceWhenModify() throws InterruptedException {
        Mockito.doReturn(null).when(spySupplierService).update(any(Supplier.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void nullResponseFromServiceWhenCreate() throws InterruptedException {
        Mockito.doReturn(null).when(spySupplierService).save(any(Supplier.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "Supplier saving failed: Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void unexpectedResponseCodeWhenGettingAllSuppliers() throws InterruptedException {
        Mockito.doReturn(null).when(spySupplierService).findAll(any(Boolean.class));
//        Mockito.doReturn(new EmsResponse(522, "")).when(spySupplierApiClient).findAll();
//        Mockito.doReturn(new EmsResponse(522, "")).when(spySupplierApiClient).findAllWithDeleted();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        checkNotificationText("Error happened while getting suppliers");
        assertEquals(0, countVisibleGridDataRows(gridXpath));
        assertEquals(0, countHiddenGridDataRows(gridXpath, showDeletedChecBoxXpath));
    }

    @Test
    public void unexpectedResponseCodeWhenGettingAddresses() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyAddressApiClient).findAll();
        Mockito.doReturn(null).when(spyAddressService).findAll(false);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Address", "Error happened while getting addresses");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(spyDataSource, null, null, 0);
    }

}
