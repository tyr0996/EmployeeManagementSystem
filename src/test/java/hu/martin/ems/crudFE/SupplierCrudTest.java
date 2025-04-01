package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.NotificationCheck;
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
public class SupplierCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    private static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = contentXpath + "/vaadin-grid";
    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-button";
    
    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.SUPPLIER_SUBMENU;

    private GridTestingUtil gridTestingUtil;

    

    @BeforeClass
    public void setup() {
        gridTestingUtil = new GridTestingUtil(driver);
        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, driver, "Supplier", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    
    public void supplierCreateTest() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
         gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
    }

    @Test
    
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
    }

    @Test
    
    public void supplierReadTest() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
         gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
    }

    @Test
    
    public void supplierDeleteTest() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
         gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    
    public void supplierUpdateTest() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
         gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
    }

    @Test
    
    //@Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/addresses.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void supplierRestoreTest() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
         gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
    
    public void supplierPermanentlyDeleteTest() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
         gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
    }

    //@Test
    public void extraFilterInvalidValue() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
         gridTestingUtil.navigateMenu(mainMenu, subMenu);
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }
    @Test
    
    public void databaseNotAvailableWhenModify() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spySupplierService).update(any(Supplier.class));
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 5);
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Internal server error", false);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    
    public void nullResponseFromServiceWhenCreate() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spySupplierService).save(any(Supplier.class));
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 7);
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "Supplier saving failed: Internal server error", false);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    
    public void unexpectedResponseCodeWhenGettingAllSuppliers() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableAfter(getClass(), spyDataSource, 2);
//        Mockito.doReturn(null).when(spySupplierService).findAll(any(Boolean.class));
//        Mockito.doReturn(new EmsResponse(522, "")).when(spySupplierApiClient).findAll();
//        Mockito.doReturn(new EmsResponse(522, "")).when(spySupplierApiClient).findAllWithDeleted();
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        gridTestingUtil.checkNotificationText("Error happened while getting suppliers");
        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(gridXpath));
        assertEquals(0, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }

    @Test
    
    public void unexpectedResponseCodeWhenGettingAddresses() throws InterruptedException, SQLException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyAddressApiClient).findAll();
//        Mockito.doReturn(null).when(spyAddressService).findAll(false);
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Address", "Error happened while getting addresses");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 0);
    }

}
