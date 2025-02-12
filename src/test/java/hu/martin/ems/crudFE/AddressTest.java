package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.model.Address;
import lombok.Getter;
import org.mockito.Mockito;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)

public class AddressTest extends BaseCrudTest {
    @Getter
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    @Getter
    private static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    @Getter
    private static final String gridXpath = contentXpath + "/vaadin-grid";

    @Getter
    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-button";
    @Getter
    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    @Getter
    private static final String subMenu = UIXpaths.ADDRESS_SUBMENU;

    private void loginAndNavigate() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
    }
    
    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Address", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
    }

    @Test
    public void addressCreateTest() throws InterruptedException {
        loginAndNavigate();
        crudTestingUtil.createTest();
    }

    @Test
    public void addressReadTest() throws InterruptedException {
        loginAndNavigate();
        crudTestingUtil.readTest();
    }

    @Test
    public void addressDeleteTest() throws InterruptedException {
        loginAndNavigate();
        crudTestingUtil.deleteTest();
    }

    @Test
    public void addressUpdateTest() throws InterruptedException {
        loginAndNavigate();
        crudTestingUtil.updateTest();
    }

    @Test
    public void addressRestoreTest() throws InterruptedException {
        loginAndNavigate();
        crudTestingUtil.restoreTest();
    }

    @Test
    public void addressPermanentlyDeleteTest() throws InterruptedException {
        loginAndNavigate();
        crudTestingUtil.permanentlyDeleteTest();
    }
    @Test
    public void nullResponseWhenGettingAllAddress() throws InterruptedException {
//        logout();

//        Mockito.doReturn(null).when(spyAddressService).findAll();
         //ApiClientben findAllWithDeleted
//        Mockito.when(spyAddressService.findAll(anyBoolean())).thenReturn(null);
//        MockitoAnnotations.openMocks(this);
        loginAndNavigate();

        Mockito.doReturn(null).when(spyAddressService).findAll(anyBoolean());
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        checkNotificationText("Error happened while getting addresses");
        assertEquals(0, countVisibleGridDataRows(gridXpath));
        assertEquals(0, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        closeNotification(100);
        closeNotification(100);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void nullResponseWhenGettingStreetTypes() throws InterruptedException {
        loginAndNavigate();
        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.STREET_TYPES_CODESTORE_ID);

//        MockitoAnnotations.openMocks(this);
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Street type", "Error happened while getting street types");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    public void nullResponseWhenGettingCities() throws InterruptedException {
        loginAndNavigate();
//        Mockito.doReturn(new EmsResponse(522, "Error happened while getting cities")).when(spyCityApiClient).findAllByIds();
        Mockito.doReturn(null).when(spyCityService).findAll(any(Boolean.class));
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("City", "Error happened while getting cities");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    public void nullResponseWhenGettingCountries() throws InterruptedException {
        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.COUNTRIES_CODESTORE_ID);
        loginAndNavigate();

        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Country code", "Error happened while getting countries");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    public void nullResponseFromServiceWhenModify() throws InterruptedException {
        Mockito.doReturn(null).when(spyAddressService).update(any(Address.class));
        loginAndNavigate();
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Address modifying failed: internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void nullResponseFromServiceWhenCreate() throws InterruptedException {
        Mockito.doReturn(null).when(spyAddressService).save(any(Address.class));
        loginAndNavigate();
        crudTestingUtil.createTest(null, "Address saving failed: internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @AfterMethod
    public void afterMethod() throws InterruptedException {
        closeCreateDialogIfNeeded();
        logout();
    }

    private void closeCreateDialogIfNeeded() throws InterruptedException {
        WebElement dialog = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay");
        if(dialog != null){
            WebElement closeButton = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/div/div/vaadin-button");
            closeButton.click();
        }
        Thread.sleep(100);
    }
}
