package hu.martin.ems.pages.core;

import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.core.component.*;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.VaadinSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import hu.martin.ems.pages.core.performResult.*;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class SimpleVaadinGridPage<T> extends VaadinPage implements ISimpleVaadinGridPage<T> {
    @Getter
    protected VaadinSwitchComponent showDeletedSwitch;

    @Getter
    protected VaadinButtonComponent createButton;
    @Getter
    protected VaadinGridComponent grid;
    @Getter
    protected VaadinSaveOrUpdateDialog saveOrUpdateDialog;
    @Getter
    protected SideMenu sideMenu;

    @Getter
    private String className;

    @Nullable
    @Getter
    private VaadinSwitchComponent showOnlyDeletable;

    public SimpleVaadinGridPage(WebDriver driver, int port, String className, @Nullable String showOnlyDeletable) {
        super(driver, port);
        this.className = className;
        this.showOnlyDeletable = showOnlyDeletable == null ? null : new VaadinSwitchComponent(driver, By.xpath(showOnlyDeletable));
        this.sideMenu = new SideMenu(driver);

    }

    @Override
    public PerformCreateResult performCreate(LinkedHashMap<String, Object> withData) {
        createButton.click();
        saveOrUpdateDialog.initWebElements();
        saveOrUpdateDialog.fill(withData);
        saveOrUpdateDialog.getSaveButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        String text = notification.getText();
        notification.close();
        PerformCreateResult res = new PerformCreateResult();
        res.setNotification(text);
        grid.waitForRefresh();
        return res;
    }

    private PerformCreateFailedResult performCreate(LinkedHashMap<String, Object> withData, DataSource spyDataSource, @Nullable Integer mockSetupFieldInDialog, Integer preSuccessMock) throws SQLException {
        if (mockSetupFieldInDialog != null) {
            MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, mockSetupFieldInDialog);
        }
        createButton.click();
        saveOrUpdateDialog.initWebElements();
        List<FailedVaadinFillableComponent> failedFieldsInSaveOrUpdateDialog =
                saveOrUpdateDialog.getAllComponent().stream().filter(v -> !v.isEnabled()).map(v -> new FailedVaadinFillableComponent(v)).toList();

        String notificationText = null;
        saveOrUpdateDialog.fill(withData);
        if (preSuccessMock != null) {
            MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, preSuccessMock);
        }
        if (mockSetupFieldInDialog == null) {
            saveOrUpdateDialog.waitForRefresh();
            saveOrUpdateDialog.getSaveButton().click();
            VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(getDriver());
            notificationText = notificationComponent.getText();
            notificationComponent.close();
        } else {
            saveOrUpdateDialog.close();
        }

        return new PerformCreateFailedResult(failedFieldsInSaveOrUpdateDialog, notificationText);
    }

    private PerformDeleteFailedResult performDelete(DataSource spyDataSource) throws SQLException {
        if (grid.getTotalNonDeletedRowNumber(showDeletedSwitch) == 0) {
            performCreate(null);
            VaadinNotificationComponent.closeAll(getDriver());
        }

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
        }

        VaadinButtonComponent deleteButton = null;
        String[] deletedData = null;
        while (deleteButton == null) {
            ElementLocation rowLocation = grid.getRandomLocation();
            if (rowLocation == null) {
                performCreate(null);
                rowLocation = new ElementLocation(1, 0);
            }
            grid.goToPage(rowLocation.getPageNumber());

            showDeletedSwitch.setStatus(false);

            String[] tempDeletedData = grid.getDataFromRowLocation(rowLocation, true);

            VaadinButtonComponent tempDeleteButton = grid.getDeleteButton(rowLocation.getRowIndex());
            if (tempDeleteButton.isEnabled()) {
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
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            withData = new LinkedHashMap<>();
            withData.put("Deletable", true);
        }
        showDeletedSwitch.init();
        if (grid.getTotalNonDeletedRowNumber(showDeletedSwitch) == 0) {
            performCreate(withData);
            VaadinNotificationComponent.closeAll(getDriver());
        }

        WebElement deleteButton = null;
        String[] deletedData = null;
        while (deleteButton == null) {
            ElementLocation rowLocation = grid.getRandomLocation();
            if (rowLocation == null) {
                performCreate(null);
                rowLocation = new ElementLocation(1, 0);
            }
            grid.goToPage(rowLocation.getPageNumber());

            showDeletedSwitch.setStatus(false);

            String[] tempDeletedData = grid.getDataFromRowLocation(rowLocation, true);

            VaadinButtonComponent tempDeleteButton = grid.getDeleteButton(rowLocation.getRowIndex());
            if (tempDeleteButton != null && tempDeleteButton.isEnabled()) {
                deleteButton = tempDeleteButton.getElement();
                deletedData = tempDeletedData;
            }
        }

        deleteButton.click();

        PerformDeleteFailedResult ret = new PerformDeleteFailedResult();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        ret.setOriginalDeletedData(deletedData);
        ret.setNotificationWhenPerform(notification.getText());
        notification.close();


        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
        }

        return ret;
    }

    @Override
    public PerformRestoreFailedResult performRestoreFailed(DataSource spyDataSource) throws SQLException {
        if (createButton != null && !createButton.isNull() &&
                grid.getTotalNonDeletedRowNumber(showDeletedSwitch) == 0 &&
                grid.getTotalDeletedRowNumber(showDeletedSwitch) == 0) {
            performCreate(null);
            VaadinNotificationComponent.closeAll(getDriver());
            performDelete();
            VaadinNotificationComponent.closeAll(getDriver());
        }

        showDeletedSwitch.setStatus(true);

        String[] deletedData = grid.getRandomDataDeletedStatus(showDeletedSwitch);
        grid.applyFilter(deletedData);
        ElementLocation deleted = new ElementLocation(1, 0);

        WebElement restoreButton = grid.getRestoreButton(deleted.getRowIndex());
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        restoreButton.click();

        grid.resetFilter();

        return new PerformRestoreFailedResult(deletedData);
    }

    @Override
    public PerformRestoreResult performRestore() {
        if (createButton != null && !createButton.isNull() &&
                grid.getTotalNonDeletedRowNumber(showDeletedSwitch) == 0 &&
                grid.getTotalDeletedRowNumber(showDeletedSwitch) == 0) {
            performCreate(null);
            VaadinNotificationComponent.closeAll(getDriver());
            performDelete();
        }

        showDeletedSwitch.setStatus(true);

        String[] deletedData = (Arrays.asList(grid.getRandomDataDeletedStatus(showDeletedSwitch)).stream().map(v -> v.equals("") ? null : v).toList()).toArray(new String[0]);
        grid.applyFilter(deletedData);
        ElementLocation deleted = new ElementLocation(1, 0);

        WebElement restoreButton = grid.getRestoreButton(deleted.getRowIndex());
        restoreButton.click();

        grid.resetFilter();

        return new PerformRestoreResult(deletedData);
    }

    @Override
    public PerformPermanentlyDeleteResult performPermanentlyDelete() {
        if (grid.getTotalDeletedRowNumber(showDeletedSwitch) == 0 && grid.getTotalNonDeletedRowNumber(showDeletedSwitch) == 0) {
            performCreate(null);
            performDelete();
        } else if (grid.getTotalDeletedRowNumber(showDeletedSwitch) == 0) {
            performDelete();
        }

        VaadinNotificationComponent.closeAll(getDriver());

        showDeletedSwitch.setStatus(true);
        this.initWebElements();

        String[] selectedData = grid.getRandomDataDeletedStatus(showDeletedSwitch);
        grid.applyFilter(selectedData);
        ElementLocation el = new ElementLocation(1, 0);

        VaadinButtonComponent pDeleteButton = grid.getPermanentlyDeleteButton(el.getRowIndex());
        pDeleteButton.click();

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
        }

        PerformPermanentlyDeleteResult ret = new PerformPermanentlyDeleteResult();

        ret.setPermanentlyDeletedData(selectedData);

        grid.resetFilter();
        return ret;
    }

    private PerformPermanentlyDeleteFailedResult performPermanentlyDelete(DataSource spyDatasource) throws SQLException {
        showDeletedSwitch.setStatus(true);

        String[] selectedData = grid.getRandomDataDeletedStatus(showDeletedSwitch);
        grid.applyFilter(selectedData);
        System.out.println("Selected data: " + Arrays.toString(selectedData));
        ElementLocation el = new ElementLocation(1, 0);

        VaadinButtonComponent pDeleteButton = grid.getPermanentlyDeleteButton(el.getRowIndex());
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDatasource, 0);
        pDeleteButton.click();

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
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
    public PerformUpdateFailedResult performUpdate(LinkedHashMap<String, Object> withData, @NotNull ElementLocation rowLocation, DataSource spyDatasource, Integer preSuccess) throws SQLException {
        grid.waitForRefresh();
        grid.goToPage(rowLocation.getPageNumber());
        String[] modifiedData = grid.getDataFromRowLocation(rowLocation, true);
        VaadinButtonComponent modifyButton = grid.getModifyButton(rowLocation.getRowIndex());
        modifyButton.click();
        saveOrUpdateDialog.initWebElements();
        saveOrUpdateDialog.fill(withData);
        List<FailedVaadinFillableComponent> failedData = saveOrUpdateDialog.getFailedComponents();

        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDatasource, preSuccess);
        saveOrUpdateDialog.getSaveButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        String notificationText = notification.getText();
        notification.close();

        return new PerformUpdateFailedResult(modifiedData, failedData, notificationText); //TODO failedFields
    }

    @Override
    public DoCreateTestData doCreateTest() {
        return doCreateTest(null);
    }

    @Override
    public DoCreateTestData doCreateTest(LinkedHashMap<String, Object> withData) {
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
        }
        DoCreateTestData ret = new DoCreateTestData();
        ret.setOriginalDeletedRowNumber(grid.getTotalDeletedRowNumber(showDeletedSwitch));
        ret.setOriginalNonDeletedRowNumber(grid.getTotalNonDeletedRowNumber(showDeletedSwitch));

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            ret.setOriginalNonDeletedOnlyDeletable(grid.getTotalNonDeletedRowNumber(showDeletedSwitch));
            ret.setOriginalDeletedOnlyDeletable(grid.getTotalDeletedRowNumber(showDeletedSwitch));
            showOnlyDeletable.setStatus(false);
        }

        PerformCreateResult pcRes = performCreate(withData);

