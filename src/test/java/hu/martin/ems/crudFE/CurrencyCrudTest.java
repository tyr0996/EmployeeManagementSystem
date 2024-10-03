package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.RandomGenerator;
import hu.martin.ems.core.date.Date;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CurrencyCrudTest extends BaseCrudTest {

    private static final String datePickerXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-date-picker/input";

    private static final String gridXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid";
    private static CrudTestingUtil crudTestingUtil;

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Product", null, gridXPath, null);
    }

    @Test
    public void selectDateRetroactively_NotSavedDate() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CURRENCY_SUBMENU);
        findVisibleElementWithXpath(gridXPath);
        LocalDate today = LocalDate.now();
        String todayString = today.format(DateTimeFormatter.ofPattern("yyyy. MM. dd"));
        assertEquals(todayString, findVisibleElementWithXpath(datePickerXPath).getAttribute("value"));
        Thread.sleep(1000);
        assertEquals(162, countVisibleGridDataRows(gridXPath));
        WebElement datePicker = findVisibleElementWithXpath(datePickerXPath);
        datePicker.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        datePicker.sendKeys(RandomGenerator.generateRandomDate());
        datePicker.sendKeys(Keys.ENTER);
        Thread.sleep(100);
        checkNotificationText("Exchange rates cannot be downloaded retroactively!");
        assertEquals(todayString, findVisibleElementWithXpath(datePickerXPath).getAttribute("value"));
    }

    @Test
    public void tryToEnterAllPossibleGoodDateFormats() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CURRENCY_SUBMENU);
        findVisibleElementWithXpath(gridXPath);
        LocalDate today = LocalDate.now();
        String todayString = today.format(DateTimeFormatter.ofPattern("yyyy. MM. dd"));
        assertEquals(todayString, findVisibleElementWithXpath(datePickerXPath).getAttribute("value"));
        Thread.sleep(1000);
        assertEquals(162, countVisibleGridDataRows(gridXPath));
        WebElement datePicker = findVisibleElementWithXpath(datePickerXPath);
        List<String> generatedDates = Date.generateAllFormatDate(today);
        for(String generatedTodayDate : generatedDates){
            datePicker.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            datePicker.sendKeys(generatedTodayDate);
            datePicker.sendKeys(Keys.ENTER);
            assertEquals(todayString, datePicker.getAttribute("value"));
        }
    }

    @Test
    public void checkEuroExistsInGrid() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CURRENCY_SUBMENU);
        findVisibleElementWithXpath(gridXPath);

        assertEquals(1, countElementResultsFromGridWithFilter(gridXPath,  "EUR", ""));
    }

}
