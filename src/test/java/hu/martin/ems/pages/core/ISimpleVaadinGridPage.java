package hu.martin.ems.pages.core;

import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinCheckboxComponent;
import hu.martin.ems.pages.core.component.VaadinGridComponent;
import hu.martin.ems.pages.core.doTestData.*;
import hu.martin.ems.pages.core.performResult.*;
import jakarta.validation.constraints.NotNull;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.LinkedHashMap;

public interface ISimpleVaadinGridPage<T> extends ILoggedInPage<T> {
    String showDeletedCheckBoxXpath = contentLayoutXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    String gridXpath = contentLayoutXpath + "/vaadin-grid";
    String createButtonXpath = contentLayoutXpath + "/vaadin-horizontal-layout/vaadin-button";

    @Override
    T initWebElements();

    VaadinCheckboxComponent getShowDeletedCheckBox();
    VaadinButtonComponent getCreateButton();
    VaadinGridComponent getGrid();


    PerformCreateResult performCreate(LinkedHashMap<String, Object> withData);
    PerformDeleteResult performDelete();

    PerformRestoreFailedResult performRestoreFailed(DataSource spyDataSource) throws SQLException;

    PerformRestoreResult performRestore();
    PerformPermanentlyDeleteResult performPermanentlyDelete();
    PerformUpdateResult performUpdate(LinkedHashMap<String, Object> withData, ElementLocation rowLocation);

    PerformUpdateFailedResult performUpdate(LinkedHashMap<String, Object> withData, @NotNull ElementLocation rowLocation, DataSource spyDatasource, Integer preSuccess) throws SQLException;

    DoCreateTestData doCreateTest();

    DoCreateTestData doCreateTest(LinkedHashMap<String, Object> withData);

    DoUpdateTestData doUpdateTest();

    DoUpdateTestData doUpdateTest(LinkedHashMap<String, Object> withDataModify);

    DoUpdateTestData doUpdateTest(LinkedHashMap<String, Object> withData1, LinkedHashMap<String, Object> withData2);

    DoDeleteTestData doDeleteTest();
    DoDeleteTestData doDeleteTest(LinkedHashMap<String, Object> withDataCreate);

    DoRestoreFailedTestData doRestoreFailedTest(DataSource spyDataSource) throws SQLException;

    DoRestoreTestData doRestoreTest();
    DoPermanentlyDeleteTestData doPermanentlyDeleteTest();
    DoReadTestData doReadTest(String extraDataFilter, boolean withInDeleted);


    DoFailedTestData doDatabaseNotAvailableWhenCreateTest(DataSource spyDatasource) throws SQLException;
    DoDeleteFailedTestData doDatabaseNotAvailableWhenDeleteTest(DataSource spyDatasource) throws SQLException;

    DoCreateFailedTestData doDatabaseNotAvailableWhenCreateTest(LinkedHashMap<String, Object> withData, DataSource spyDatasource) throws SQLException;

    DoUpdateFailedTestData doDatabaseNotAvailableWhenUpdateTest(LinkedHashMap<String, Object> withDataCreate, LinkedHashMap<String, Object> withDataModify, DataSource spyDatasource, Integer preSuccess) throws SQLException;

    DoRestoreTestData doDatabaseNotAvailableWhenRestoreTest(DataSource spyDatasource) throws SQLException;
    DoPermanentlyDeleteFailedTestData doDatabaseNotAvailableWhenPermanentlyDeleteTest(DataSource spyDatasource) throws SQLException;


}
