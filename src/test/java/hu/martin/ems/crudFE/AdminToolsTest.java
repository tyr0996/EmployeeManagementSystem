package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AdminToolsTest extends BaseCrudTest {
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
    }
}
