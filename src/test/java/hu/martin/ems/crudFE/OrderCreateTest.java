package hu.martin.ems.crudFE;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.model.Employee;
import hu.martin.ems.model.Order;
import org.checkerframework.checker.units.qual.C;
import org.mockito.Mockito;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.mockito.ArgumentMatchers.any;
import static org.testng.AssertJUnit.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class OrderCreateTest extends BaseCrudTest {

    public static final String customerComboBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-form-layout[1]/vaadin-combo-box";
    public static final String currencyComboBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-form-layout[2]/vaadin-combo-box[1]";
    public static final String paymentMethodComboBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-form-layout[2]/vaadin-combo-box[2]";
    private static final String orderElementShowDeletedXpath = OrderElementCrudTest.showDeletedChecBoxXpath;
    private static final String orderElementGridXpath = OrderElementCrudTest.gridXpath;
    private static final String orderElementCreateButtonXpath = OrderElementCrudTest.createButtonXpath;
    private static final String orderXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid";
    public static final String orderCreateOrderButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-form-layout[2]/vaadin-button";

    public static final String previouslyOrderedCheckboxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";

    private static CrudTestingUtil crudTestingUtil;
    private static CrudTestingUtil orderElementCrudTestingUtil;

    public static final String createOrderGridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";

    private static final String mainMenu = UIXpaths.ORDERS_MENU;
    private static final String subMenu = UIXpaths.ORDER_CREATE_SUBMENU;

    @BeforeClass
    public void setup() {
        setupTest();
    }

    public static void setupTest(){
        crudTestingUtil = new CrudTestingUtil(driver, "Order", null, null, null);
        orderElementCrudTestingUtil = new CrudTestingUtil(driver, "OrderElement", orderElementShowDeletedXpath, orderElementGridXpath, orderElementCreateButtonXpath);
        GridTestingUtil.driver = driver;
    }

    @Test
    public void createOrderTest() throws InterruptedException {
        createOrder();
    }

    @Test
    public void customerNotSelectedShowPreviouslyGridIsEmptyTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        int originalRows = countVisibleGridDataRows(createOrderGridXpath);

        WebElement previously = findVisibleElementWithXpath(previouslyOrderedCheckboxXpath);
        setCheckboxStatus(previouslyOrderedCheckboxXpath, true);
        assertEquals(0, countVisibleGridDataRows(createOrderGridXpath));
        setCheckboxStatus(previouslyOrderedCheckboxXpath, false);
        assertEquals(originalRows, countVisibleGridDataRows(createOrderGridXpath));
    }

    public static void createOrder() throws InterruptedException {
        createOrder(null, true);
    }

    public static void createOrder(String notificationText, Boolean requiredSuccess) throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);

        int originalOrderNumber = countVisibleGridDataRows(createOrderGridXpath);

        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        WebElement customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);
        String customerName = selectRandomFromComboBox(customerComboBox);
        int originalOrderElements = countVisibleGridDataRows(createOrderGridXpath);

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        Thread.sleep(100);

        findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, String> sameUser = new LinkedHashMap<>();
        sameUser.put("Customer", customerName);
        sameUser.put("Supplier", "");
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);

        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);
        selectElementByTextFromComboBox(customerComboBox, customerName);
        //selectRandomFromComboBox(customerComboBox);
        Thread.sleep(200);
        findVisibleElementWithXpath(createOrderGridXpath);
        assertEquals(originalOrderElements + 3, countVisibleGridDataRows(createOrderGridXpath)); //TODO valamiért ez mindig 0-ra jön ki éles futáskor.

        selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 2);
        selectRandomFromComboBox(findVisibleElementWithXpath(currencyComboBoxXpath));
        selectRandomFromComboBox(findVisibleElementWithXpath(paymentMethodComboBoxXpath));

        findClickableElementWithXpath(orderCreateOrderButtonXpath).click();
        if(notificationText == null){
            checkNotificationContainsTexts("Order saved:");
        }
        else{
            checkNotificationText(notificationText);
        }

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        if(requiredSuccess){
            assertEquals(originalOrderNumber + 1, countVisibleGridDataRows(createOrderGridXpath));
        }
        else{
            assertEquals(originalOrderNumber, countVisibleGridDataRows(createOrderGridXpath));
        }


        //TODO megcsinálni, hogy ha nincs kiválasztva, akkor legyen hibaüzenet hogy nem hozható létre.
    }

    @Test
    public void createFailedTest() throws JsonProcessingException, InterruptedException {

        Mockito.doThrow(JsonProcessingException.class).when(spyOrderApiClient).writeValueAsString(any(Order.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(10);
        createOrder(Order.class.getSimpleName() + " saving failed", false);
    }
}