//        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        ret.setNotificationWhenPerform(pcRes.getNotification());
//        notification.close();

        showDeletedSwitch.setStatus(false);

        ret.setDeletedRowNumberAfterMethod(grid.getTotalDeletedRowNumber(showDeletedSwitch));
        ret.setNonDeletedRowNumberAfterMethod(grid.getTotalNonDeletedRowNumber(showDeletedSwitch));
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            ret.setDeletedRowNumberOnlyDeletableAfterMethod(grid.getTotalDeletedRowNumber(showDeletedSwitch));
            ret.setNonDeletedRowNumberOnlyDeletableAfterMethod(grid.getTotalNonDeletedRowNumber(showDeletedSwitch));
            showOnlyDeletable.setStatus(false);
        }

        return ret;
    }


    @Override
    public DoUpdateTestData doUpdateTest() {
        return doUpdateTest(null, null);
    }

    @Override
    public DoUpdateTestData doUpdateTest(LinkedHashMap<String, Object> withDataModify) {
        return doUpdateTest(null, withDataModify);
    }

    @Override
    public DoUpdateTestData doUpdateTest(LinkedHashMap<String, Object> withDataCreate, LinkedHashMap<String, Object> withDataModify) {
        DoUpdateTestData ret = new DoUpdateTestData();

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            grid.waitForRefresh();
        }
        ElementLocation rowLocation = grid.getRandomLocation();
        if (rowLocation == null) {
            performCreate(withDataCreate);
        }
        while (!getGrid().getModifyButton(rowLocation.getRowIndex()).isEnabled()) {
            rowLocation = grid.getRandomLocation();
        }

        PerformUpdateResult pur = performUpdate(withDataModify, rowLocation);
        ret.setResult(pur);

        grid.resetFilter();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        ret.setNotificationWhenPerform(notification.getText());
        notification.close();

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
            grid.waitForRefresh();
        }

        return ret;
    }

    @Override
    public DoDeleteTestData doDeleteTest() {
        return doDeleteTest(null);
    }

    @Override
    public DoDeleteTestData doDeleteTest(LinkedHashMap<String, Object> withDataCreate) {
//        DoDeleteTestData ret = new DoDeleteTestData();

        int originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        if (originalNonDeletedRowNumber == 0) {
            performCreate(withDataCreate);
        }

        originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        Integer originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        Integer originalNonDeletedOnlyDeletable = originalNonDeletedRowNumber;
        Integer originalDeletedOnlyDeletable = originalDeletedRowNumber;


        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            originalNonDeletedOnlyDeletable = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
            originalDeletedOnlyDeletable = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        }


        PerformDeleteResult pdr = performDelete();

