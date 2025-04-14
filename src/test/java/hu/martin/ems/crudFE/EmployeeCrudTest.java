package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.EmployeePage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.EmployeeSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeCrudTest extends BaseCrudTest {
    @Test
    public void employeeCreateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.EMPLOYEE_SUBMENU);

        EmployeePage employeePage = new EmployeePage(driver, port);
        DoCreateTestData testResult = employeePage.doCreateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Employee saved: ");
    }

    @Test
    public void employeeReadTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.EMPLOYEE_SUBMENU);

        EmployeePage employeePage = new EmployeePage(driver, port);
        DoReadTestData testResult = employeePage.doReadTest(null, true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
    }

    @Test
    public void employeeDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.EMPLOYEE_SUBMENU);

        EmployeePage employeePage = new EmployeePage(driver, port);
        DoDeleteTestData testResult = employeePage.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Employee deleted: ");

        employeePage.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, employeePage.getGrid().getTotalDeletedRowNumber(employeePage.getShowDeletedCheckBox()));
        assertEquals(0, employeePage.getGrid().getTotalNonDeletedRowNumber(employeePage.getShowDeletedCheckBox()));
        employeePage.getGrid().resetFilter();
    }

    @Test
    public void employeeUpdateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.EMPLOYEE_SUBMENU);

        EmployeePage employeePage = new EmployeePage(driver, port);
        DoUpdateTestData testResult = employeePage.doUpdateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Employee updated: ");

        employeePage.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, employeePage.getGrid().getTotalDeletedRowNumber(employeePage.getShowDeletedCheckBox()));
        assertEquals(0, employeePage.getGrid().getTotalNonDeletedRowNumber(employeePage.getShowDeletedCheckBox()));
        employeePage.getGrid().resetFilter();
    }

    @Test
    public void employeeRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.EMPLOYEE_SUBMENU);

        EmployeePage employeePage = new EmployeePage(driver, port);
        DoRestoreTestData testResult = employeePage.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Employee restored: ");

        employeePage = new EmployeePage(driver, port);
        employeePage.getGrid().applyFilter(testResult.getResult().getRestoredData());
        employeePage.getGrid().waitForRefresh();
        assertEquals(employeePage.getGrid().getTotalDeletedRowNumber(employeePage.getShowDeletedCheckBox()), 0);
        assertEquals(employeePage.getGrid().getTotalNonDeletedRowNumber(employeePage.getShowDeletedCheckBox()), 1);
        employeePage.getGrid().resetFilter();

    }

    @Test
    public void employeePermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.EMPLOYEE_SUBMENU);

        EmployeePage employeePage = new EmployeePage(driver, port);
        DoPermanentlyDeleteTestData testResult = employeePage.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("Employee permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        employeePage.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, employeePage.getGrid().getTotalDeletedRowNumber(employeePage.getShowDeletedCheckBox()));
        assertEquals(0, employeePage.getGrid().getTotalNonDeletedRowNumber(employeePage.getShowDeletedCheckBox()));
        employeePage.getGrid().resetFilter();

        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Clearing database was successful");
        notificationComponent.close();
    }

    @Test
    public void gettingUsersFailed() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 1);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.EMPLOYEE_SUBMENU);

        EmployeePage employeePage = new EmployeePage(driver, port);
        employeePage.getCreateButton().click();

        EmployeeSaveOrUpdateDialog dialog = new EmployeeSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        List<FailedVaadinFillableComponent> failedComponents = dialog.getFailedComponents();
        assertEquals(failedComponents.size(), 1);
        assertEquals(failedComponents.get(0).getErrorMessage(), "Error happened while getting users");
    }

    @Test
    public void gettingEmployeesFailed() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 0);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.EMPLOYEE_SUBMENU);

        EmployeePage employeePage = new EmployeePage(driver, port);
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Error happened while getting employees");
        notificationComponent.close();

        assertEquals(employeePage.getGrid().getTotalDeletedRowNumber(employeePage.getShowDeletedCheckBox()), 0);
        assertEquals(employeePage.getGrid().getTotalNonDeletedRowNumber(employeePage.getShowDeletedCheckBox()), 0);
        
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.EMPLOYEE_SUBMENU);

        EmployeePage employeePage = new EmployeePage(driver, port);
        DoDeleteFailedTestData testResult = employeePage.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Internal Server Error");
    }

    @Test
    public void databaseNotAvailableWhilePermanentlyDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.EMPLOYEE_SUBMENU);

        EmployeePage employeePage = new EmployeePage(driver, port);
        DoPermanentlyDeleteFailedTestData testResult = employeePage.doDatabaseNotAvailableWhenPermanentlyDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Employee permanently deletion failed: Internal Server Error");
    }

    @Test
    public void databaseNotAvailableWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.EMPLOYEE_SUBMENU);

        EmployeePage employeePage = new EmployeePage(driver, port);
        DoUpdateFailedTestData testResult = employeePage.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("Employee modifying failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    public void databaseNotAvailableWhenCreate() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.EMPLOYEE_SUBMENU);

        EmployeePage employeePage = new EmployeePage(driver, port);
        DoCreateFailedTestData testResult = employeePage.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Employee saving failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    public void dialogClosingTest(){
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.EMPLOYEE_SUBMENU);

        EmployeePage employeePage = new EmployeePage(driver, port);

        employeePage.getCreateButton().click();
        EmployeeSaveOrUpdateDialog dialog = new EmployeeSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        dialog.close();
    }
}
