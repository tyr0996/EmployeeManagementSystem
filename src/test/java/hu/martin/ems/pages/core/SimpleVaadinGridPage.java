package hu.martin.ems.pages.core;

import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinCheckboxComponent;
import hu.martin.ems.pages.core.component.VaadinGridComponent;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.component.saveOrUpdateDialog.VaadinSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import hu.martin.ems.pages.core.performResult.*;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class SimpleVaadinGridPage<T> extends VaadinPage implements ISimpleVaadinGridPage<T> {
    @Getter
    protected VaadinCheckboxComponent showDeletedCheckBox;
    @Getter
    protected VaadinButtonComponent createButton;
    @Getter
    protected VaadinGridComponent grid;
    @Getter
    protected VaadinSaveOrUpdateDialog saveOrUpdateDialog;

    @Getter
    private String className;

    @Nullable
    @Getter
    private VaadinCheckboxComponent showOnlyDeletable;

    public SimpleVaadinGridPage(WebDriver driver, int port, String className, @Nullable String showOnlyDeletable){
        super(driver, port);
        this.className = className;
        this.showOnlyDeletable = showOnlyDeletable == null ? new VaadinCheckboxComponent(getDriver(), null) : new VaadinCheckboxComponent(driver, getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(showOnlyDeletable))));
