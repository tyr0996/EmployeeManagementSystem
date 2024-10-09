package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import org.checkerframework.checker.units.qual.C;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;
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

    public static void createOrder() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);

        int originalOrderNumber = countVisibleGridDataRows(createOrderGridXpath);


        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_CREATE_SUBMENU);
        Thread.sleep(100);

        WebElement customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);
        selectElementByTextFromComboBox(customerComboBox, "Gálber Martin");
        int originalOrderElements = countVisibleGridDataRows(createOrderGridXpath);

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        Thread.sleep(100);

        findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, String> sameUser = new LinkedHashMap<>();
        sameUser.put("Customer", "Gálber Martin");
        sameUser.put("Supplier", "");
        orderElementCrudTestingUtil.createTest(sameUser, null, true);
        orderElementCrudTestingUtil.createTest(sameUser, null, true);
        orderElementCrudTestingUtil.createTest(sameUser, null, true);

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_CREATE_SUBMENU);
        Thread.sleep(100);

        customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);
        selectElementByTextFromComboBox(customerComboBox, "Gálber Martin");
        Thread.sleep(100);
        findVisibleElementWithXpath(createOrderGridXpath);
        assertEquals(originalOrderElements + 3, countVisibleGridDataRows(createOrderGridXpath)); //TODO valamiért ez mindig 0-ra jön ki éles futáskor.

        selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath);
        selectRandomFromComboBox(findVisibleElementWithXpath(currencyComboBoxXpath));
        selectRandomFromComboBox(findVisibleElementWithXpath(paymentMethodComboBoxXpath));

        findClickableElementWithXpath(orderCreateOrderButtonXpath).click();
        checkNotificationContainsTexts("Order saved:");

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        assertEquals(originalOrderNumber + 1, countVisibleGridDataRows(createOrderGridXpath));


        //TODO megcsinálni, hogy ha nincs kiválasztva, akkor legyen hibaüzenet hogy nem hozható létre.
    }
}
