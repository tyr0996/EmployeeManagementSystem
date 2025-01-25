package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.model.CodeStore;
import org.mockito.Mockito;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CodeStoreCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    private static final String showDeletedChecBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";
    private static final String createButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button";
    private static final String showOnlyDeletableCodeStores = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-checkbox";
    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.CODESTORE_SUBMENU;
    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "CodeStore", showDeletedChecBoxXpath, gridXpath, createButtonXpath, showOnlyDeletableCodeStores);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void codestoreCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
    }

    @Test
    public void codestoreReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
    }

    @Test
    public void codestoreDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void codestoreUpdateTest() throws InterruptedException, IOException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, null, true, showOnlyDeletableCodeStores);
        resetDatabase();
    }

    @Test
    public void codestoreRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
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
    public void nullResponseFromServiceWhenModify() throws InterruptedException {
        Mockito.doReturn(null).when(spyCodeStoreService).update(any(CodeStore.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Codestore modifying failed: internal server error", false, showOnlyDeletableCodeStores); //TODO ez meghal
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void nullResponseFromServiceWhenCreate() throws InterruptedException {
        Mockito.doReturn(null).when(spyCodeStoreService).save(any(CodeStore.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "CodeStore saving failed", false);
        checkNoMoreNotificationsVisible();
    }


    @Test
    public void gettingAllCodeStoresFailed() throws InterruptedException {
        Mockito.doReturn(null).when(spyCodeStoreService).findAll(true);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        checkNotificationText("Error happened while getting codestores");
        checkNoMoreNotificationsVisible();
        assertEquals(0, countVisibleGridDataRows(gridXpath));
        assertEquals(0, countHiddenGridDataRows(gridXpath, showDeletedChecBoxXpath));
    }

    @Test
    public void databaseUnavailableWhenSavingCodeStore() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(spyDataSource, null, null, 0);
    }
}
