package hu.martin.ems.base;

import hu.martin.ems.UITests.ElementLocation;
import lombok.Setter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.LinkedHashMap;
import java.util.List;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.junit.jupiter.api.Assertions.*;

public class CrudTestingUtil {

    public final WebDriver driver;

    private final String showDeletedCheckBoxXpath;
    private final String gridXpath;
    private final String createButtonXpath;
    private LinkedHashMap<String, String> createOrUpdateDialogPartXpaths;
    private final String className;
    private final String showOnlyDeletableCheckboxXpath;

    public CrudTestingUtil(WebDriver driver,
                           String className,
                           String showDeletedCheckBoxXpath,
                           String gridXpath,
                           String createButtonXpath){
        this(driver, className, showDeletedCheckBoxXpath, gridXpath, createButtonXpath, null);
    }

    public CrudTestingUtil(WebDriver driver,
                           String className,
                           String showDeletedCheckBoxXpath,
                           String gridXpath,
                           String createButtonXpath,
                           String showOnlyDeletableCheckboxXpath){
        this.driver = driver;
        this.className = className;
        this.createButtonXpath = createButtonXpath;
        GridTestingUtil.driver = driver;

        this.showDeletedCheckBoxXpath = showDeletedCheckBoxXpath;
        this.showOnlyDeletableCheckboxXpath = showOnlyDeletableCheckboxXpath;
        this.gridXpath = gridXpath;
        this.createOrUpdateDialogPartXpaths = new LinkedHashMap<>();
    }

