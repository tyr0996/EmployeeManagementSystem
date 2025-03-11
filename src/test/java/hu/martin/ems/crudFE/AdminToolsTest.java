package hu.martin.ems.crudFE;

import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.service.AdminToolsService;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminToolsTest extends BaseCrudTest {

    private GridTestingUtil gridTestingUtil;

    @SpyBean
    public AdminToolsService spyAdminToolsService;


    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);
        gridTestingUtil = new GridTestingUtil(getDriver());
        new CrudTestingUtil(gridTestingUtil, getDriver(), null, null, null, null);
    }

    private static String clearDatabaseButtonXpath = contentXpath + "/vaadin-button";

    @Test
    @Video
    public void clearDatabaseTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ADMINTOOLS_SUB_MENU);
        WebElement button = gridTestingUtil.findVisibleElementWithXpath(clearDatabaseButtonXpath);
        button.click();
        gridTestingUtil.checkNotificationText("Clearing database was successful");
    }

    @Test
    @Video
    public void clearDatabaseExceptionsTestThanSuccess() throws Exception {
//        MockitoAnnotations.openMocks(this);
//        MockitoAnnotations.openMocks(this);
        Mockito.doThrow(new ClassNotFoundException())
               .doThrow(new InvocationTargetException(new Throwable()))
               .doThrow(new InstantiationException())
               .doThrow(new IllegalAccessException())
               .doCallRealMethod()
        .when(spyAdminToolsService).clearAllDatabaseTable();
//        AdminToolsService originalAdminToolsService = BeanProvider.getBean(AdminToolsService.class);
//        BeanProvider.getBean(AdminToolsController.class).setAdminToolsService(adminToolsService);

        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        Thread.sleep(100);
        gridTestingUtil.navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ADMINTOOLS_SUB_MENU);

        WebElement button = gridTestingUtil.findVisibleElementWithXpath(clearDatabaseButtonXpath);
        button.click();
        gridTestingUtil.checkNotificationText("Clearing database failed for one or more table");
        button.click();
        gridTestingUtil.checkNotificationText("Clearing database failed for one or more table");
        button.click();
        gridTestingUtil.checkNotificationText("Clearing database failed for one or more table");
        button.click();
        gridTestingUtil.checkNotificationText("Clearing database failed for one or more table");
        button.click();
        gridTestingUtil.checkNotificationText("Clearing database was successful");
//        Mockito.reset(adminToolsService);
//        BeanProvider.getBean(AdminToolsController.class).setAdminToolsService(originalAdminToolsService);
    }


}
