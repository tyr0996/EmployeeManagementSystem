package hu.martin.ems.crudFE;

import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import lombok.Getter;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;

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

    private GridTestingUtil gridTestingUtil;

    

    @BeforeClass
    public void setup() {
        gridTestingUtil = new GridTestingUtil(getDriver());
        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, getDriver(), "City", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(getDriver(), Duration.ofMillis(5000));
    }

    @Test
    @Video
    public void cityCreateTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void cityReadTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void cityDeleteTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void cityUpdateTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void cityRestoreTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void cityPermanentlyDeleteTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
    }

//    @Test
//    public void extraFilterInvalidValue() throws InterruptedException {
//        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        NotificationCheck nc = new NotificationCheck();
//        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
//        crudTestingUtil.readTest(null, "{invalid json}", true, nc);
//    }


//    @Test(enabled = false)
//    public void unexpcetedResponseCodeCreate() throws InterruptedException {
//        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        crudTestingUtil.createNotExpectedStatusCodeSave(spyCityApiClient, City.class);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }
//
//    @Test(enabled = false)
//    public void unexpcetedResponseCodeUpdate() throws InterruptedException {
//        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        crudTestingUtil.updateNotExpectedStatusCode(spyCityApiClient, City.class);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    @Test
    @Video
    public void nullResponseFromServiceWhenModify() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCityService).update(any(City.class));
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 6);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Internal server error", false);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void nullResponseFromServiceWhenCreate() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCityService).save(any(City.class));
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 6);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "City saving failed", false);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void gettingCountriesFailed() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyCodeStoreApiClient).getChildren(StaticDatas.COUNTRIES_CODESTORE_ID);
//        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
//        failedFieldData.put("Country code", "Error happened while getting countries");
//        Thread.sleep(100);
//
//        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    @Test
    @Video
    public void gettingCountriesFailed() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.COUNTRIES_CODESTORE_ID); //id:6
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Country code", "Error happened while getting countries");
        Thread.sleep(100);

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void gettingCitiesFailed() {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyCityApiClient).findAllWithDeleted();
//        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.checkNotificationText("Getting cities failed");
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    @Test
    @Video
    public void gettingCitiesFailed() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
//        Mockito.doReturn(null).when(spyCityService).findAll(true); //ApiClient-ben findAllWithDeleted
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.checkNotificationText("Getting cities failed");
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 0);
    }
}
