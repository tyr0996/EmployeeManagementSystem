package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testng.Assert.assertNotEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Listeners(UniversalVideoListener.class)
public class OrderElementCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    public static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    public static final String gridXpath = contentXpath + "/vaadin-grid";
    public static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-button";

    private static final String mainMenu = UIXpaths.ORDERS_MENU;
    private static final String subMenu = UIXpaths.ORDER_ELEMENT_SUBMENU;

    private GridTestingUtil gridTestingUtil;

    

    private OrderCreateTest orderCreateTest;

    @BeforeClass
    public void setup() {
        gridTestingUtil = new GridTestingUtil(getDriver());
        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, getDriver(), "OrderElement", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(getDriver(), Duration.ofMillis(5000));
        orderCreateTest = new OrderCreateTest();
    }

    @Test
    @Video
    public void orderElementCreateTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
    }

    @Test
    @Video
    public void orderElementReadTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        List<String[]> allFullLines = crudTestingUtil.getAllDataLinesFull();
        List<String[]> allNonOrderedLines = crudTestingUtil.getAllDataLinesWithEmpty();
        while(allFullLines.size() == 0){
            orderCreateTest.createOrder();
            gridTestingUtil.navigateMenu(mainMenu, subMenu);
            allFullLines = crudTestingUtil.getAllDataLinesFull();
        }

        while(allNonOrderedLines.size() == 0) {
            orderElementCreateTest();
            gridTestingUtil.navigateMenu(mainMenu, subMenu);
            allNonOrderedLines = crudTestingUtil.getAllDataLinesWithEmpty();
        }

        Collections.shuffle(allFullLines);
        Collections.shuffle(allNonOrderedLines);

        crudTestingUtil.readTest(allFullLines.get(0), null, false, null);
        crudTestingUtil.readTest(allNonOrderedLines.get(0), null, false, null);
    }

    @Test
    @Video
    public void orderElementDeleteTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }


    @Test
    @Video
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
    }

    @Test
    @Video
    //@Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/products.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void orderElementUpdateTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
    }

    @Test
    @Video
    public void orderElementRestoreTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
    @Video
    public void orderElementPermanentlyDeleteTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
    }

    //@Test
    public void extraFilterInvalidValue() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }


    @Test
    @Video
    public void findAllOrderElementWithDeletedFailedButWithoutIsSuccess() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
//        Mockito.doReturn(null).when(spyOrderElementService).findAll(true); //ApiClientben findAllWithDeleted();
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        int elements =  gridTestingUtil.countVisibleGridDataRows(gridXpath);
        assertNotEquals(0, elements);
        gridTestingUtil.checkNoMoreNotificationsVisible();
        gridTestingUtil.findVisibleElementWithXpath(showDeletedCheckBoxXpath).click();
        Thread.sleep(100);
        gridTestingUtil.checkNotificationText("Getting order elements failed");
        assertNotEquals(0, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath)); //Ez azért hal meg, mert nincs törölt elem a sima futáskor. Viszont ha az egészet futtatom, akkor viszont van törölt elem.
        assertEquals(elements, gridTestingUtil.countVisibleGridDataRows(gridXpath));
    }

    @Test
    @Video
    public void findAllOrderElementWithAndWithoutFailed() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableAfter(getClass(), spyDataSource, 2);
//        Mockito.doReturn(null).when(spyOrderElementService).findAll(true); //ApiClientben findAllWithDeleted();
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.checkNotificationText("Error happened while getting order elements");
        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(gridXpath));
        assertEquals(0, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath, "Getting order elements failed"));
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void findAllSuppliersFailed() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spySupplierService).findAll(false);//Controllerben alapértelmezett
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 5);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Supplier", "Error happened while getting suppliers");
        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    @Video
    public void findAllCustomersFailed() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
//        Mockito.doReturn(null).when(spyCustomerService).findAll(false); //Controllerben alapértelmezett
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Customer", "Error happened while getting customers");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    @Video
    public void findAllProductsFailed() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyProductService).findAll(false); //Controllerben alapértelmezett
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Product", "Error happened while getting products");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    @Video
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 0);
    }

    @Test
    @Video
    public void databaseUnavailableWhenModifying() throws SQLException, InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenUpdateEntity(spyDataSource, null, null, 0);
    }
}