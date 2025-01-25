package hu.martin.ems.crudApi;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import org.mockito.Mockito;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

import static hu.martin.ems.base.GridTestingUtil.checkNotificationText;
import static hu.martin.ems.base.GridTestingUtil.navigateMenu;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CodeStoreApiTest extends BaseCrudTest {

    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.CODESTORE_SUBMENU;

    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;
    private static final String showDeletedChecBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";
    private static final String createButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button";
    private static final String showOnlyDeletableCodeStores = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-checkbox";


    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "CodeStore", showDeletedChecBoxXpath, gridXpath, createButtonXpath, showOnlyDeletableCodeStores);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void serviceFindAllReturnsNullTest() {
        Mockito.when(spyCodeStoreService.findAll(any(Boolean.class))).thenReturn(null);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        checkNotificationText("Error happened while getting codestores");
    }
}
