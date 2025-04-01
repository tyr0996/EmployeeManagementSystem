package hu.martin.ems.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.PaginationData;
import hu.martin.ems.core.model.BaseEntity;
import hu.martin.ems.vaadin.api.EmsApiClient;
import hu.martin.ems.vaadin.api.UserApiClient;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.*;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

public class CrudTestingUtil {

    public final WebDriver driver;

    private final String showDeletedCheckBoxXpath;
    private final String gridXpath;
    private final String createButtonXpath;
    private LinkedHashMap<String, String> createOrUpdateDialogPartXpaths;
    private final String className;
    private final String showOnlyDeletableCheckboxXpath;
    
    private GridTestingUtil gridTestingUtil;

    public  CrudTestingUtil(GridTestingUtil gridTestingUtil,
                           WebDriver driver,
                           String className,
                           String showDeletedCheckBoxXpath,
                           String gridXpath,
                           String createButtonXpath){
        this(gridTestingUtil, driver, className, showDeletedCheckBoxXpath, gridXpath, createButtonXpath, null);
    }

    public CrudTestingUtil(GridTestingUtil gridTestingUtil,
                           WebDriver driver,
                           String className,
                           String showDeletedCheckBoxXpath,
                           String gridXpath,
                           String createButtonXpath,
                           String showOnlyDeletableCheckboxXpath){
        this.driver = driver;
        this.className = className;
        this.gridTestingUtil = gridTestingUtil;
        this.createButtonXpath = createButtonXpath;
        this.showDeletedCheckBoxXpath = showDeletedCheckBoxXpath;
        this.showOnlyDeletableCheckboxXpath = showOnlyDeletableCheckboxXpath;
        this.gridXpath = gridXpath;
        this.createOrUpdateDialogPartXpaths = new LinkedHashMap<>();
    }

    public void updateTest() throws InterruptedException {
        updateTest(null, null, true, null);
    }

    public void updateTest(LinkedHashMap<String, String> withData, String updateNotificationText, Boolean requiredSuccess) throws InterruptedException {
        updateTest(withData, updateNotificationText, requiredSuccess, null);
    }

