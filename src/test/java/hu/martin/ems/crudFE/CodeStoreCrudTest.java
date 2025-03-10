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

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CodeStoreCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    @Getter
    private static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    @Getter
    private static final String gridXpath =                contentXpath + "/vaadin-grid";
    @Getter
    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-button";

    @Getter
    private static final String showOnlyDeletableCodeStores = contentXpath + "/vaadin-checkbox";


    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.CODESTORE_SUBMENU;
    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "CodeStore", showDeletedCheckBoxXpath, gridXpath, createButtonXpath, showOnlyDeletableCodeStores);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    @Video
    public void codestoreCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
    }

    @Test
    @Video
    public void codestoreReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
    }

    @Test
    @Video
    public void codestoreDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    @Video
    public void codestoreUpdateTest() throws InterruptedException, IOException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, null, true, showOnlyDeletableCodeStores);
        resetDatabase();
    }

    @Test
    @Video
    public void codestoreRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
    @Video
    public void codestorePermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
    }

//    @Test(enabled = false)
//    public void unexpcetedResponseCodeCreate() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        crudTestingUtil.createNotExpectedStatusCodeSave(spyCodeStoreApiClient, CodeStore.class);
//    }
//
//    @Test(enabled = false)
//    public void unexpcetedResponseCodeUpdate() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        crudTestingUtil.updateNotExpectedStatusCode(spyCodeStoreApiClient, CodeStore.class, showOnlyDeletableCodeStores);
//    }

    @Test
    @Video
    public void nullResponseFromServiceWhenModify() throws InterruptedException, SQLException {
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
//        Mockito.doReturn(null).when(spyCodeStoreService).update(any(CodeStore.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Codestore modifying failed: internal server error", false, showOnlyDeletableCodeStores);
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void nullResponseFromServiceWhenCreate() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).save(any(CodeStore.class));
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "CodeStore saving failed", false);
        checkNoMoreNotificationsVisible();
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
    public void gettingAllCodeStoresFailed() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).findAll(true);
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 2);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        checkNotificationText("Error happened while getting codestores");
        checkNoMoreNotificationsVisible();
        assertEquals(0, countVisibleGridDataRows(gridXpath));
        assertEquals(0, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }

    @Test
    @Video
    public void databaseUnavailableWhenSavingCodeStore() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 0);
    }
}
