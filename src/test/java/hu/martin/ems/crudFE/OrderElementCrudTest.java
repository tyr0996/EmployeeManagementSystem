package hu.martin.ems.crudFE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.notification.Notification;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.EmsResponse;
import org.mockito.Mockito;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class OrderElementCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    public static final String showDeletedChecBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";
    public static final String gridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";
    public static final String createButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button";

    private static final String mainMenu = UIXpaths.ORDERS_MENU;
    private static final String subMenu = UIXpaths.ORDER_ELEMENT_SUBMENU;

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "OrderElement", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    //@Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/products.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void orderElementCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
    }

    @Test
    public void orderElementReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        List<String[]> allFullLines = crudTestingUtil.getAllDataLinesFull();
        List<String[]> allNonOrderedLines = crudTestingUtil.getAllDataLinesWithEmpty();
        while(allFullLines.size() == 0){
            OrderCreateTest.createOrder();
            navigateMenu(mainMenu, subMenu);
            allFullLines = crudTestingUtil.getAllDataLinesFull();
        }

        while(allNonOrderedLines.size() == 0) {
            orderElementCreateTest();
            navigateMenu(mainMenu, subMenu);
            allNonOrderedLines = crudTestingUtil.getAllDataLinesWithEmpty();
        }

        Collections.shuffle(allFullLines);
        Collections.shuffle(allNonOrderedLines);

        crudTestingUtil.readTest(allFullLines.get(0), null, false, null);
        crudTestingUtil.readTest(allNonOrderedLines.get(0), null, false, null);
    }

    @Test
    public void orderElementDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }


    @Test
    //@Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/products.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void orderElementUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
    }

    @Test
    public void orderElementRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
    public void orderElementPermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
    }

    @Test
    public void extraFilterInvalidValue() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }

    @Test
    public void createFailedTest() throws JsonProcessingException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        crudTestingUtil.createFailedTest(port, spyOrderElementApiClient, mainMenu, subMenu);
    }

    @Test
    public void findAllOrderElementWithDeletedFailed() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyOrderElementApiClient).findAllWithDeleted();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        checkNotificationText("Error happened while getting order elements");
        checkNoMoreNotificationsVisible();
        assertEquals(0, countVisibleGridDataRows(gridXpath));
        assertEquals(0, countHiddenGridDataRows(gridXpath, showDeletedChecBoxXpath));
    }

    @Test
    public void findAllSuppliersFailed() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spySupplierApiClient).findAll();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Supplier", "Error happened while getting suppliers");
        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    public void findAllCustomersFailed() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyCustomerApiClient).findAll();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Customer", "Error happened while getting customers");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    public void findAllProductsFailed() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyProductApiClient).findAll();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Product", "Error happened while getting products");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }
}