    public void updateTest(LinkedHashMap<String, String> withData, String updateNotificationText, Boolean requiredSuccess, String showOnlyDeletableCheckboxXpath) throws InterruptedException {
        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
        }
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null){
            createTest();
            rowLocation = new ElementLocation(1, 0);
        }
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
            if(rowLocation == null){
                updateTest(withData, updateNotificationText, requiredSuccess, showOnlyDeletableCheckboxXpath);
            }
        }

        WebElement modifyButton = null;
        String[] modifiedData = null;
        while(modifyButton == null){
            rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
            if(rowLocation == null){
                createTest();
                rowLocation = new ElementLocation(1, 0);
            }
            ElementLocation random = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
            gridTestingUtil.goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());

            gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);

            WebElement showDeletedCheckBox = gridTestingUtil.findVisibleElementWithXpath(showDeletedCheckBoxXpath);
            String[] tempModifiedData = gridTestingUtil.getDataFromRowLocation(gridXpath, rowLocation);

            Thread.sleep(200);
            WebElement tempModifyButton = gridTestingUtil.getModifyButton(gridXpath, rowLocation.getRowIndex());
            if(gridTestingUtil.isEnabled(tempModifyButton)){
                modifyButton = tempModifyButton;
                modifiedData = tempModifiedData;
            } else {

            }
        }
        modifyButton.click();


        WebElement dialog = gridTestingUtil.findVisibleElementWithXpath("//*[@id=\"overlay\"]");
        WebElement saveEmployeeButton = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        fillCreateOrUpdateForm(withData);

        saveEmployeeButton.click();

        gridTestingUtil.resetFilter(gridXpath);

        gridTestingUtil.checkNotificationContainsTexts(updateNotificationText == null ? className + " updated: " : updateNotificationText);
        Thread.sleep(100);
        assertEquals(requiredSuccess ? 0 : 1, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, modifiedData));

        Thread.sleep(100);
    }

    public void createTest() throws InterruptedException {
        createTest(null, null, true);
    }

    public void createTest(LinkedHashMap<String, String> withData, String saveNotificationText, Boolean requiredSuccess) throws InterruptedException {
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        int originalVisible = gridTestingUtil.countVisibleGridDataRows(gridXpath);
        int originalInvisible = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int origivalVisibleOnlyDeletable = 0;
        int originalInvisibleOnlyDeletable = 0;

        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            origivalVisibleOnlyDeletable = gridTestingUtil.countVisibleGridDataRows(gridXpath);
            originalInvisibleOnlyDeletable = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }

        WebElement createButton = gridTestingUtil.findClickableElementWithXpathWithWaiting(createButtonXpath);
        createButton.click();
        Thread.sleep(200);

        fillCreateOrUpdateForm(withData);

        WebElement saveButton = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", saveButton);
        Thread.sleep(500);

        if(requiredSuccess){
            gridTestingUtil.checkNotificationContainsTexts(saveNotificationText == null ? className + " saved: " : saveNotificationText);
        }
        else{
            gridTestingUtil.checkNotificationContainsTexts(saveNotificationText == null ? className + " saving failed" : saveNotificationText);
        }

        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);
        assertEquals(requiredSuccess ? originalVisible + 1 : originalVisible, gridTestingUtil.countVisibleGridDataRows(gridXpath));
        assertEquals(originalInvisible, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            Thread.sleep(10);
            assertEquals(requiredSuccess ? origivalVisibleOnlyDeletable + 1 : origivalVisibleOnlyDeletable, gridTestingUtil.countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
            assertEquals(originalInvisibleOnlyDeletable, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
            Thread.sleep(10);
        }

        assertEquals(requiredSuccess ? originalVisible + 1 : originalVisible, gridTestingUtil.countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
        assertEquals(originalInvisible, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }


//    public void createNotExpectedStatusCodeSave(EmsApiClient spyClient, Class<?> objectClass) throws InterruptedException {
//        MockitoAnnotations.openMocks(this);
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyClient).save(any(objectClass));
//        createTest(null, "Not expected status-code in saving", false);
//    }

//    public void createReturns(BaseService spyService, Class<?> objectClass, EmsResponse response) throws InterruptedException {
//        Mockito.doReturn(response).when(spyClient).save(any(objectClass));
//        createTest(null, response.getDescription(), false);
//    }
//
//    public void updateReturns(EmsApiClient spyClient, Class<?> objectClass, EmsResponse response) throws InterruptedException {
//        Mockito.doReturn(response).when(spyClient).update(any(objectClass));
//        updateTest(null, response.getDescription(), false);
//    }

//    public void updateNotExpectedStatusCode(EmsApiClient spyClient, Class<?> objectClass) throws InterruptedException {
//        updateNotExpectedStatusCode(spyClient, objectClass, null);
//    }
//
//    public void updateNotExpectedStatusCode(EmsApiClient spyClient, Class<?> objectClass, String showOnlyDeletableCheckboxXpath) throws InterruptedException {
//        MockitoAnnotations.openMocks(this);
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyClient).update(any(objectClass));
//        updateTest(null, "not expected status-code in modifying", false, showOnlyDeletableCheckboxXpath);
//    }

    public void fillCreateOrUpdateForm(LinkedHashMap<String, String> withData) throws InterruptedException {
        fillCreateOrUpdateForm(withData, null);
    }

    /**
     * Fill the create or update form.
     * @param withData The key value is the label of the field. The value is the required value.
     * @param disabledFields The labels of the fields, which must be disabled at the state.
     * @throws InterruptedException
     */
    public void fillCreateOrUpdateForm(LinkedHashMap<String, String> withData, LinkedHashMap<String, String> disabledFields) throws InterruptedException {
        List<WebElement> fields = gridTestingUtil.findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout").findElements(By.xpath("./*"));
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
            catch (NoSuchElementException ignore) {
//                System.out.println("Ignore *************");
//                printToConsole(fields.get(i));
            }

            if(disabledFields != null && disabledFields.get(fieldLabel) != null){
                assertEquals(false, gridTestingUtil.isEnabled(fields.get(i)), "Field must be disabled, but it is enabled: " + fieldLabel);
                assertEquals(disabledFields.get(fieldLabel), gridTestingUtil.getFieldErrorMessage(fields.get(i)), "The field is disabled, but the error message doesn't match");
            }
            else {
                if(previousPasswordFieldValue == null){
                    previousPasswordFieldValue = gridTestingUtil.fillElementWith(fields.get(i), showOnlyDeletableCheckboxXpath != null, null, withData.get(fieldLabel));
                }
                else{
                    gridTestingUtil.fillElementWith(fields.get(i), showOnlyDeletableCheckboxXpath != null,  previousPasswordFieldValue, withData.get(fieldLabel));
                }
                if(!fieldLabel.isEmpty()){
                    fields.get(i).sendKeys(Keys.ENTER);
                }
            }
        }
    }

    public void deleteTest() throws InterruptedException {
        deleteTest(null, true);
    }

    public void deleteTest(String notification, Boolean requiredSuccess, String... originalFilter) throws InterruptedException {
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        int originalVisible = gridTestingUtil.countVisibleGridDataRows(gridXpath);
        int originalInvisible = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalVisibleDeletable = originalVisible;
        int originalInvisibleDeletable = originalInvisible;

        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            originalVisibleDeletable = gridTestingUtil.countVisibleGridDataRows(gridXpath);
            originalInvisibleDeletable = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }

        if(originalVisibleDeletable == 0){
            createTest();
            originalVisible++;
            originalVisibleDeletable++;
        }

        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
        }
        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);


        WebElement deleteButton = null;
        String[] deletedData = null;
        while(deleteButton == null){
            ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
            if(rowLocation == null){
                createTest();
                rowLocation = new ElementLocation(1, 0);
            }
            gridTestingUtil.goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());

            gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);


            WebElement showDeletedCheckBox = gridTestingUtil.findVisibleElementWithXpath(showDeletedCheckBoxXpath);
            String[] tempDeletedData = gridTestingUtil.getDataFromRowLocation(gridXpath, rowLocation);

            Thread.sleep(200);
            WebElement tempDeleteButton = gridTestingUtil.getDeleteButton(gridXpath, rowLocation.getRowIndex());
            if(tempDeleteButton.getDomAttribute("disabled") == null){
                deleteButton = tempDeleteButton;
                deletedData = tempDeletedData;
            }
        }

        deleteButton.click();
        Thread.sleep(100);
        gridTestingUtil.checkNotificationContainsTexts(notification == null ? className + " deleted: " : notification);
        if(this.showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        Thread.sleep(2000);
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);

        gridTestingUtil.resetFilter(gridXpath);
        assertEquals(requiredSuccess ? 0 : 1, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, deletedData));
        if(originalFilter != null){
            gridTestingUtil.applyFilter(gridXpath, originalFilter);
        }
        Thread.sleep(100);
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);
        Thread.sleep(2000);
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        assertEquals(requiredSuccess ? originalVisible - 1 : originalVisible, gridTestingUtil.countVisibleGridDataRows(gridXpath));
        assertEquals(requiredSuccess ? originalInvisible + 1 : originalInvisible, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, true);
        assertEquals(originalInvisible + originalVisible, gridTestingUtil.countVisibleGridDataRows(gridXpath));
        gridTestingUtil.resetFilter(gridXpath);
        assertEquals(1, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, deletedData));
        if(originalFilter != null){
            gridTestingUtil.applyFilter(gridXpath, originalFilter);
        }
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);

        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            assertEquals(requiredSuccess ? originalVisibleDeletable - 1 : originalVisibleDeletable, gridTestingUtil.countVisibleGridDataRows(gridXpath));
            assertEquals(requiredSuccess ? originalInvisibleDeletable + 1 : originalInvisibleDeletable, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
            gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, true);
            assertEquals(originalInvisibleDeletable + originalVisibleDeletable, gridTestingUtil.countVisibleGridDataRows(gridXpath));
            assertEquals(1, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, deletedData));
            gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
    }

    public void databaseNotAvailableWhenDeleteTest(DataSource spyDatasource, String notification, String... originalFilter) throws InterruptedException, SQLException {
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        int originalVisible = gridTestingUtil.countVisibleGridDataRows(gridXpath);
        int originalInvisible = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalVisibleDeletable = originalVisible;
        int originalInvisibleDeletable = originalInvisible;

        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            originalVisibleDeletable = gridTestingUtil.countVisibleGridDataRows(gridXpath);
            originalInvisibleDeletable = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }

        if(originalVisibleDeletable == 0){
            createTest();
            originalVisible++;
            originalVisibleDeletable++;
        }

        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
        }
        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);


        WebElement deleteButton = null;
        String[] deletedData = null;
        while(deleteButton == null){
            ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
            if(rowLocation == null){
                createTest();
                rowLocation = new ElementLocation(1, 0);
            }
            gridTestingUtil.goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());

            gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);


            WebElement showDeletedCheckBox = gridTestingUtil.findVisibleElementWithXpath(showDeletedCheckBoxXpath);
            String[] tempDeletedData = gridTestingUtil.getDataFromRowLocation(gridXpath, rowLocation);

            Thread.sleep(200);
            WebElement tempDeleteButton = gridTestingUtil.getDeleteButton(gridXpath, rowLocation.getRowIndex());
            if(tempDeleteButton.getDomAttribute("disabled") == null){
                deleteButton = tempDeleteButton;
                deletedData = tempDeletedData;
            }
        }

        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(this, spyDatasource, 0);

        deleteButton.click();
        Thread.sleep(100);
        gridTestingUtil.checkNotificationContainsTexts(notification == null ? className + " deleted: " : notification);
        if(this.showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        Thread.sleep(2000);
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);

        gridTestingUtil.resetFilter(gridXpath);
        assertEquals(1, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, deletedData));
        if(originalFilter != null){
            gridTestingUtil.applyFilter(gridXpath, originalFilter);
        }
        Thread.sleep(100);
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);
        Thread.sleep(2000);
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        assertEquals(originalVisible, gridTestingUtil.countVisibleGridDataRows(gridXpath));
        assertEquals(originalInvisible, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, true);
        assertEquals(originalInvisible + originalVisible, gridTestingUtil.countVisibleGridDataRows(gridXpath));
        gridTestingUtil.resetFilter(gridXpath);
        assertEquals(1, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, deletedData));
        if(originalFilter != null){
            gridTestingUtil.applyFilter(gridXpath, originalFilter);
        }
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);

        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            assertEquals(originalVisibleDeletable, gridTestingUtil.countVisibleGridDataRows(gridXpath));
            assertEquals(originalInvisibleDeletable, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
            gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, true);
            assertEquals(originalInvisibleDeletable + originalVisibleDeletable, gridTestingUtil.countVisibleGridDataRows(gridXpath));
            assertEquals(1, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, deletedData));
            gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
    }

    public void permanentlyDeleteTest() throws InterruptedException {
        int originalVisible = gridTestingUtil.countVisibleGridDataRows(gridXpath);
        int originalInvisible = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalVisibleDeletable = originalVisible;
        int originalInvisibleDeletable = originalInvisible;
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            originalVisibleDeletable = gridTestingUtil.countVisibleGridDataRows(gridXpath);
            originalInvisibleDeletable = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
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
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
        }


        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, true);

        String[] selectedData = gridTestingUtil.getRandomDataDeletedStatusFromGrid(gridXpath, showDeletedCheckBoxXpath);
        //assertEquals(1, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, deletedData));
        gridTestingUtil.applyFilter(gridXpath, selectedData);
        ElementLocation el = new ElementLocation(1, 0);

//        ElementLocation el = getRandomLocationDeletedStatusFromGrid(gridXpath, showDeletedCheckBoxXpath);
//        WebElement showDeleted = gridTestingUtil.findClickableElementWithXpathWithWaiting(showDeletedCheckBoxXpath);
//        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, true);
//        Thread.sleep(500);
//        gridTestingUtil.goToPageInPaginatedGrid(gridXpath, el.getPageNumber());
//        String[] selectedData = gridTestingUtil.getDataFromRowLocation(gridXpath, el);
//        assertEquals(1, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, selectedData));
//        gridTestingUtil.goToPageInPaginatedGrid(gridXpath, el.getPageNumber());
        WebElement pDeleteButton = gridTestingUtil.getPermanentlyDeleteButton(gridXpath, el.getRowIndex());
        pDeleteButton.click();

        if(this.showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }

        gridTestingUtil.checkNotificationContainsTexts(className + " permanently deleted: ");
        Thread.sleep(500);
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);
        assertEquals(0, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, selectedData));
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, true);
        assertEquals(0, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, selectedData));
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);
        Thread.sleep(500);

        assertEquals(originalInvisible - 1, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);
        assertEquals(originalVisible, gridTestingUtil.countVisibleGridDataRows(gridXpath));

        if(this.showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);
            assertEquals(originalInvisibleDeletable - 1, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
            assertEquals(originalVisibleDeletable, gridTestingUtil.countVisibleGridDataRows(gridXpath));
            assertEquals(0, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, selectedData));
            gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, true);
            assertEquals(0, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, selectedData));
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
            gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);
        }
    }


    public List<String[]> getAllDataLinesFull() throws InterruptedException {
        PaginationData pd = gridTestingUtil.getGridPaginationData(gridXpath);
        List<String[]> allFullData = new ArrayList<>();

        for(int i = 0; i < pd.getTotalElements(); i++){
            ElementLocation el = new ElementLocation((i / pd.getPageSize()) + 1, i % pd.getPageSize());
            String[] data = gridTestingUtil.getDataFromRowLocation(gridXpath, el);
            Boolean goodData = true;
            for(int j = 0; j < data.length; j++){
               if(data[j].equals("") || data[j] == null){
                    goodData = false;
                    break;
               }
            }
            if(goodData){
                allFullData.add(data);
            }
        }


        return allFullData;

    }

    public List<String[]> getAllDataLinesWithEmpty() throws InterruptedException {
        PaginationData pd = gridTestingUtil.getGridPaginationData(gridXpath);
        List<String[]> allLinesWithEmpty = new ArrayList<>();

        for(int i = 0; i < pd.getTotalElements(); i++){
            ElementLocation elementLocation = new ElementLocation((i / pd.getPageSize()) + 1, i % pd.getPageSize());
            String[] data = gridTestingUtil.getDataFromRowLocation(gridXpath, elementLocation);
            Boolean goodData = false;
            for(int j = 0; j < data.length; j++){
                if(data[j].equals("") || data[j] == null){
                    goodData = true;
                    break;
                }
            }
            if(goodData){
                allLinesWithEmpty.add(data);
            }
        }
        return allLinesWithEmpty;
    }

    public void readTest() throws InterruptedException {
        ElementLocation randomLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(randomLocation == null){
            createTest();
            randomLocation = new ElementLocation(1, 0);
        }
        readTest(gridTestingUtil.getDataFromRowLocation(gridXpath, randomLocation), null, false, null); //TODO
    }

    public void createFailedTest(int port, EmsApiClient spyApiClient, String mainMenuXpath, String subMenuXpath, String subSubButtonXpath) throws InterruptedException, JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        if(spyApiClient instanceof UserApiClient){
            Mockito.doCallRealMethod().doThrow(JsonProcessingException.class).doCallRealMethod().when(spyApiClient).writeValueAsString(any(BaseEntity.class));
        }
        else{
            Mockito.doThrow(JsonProcessingException.class).doCallRealMethod().when(spyApiClient).writeValueAsString(any(BaseEntity.class));
        }
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenuXpath, subMenuXpath);
        Thread.sleep(10);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(subSubButtonXpath).click();
        Thread.sleep(100);
        createTest(null, "", false);
    }

    /**
     *
     * @param data
     * @param extraDataFilter Ha null, akkor nem kell a NotificationCheck
     * @param withInDeleted
     * @param notificationCheck Ha null, akkor lehet, hogy meghal az extraDataFilter szűrés
     * @throws InterruptedException
     */
    public void readTest(String[] data, String extraDataFilter, Boolean withInDeleted, NotificationCheck notificationCheck) throws InterruptedException {
        if(data == null || data.length == 0){
            gridTestingUtil.findVisibleElementWithXpath(gridXpath);
            ElementLocation randomLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
            data = gridTestingUtil.getDataFromRowLocation(gridXpath, randomLocation);
        }
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        int originalVisible = gridTestingUtil.countVisibleGridDataRows(gridXpath);
        int originalInvisible = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalVisibleDeletable = originalVisible;
        int originalInvisibleDeletable = originalInvisible;

        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            originalVisibleDeletable = gridTestingUtil.countVisibleGridDataRows(gridXpath);
            originalInvisibleDeletable = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }

        if(originalVisibleDeletable == 0){
            createTest();
            originalVisible++;
            originalVisibleDeletable++;
        }

        Boolean originalDeleted = gridTestingUtil.getCheckboxStatus(showDeletedCheckBoxXpath);

        if(withInDeleted){
            gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, true);
        }
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        if(extraDataFilter != null){
            gridTestingUtil.setExtraDataFilterValue(gridXpath, extraDataFilter, notificationCheck);
        }
        int elements = gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, data);
        assertEquals(1, elements);
        if(extraDataFilter != null){
            gridTestingUtil.clearExtraDataFilter(gridXpath);
        }
        gridTestingUtil.setCheckboxStatus(gridXpath, originalDeleted);
    }

    public void restoreTest() throws InterruptedException {
        restoreTest(null, true);
    }

    public void restoreTest(String notification, Boolean needSuccess) throws InterruptedException {
        int originalVisibleRows = gridTestingUtil.countVisibleGridDataRows(gridXpath);
        int originalInvisibleRows = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        WebElement showDeletedButton = gridTestingUtil.findClickableElementWithXpathWithWaiting(showDeletedCheckBoxXpath);

        if(createButtonXpath != null && createButtonXpath != ""){
            if(originalInvisibleRows == 0) {
                createTest();
                deleteTest();
                originalInvisibleRows++;
            }
        }

        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, true);

        String[] deletedData = gridTestingUtil.getRandomDataDeletedStatusFromGrid(gridXpath, showDeletedCheckBoxXpath);
        gridTestingUtil.applyFilter(gridXpath, deletedData);
        ElementLocation deleted = new ElementLocation(1, 0);

        WebElement restoreButton = gridTestingUtil.getRestoreButton(gridXpath, deleted.getRowIndex());
        restoreButton.click();
        gridTestingUtil.checkNotificationContainsTexts(notification == null ? className + " restored: " : notification);
        gridTestingUtil.resetFilter(gridXpath);
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);
        if(needSuccess){
            assertEquals(1, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, deletedData));

            int newVisibleRows = gridTestingUtil.countVisibleGridDataRows(gridXpath);
            int newInvisibleRows = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            assertEquals(originalVisibleRows + 1, newVisibleRows);
            assertEquals(originalInvisibleRows - 1, newInvisibleRows);
        }
        else{
            assertEquals(0, gridTestingUtil.countElementResultsFromGridWithFilter(gridXpath, deletedData));

            int newVisibleRows = gridTestingUtil.countVisibleGridDataRows(gridXpath);
            int newInvisibleRows = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            assertEquals(originalVisibleRows, newVisibleRows);
            assertEquals(originalInvisibleRows, newInvisibleRows);
        }
    }


    /**
     * Use this method, when you want to test if some data fetching from database failed, so the save button not available
     * @param failedFieldData key: the label of the field
     *                        value: required error message of the field
     */
    @Deprecated
    public void createUnexpectedResponseCodeWhileGettingData(LinkedHashMap<String, String> withData, LinkedHashMap<String, String> failedFieldData) throws InterruptedException {
        WebElement createButton = gridTestingUtil.findClickableElementWithXpathWithWaiting(createButtonXpath);
        createButton.click();
        Thread.sleep(200);

        fillCreateOrUpdateForm(withData, failedFieldData);

        WebElement saveButton = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        assertEquals(false, gridTestingUtil.isEnabled(saveButton), "If using Mockito.findAll(), replace it with Mockito.findAll(false), because the fields only contain non-deleted data");
    }

