package hu.martin.ems.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.PaginationData;
import hu.martin.ems.core.model.BaseEntity;
import hu.martin.ems.core.model.EmsResponse;
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

import static hu.martin.ems.base.GridTestingUtil.*;
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
        updateTest(null, null, true, null);
    }

    public void updateTest(LinkedHashMap<String, String> withData, String updateNotificationText, Boolean requiredSuccess) throws InterruptedException {
        updateTest(withData, updateNotificationText, requiredSuccess, null);
    }

    public void updateTest(LinkedHashMap<String, String> withData, String updateNotificationText, Boolean requiredSuccess, String showOnlyDeletableCheckboxXpath) throws InterruptedException {
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
        WebElement saveEmployeeButton = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        fillCreateOrUpdateForm(withData);

        saveEmployeeButton.click();

        checkNotificationContainsTexts(updateNotificationText == null ? className + " updated: " : updateNotificationText);
        Thread.sleep(100);
        assertEquals(requiredSuccess ? 0 : 1, countElementResultsFromGridWithFilter(gridXpath, originalData));

        Thread.sleep(100);
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

        WebElement createButton = findClickableElementWithXpathWithWaiting(createButtonXpath);
        createButton.click();
        Thread.sleep(200);

        fillCreateOrUpdateForm(withData);

        WebElement saveButton = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", saveButton);

        if(requiredSuccess){
            checkNotificationContainsTexts(saveNotificationText == null ? className + " saved: " : saveNotificationText);
        }
        else{
            checkNotificationContainsTexts(saveNotificationText == null ? className + " saving failed" : saveNotificationText);
        }

        findVisibleElementWithXpath(gridXpath);
        setCheckboxStatus(showDeletedCheckBoxXpath, false);
        assertEquals(requiredSuccess ? originalVisible + 1 : originalVisible, countVisibleGridDataRows(gridXpath));
        assertEquals(originalInvisible, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            Thread.sleep(10);
            assertEquals(requiredSuccess ? origivalVisibleOnlyDeletable + 1 : origivalVisibleOnlyDeletable, countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
            assertEquals(originalInvisibleOnlyDeletable, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
            Thread.sleep(10);
        }

        assertEquals(requiredSuccess ? originalVisible + 1 : originalVisible, countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
        assertEquals(originalInvisible, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }


    public void createNotExpectedStatusCodeSave(EmsApiClient spyClient, Class<?> objectClass) throws InterruptedException {
        MockitoAnnotations.openMocks(this);
        Mockito.doReturn(new EmsResponse(522, "")).when(spyClient).save(any(objectClass));
        createTest(null, "Not expected status-code in saving", false);
    }

    public void updateNotExpectedStatusCode(EmsApiClient spyClient, Class<?> objectClass) throws InterruptedException {
        updateNotExpectedStatusCode(spyClient, objectClass, null);
    }

    public void updateNotExpectedStatusCode(EmsApiClient spyClient, Class<?> objectClass, String showOnlyDeletableCheckboxXpath) throws InterruptedException {
        MockitoAnnotations.openMocks(this);
        Mockito.doReturn(new EmsResponse(522, "")).when(spyClient).update(any(objectClass));
        updateTest(null, "not expected status-code in modifying", false, showOnlyDeletableCheckboxXpath);
    }

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

            if(disabledFields != null && disabledFields.get(fieldLabel) != null){
                assertEquals(false, isEnabled(fields.get(i)), "Field must be disabled, but it is enabled: " + fieldLabel);
                assertEquals(disabledFields.get(fieldLabel), getFieldErrorMessage(fields.get(i)), "The field is disabled, but the error message doesn't match");
            }
            else {
                if(previousPasswordFieldValue == null){
                    previousPasswordFieldValue = fillElementWith(fields.get(i), showOnlyDeletableCheckboxXpath != null, null, withData.get(fieldLabel));
                }
                else{
                    fillElementWith(fields.get(i), showOnlyDeletableCheckboxXpath != null,  previousPasswordFieldValue, withData.get(fieldLabel));
                }
            }
        }
    }

    public void deleteTest() throws InterruptedException {
        deleteTest(null, true);
    }

    public void deleteTest(String notification, Boolean requiredSuccess, String... originalFilter) throws InterruptedException {
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
        Thread.sleep(100);
        checkNotificationContainsTexts(notification == null ? className + " deleted: " : notification);
        if(this.showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        Thread.sleep(2000);
        findVisibleElementWithXpath(gridXpath);

        resetFilter(gridXpath);
        assertEquals(requiredSuccess ? 0 : 1, countElementResultsFromGridWithFilter(gridXpath, deletedData));
        if(originalFilter != null){
            applyFilter(gridXpath, originalFilter);
        }
        Thread.sleep(100);
        findVisibleElementWithXpath(gridXpath);
        setCheckboxStatus(showDeletedCheckBoxXpath, false);
        Thread.sleep(2000);
        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
        }
        assertEquals(requiredSuccess ? originalVisible - 1 : originalVisible, countVisibleGridDataRows(gridXpath));
        assertEquals(requiredSuccess ? originalInvisible + 1 : originalInvisible, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        setCheckboxStatus(showDeletedCheckBoxXpath, true);
        assertEquals(originalInvisible + originalVisible, countVisibleGridDataRows(gridXpath));
        resetFilter(gridXpath);
        assertEquals(1, countElementResultsFromGridWithFilter(gridXpath, deletedData));
        if(originalFilter != null){
            applyFilter(gridXpath, originalFilter);
        }
        setCheckboxStatus(showDeletedCheckBoxXpath, false);

        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            assertEquals(requiredSuccess ? originalVisibleDeletable - 1 : originalVisibleDeletable, countVisibleGridDataRows(gridXpath));
            assertEquals(requiredSuccess ? originalInvisibleDeletable + 1 : originalInvisibleDeletable, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
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
//        WebElement showDeleted = findClickableElementWithXpathWithWaiting(showDeletedCheckBoxXpath);
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
        setCheckboxStatus(showDeletedCheckBoxXpath, false);
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


    public List<String[]> getAllDataLinesFull() throws InterruptedException {
        PaginationData pd = getGridPaginationData(gridXpath);
        List<String[]> allFullData = new ArrayList<>();

        for(int i = 0; i < pd.getTotalElements(); i++){
            ElementLocation el = new ElementLocation((i / pd.getPageSize()) + 1, i % pd.getPageSize());
            String[] data = getDataFromRowLocation(gridXpath, el);
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
        PaginationData pd = getGridPaginationData(gridXpath);
        List<String[]> allLinesWithEmpty = new ArrayList<>();

        for(int i = 0; i < pd.getTotalElements(); i++){
            ElementLocation elementLocation = new ElementLocation((i / pd.getPageSize()) + 1, i % pd.getPageSize());
            String[] data = getDataFromRowLocation(gridXpath, elementLocation);
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
        ElementLocation randomLocation = getRandomLocationFromGrid(gridXpath);
        if(randomLocation == null){
            createTest();
            randomLocation = new ElementLocation(1, 0);
        }
        readTest(getDataFromRowLocation(gridXpath, randomLocation), null, false, null); //TODO
    }

    public void createFailedTest(int port, EmsApiClient spyApiClient, String mainMenuXpath, String subMenuXpath, String subSubButtonXpath) throws InterruptedException, JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        if(spyApiClient instanceof UserApiClient){
            Mockito.doCallRealMethod().doThrow(JsonProcessingException.class).doCallRealMethod().when(spyApiClient).writeValueAsString(any(BaseEntity.class));
        }
        else{
            Mockito.doThrow(JsonProcessingException.class).doCallRealMethod().when(spyApiClient).writeValueAsString(any(BaseEntity.class));
        }
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenuXpath, subMenuXpath);
        Thread.sleep(10);
        findClickableElementWithXpathWithWaiting(subSubButtonXpath).click();
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
        int elements = countElementResultsFromGridWithFilter(gridXpath, data);
        if(elements != 1){
            System.out.println("SANYI");
        }
        assertEquals(1, elements);
        if(extraDataFilter != null){
            clearExtraDataFilter(gridXpath);
        }
        setCheckboxStatus(gridXpath, originalDeleted);
    }

    public void restoreTest() throws InterruptedException {
        restoreTest(null, true);
    }

    public void restoreTest(String notification, Boolean needSuccess) throws InterruptedException {
        int originalVisibleRows = countVisibleGridDataRows(gridXpath);
        int originalInvisibleRows = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        WebElement showDeletedButton = findClickableElementWithXpathWithWaiting(showDeletedCheckBoxXpath);

        if(createButtonXpath != null && createButtonXpath != ""){
            if(originalInvisibleRows == 0) {
                createTest();
                deleteTest();
                originalInvisibleRows++;
            }
        }

        setCheckboxStatus(showDeletedCheckBoxXpath, true);

        String[] deletedData = getRandomDataDeletedStatusFromGrid(gridXpath, showDeletedCheckBoxXpath);
        applyFilter(gridXpath, deletedData);
        ElementLocation deleted = new ElementLocation(1, 0);

        WebElement restoreButton = getRestoreButton(gridXpath, deleted.getRowIndex());
        restoreButton.click();
        checkNotificationContainsTexts(notification == null ? className + " restored: " : notification);
        resetFilter(gridXpath);
        setCheckboxStatus(showDeletedCheckBoxXpath, false);
        if(needSuccess){
            assertEquals(1, countElementResultsFromGridWithFilter(gridXpath, deletedData));

            int newVisibleRows = countVisibleGridDataRows(gridXpath);
            int newInvisibleRows = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            assertEquals(originalVisibleRows + 1, newVisibleRows);
            assertEquals(originalInvisibleRows - 1, newInvisibleRows);
        }
        else{
            assertEquals(0, countElementResultsFromGridWithFilter(gridXpath, deletedData));

            int newVisibleRows = countVisibleGridDataRows(gridXpath);
            int newInvisibleRows = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
            assertEquals(originalVisibleRows, newVisibleRows);
            assertEquals(originalInvisibleRows, newInvisibleRows);
        }
    }


    /**
     * Use this method, when you want to test if some data fetching from database failed, so the save button not available
     * @param failedFieldData key: the label of the field
     *                        value: required error message of the field
     */
    public void createUnexpectedResponseCodeWhileGettingData(LinkedHashMap<String, String> withData, LinkedHashMap<String, String> failedFieldData) throws InterruptedException {
        WebElement createButton = findClickableElementWithXpathWithWaiting(createButtonXpath);
        createButton.click();
        Thread.sleep(200);

        fillCreateOrUpdateForm(withData, failedFieldData);

        WebElement saveButton = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        assertEquals(false, isEnabled(saveButton));
    }

    /**
     * Simulates that the getting entities failed, because database connection error
     * @param spyDataSource the DataSource, which annotated with @SpyBean
     * @param port the port
     * @param mainMenu mainMenu name. You can find it in the test class
     * @param subMenu subMenu name. You can find it in the test class
     * @param notificationEntityClassName The entity name, which have to between "Getting " and " failed" in the notification.
     * @throws SQLException
     * @throws InterruptedException
     */
    public void databaseUnavailableWhenGetAllEntity(DataSource spyDataSource, int port, String mainMenu, String subMenu, String notificationEntityClassName) throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        Thread.sleep(100);
        Mockito.doThrow(new SQLException("Connection refused: getsockopt")).when(spyDataSource).getConnection();
        navigateMenu(mainMenu, subMenu);
        checkNotificationText("Getting " + notificationEntityClassName + " failed");
        assertEquals(0, countVisibleGridDataRows(gridXpath));
        assertEquals(0, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        checkNoMoreNotificationsVisible();
    }

    public void updateUnexpectedResponseCodeWhileGettingData(LinkedHashMap<String, String> withData, LinkedHashMap<String, String> failedFieldData) throws InterruptedException {
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
        Thread.sleep(200);

        fillCreateOrUpdateForm(withData, failedFieldData);

        WebElement saveButton = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        assertEquals(false, isEnabled(saveButton));
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
    public void databaseUnavailableWhenSaveEntity(DataSource spyDataSource, LinkedHashMap<String, String> withData, String saveNotificationText, Integer preSuccess) throws InterruptedException, SQLException {
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

        WebElement createButton = findClickableElementWithXpathWithWaiting(createButtonXpath);
        createButton.click();
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


        WebElement saveButton = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", saveButton);
//        Mockito.reset(spyDataSource);


//        closeNotification(1000);
//        Mockito.verify(spyDataSource, Mockito.times(2)).getConnection();
        checkNotificationContainsTexts(saveNotificationText == null ? className + " saving failed" : saveNotificationText);
//        Mockito.verify(spyDataSource, Mockito.times(2)).getConnection();


        findVisibleElementWithXpath(gridXpath);
        setCheckboxStatus(showDeletedCheckBoxXpath, false);
        assertEquals(originalVisible, countVisibleGridDataRows(gridXpath));
        assertEquals(originalInvisible, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            Thread.sleep(10);
            assertEquals(origivalVisibleOnlyDeletable, countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
            assertEquals(originalInvisibleOnlyDeletable, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
            Thread.sleep(10);
        }

        assertEquals(originalVisible, countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
        assertEquals(originalInvisible, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }

    public void databaseUnavailableWhenUpdateEntity(DataSource spyDataSource, LinkedHashMap<String, String> withData, String saveNotificationText, Integer preSuccess) throws InterruptedException, SQLException {
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

        ElementLocation random = getRandomLocationFromGrid(gridXpath);
        goToPageInPaginatedGrid(gridXpath, random.getPageNumber());

        WebElement modifyButton = getModifyButton(gridXpath, random.getRowIndex());
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


        WebElement saveButton = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click()", saveButton);
        Thread.sleep(1000);
        //        Mockito.reset(spyDataSource);


//        closeNotification(1000);
//        Mockito.verify(spyDataSource, Mockito.times(2)).getConnection();
        checkNotificationContainsTexts(saveNotificationText == null ? className + " modifying failed" : saveNotificationText);
//        Mockito.verify(spyDataSource, Mockito.times(2)).getConnection();


        findVisibleElementWithXpath(gridXpath);
        setCheckboxStatus(showDeletedCheckBoxXpath, false);
        assertEquals(originalVisible, countVisibleGridDataRows(gridXpath));
        assertEquals(originalInvisible, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        if(showOnlyDeletableCheckboxXpath != null){
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, true);
            Thread.sleep(10);
            assertEquals(origivalVisibleOnlyDeletable, countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
            assertEquals(originalInvisibleOnlyDeletable, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
            setCheckboxStatus(showOnlyDeletableCheckboxXpath, false);
            Thread.sleep(10);
        }

        assertEquals(originalVisible, countVisibleGridDataRows(gridXpath), "solve1: use refresh grid after enter save button in the vaadin class");
        assertEquals(originalInvisible, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }
}
