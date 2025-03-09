package hu.martin.ems.crudFE;

import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;

import static hu.martin.ems.base.GridTestingUtil.checkNotificationText;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminToolsTest extends BaseCrudTest {
    @BeforeClass
    public void setup() {
        new CrudTestingUtil(driver, null, null, null, null);
        GridTestingUtil.driver = driver;
    }

    private static String clearDatabaseButtonXpath = contentXpath + "/vaadin-button";

    @Test
    @Video
    public void clearDatabaseTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        GridTestingUtil.navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ADMINTOOLS_SUB_MENU);
        WebElement button = GridTestingUtil.findVisibleElementWithXpath(clearDatabaseButtonXpath);
        button.click();
        checkNotificationText("Clearing database was successful");
    }

    @Test
    @Video
    public void clearDatabaseExceptionsTestThanSuccess() throws Exception {
        MockitoAnnotations.openMocks(this);
        Mockito.doThrow(new ClassNotFoundException())
               .doThrow(new InvocationTargetException(new Throwable()))
               .doThrow(new InstantiationException())
               .doThrow(new IllegalAccessException())
               .doCallRealMethod()
        .when(spyAdminToolsService).clearAllDatabaseTable();
//        AdminToolsService originalAdminToolsService = BeanProvider.getBean(AdminToolsService.class);
//        BeanProvider.getBean(AdminToolsController.class).setAdminToolsService(adminToolsService);

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
//        Mockito.reset(adminToolsService);
//        BeanProvider.getBean(AdminToolsController.class).setAdminToolsService(originalAdminToolsService);
    }


}