//        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
//        String notificationWhenPerform = notification.getText();
//        notification.close();


        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
        }

        grid.resetFilter();

        showDeletedSwitch.setStatus(false);
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
        }

        Integer nonDeletedRowNumberAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        Integer deletedRowNumberAfterMethod = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        Integer deletedRowNumberOnlyDeletableAfterMethod = null;
        Integer nonDeletedRowNumberOnlyDeletableAfterMethod = null;

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            deletedRowNumberOnlyDeletableAfterMethod = grid.getTotalDeletedRowNumber(showDeletedSwitch);
            nonDeletedRowNumberOnlyDeletableAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
            showDeletedSwitch.setStatus(false);
            showOnlyDeletable.setStatus(false);
        }

        DoDeleteTestData ret = new DoDeleteTestData(originalNonDeletedRowNumber,
                originalDeletedRowNumber,
                originalNonDeletedOnlyDeletable,
                originalDeletedOnlyDeletable,
                pdr.getNotificationWhenPerform(),
                pdr,
                nonDeletedRowNumberAfterMethod,
                deletedRowNumberAfterMethod,
                nonDeletedRowNumberOnlyDeletableAfterMethod,
                deletedRowNumberOnlyDeletableAfterMethod);
        return ret;
    }

    @Override
    public DoRestoreFailedTestData doRestoreFailedTest(DataSource spyDataSource) throws SQLException {
        Integer originalDeleted = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        Integer originalNonDeleted = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        Integer originalDeletedOnlyDeletable = originalDeleted;
        Integer originalNonDeletedOnlyDeletable = originalNonDeleted;

        if (originalDeletedOnlyDeletable == 0) {
            performDelete();
            grid.waitForRefresh();
            VaadinNotificationComponent.closeAll(getDriver());

            originalNonDeleted = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
            originalDeletedOnlyDeletable = originalDeleted;
            originalDeleted = grid.getTotalDeletedRowNumber(showDeletedSwitch);
            originalNonDeletedOnlyDeletable = originalNonDeleted;
        }
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            originalDeletedOnlyDeletable = grid.getTotalDeletedRowNumber(showDeletedSwitch);
            originalNonDeletedOnlyDeletable = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
            showOnlyDeletable.setStatus(false);
        }

        initWebElements();
        PerformRestoreFailedResult prr = performRestoreFailed(spyDataSource);

        String notificationWhenPerform = null;
        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        notificationWhenPerform = notification.getText();
        notification.close();

        Integer nonDeletedRowNumberAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        Integer deletedRowNumberAfterMethod = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        Integer deletedRowNumberOnlyDeletableAfterMethod = deletedRowNumberAfterMethod;
        Integer nonDeletedRowNumberOnlyDeletableAfterMethod = nonDeletedRowNumberAfterMethod;
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            deletedRowNumberOnlyDeletableAfterMethod = grid.getTotalDeletedRowNumber(showDeletedSwitch);
            nonDeletedRowNumberOnlyDeletableAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
            showOnlyDeletable.setStatus(false);
        }

        return new DoRestoreFailedTestData(originalNonDeleted,
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
    public DoRestoreTestData doRestoreTest() {
        Integer originalDeleted = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        Integer originalNonDeleted = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        Integer originalDeletedOnlyDeletable = originalDeleted;
        Integer originalNonDeletedOnlyDeletable = originalNonDeleted;

        if (originalDeletedOnlyDeletable == 0) {
            performDelete();
            grid.waitForRefresh();
            VaadinNotificationComponent.closeAll(getDriver());

            originalNonDeleted = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
            originalDeletedOnlyDeletable = originalDeleted;
            originalDeleted = grid.getTotalDeletedRowNumber(showDeletedSwitch);
            originalNonDeletedOnlyDeletable = originalNonDeleted;
        }
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            originalDeletedOnlyDeletable = grid.getTotalDeletedRowNumber(showDeletedSwitch);
            originalNonDeletedOnlyDeletable = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
            showOnlyDeletable.setStatus(false);
        }

        initWebElements();
        PerformRestoreResult prr = performRestore();

        String notificationWhenPerform = null;
        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        notificationWhenPerform = notification.getText();
        notification.close();

        Integer nonDeletedRowNumberAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        Integer deletedRowNumberAfterMethod = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        Integer deletedRowNumberOnlyDeletableAfterMethod = deletedRowNumberAfterMethod;
        Integer nonDeletedRowNumberOnlyDeletableAfterMethod = nonDeletedRowNumberAfterMethod;
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            deletedRowNumberOnlyDeletableAfterMethod = grid.getTotalDeletedRowNumber(showDeletedSwitch);
            nonDeletedRowNumberOnlyDeletableAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
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

        Integer originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        Integer originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedSwitch);
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
            originalNonDeletedOnlyDeletable = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
            originalDeletedOnlyDeletable = grid.getTotalDeletedRowNumber(showDeletedSwitch);
            showOnlyDeletable.setStatus(false);
        }

        originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedSwitch);

        PerformPermanentlyDeleteResult ppdr = performPermanentlyDelete();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        String notificationWhenPerform = notification.getText();
        notification.close();

        showDeletedSwitch.setStatus(false);

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
        }

        grid.waitForRefresh();
        Integer nonDeletedRowNumberAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        Integer deletedRowNumberAfterMethod = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        Integer deletedRowNumberOnlyDeletableAfterMethod = null;
        Integer nonDeletedRowNumberOnlyDeletableAfterMethod = null;

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            deletedRowNumberOnlyDeletableAfterMethod = grid.getTotalDeletedRowNumber(showDeletedSwitch);
            nonDeletedRowNumberOnlyDeletableAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
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
    public DoReadTestData doReadTest(boolean withInDeleted) {
        String notification = null;
        if (VaadinNotificationComponent.hasNotification(getDriver()) != null) {
            VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(getDriver());
            notification = notificationComponent.getText();
        }

        ElementLocation randomLocation = grid.getRandomLocation();
        String[] data = grid.getDataFromRowLocation(randomLocation, true);

        int originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        int originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        int originalNonDeletedOnlyDeletable = originalNonDeletedRowNumber;
        int originalDeletedOnlyDeletable = originalDeletedRowNumber;

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            originalNonDeletedOnlyDeletable = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
            originalDeletedOnlyDeletable = grid.getTotalDeletedRowNumber(showDeletedSwitch);
            showOnlyDeletable.setStatus(false);
        }

        if (originalNonDeletedRowNumber == 0) { //TODO: lehet hogy ide kell a deletable.
            performCreate(null);
            originalNonDeletedRowNumber++;
            originalNonDeletedOnlyDeletable++;
        }

        Boolean originalDeleted = showDeletedSwitch.getStatus();

        if (withInDeleted) {
            showDeletedSwitch.setStatus(true);
        }
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
        }
        showDeletedSwitch.setStatus(originalDeleted);

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
        return doDatabaseNotAvailableWhenCreateTest(null, spyDatasource);
    }

    public DoCreateFailedTestData doDatabaseNotAvailableWhenCreateTest(LinkedHashMap<String, Object> withData, DataSource spyDatasource, Integer preSuccessMock) throws SQLException {
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
        }

        DoCreateFailedTestData ret = new DoCreateFailedTestData();
        ret.setOriginalDeletedRowNumber(grid.getTotalDeletedRowNumber(showDeletedSwitch));
        ret.setOriginalNonDeletedRowNumber(grid.getTotalNonDeletedRowNumber(showDeletedSwitch));

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            ret.setOriginalNonDeletedOnlyDeletable(grid.getTotalNonDeletedRowNumber(showDeletedSwitch));
            ret.setOriginalDeletedOnlyDeletable(grid.getTotalDeletedRowNumber(showDeletedSwitch));
            showOnlyDeletable.setStatus(false);
        }

        PerformCreateFailedResult failedResult = performCreate(withData, spyDatasource, null, preSuccessMock);
        ret.setNotificationWhenPerform(failedResult.getNotificationText());

        showDeletedSwitch.setStatus(false);

        ret.setDeletedRowNumberAfterMethod(grid.getTotalDeletedRowNumber(showDeletedSwitch));
        ret.setNonDeletedRowNumberAfterMethod(grid.getTotalNonDeletedRowNumber(showDeletedSwitch));
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            ret.setDeletedRowNumberOnlyDeletableAfterMethod(grid.getTotalDeletedRowNumber(showDeletedSwitch));
            ret.setNonDeletedRowNumberOnlyDeletableAfterMethod(grid.getTotalNonDeletedRowNumber(showDeletedSwitch));
            showOnlyDeletable.setStatus(false);
        }
        ret.setResult(failedResult);
        return ret;
    }

    @Override
    public DoCreateFailedTestData doDatabaseNotAvailableWhenCreateTest(LinkedHashMap<String, Object> withData, DataSource spyDatasource) throws SQLException {
        return doDatabaseNotAvailableWhenCreateTest(withData, spyDatasource, 0);
    }

    @Override
    public DoUpdateFailedTestData doDatabaseNotAvailableWhenUpdateTest(LinkedHashMap<String, Object> withDataCreate, LinkedHashMap<String, Object> withDataModify, DataSource spyDatasource, Integer preSuccess) throws SQLException {
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            grid.waitForRefresh();
        }
        ElementLocation rowLocation = grid.getRandomLocation();
        if (rowLocation == null) {
            performCreate(withDataCreate);
            VaadinNotificationComponent.closeAll(getDriver());
            rowLocation = new ElementLocation(1, 0);
        }
        while (!grid.getModifyButton(rowLocation.getRowIndex()).isEnabled()) {
            rowLocation = grid.getRandomLocation();
        }

        PerformUpdateFailedResult pufr = performUpdate(withDataModify, rowLocation, spyDatasource, preSuccess);

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
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

        int originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        int originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        if (originalNonDeletedRowNumber == 0) {
            performCreate(null);
        }

        originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);

        if (originalDeletedRowNumber == 0 && originalNonDeletedRowNumber == 0) {
            performCreate(null);
            performDelete();
        } else if (originalDeletedRowNumber == 0) {
            performDelete();
        }
        VaadinNotificationComponent.closeAll(getDriver());


        originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        Integer originalNonDeletedOnlyDeletable = originalNonDeletedRowNumber;
        Integer originalDeletedOnlyDeletable = originalDeletedRowNumber;


        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            originalNonDeletedOnlyDeletable = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
            originalDeletedOnlyDeletable = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        }

        PerformPermanentlyDeleteFailedResult pdr = performPermanentlyDelete(spyDataSource);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        String notificationWhenPerform = notification.getText();
        notification.close();

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
        }

        grid.resetFilter();

        showDeletedSwitch.setStatus(false);
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
        }

        Integer nonDeletedRowNumberAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        Integer deletedRowNumberAfterMethod = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        Integer deletedRowNumberOnlyDeletableAfterMethod = null;
        Integer nonDeletedRowNumberOnlyDeletableAfterMethod = null;

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            deletedRowNumberOnlyDeletableAfterMethod = grid.getTotalDeletedRowNumber(showDeletedSwitch);
            nonDeletedRowNumberOnlyDeletableAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
            showDeletedSwitch.setStatus(false);
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
        int originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        if (originalNonDeletedRowNumber == 0) {
            performCreate(null);
        }

        originalNonDeletedRowNumber = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        Integer originalDeletedRowNumber = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        Integer originalNonDeletedOnlyDeletable = originalNonDeletedRowNumber;
        Integer originalDeletedOnlyDeletable = originalDeletedRowNumber;


        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            originalNonDeletedOnlyDeletable = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
            originalDeletedOnlyDeletable = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        }

        PerformDeleteResult pdr = performDelete(spyDataSource);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(getDriver());
        String notificationWhenPerform = notification.getText();
        notification.close();

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
        }

        grid.resetFilter();

        showDeletedSwitch.setStatus(false);
        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(false);
        }

        Integer nonDeletedRowNumberAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
        Integer deletedRowNumberAfterMethod = grid.getTotalDeletedRowNumber(showDeletedSwitch);
        Integer deletedRowNumberOnlyDeletableAfterMethod = null;
        Integer nonDeletedRowNumberOnlyDeletableAfterMethod = null;

        if (showOnlyDeletable != null && !showOnlyDeletable.isNull()) {
            showOnlyDeletable.setStatus(true);
            deletedRowNumberOnlyDeletableAfterMethod = grid.getTotalDeletedRowNumber(showDeletedSwitch);
            nonDeletedRowNumberOnlyDeletableAfterMethod = grid.getTotalNonDeletedRowNumber(showDeletedSwitch);
            showDeletedSwitch.setStatus(false);
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
    public void logout() {
        if (!getDriver().getCurrentUrl().contains("http://localhost:" + getPort() + "/login") &&
                !getDriver().getCurrentUrl().contains("data:,")) {
            getSideMenu().logout();
        }
    }
}