//    /**
//     * Simulates that the getting entities failed, because database connection error
//     * @param spyDataSource the DataSource, which annotated with @SpyBean
//     * @param port the port
//     * @param mainMenu mainMenu name. You can find it in the test class
//     * @param subMenu subMenu name. You can find it in the test class
//     * @param notificationEntityClassName The entity name, which have to between "Getting " and " failed" in the notification.
//     * @throws SQLException
//     * @throws InterruptedException
//     */
//    public void databaseUnavailableWhenGetAllEntity(Class<?> testClass, DataSource spyDataSource, int port, String mainMenu, String subMenu, String notificationEntityClassName) throws SQLException, InterruptedException {
//        gridTestingUtil.loginWith(driver, port, "admin", "admin");
//        Thread.sleep(100);
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(testClass, spyDataSource, );
////        Mockito.doThrow(new SQLException("Connection refused: getsockopt")).when(spyDataSource).getConnection();
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.checkNotificationText("Getting " + notificationEntityClassName + " failed");
//        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(gridXpath));
//        assertEquals(0, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    public void updateUnexpectedResponseCodeWhileGettingData(LinkedHashMap<String, String> withData, LinkedHashMap<String, String> failedFieldData) throws InterruptedException {
        WebElement grid = gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
        }
        ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
        if(rowLocation == null){
            createTest();
            rowLocation = new ElementLocation(1, 0);
        }
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
        }

        String[] originalData = gridTestingUtil.getDataFromRowLocation(gridXpath, rowLocation);

        gridTestingUtil.goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());
        WebElement modifyButton = gridTestingUtil.getModifyButton(gridXpath, rowLocation.getRowIndex());
        Thread.sleep(200);
        modifyButton.click();
        Thread.sleep(200);

        fillCreateOrUpdateForm(withData, failedFieldData);

        WebElement saveButton = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        assertEquals(false, gridTestingUtil.isEnabled(saveButton));
    }

    /**
     *
     * Simulates that the saving entity failed, because database connection error
     * @param spyDataSource the DataSource, which annotated with @SpyBean
     * @param withData the data, if you want to fill the create dialog with specified data. Other case set to null.
     * @param saveNotificationText the required text, which appear in a notification after clicking the save button. If
     *                             the default is required, set this to null
     * @param preSuccess the number, which is getting connection with the database before saving (for example check if
     *                   some other data is exists, or some other things)
     * @throws InterruptedException
     * @throws SQLException
     */
    public void databaseUnavailableWhenSaveEntity(Object testClass, DataSource spyDataSource, LinkedHashMap<String, String> withData, String saveNotificationText, Integer preSuccess) throws InterruptedException, SQLException {
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        int originalVisible = gridTestingUtil.countVisibleGridDataRows(gridXpath);
        int originalInvisible = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int origivalVisibleOnlyDeletable = 0;
        int originalInvisibleOnlyDeletable = 0;

        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            origivalVisibleOnlyDeletable = gridTestingUtil.countVisibleGridDataRows(gridXpath);
            originalInvisibleOnlyDeletable = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }

        WebElement createButton = gridTestingUtil.findClickableElementWithXpathWithWaiting(createButtonXpath);
        createButton.click();
        Thread.sleep(200);

        fillCreateOrUpdateForm(withData);

        Thread.sleep(100);

        Mockito.reset(spyDataSource);
