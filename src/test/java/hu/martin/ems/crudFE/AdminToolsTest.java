package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.controller.AdminToolsController;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.service.AdminToolsService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;

import static hu.martin.ems.base.GridTestingUtil.checkNotificationText;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AdminToolsTest extends BaseCrudTest {
    @Mock
    AdminToolsService adminToolsService;

    @BeforeClass
    public void setup() {
        new CrudTestingUtil(driver, null, null, null, null);
        GridTestingUtil.driver = driver;
    }

    private static String clearDatabaseButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-button";

    @Test
    public void clearDatabaseTest(){
        TestingUtils.loginWith(driver, port, "admin", "admin");
        GridTestingUtil.navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ADMINTOOLS_SUB_MENU);
        WebElement button = GridTestingUtil.findVisibleElementWithXpath(clearDatabaseButtonXpath);
        button.click();
        checkNotificationText("Clearing database was successful");
    }

    @Test
    public void clearDatabaseExceptionsTestThanSuccess() throws Exception {
        MockitoAnnotations.openMocks(this);
        Mockito.doThrow(new ClassNotFoundException())
               .doThrow(new InvocationTargetException(new Throwable()))
               .doThrow(new InstantiationException())
               .doThrow(new IllegalAccessException())
               .doNothing()
        .when(adminToolsService).clearAllDatabaseTable();
        AdminToolsService originalAdminToolsService = BeanProvider.getBean(AdminToolsService.class);
        BeanProvider.getBean(AdminToolsController.class).setAdminToolsService(adminToolsService);

        TestingUtils.loginWith(driver, port, "admin", "admin");
        Thread.sleep(100);
        GridTestingUtil.navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ADMINTOOLS_SUB_MENU);

        WebElement button = GridTestingUtil.findVisibleElementWithXpath(clearDatabaseButtonXpath);
        button.click();
        checkNotificationText("Clearing database failed for one or more table");
        button.click();
        checkNotificationText("Clearing database failed for one or more table");
        button.click();
        checkNotificationText("Clearing database failed for one or more table");
        button.click();
        checkNotificationText("Clearing database failed for one or more table");
        button.click();
        checkNotificationText("Clearing database was successful");
        Mockito.reset(adminToolsService);
        BeanProvider.getBean(AdminToolsController.class).setAdminToolsService(originalAdminToolsService);
    }


}