    public void updateTest() throws InterruptedException {
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
        }
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null){
            createTest();
            rowLocation = new ElementLocation(1, 0);
        }
        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
        }
        goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());
        WebElement modifyButton = getModifyButton(gridXpath, rowLocation.getRowIndex());
        Thread.sleep(200);
        modifyButton.click();

        WebElement dialog = findVisibleElementWithXpath("//*[@id=\"overlay\"]");
        WebElement saveEmployeeButton = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        List<WebElement> fields = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout").findElements(By.xpath("./*"));

        String previouslyGeneratedPassword = null;
         //TODO megoldani, hogy ne minden mezőt frissítsen
        for(int i = 0; i < fields.size(); i++){
            if(previouslyGeneratedPassword == null) {
                previouslyGeneratedPassword = fillElementWithRandom(fields.get(i), showOnlyDeletableCheckboxXpath != null, null);
            }
            else{
                fillElementWithRandom(fields.get(i), showOnlyDeletableCheckboxXpath != null, previouslyGeneratedPassword);
            }
        }

        saveEmployeeButton.click();

        checkNotificationContainsTexts(className + " updated: ");
        Thread.sleep(500);
    }

    public void createTest() throws InterruptedException {
        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        int originalVisible = countVisibleGridDataRows(gridXpath);
        int originalInvisible = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int origivalVisibleOnlyDeletable = 0;
        int originalInvisibleOnlyDeletable = 0;

        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            origivalVisibleOnlyDeletable = countVisibleGridDataRows(gridXpath);
            originalInvisibleOnlyDeletable = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }

        WebElement createButton = findClickableElementWithXpath(createButtonXpath);
        createButton.click();

        fillCreateOrUpdateForm();

        WebElement saveButton = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        saveButton.click();
        checkNotificationContainsTexts(className + " saved: ");
        findVisibleElementWithXpath(gridXpath);
        assertEquals(originalVisible + 1, countVisibleGridDataRows(gridXpath));
        assertEquals(originalInvisible, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            assertEquals(origivalVisibleOnlyDeletable + 1, countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
            assertEquals(originalInvisibleOnlyDeletable, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
    }

    private void fillCreateOrUpdateForm() throws InterruptedException {
        List<WebElement> fields = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout").findElements(By.xpath("./*"));
        String previousPasswordFieldValue = null;
        for(int i = 0; i < fields.size(); i++){
            if(previousPasswordFieldValue == null){
                previousPasswordFieldValue = fillElementWithRandom(fields.get(i), showOnlyDeletableCheckboxXpath != null, null);
            }
            else{
                fillElementWithRandom(fields.get(i), showOnlyDeletableCheckboxXpath != null,  previousPasswordFieldValue);
            }
        }
    }

    public void deleteTest() throws InterruptedException {
        findVisibleElementWithXpath(gridXpath);
        int originalVisible = countVisibleGridDataRows(gridXpath);
        int originalInvisible = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalVisibleDeletable = originalVisible;
        int originalInvisibleDeletable = originalInvisible;

        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            originalVisibleDeletable = countVisibleGridDataRows(gridXpath);
            originalInvisibleDeletable = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }

        if(originalVisibleDeletable == 0){
            createTest();
            originalVisible++;
            originalVisibleDeletable++;
        }

        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
        }
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null){
            createTest();
            rowLocation = new ElementLocation(1, 0);
        }
        goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());

        setCheckboxStatus(showDeletedCheckBoxXpath, false);


        WebElement showDeletedCheckBox = findVisibleElementWithXpath(showDeletedCheckBoxXpath);
        String[] deletedData = getDataFromRowLocation(gridXpath, rowLocation);

        Thread.sleep(200);
        WebElement deleteButton = getDeleteButton(gridXpath, rowLocation.getRowIndex());
        deleteButton.click();
        checkNotificationContainsTexts(className + " deleted: ");
        if(this.showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        Thread.sleep(2000);
        findVisibleElementWithXpath(gridXpath);

        assertEquals(0, countElementResultsFromGridWithFilter(gridXpath, deletedData));
        findVisibleElementWithXpath(gridXpath);
        setCheckboxStatus(showDeletedCheckBoxXpath, false);
        Thread.sleep(2000);
        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        assertEquals(originalVisible - 1, countVisibleGridDataRows(gridXpath));
        assertEquals(originalInvisible + 1, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        setCheckboxStatus(showDeletedCheckBoxXpath, true);
        assertEquals(originalInvisible + originalVisible, countVisibleGridDataRows(gridXpath));
        assertEquals(1, countElementResultsFromGridWithFilter(gridXpath, deletedData));
        setCheckboxStatus(showDeletedCheckBoxXpath, false);

        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            assertEquals(originalVisibleDeletable - 1, countVisibleGridDataRows(gridXpath));
            assertEquals(originalInvisibleDeletable + 1, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
            setCheckboxStatus(showDeletedCheckBoxXpath, true);
            assertEquals(originalInvisibleDeletable + originalVisibleDeletable, countVisibleGridDataRows(gridXpath));
            assertEquals(1, countElementResultsFromGridWithFilter(gridXpath, deletedData));
            setCheckboxStatus(showDeletedCheckBoxXpath, false);
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }

        //TODO meg kellene nézni a táblát, hogy tényleg nem találjuk-e meg.
    }

    public void permanentlyDeleteTest() throws InterruptedException {
        int originalVisible = countVisibleGridDataRows(gridXpath);
        int originalInvisible = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalVisibleDeletable = originalVisible;
        int originalInvisibleDeletable = originalInvisible;
        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            originalVisibleDeletable = countVisibleGridDataRows(gridXpath);
            originalInvisibleDeletable = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }


        if(originalVisibleDeletable == 0 && originalInvisibleDeletable == 0){
            createTest();
            Thread.sleep(500);
            originalVisible++;
        }
        if(originalInvisibleDeletable == 0){
            deleteTest();
            Thread.sleep(500);
            originalVisible--;
            originalVisibleDeletable--;
            originalInvisible++;
            originalInvisibleDeletable++;
        }
        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
        }

        ElementLocation el = getRandomLocationDeletedStatusFromGrid(gridXpath, showDeletedCheckBoxXpath);
        WebElement showDeleted = findClickableElementWithXpath(showDeletedCheckBoxXpath);
        setCheckboxStatus(showDeletedCheckBoxXpath, true);
        Thread.sleep(500);
        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());
        String[] selectedData = getDataFromRowLocation(gridXpath, el);
        assertEquals(1, countElementResultsFromGridWithFilter(gridXpath, selectedData));
        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());
        WebElement pDeleteButton = getPermanentlyDeleteButton(gridXpath, el.getRowIndex());
        pDeleteButton.click();

        if(this.showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }

        checkNotificationContainsTexts(className + " permanently deleted: ");
        Thread.sleep(500);
        showDeleted = findClickableElementWithXpathWithWaiting(showDeletedCheckBoxXpath);
        setCheckboxStatus(showDeletedCheckBoxXpath, false);
        assertEquals(0, countElementResultsFromGridWithFilter(gridXpath, selectedData));
        setCheckboxStatus(showDeletedCheckBoxXpath, true);
        assertEquals(0, countElementResultsFromGridWithFilter(gridXpath, selectedData));
        setCheckboxStatus(showDeletedCheckBoxXpath, false);
        Thread.sleep(500);

        assertEquals(originalInvisible - 1, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        assertEquals(originalVisible, countVisibleGridDataRows(gridXpath));

        if(this.showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            setCheckboxStatus(showDeletedCheckBoxXpath, false);
            assertEquals(originalInvisibleDeletable - 1, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
            assertEquals(originalVisibleDeletable, countVisibleGridDataRows(gridXpath));
            assertEquals(0, countElementResultsFromGridWithFilter(gridXpath, selectedData));
            setCheckboxStatus(showDeletedCheckBoxXpath, true);
            assertEquals(0, countElementResultsFromGridWithFilter(gridXpath, selectedData));
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
            setCheckboxStatus(showDeletedCheckBoxXpath, false);
        }
    }

    public void readTest(){
//        lookingForElementInGrid(grid, createdEmployee.getFirstName(),
//                createdEmployee.getLastName(),
//                createdEmployee.getRoleName(),
//                createdEmployee.getSalary(),
//                "skip");
    }

    public void restoreTest() throws InterruptedException {
        int originalVisibleRows = countVisibleGridDataRows(gridXpath);
        int originalInvisibleRows = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        WebElement showDeletedButton = findClickableElementWithXpath(showDeletedCheckBoxXpath);

        if(originalInvisibleRows == 0){
            createTest();
            deleteTest();
            originalInvisibleRows++;
        }

        setCheckboxStatus(showDeletedCheckBoxXpath, false);
        ElementLocation el = getRandomLocationDeletedStatusFromGrid(gridXpath, showDeletedCheckBoxXpath);
        //WebElement showDeleted = findClickableElementWithXpath(showDeletedCheckBoxXpath);
        setCheckboxStatus(showDeletedCheckBoxXpath, true);
        Thread.sleep(500);
        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());
        String[] originalData = getDataFromRowLocation(gridXpath, el);
        //assertNotNull(lookingForElementInGrid(gridXpath, originalData));
        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());

        WebElement restoreButton = getRestoreButton(gridXpath, el.getRowIndex());
        restoreButton.click();
        checkNotificationContainsTexts(className + " restored: ");
        findClickableElementWithXpath(showDeletedCheckBoxXpath).click();
        assertEquals(1, countElementResultsFromGridWithFilter(gridXpath, originalData));

        setCheckboxStatus(showDeletedCheckBoxXpath, false);
        int newVisibleRows = countVisibleGridDataRows(gridXpath);
        int newInvisibleRows = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        assertEquals(originalVisibleRows + 1, newVisibleRows);
        assertEquals(originalInvisibleRows - 1, newInvisibleRows);
    }
}
