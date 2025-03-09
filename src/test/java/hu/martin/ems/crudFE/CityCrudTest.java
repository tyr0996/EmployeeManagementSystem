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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CityCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    @Getter
    private static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    @Getter
    private static final String gridXpath = contentXpath + "/vaadin-grid";
    @Getter
    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-button";

    private static final String subMenu = UIXpaths.CITY_SUBMENU;
    private static final String mainMenu = UIXpaths.ADMIN_MENU;


    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "City", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    @Video
    public void cityCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void cityReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void cityDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void cityUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void cityRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void cityPermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
    }

//    @Test
//    public void extraFilterInvalidValue() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        NotificationCheck nc = new NotificationCheck();
//        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
//        crudTestingUtil.readTest(null, "{invalid json}", true, nc);
//    }


//    @Test(enabled = false)
//    public void unexpcetedResponseCodeCreate() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        crudTestingUtil.createNotExpectedStatusCodeSave(spyCityApiClient, City.class);
//        checkNoMoreNotificationsVisible();
//    }
//
//    @Test(enabled = false)
//    public void unexpcetedResponseCodeUpdate() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        crudTestingUtil.updateNotExpectedStatusCode(spyCityApiClient, City.class);
//        checkNoMoreNotificationsVisible();
//    }

    @Test
    @Video
    public void nullResponseFromServiceWhenModify() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCityService).update(any(City.class));
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 6);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void nullResponseFromServiceWhenCreate() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCityService).save(any(City.class));
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 6);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "City saving failed", false);
        checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void gettingCountriesFailed() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyCodeStoreApiClient).getChildren(StaticDatas.COUNTRIES_CODESTORE_ID);
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
//        failedFieldData.put("Country code", "Error happened while getting countries");
//        Thread.sleep(100);
//
//        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
//        checkNoMoreNotificationsVisible();
//    }

    @Test
    @Video
    public void gettingCountriesFailed() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.COUNTRIES_CODESTORE_ID); //id:6
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Country code", "Error happened while getting countries");
        Thread.sleep(100);

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
        checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void gettingCitiesFailed() {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyCityApiClient).findAllWithDeleted();
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        checkNotificationText("Getting cities failed");
//        checkNoMoreNotificationsVisible();
//    }

    @Test
    @Video
    public void gettingCitiesFailed() throws InterruptedException, SQLException {
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
//        Mockito.doReturn(null).when(spyCityService).findAll(true); //ApiClient-ben findAllWithDeleted
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        checkNotificationText("Getting cities failed");
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