//        this.showOnlyDeletable = showOnlyDeletable;
//        this.saveOrUpdateDialog = saveOrUpdateDialog;
    }

    public void setShowOnlyDeletable(String xpath){
        showOnlyDeletable = new VaadinCheckboxComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))));
    }

    @Override
    public PerformCreateResult performCreate(LinkedHashMap<String, Object> withData) {
        createButton.click();
        saveOrUpdateDialog.initWebElements();
        saveOrUpdateDialog.fill(withData);
        saveOrUpdateDialog.getSaveButton().click();
        new VaadinNotificationComponent(getDriver()).close();
        grid.waitForRefresh();
        return new PerformCreateResult();
    }

    private PerformCreateFailedResult performCreate(LinkedHashMap<String, Object> withData, DataSource spyDataSource, @Nullable Integer mockSetupFieldInDialog, Boolean mockCreation) throws SQLException {
        if(mockSetupFieldInDialog != null){
            MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource,mockSetupFieldInDialog);
        }
        createButton.click();
        saveOrUpdateDialog.initWebElements();
        List<FailedVaadinFillableComponent> failedFieldsInSaveOrUpdateDialog =
                saveOrUpdateDialog.getAllComponent().stream().filter(v -> !v.isEnabled()).map(v -> new FailedVaadinFillableComponent(v)).toList();

        String notificationText = null;
        saveOrUpdateDialog.fill(withData);
        if(mockCreation){
            MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        }
        if(mockSetupFieldInDialog == null){
            saveOrUpdateDialog.getSaveButton().click();
            VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(getDriver());
            notificationText = notificationComponent.getText();
            notificationComponent.close();
        }
        else{
            saveOrUpdateDialog.close();
        }

        return new PerformCreateFailedResult(failedFieldsInSaveOrUpdateDialog, notificationText);
    }

    private PerformDeleteFailedResult performDelete(DataSource spyDataSource) throws SQLException {
        if(grid.getTotalNonDeletedRowNumber(showDeletedCheckBox) == 0){
            performCreate(null);
            VaadinNotificationComponent.closeAll(getDriver());
        }

        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
        }

        VaadinButtonComponent deleteButton = null;
        String[] deletedData = null;
        while(deleteButton == null){
            ElementLocation rowLocation = grid.getRandomLocation();
            if(rowLocation == null){
                performCreate(null);
                rowLocation = new ElementLocation(1, 0);
            }
            grid.goToPage(rowLocation.getPageNumber());

            showDeletedCheckBox.setStatus(false);

            String[] tempDeletedData = grid.getDataFromRowLocation(rowLocation, true);

            VaadinButtonComponent tempDeleteButton = grid.getDeleteButton(rowLocation.getRowIndex());
            if(tempDeleteButton.isEnabled()){
                deleteButton = tempDeleteButton;
                deletedData = tempDeletedData;
            }
        }

        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        deleteButton.click();

        PerformDeleteFailedResult ret = new PerformDeleteFailedResult();
        ret.setOriginalDeletedData(deletedData);
        return ret;
    }

    @Override
    public PerformDeleteResult performDelete() {
        LinkedHashMap<String, Object> withData = null;
        if(showOnlyDeletable != null && !showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            withData = new LinkedHashMap<>();
            withData.put("Deletable", true);
        }

        if(grid.getTotalNonDeletedRowNumber(showDeletedCheckBox) == 0){
            performCreate(withData);
            VaadinNotificationComponent.closeAll(getDriver());
        }

        WebElement deleteButton = null;
        String[] deletedData = null;
        while(deleteButton == null){
            ElementLocation rowLocation = grid.getRandomLocation();
            if(rowLocation == null){
                performCreate(null);
                rowLocation = new ElementLocation(1, 0);
            }
            grid.goToPage(rowLocation.getPageNumber());

            showDeletedCheckBox.setStatus(false);

            String[] tempDeletedData = grid.getDataFromRowLocation(rowLocation, true);

            VaadinButtonComponent tempDeleteButton = grid.getDeleteButton(rowLocation.getRowIndex());
            if(tempDeleteButton.isEnabled()){
                deleteButton = tempDeleteButton.getElement();
                deletedData = tempDeletedData;
            }
        }

        deleteButton.click();

        PerformDeleteFailedResult ret = new PerformDeleteFailedResult();
        ret.setOriginalDeletedData(deletedData);

        if(showOnlyDeletable != null && !showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
        }

        return ret;
    }

    @Override
    public PerformRestoreResult performRestore() {
        if(!createButton.isNull() &&
                grid.getTotalNonDeletedRowNumber(showDeletedCheckBox) == 0 &&
                grid.getTotalDeletedRowNumber(showDeletedCheckBox) == 0){
            performCreate(null);
            VaadinNotificationComponent.closeAll(getDriver());
            performDelete();
            VaadinNotificationComponent.closeAll(getDriver());
        }

        showDeletedCheckBox.setStatus(true);

        String[] deletedData = grid.getRandomDataDeletedStatus(showDeletedCheckBox);
        grid.applyFilter(deletedData);
        ElementLocation deleted = new ElementLocation(1, 0);

        WebElement restoreButton = grid.getRestoreButton(deleted.getRowIndex());
        restoreButton.click();

        grid.resetFilter();

        return new PerformRestoreResult(deletedData);
    }

    @Override
    public PerformPermanentlyDeleteResult performPermanentlyDelete() {
        if(grid.getTotalDeletedRowNumber(showDeletedCheckBox) == 0 && grid.getTotalNonDeletedRowNumber(showDeletedCheckBox) == 0){
            performCreate(null);
            performDelete();
        }
        else if(grid.getTotalDeletedRowNumber(showDeletedCheckBox) == 0) {
            performDelete();
        }
        VaadinNotificationComponent.closeAll(getDriver());

        showDeletedCheckBox.setStatus(true);

        String[] selectedData = grid.getRandomDataDeletedStatus(showDeletedCheckBox);
        grid.applyFilter(selectedData);
        ElementLocation el = new ElementLocation(1, 0);

        VaadinButtonComponent pDeleteButton = grid.getPermanentlyDeleteButton(el.getRowIndex());
        pDeleteButton.click();

        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
        }

        PerformPermanentlyDeleteResult ret = new PerformPermanentlyDeleteResult();
        ret.setPermanentlyDeletedData(selectedData);

        grid.resetFilter();
        return ret;
    }

    private PerformPermanentlyDeleteFailedResult performPermanentlyDelete(DataSource spyDatasource) throws SQLException {
        showDeletedCheckBox.setStatus(true);

        String[] selectedData = grid.getRandomDataDeletedStatus(showDeletedCheckBox);
        grid.applyFilter(selectedData);
        ElementLocation el = new ElementLocation(1, 0);

        VaadinButtonComponent pDeleteButton = grid.getPermanentlyDeleteButton(el.getRowIndex());
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDatasource, 0);
        pDeleteButton.click();

        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
        }

        PerformPermanentlyDeleteFailedResult ret = new PerformPermanentlyDeleteFailedResult();
        ret.setPermanentlyDeletedData(selectedData);

        grid.resetFilter();
        return ret;
    }

    @Override
    public PerformUpdateResult performUpdate(LinkedHashMap<String, Object> withData, @NotNull ElementLocation rowLocation) {
        PerformUpdateResult ret = new PerformUpdateResult();
        grid.goToPage(rowLocation.getPageNumber());
        String[] modifiedData = grid.getDataFromRowLocation(rowLocation, true);
        ret.setOriginalModifiedData(modifiedData);
        VaadinButtonComponent modifyButton = grid.getModifyButton(rowLocation.getRowIndex());
        modifyButton.click();
        saveOrUpdateDialog.initWebElements();
        saveOrUpdateDialog.fill(withData);
        saveOrUpdateDialog.getSaveButton().click();

        return ret;
    }

    @Override
    public PerformUpdateFailedResult performUpdate(LinkedHashMap<String, Object> withData, @NotNull ElementLocation rowLocation, DataSource spyDatasource) throws SQLException {
        grid.waitForRefresh();
        grid.goToPage(rowLocation.getPageNumber());
        String[] modifiedData = grid.getDataFromRowLocation(rowLocation, true);
        VaadinButtonComponent modifyButton = grid.getModifyButton(rowLocation.getRowIndex());
        modifyButton.click();
        saveOrUpdateDialog.initWebElements();
        saveOrUpdateDialog.fill(withData);
        List<FailedVaadinFillableComponent> failedData = saveOrUpdateDialog.getFailedComponents();

        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDatasource, 0);
        saveOrUpdateDialog.getSaveButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        String notificationText = notification.getText();
        notification.close();

        return new PerformUpdateFailedResult(modifiedData, failedData, notificationText); //TODO failedFields
    }

    @Override
    public DoCreateTestData doCreateTest(){
        return doCreateTest(null);
    }

    @Override
    public DoCreateTestData doCreateTest(LinkedHashMap<String, Object> withData) {
        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
        }
        DoCreateTestData ret = new DoCreateTestData();
        ret.setOriginalDeletedRowNumber(grid.getTotalDeletedRowNumber(showDeletedCheckBox));
        ret.setOriginalNonDeletedRowNumber(grid.getTotalNonDeletedRowNumber(showDeletedCheckBox));

        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            ret.setOriginalNonDeletedOnlyDeletable(grid.getTotalNonDeletedRowNumber(showDeletedCheckBox));
            ret.setOriginalDeletedOnlyDeletable(grid.getTotalDeletedRowNumber(showDeletedCheckBox));
            showOnlyDeletable.setStatus(false);
        }

        performCreate(withData);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        ret.setNotificationWhenPerform(notification.getText());
        notification.close();

        showDeletedCheckBox.setStatus(false);

        ret.setDeletedRowNumberAfterMethod(grid.getTotalDeletedRowNumber(showDeletedCheckBox));
        ret.setNonDeletedRowNumberAfterMethod(grid.getTotalNonDeletedRowNumber(showDeletedCheckBox));
        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            ret.setDeletedRowNumberOnlyDeletableAfterMethod(grid.getTotalDeletedRowNumber(showDeletedCheckBox));
            ret.setNonDeletedRowNumberOnlyDeletableAfterMethod(grid.getTotalNonDeletedRowNumber(showDeletedCheckBox));
            showOnlyDeletable.setStatus(false);
        }

        return ret;
    }


    @Override
    public DoUpdateTestData doUpdateTest(){
        return doUpdateTest(null, null);
    }

    @Override
    public DoUpdateTestData doUpdateTest(LinkedHashMap<String, Object> withDataModify){
        return doUpdateTest(null, withDataModify);
    }

    @Override
    public DoUpdateTestData doUpdateTest(LinkedHashMap<String, Object> withDataCreate, LinkedHashMap<String, Object> withDataModify) {
        DoUpdateTestData ret = new DoUpdateTestData();

        if(showOnlyDeletable != null && !showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            grid.waitForRefresh();
        }
        ElementLocation rowLocation = grid.getRandomLocation();
        if(rowLocation == null) {
            performCreate(withDataCreate);
        }
        while(!getGrid().getModifyButton(rowLocation.getRowIndex()).isEnabled()){
            rowLocation = grid.getRandomLocation();
        }

        PerformUpdateResult pur = performUpdate(withDataModify, rowLocation);
        ret.setResult(pur);

        grid.resetFilter();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        ret.setNotificationWhenPerform(notification.getText());
        notification.close();

        if(showOnlyDeletable != null && !showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
            grid.waitForRefresh();
        }

        return ret;
    }

    @Override
    public DoDeleteTestData doDeleteTest(){
        return doDeleteTest(null);
    }

    @Override
    public DoDeleteTestData doDeleteTest(LinkedHashMap<String, Object> withDataCreate) {
//        DoDeleteTestData ret = new DoDeleteTestData();

        int originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        if(originalNonDeletedRowNumber == 0){
            performCreate(withDataCreate);
        }

        originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        Integer originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        Integer originalNonDeletedOnlyDeletable = originalNonDeletedRowNumber;
        Integer originalDeletedOnlyDeletable = originalDeletedRowNumber;


        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            originalNonDeletedOnlyDeletable = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
            originalDeletedOnlyDeletable = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        }


        PerformDeleteResult pdr = performDelete();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        String notificationWhenPerform = notification.getText();
        notification.close();



        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
        }

        grid.resetFilter();

        showDeletedCheckBox.setStatus(false);
        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
        }

        Integer nonDeletedRowNumberAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        Integer deletedRowNumberAfterMethod = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        Integer deletedRowNumberOnlyDeletableAfterMethod = null;
        Integer nonDeletedRowNumberOnlyDeletableAfterMethod = null;

        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            deletedRowNumberOnlyDeletableAfterMethod = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
            nonDeletedRowNumberOnlyDeletableAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
            showDeletedCheckBox.setStatus(false);
            showOnlyDeletable.setStatus(false);
        }

        DoDeleteTestData ret = new DoDeleteTestData(originalNonDeletedRowNumber,
                originalDeletedRowNumber,
                originalNonDeletedOnlyDeletable,
                originalDeletedOnlyDeletable,
                notificationWhenPerform,
                pdr,
                nonDeletedRowNumberAfterMethod,
                deletedRowNumberAfterMethod,
                nonDeletedRowNumberOnlyDeletableAfterMethod,
                deletedRowNumberOnlyDeletableAfterMethod);
        return ret;
    }

    @Override
    public DoRestoreTestData doRestoreTest() {
        Integer originalDeleted = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        Integer originalNonDeleted = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        Integer originalDeletedOnlyDeletable = originalDeleted;
        Integer originalNonDeletedOnlyDeletable = originalNonDeleted;

        if(originalDeletedOnlyDeletable == 0){
            performDelete();
            VaadinNotificationComponent.closeAll(getDriver());

            originalNonDeleted = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
            originalDeletedOnlyDeletable = originalDeleted;
            originalDeleted = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
            originalNonDeletedOnlyDeletable = originalNonDeleted;
        }
        if(showOnlyDeletable != null && !showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            originalDeletedOnlyDeletable = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
            originalNonDeletedOnlyDeletable = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
            showOnlyDeletable.setStatus(false);
        }

        PerformRestoreResult prr = performRestore();

        String notificationWhenPerform = null;
        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        notificationWhenPerform = notification.getText();
        notification.close();

        Integer nonDeletedRowNumberAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        Integer deletedRowNumberAfterMethod = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        Integer deletedRowNumberOnlyDeletableAfterMethod = deletedRowNumberAfterMethod;
        Integer nonDeletedRowNumberOnlyDeletableAfterMethod = nonDeletedRowNumberAfterMethod;
        if(showOnlyDeletable != null && !showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            deletedRowNumberOnlyDeletableAfterMethod = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
            nonDeletedRowNumberOnlyDeletableAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
            showOnlyDeletable.setStatus(false);
        }

        return new DoRestoreTestData(originalNonDeleted,
                originalDeleted,
                originalNonDeletedOnlyDeletable,
                originalDeletedOnlyDeletable,
                notificationWhenPerform,
                prr,
                nonDeletedRowNumberAfterMethod,
                deletedRowNumberAfterMethod,
                nonDeletedRowNumberOnlyDeletableAfterMethod,
                deletedRowNumberOnlyDeletableAfterMethod);
    }

    @Override
    public DoPermanentlyDeleteTestData doPermanentlyDeleteTest() {
        Integer originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        Integer originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        Integer originalNonDeletedOnlyDeletable = originalNonDeletedRowNumber;
        Integer originalDeletedOnlyDeletable = originalDeletedRowNumber;

        if (originalDeletedRowNumber == 0 && originalNonDeletedRowNumber == 0) {
            performCreate(null);
        }
        if (originalDeletedRowNumber == 0) {
            performDelete();
        }

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            originalNonDeletedOnlyDeletable = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
            originalDeletedOnlyDeletable = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
            showOnlyDeletable.setStatus(false);
        }

        originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedCheckBox);

        PerformPermanentlyDeleteResult ppdr = performPermanentlyDelete();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        String notificationWhenPerform = notification.getText();
        notification.close();

        showDeletedCheckBox.setStatus(false);


        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
        }

        grid.waitForRefresh();
        Integer nonDeletedRowNumberAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        Integer deletedRowNumberAfterMethod = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        Integer deletedRowNumberOnlyDeletableAfterMethod = null;
        Integer nonDeletedRowNumberOnlyDeletableAfterMethod = null;

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            deletedRowNumberOnlyDeletableAfterMethod = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
            nonDeletedRowNumberOnlyDeletableAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
            showOnlyDeletable.setStatus(false);
        }

        return new DoPermanentlyDeleteTestData(
                originalNonDeletedRowNumber,
                originalDeletedRowNumber,
                originalNonDeletedOnlyDeletable,
                originalDeletedOnlyDeletable,
                notificationWhenPerform,
                ppdr,
                nonDeletedRowNumberAfterMethod,
                deletedRowNumberAfterMethod,
                nonDeletedRowNumberOnlyDeletableAfterMethod,
                deletedRowNumberOnlyDeletableAfterMethod
        );
    }

    @Override
    public DoReadTestData doReadTest(String extraDataFilter, boolean withInDeleted) {
        String notification = null;
        if(VaadinNotificationComponent.hasNotification(getDriver())){
            VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(getDriver());
            notification = notificationComponent.getText();
        }

        ElementLocation randomLocation = grid.getRandomLocation();
        String[] data = grid.getDataFromRowLocation(randomLocation, true);

        int originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        int originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        int originalNonDeletedOnlyDeletable = originalNonDeletedRowNumber;
        int originalDeletedOnlyDeletable = originalDeletedRowNumber;

        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            originalNonDeletedOnlyDeletable = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
            originalDeletedOnlyDeletable = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
            showOnlyDeletable.setStatus(false);
        }

        if(originalNonDeletedRowNumber == 0){ //TODO: lehet hogy ide kell a deletable.
            performCreate(null);
            originalNonDeletedRowNumber++;
            originalNonDeletedOnlyDeletable++;
        }

        Boolean originalDeleted = showDeletedCheckBox.getStatus();

        if(withInDeleted){
            showDeletedCheckBox.setStatus(true);
//            gridTestingUtil.setCheckboxStatus(showDeletedCheckBoxXpath, true);
        }
        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
        }
        if(extraDataFilter != null){
            grid.setExtraDataFilter(extraDataFilter);
//            gridTestingUtil.setExtraDataFilterValue(gridXpath, extraDataFilter, notificationCheck);
        }

        if(extraDataFilter != null){
            grid.clearExtraDataFilter();
        }
        showDeletedCheckBox.setStatus(originalDeleted);

        DoReadTestData ret = new DoReadTestData(
                originalNonDeletedRowNumber,
                originalDeletedRowNumber,
                originalNonDeletedOnlyDeletable,
                originalDeletedOnlyDeletable,
                notification,
                originalNonDeletedRowNumber,
                originalDeletedRowNumber,
                originalNonDeletedOnlyDeletable,
                originalDeletedOnlyDeletable
        );
        return ret;
    }

    @Override
    public DoCreateFailedTestData doDatabaseNotAvailableWhenCreateTest(DataSource spyDatasource) throws SQLException {
        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
        }

        DoCreateFailedTestData ret = new DoCreateFailedTestData();
        ret.setOriginalDeletedRowNumber(grid.getTotalDeletedRowNumber(showDeletedCheckBox));
        ret.setOriginalNonDeletedRowNumber(grid.getTotalNonDeletedRowNumber(showDeletedCheckBox));

        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            ret.setOriginalNonDeletedOnlyDeletable(grid.getTotalNonDeletedRowNumber(showDeletedCheckBox));
            ret.setOriginalDeletedOnlyDeletable(grid.getTotalDeletedRowNumber(showDeletedCheckBox));
            showOnlyDeletable.setStatus(false);
        }

        PerformCreateFailedResult failedResult = performCreate(null, spyDatasource, null, true);
        ret.setNotificationWhenPerform(failedResult.getNotificationText());

        showDeletedCheckBox.setStatus(false);

        ret.setDeletedRowNumberAfterMethod(grid.getTotalDeletedRowNumber(showDeletedCheckBox));
        ret.setNonDeletedRowNumberAfterMethod(grid.getTotalNonDeletedRowNumber(showDeletedCheckBox));
        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            ret.setDeletedRowNumberOnlyDeletableAfterMethod(grid.getTotalDeletedRowNumber(showDeletedCheckBox));
            ret.setNonDeletedRowNumberOnlyDeletableAfterMethod(grid.getTotalNonDeletedRowNumber(showDeletedCheckBox));
            showOnlyDeletable.setStatus(false);
        }
        ret.setResult(failedResult);
        return ret;
    }

    @Override
    public DoUpdateFailedTestData doDatabaseNotAvailableWhenUpdateTest(LinkedHashMap<String, Object> withDataCreate, LinkedHashMap<String, Object> withDataModify, DataSource spyDatasource) throws SQLException {
        if(showOnlyDeletable != null && !showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            grid.waitForRefresh();
        }
        ElementLocation rowLocation = grid.getRandomLocation();
        if(rowLocation == null) {
            performCreate(withDataCreate);
            VaadinNotificationComponent.closeAll(getDriver());
            rowLocation = new ElementLocation(1, 0);
        }
        while(!grid.getModifyButton(rowLocation.getRowIndex()).isEnabled()){
            rowLocation = grid.getRandomLocation();
        }

        PerformUpdateFailedResult pufr = performUpdate(withDataModify, rowLocation, spyDatasource);

        if(showOnlyDeletable != null && !showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
            grid.waitForRefresh();
        }

        DoUpdateFailedTestData duftd = new DoUpdateFailedTestData();
        duftd.setResult(pufr);
        return duftd;
    }

    @Override
    public DoRestoreTestData doDatabaseNotAvailableWhenRestoreTest(DataSource spyDatasource) throws SQLException {
        return null;
    }

    @Override
    public DoPermanentlyDeleteFailedTestData doDatabaseNotAvailableWhenPermanentlyDeleteTest(DataSource spyDataSource) throws SQLException {

        int originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        int originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        if(originalNonDeletedRowNumber == 0){
            performCreate(null);
        }

        originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);

        if(originalDeletedRowNumber == 0 && originalNonDeletedRowNumber == 0){
            performCreate(null);
            performDelete();
        }
        else if(originalDeletedRowNumber == 0) {
            performDelete();
        }
        VaadinNotificationComponent.closeAll(getDriver());



        originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        Integer originalNonDeletedOnlyDeletable = originalNonDeletedRowNumber;
        Integer originalDeletedOnlyDeletable = originalDeletedRowNumber;


        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            originalNonDeletedOnlyDeletable = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
            originalDeletedOnlyDeletable = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        }

        PerformPermanentlyDeleteFailedResult pdr = performPermanentlyDelete(spyDataSource);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        String notificationWhenPerform = notification.getText();
        notification.close();

        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
        }

        grid.resetFilter();

        showDeletedCheckBox.setStatus(false);
        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
        }

        Integer nonDeletedRowNumberAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        Integer deletedRowNumberAfterMethod = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        Integer deletedRowNumberOnlyDeletableAfterMethod = null;
        Integer nonDeletedRowNumberOnlyDeletableAfterMethod = null;

        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            deletedRowNumberOnlyDeletableAfterMethod = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
            nonDeletedRowNumberOnlyDeletableAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
            showDeletedCheckBox.setStatus(false);
            showOnlyDeletable.setStatus(false);
        }

        DoPermanentlyDeleteFailedTestData ret = new DoPermanentlyDeleteFailedTestData(originalNonDeletedRowNumber,
                originalDeletedRowNumber,
                originalNonDeletedOnlyDeletable,
                originalDeletedOnlyDeletable,
                notificationWhenPerform,
                pdr,
                nonDeletedRowNumberAfterMethod,
                deletedRowNumberAfterMethod,
                nonDeletedRowNumberOnlyDeletableAfterMethod,
                deletedRowNumberOnlyDeletableAfterMethod);
        return ret;
    }


    @Override
    public DoDeleteFailedTestData doDatabaseNotAvailableWhenDeleteTest(DataSource spyDataSource) throws SQLException {
//        DoDeleteTestData ret = new DoDeleteTestData();

        int originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        if(originalNonDeletedRowNumber == 0){
            performCreate(null);
        }

        originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        Integer originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        Integer originalNonDeletedOnlyDeletable = originalNonDeletedRowNumber;
        Integer originalDeletedOnlyDeletable = originalDeletedRowNumber;


        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            originalNonDeletedOnlyDeletable = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
            originalDeletedOnlyDeletable = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        }

        PerformDeleteResult pdr = performDelete(spyDataSource);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        String notificationWhenPerform = notification.getText();
        notification.close();

        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
        }

        grid.resetFilter();

        showDeletedCheckBox.setStatus(false);
        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(false);
        }

        Integer nonDeletedRowNumberAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
        Integer deletedRowNumberAfterMethod = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
        Integer deletedRowNumberOnlyDeletableAfterMethod = null;
        Integer nonDeletedRowNumberOnlyDeletableAfterMethod = null;

        if(!showOnlyDeletable.isNull()){
            showOnlyDeletable.setStatus(true);
            deletedRowNumberOnlyDeletableAfterMethod = grid.getTotalDeletedRowNumber(showDeletedCheckBox);
            nonDeletedRowNumberOnlyDeletableAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedCheckBox);
            showDeletedCheckBox.setStatus(false);
            showOnlyDeletable.setStatus(false);
        }

        DoDeleteFailedTestData ret = new DoDeleteFailedTestData(originalNonDeletedRowNumber,
                originalDeletedRowNumber,
                originalNonDeletedOnlyDeletable,
                originalDeletedOnlyDeletable,
                notificationWhenPerform,
                pdr,
                nonDeletedRowNumberAfterMethod,
                deletedRowNumberAfterMethod,
                nonDeletedRowNumberOnlyDeletableAfterMethod,
                deletedRowNumberOnlyDeletableAfterMethod);
        return ret;
    }

    @Override
    public void logout(){
        //TODO
    }
}
