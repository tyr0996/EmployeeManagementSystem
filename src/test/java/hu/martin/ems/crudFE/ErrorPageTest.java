package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.GridTestingUtil;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Listeners(UniversalVideoListener.class)
public class ErrorPageTest extends BaseCrudTest {

    private GridTestingUtil gridTestingUtil;

    

    @BeforeClass
    public void setup() {
        gridTestingUtil = new GridTestingUtil(getDriver());
    }

    @Test
    @Video
    public void pageLoadFailedIllegalAccessException() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "robi", "robi");
        gridTestingUtil.navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ADMINTOOLS_SUB_MENU);
        gridTestingUtil.checkNoPermissionPage();
    }

    @Test
    @Video
    public void pageLoadFailedNotFoundException() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        Thread.sleep(100);
        getDriver().get("http://localhost:" + port + "/notExistingPageURL");
        Thread.sleep(100);
        gridTestingUtil.checkNotFoundPage();
    }
}
