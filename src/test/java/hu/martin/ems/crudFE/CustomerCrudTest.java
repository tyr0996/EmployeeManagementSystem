package hu.martin.ems.crudFE;

import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import lombok.Getter;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomerCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;
//    private static WebDriver driver = WebDriverProvider.get();

    @Getter
    private static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    @Getter
    private static final String gridXpath = contentXpath + "/vaadin-grid";

    @Getter
    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-button";
    
    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.CUSTOMER_SUBMENU;

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Customer", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
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
    public void customerCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
    }

    @Test
    @Video
    public void customerReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
    }

    @Test
    @Video
    public void customerDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    @Video
    public void customerUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
    }

    @Test
    @Video
    public void customerRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
    @Video
    public void customerPermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
    }

    @Test
    @Video
    public void nullResponseFromServiceWhenModify() throws InterruptedException, SQLException {
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 5);
//        Mockito.doReturn(null).when(spyCustomerService).update(any(Customer.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void nullResponseFromServiceWhenCreate() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCustomerService).save(any(Customer.class));
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 7);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "Customer saving failed", false);
        checkNoMoreNotificationsVisible();
    }



    @Test
    @Video
    public void unexpectedResponseCodeWhenFindAllCustomer() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCustomerService).findAll(true); //ApiClient .findAllWithDeleted();
        mockDatabaseNotAvailableAfter(getClass(), spyDataSource, 2);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(200);
        checkNotificationContainsTexts("Error happened while getting customers");
        checkNoMoreNotificationsVisible();
        assertEquals(countVisibleGridDataRows(gridXpath), 0);
        assertEquals(countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath), 0);
    }

    @Test
    @Video
    public void unexpectedResponseCodeWhenFindAllAddress() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyAddressService).findAll(false);
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedData = new LinkedHashMap<>();
        failedData.put("Address", "Error happened while getting addresses");
        checkNoMoreNotificationsVisible();
        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedData);
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 0);
    }
}
