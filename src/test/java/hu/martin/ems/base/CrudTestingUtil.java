package hu.martin.ems.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.UITests.ElementLocation;
import org.openqa.selenium.*;

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
        updateTest(null, null, true);
    }

    public void updateTest(LinkedHashMap<String, String> withData, String updateNotificationText, Boolean requiredSuccess) throws InterruptedException {
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

        String[] originalData = getDataFromRowLocation(gridXpath, rowLocation);

        goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());
        WebElement modifyButton = getModifyButton(gridXpath, rowLocation.getRowIndex());
        Thread.sleep(200);
        modifyButton.click();

        WebElement dialog = findVisibleElementWithXpath("//*[@id=\"overlay\"]");
        WebElement saveEmployeeButton = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        fillCreateOrUpdateForm(withData);

        saveEmployeeButton.click();

        checkNotificationContainsTexts(updateNotificationText == null ? className + " updated: " : updateNotificationText);
        assertEquals(requiredSuccess ? 0 : 1, countElementResultsFromGridWithFilter(gridXpath, originalData));

        Thread.sleep(500);
    }

    public void createTest() throws InterruptedException {
        createTest(null, null, true);
    }

    public void createTest(LinkedHashMap<String, String> withData, String saveNotificationText, Boolean requiredSuccess) throws InterruptedException {
        findVisibleElementWithXpath(gridXpath);
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

        fillCreateOrUpdateForm(withData);

        WebElement saveButton = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", saveButton);

        checkNotificationContainsTexts(saveNotificationText == null ? className + " saved: " : saveNotificationText);
        findVisibleElementWithXpath(gridXpath);
        assertEquals(requiredSuccess ? originalVisible + 1 : originalVisible, countVisibleGridDataRows(gridXpath));
        assertEquals(originalInvisible, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            assertEquals(requiredSuccess ? origivalVisibleOnlyDeletable + 1 : originalVisible, countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
            assertEquals(originalInvisibleOnlyDeletable, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
    }

    /**
     * Fill the create or update form.
     * @param withData: The key value is the label of the field. It will fill the element with the value, if
     *                the key value is the label of the field
     * @throws InterruptedException
     */
    public void fillCreateOrUpdateForm(LinkedHashMap<String, String> withData) throws InterruptedException {
        List<WebElement> fields = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout").findElements(By.xpath("./*"));
        if(withData == null){
            withData = new LinkedHashMap<>();
        }
        String previousPasswordFieldValue = null;
        for(int i = 0; i < fields.size(); i++){
            String fieldLabel = "";
            try{
                WebElement label = fields.get(i).findElement(By.tagName("label"));
                fieldLabel = label.getText();
            }
            catch (NoSuchElementException ignore) {}

            if(previousPasswordFieldValue == null){
                previousPasswordFieldValue = fillElementWith(fields.get(i), showOnlyDeletableCheckboxXpath != null, null, withData.get(fieldLabel));
            }
            else{
                fillElementWith(fields.get(i), showOnlyDeletableCheckboxXpath != null,  previousPasswordFieldValue, withData.get(fieldLabel));
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


        setCheckboxStatus(showDeletedCheckBoxXpath, true);

        String[] selectedData = getRandomDataDeletedStatusFromGrid(gridXpath, showDeletedCheckBoxXpath);
        //assertEquals(1, countElementResultsFromGridWithFilter(gridXpath, deletedData));
        applyFilter(gridXpath, selectedData);
        ElementLocation el = new ElementLocation(1, 0);

//        ElementLocation el = getRandomLocationDeletedStatusFromGrid(gridXpath, showDeletedCheckBoxXpath);
//        WebElement showDeleted = findClickableElementWithXpath(showDeletedCheckBoxXpath);
//        setCheckboxStatus(showDeletedCheckBoxXpath, true);
//        Thread.sleep(500);
//        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());
//        String[] selectedData = getDataFromRowLocation(gridXpath, el);
//        assertEquals(1, countElementResultsFromGridWithFilter(gridXpath, selectedData));
//        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());
        WebElement pDeleteButton = getPermanentlyDeleteButton(gridXpath, el.getRowIndex());
        pDeleteButton.click();

        if(this.showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }

        checkNotificationContainsTexts(className + " permanently deleted: ");
        Thread.sleep(500);
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

    public void readTest() throws InterruptedException {
        ElementLocation randomLocation = getRandomLocationFromGrid(gridXpath);
        readTest(getDataFromRowLocation(gridXpath, randomLocation), null, false, null); //TODO
    }

    public void readTest(String[] data, String extraDataFilter, Boolean withInDeleted, NotificationCheck notificationCheck) throws InterruptedException {
        if(data == null || data.length == 0){
            findVisibleElementWithXpath(gridXpath);
            ElementLocation randomLocation = getRandomLocationFromGrid(gridXpath);
            data = getDataFromRowLocation(gridXpath, randomLocation);
        }
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

        Boolean originalDeleted = getCheckboxStatus(showDeletedCheckBoxXpath);

        if(withInDeleted){
            setCheckboxStatus(showDeletedCheckBoxXpath, true);
        }
        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        if(extraDataFilter != null){
            setExtraDataFilterValue(gridXpath, extraDataFilter, notificationCheck);
        }
        int filteredResultCount = countElementResultsFromGridWithFilter(gridXpath, data);
        if(filteredResultCount != 1){
            System.out.println("kiscica");
        }
        assertEquals(1, filteredResultCount); //TODO Ha van olyan, hogy showOnlyDeletable, akkor ide más érték is jöhet!
        if(extraDataFilter != null){
            clearExtraDataFilter(gridXpath);
        }
        setCheckboxStatus(gridXpath, originalDeleted);
    }

    public void restoreTest() throws InterruptedException {
        int originalVisibleRows = countVisibleGridDataRows(gridXpath);
        int originalInvisibleRows = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        WebElement showDeletedButton = findClickableElementWithXpath(showDeletedCheckBoxXpath);

        if(createButtonXpath != null && createButtonXpath != ""){
            if(originalInvisibleRows == 0) {
                createTest();
                deleteTest();
                originalInvisibleRows++;
            }
        }

        setCheckboxStatus(showDeletedCheckBoxXpath, true);

        String[] deletedData = getRandomDataDeletedStatusFromGrid(gridXpath, showDeletedCheckBoxXpath);
        //assertEquals(1, countElementResultsFromGridWithFilter(gridXpath, deletedData));
        applyFilter(gridXpath, deletedData);
        ElementLocation deleted = new ElementLocation(1, 0);

        //ElementLocation el = getRandomLocationDeletedStatusFromGrid(gridXpath, showDeletedCheckBoxXpath);
        //WebElement showDeleted = findClickableElementWithXpath(showDeletedCheckBoxXpath);
//        setCheckboxStatus(showDeletedCheckBoxXpath, true);
//        Thread.sleep(500);
//        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());
//        String[] originalData = getDataFromRowLocation(gridXpath, el);
//        //assertNotNull(lookingForElementInGrid(gridXpath, originalData));
//        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());

        WebElement restoreButton = getRestoreButton(gridXpath, deleted.getRowIndex());
        restoreButton.click();
        checkNotificationContainsTexts(className + " restored: ");
        resetFilter(gridXpath);
        setCheckboxStatus(showDeletedCheckBoxXpath, false);
        assertEquals(1, countElementResultsFromGridWithFilter(gridXpath, deletedData));

        int newVisibleRows = countVisibleGridDataRows(gridXpath);
        int newInvisibleRows = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        assertEquals(originalVisibleRows + 1, newVisibleRows);
        assertEquals(originalInvisibleRows - 1, newInvisibleRows);
    }
}
