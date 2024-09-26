package hu.martin.ems.base;

import hu.martin.ems.UITests.ElementLocation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import static hu.martin.ems.base.GridTestingUtil.*;
import static hu.martin.ems.base.RandomGenerator.generateRandomOnlyLetterString;
import static org.junit.jupiter.api.Assertions.*;

public class CrudTestingUtil {

    public final WebDriver driver;

    private final String showDeletedCheckBoxXpath;
    private final String gridXpath;
    private final String createButtonXpath;
    private LinkedHashMap<String, String> createOrUpdateDialogPartXpaths;
    private final String className;

    public CrudTestingUtil(WebDriver driver,
                           String className,
                           String showDeletedCheckBoxXpath,
                           String gridXpath,
                           String createButtonXpath){
        this.driver = driver;
        this.className = className;
        this.createButtonXpath = createButtonXpath;
        GridTestingUtil.driver = driver;

        this.showDeletedCheckBoxXpath = showDeletedCheckBoxXpath;
        this.gridXpath = gridXpath;
        this.createOrUpdateDialogPartXpaths = new LinkedHashMap<>();
    }

    public void updateTest() throws InterruptedException {
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath, showDeletedCheckBoxXpath);
        if(rowLocation == null){
            createTest();
            rowLocation = new ElementLocation(1, 0);
        }
        goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());
        WebElement modifyButton = getModifyButton(gridXpath, rowLocation.getRowIndex());
        Thread.sleep(200);
        modifyButton.click();

        WebElement dialog = findVisibleElementWithXpath("//*[@id=\"overlay\"]");
        WebElement saveEmployeeButton = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        List<WebElement> fields = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout").findElements(By.xpath("./*"));
        Random rnd = new Random();
        Boolean modifiedMinimumOneData = false;
        while (!modifiedMinimumOneData){
            for(int i = 0; i < fields.size(); i++){
                if(rnd.nextBoolean()){
                    printToConsole(fields.get(i));
                    fillElementWithRandom(fields.get(i));
                    System.out.println("*********************************************");
                    modifiedMinimumOneData = true;
                }
            }
        }

        saveEmployeeButton.click();

        checkNotificationContainsTexts(className + " saved: ");
        Thread.sleep(500);
    }

    public void createTest() throws InterruptedException {
        int originalVisible = countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalInvisible = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);

        WebElement createButton = findClickableElementWithXpath(createButtonXpath);
        createButton.click();


        List<WebElement> fields = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout").findElements(By.xpath("./*"));
        List<String> generatedData = new ArrayList<>();
        for(int i = 0; i < fields.size(); i++){
            fillElementWithRandom(fields.get(i));
        }

        WebElement saveEmployeeButton = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        saveEmployeeButton.click();
        checkNotificationContainsTexts(className + " saved: ");
        //assertNotNull(lookingForElementInGrid(gridXpath, firstName, lastName, "Martin", "20500"));
        Thread.sleep(2000);
        assertEquals(originalVisible + 1, countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        assertEquals(originalInvisible, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }

    public void deleteTest() throws InterruptedException {
        int originalVisible = countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalInvisible = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);

        if(originalVisible == 0){
            createTest();
            originalVisible++;
        }
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath, showDeletedCheckBoxXpath);
        if(rowLocation == null){
            createTest();
            rowLocation = new ElementLocation(1, 0);
        }
        goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());


        WebElement showDeletedCheckBox = findVisibleElementWithXpath(showDeletedCheckBoxXpath);
        String[] deletedData = getDataFromRowLocation(gridXpath, rowLocation);

        Thread.sleep(200);
        WebElement deleteButton = getDeleteButton(gridXpath, rowLocation.getRowIndex());
        deleteButton.click();
        checkNotificationContainsTexts(className + " deleted: ");
        Thread.sleep(2000);

        assertNull(lookingForElementInGrid(gridXpath, deletedData));
        assertEquals(originalVisible - 1, countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        assertEquals(originalInvisible + 1, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        showDeletedCheckBox.click();
        assertEquals(originalInvisible + originalVisible, countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        assertNotNull(lookingForElementInGrid(gridXpath, deletedData));
        showDeletedCheckBox.click();

        //TODO meg kellene nézni a táblát, hogy tényleg nem találjuk-e meg.
    }

    public void permanentlyDeleteTest() throws InterruptedException {
        int originalVisible = countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalInvisible = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        if(originalVisible == 0 && originalInvisible == 0){
            createTest();
            Thread.sleep(500);
            originalVisible++;
        }
        if(originalInvisible == 0){
            deleteTest();
            Thread.sleep(500);
            originalVisible--;
            originalInvisible++;
        }
        ElementLocation el = getRandomLocationDeletedStatusFromGrid(gridXpath, showDeletedCheckBoxXpath);
        WebElement showDeleted = findClickableElementWithXpath(showDeletedCheckBoxXpath);
        showDeleted.click();
        Thread.sleep(500);
        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());
        String[] selectedData = getDataFromRowLocation(gridXpath, el);
        assertNotNull(lookingForElementInGrid(gridXpath, selectedData));
        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());
        WebElement pDeleteButton = getPermanentlyDeleteButton(gridXpath, el.getRowIndex());
        pDeleteButton.click();
        checkNotificationContainsTexts(className + " permanently deleted: ");
        Thread.sleep(500);
        showDeleted = findClickableElementWithXpathWithWaiting(showDeletedCheckBoxXpath);
        showDeleted.click();
        assertNull(lookingForElementInGrid(gridXpath, selectedData));
        Thread.sleep(500);

        assertEquals(originalInvisible - 1, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }

    public void readTest(){
//        lookingForElementInGrid(grid, createdEmployee.getFirstName(),
//                createdEmployee.getLastName(),
//                createdEmployee.getRoleName(),
//                createdEmployee.getSalary(),
//                "skip");
    }

    public void restoreTest() throws InterruptedException {
        int originalVisibleRows = countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalInvisibleRows = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        WebElement showDeletedButton = findClickableElementWithXpath(showDeletedCheckBoxXpath);

        if(originalInvisibleRows == 0){
            createTest();
            deleteTest();
            originalInvisibleRows++;
        }

        if(getCheckboxStatus(showDeletedCheckBoxXpath)){
            showDeletedButton.click();
        }
        ElementLocation el = getRandomLocationDeletedStatusFromGrid(gridXpath, showDeletedCheckBoxXpath);
        //WebElement showDeleted = findClickableElementWithXpath(showDeletedCheckBoxXpath);
        setShowDeletedCheckboxStatus(showDeletedCheckBoxXpath, true);
        Thread.sleep(500);
        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());
        String[] originalData = getDataFromRowLocation(gridXpath, el);
        //assertNotNull(lookingForElementInGrid(gridXpath, originalData));
        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());

        WebElement restoreButton = getRestoreButton(gridXpath, el.getRowIndex());
        restoreButton.click();
        checkNotificationContainsTexts(className + " restored: ");
        findClickableElementWithXpath(showDeletedCheckBoxXpath).click();
        assertNotNull(lookingForElementInGrid(gridXpath, originalData));

        int newVisibleRows = countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int newInvisibleRows = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        assertEquals(originalVisibleRows + 1, newVisibleRows);
        assertEquals(originalInvisibleRows - 1, newInvisibleRows);
    }
}