//        for(int i = 0; i < preSuccess; i++){

//        }



        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(testClass, spyDataSource, preSuccess);

//        Mockito.doCallRealMethod().when(spyDataSource).getConnection();
//        Mockito.doThrow(new SQLException("Connection refused: getsockopt")).doCallRealMethod().when(spyDataSource).getConnection();
//        Mockito.doCallRealMethod().doThrow(new SQLException("Connection refused: getsockopt")).doCallRealMethod().when(spyDataSource).getConnection();


        WebElement saveButton = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", saveButton);
//        Mockito.reset(spyDataSource);
        Thread.sleep(200);


//        gridTestingUtil.closeNotification(1000);
//        Mockito.verify(spyDataSource, Mockito.times(2)).getConnection();
        gridTestingUtil.checkNotificationContainsTexts(saveNotificationText == null ? className + " saving failed: internal server error" : saveNotificationText);
//        Mockito.verify(spyDataSource, Mockito.times(2)).getConnection();


        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);
        assertEquals(originalVisible, gridTestingUtil.countVisibleGridDataRows(gridXpath));
        assertEquals(originalInvisible, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            Thread.sleep(10);
            assertEquals(origivalVisibleOnlyDeletable, gridTestingUtil.countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
            assertEquals(originalInvisibleOnlyDeletable, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
            Thread.sleep(10);
        }

        assertEquals(originalVisible, gridTestingUtil.countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
        assertEquals(originalInvisible, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }

    public void databaseUnavailableWhenUpdateEntity(DataSource spyDataSource, LinkedHashMap<String, String> withData, String saveNotificationText, Integer preSuccess) throws InterruptedException, SQLException {
        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        int originalVisible = gridTestingUtil.countVisibleGridDataRows(gridXpath);
        int originalInvisible = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int origivalVisibleOnlyDeletable = 0;
        int originalInvisibleOnlyDeletable = 0;

        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            origivalVisibleOnlyDeletable = gridTestingUtil.countVisibleGridDataRows(gridXpath);
            originalInvisibleOnlyDeletable = gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }

        WebElement modifyButton = null;
        String[] modifiedData = null;
        while(modifyButton == null){
            ElementLocation rowLocation = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
            if(rowLocation == null){
                createTest();
                rowLocation = new ElementLocation(1, 0);
            }
            ElementLocation random = gridTestingUtil.getRandomLocationFromGrid(gridXpath);
            gridTestingUtil.goToPageInPaginatedGrid(gridXpath, random.getPageNumber());

            gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);

            WebElement tempModifyButton = gridTestingUtil.getModifyButton(gridXpath, random.getRowIndex());

            WebElement showDeletedCheckBox = gridTestingUtil.findVisibleElementWithXpath(showDeletedCheckBoxXpath);
            String[] tempModifiedData = gridTestingUtil.getDataFromRowLocation(gridXpath, rowLocation);

            Thread.sleep(200);
            if(tempModifyButton.getDomAttribute("disabled") == null){
                modifyButton = tempModifyButton;
                modifiedData = tempModifiedData;
            }
        }

        modifyButton.click();
        Thread.sleep(200);

        fillCreateOrUpdateForm(withData);

        Thread.sleep(100);

        Mockito.reset(spyDataSource);
//        for(int i = 0; i < preSuccess; i++){

//        }

        AtomicInteger callCount = new AtomicInteger(0);

        Mockito.doAnswer(invocation -> {
            int currentCall = callCount.incrementAndGet();

            // Az első néhány hívásnál (limitig) az eredeti metódust hívjuk
            if (currentCall <= preSuccess) {
                return invocation.callRealMethod();
            }
            // Kivételt dobunk egyszer, ha elértük a limitet
            else if (currentCall == preSuccess + 1) {
                throw new SQLException("Connection refused: getsockopt");
            }
            // A további hívásoknál visszaállunk az eredeti metódus hívására
            else {
                return invocation.callRealMethod();
            }
        }).when(spyDataSource).getConnection();

//        Mockito.doCallRealMethod().when(spyDataSource).getConnection();
//        Mockito.doThrow(new SQLException("Connection refused: getsockopt")).doCallRealMethod().when(spyDataSource).getConnection();
//        Mockito.doCallRealMethod().doThrow(new SQLException("Connection refused: getsockopt")).doCallRealMethod().when(spyDataSource).getConnection();


        WebElement saveButton = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", saveButton);
        Thread.sleep(1000);
        //        Mockito.reset(spyDataSource);


//        gridTestingUtil.closeNotification(1000);
//        Mockito.verify(spyDataSource, Mockito.times(2)).getConnection();
        gridTestingUtil.checkNotificationContainsTexts(saveNotificationText == null ? className + " modifying failed" : saveNotificationText);
//        Mockito.verify(spyDataSource, Mockito.times(2)).getConnection();


        gridTestingUtil.findVisibleElementWithXpath(gridXpath);
        gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, false);
        assertEquals(originalVisible, gridTestingUtil.countVisibleGridDataRows(gridXpath));
        assertEquals(originalInvisible, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        if(showOnlyDeletableCheckboxXpath != null){
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            Thread.sleep(10);
            assertEquals(origivalVisibleOnlyDeletable, gridTestingUtil.countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
            assertEquals(originalInvisibleOnlyDeletable, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
            gridTestingUtil.setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
            Thread.sleep(10);
        }

        assertEquals(originalVisible, gridTestingUtil.countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
        assertEquals(originalInvisible, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }
}
