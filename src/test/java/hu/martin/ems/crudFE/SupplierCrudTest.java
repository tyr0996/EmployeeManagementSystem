package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.NotificationCheck;
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
public class SupplierCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    private static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = contentXpath + "/vaadin-grid";
    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-button";
    
    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.SUPPLIER_SUBMENU;

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Supplier", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    @Video
    public void supplierCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
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
    public void supplierReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
    }

    @Test
    @Video
    public void supplierDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    @Video
    public void supplierUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
    }

    @Test
    @Video
    //@Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/addresses.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void supplierRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
    @Video
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
    @Video
    public void databaseNotAvailableWhenModify() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spySupplierService).update(any(Supplier.class));
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 5);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void nullResponseFromServiceWhenCreate() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spySupplierService).save(any(Supplier.class));
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 7);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "Supplier saving failed: Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void unexpectedResponseCodeWhenGettingAllSuppliers() throws InterruptedException, SQLException {
        mockDatabaseNotAvailableAfter(getClass(), spyDataSource, 2);
//        Mockito.doReturn(null).when(spySupplierService).findAll(any(Boolean.class));
//        Mockito.doReturn(new EmsResponse(522, "")).when(spySupplierApiClient).findAll();
//        Mockito.doReturn(new EmsResponse(522, "")).when(spySupplierApiClient).findAllWithDeleted();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        checkNotificationText("Error happened while getting suppliers");
        assertEquals(0, countVisibleGridDataRows(gridXpath));
        assertEquals(0, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }

    @Test
    @Video
    public void unexpectedResponseCodeWhenGettingAddresses() throws InterruptedException, SQLException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyAddressApiClient).findAll();
//        Mockito.doReturn(null).when(spyAddressService).findAll(false);
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Address", "Error happened while getting addresses");

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
