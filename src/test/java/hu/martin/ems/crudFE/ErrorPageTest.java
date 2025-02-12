package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.GridTestingUtil;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ErrorPageTest extends BaseCrudTest {

    @BeforeClass
    public void setup() {
        GridTestingUtil.driver = driver;
    }

    @Test
    public void pageLoadFailedIllegalAccessException() throws IllegalAccessException, InterruptedException {
        Mockito.doThrow(IllegalAccessException.class).when(spyComponentManager).setEditObjectFieldToNull(any());
        TestingUtils.loginWith(driver, port, "robi", "robi");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ADMINTOOLS_SUB_MENU);
        checkNoPermissionPage();
    }

    @Test
    public void pageLoadFailedNotFoundException() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        Thread.sleep(100);
        driver.get("http://localhost:" + port + "/notExistingPageURL");
        Thread.sleep(100);
        checkNotFoundPage();
    }
}
