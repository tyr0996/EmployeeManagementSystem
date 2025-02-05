package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.model.Address;
import lombok.Getter;
import org.mockito.Mockito;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.mockito.ArgumentMatchers.any;
import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)

public class AddressTest extends BaseCrudTest {
    @Getter
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    @Getter
    private static final String showDeletedChecBoxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    @Getter
    private static final String gridXpath = contentXpath + "/vaadin-grid";

    @Getter
    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-button";
    @Getter
    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    @Getter
    private static final String subMenu = UIXpaths.ADDRESS_SUBMENU;
    
    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Address", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
    }

    @Test
    public void addressCreateTest() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
        crudTestingUtil.createTest();
    }

    @Test
    public void addressReadTest() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
    }

    @Test
    public void addressDeleteTest() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void addressUpdateTest() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
    }

    @Test
    public void addressRestoreTest() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
    public void addressPermanentlyDeleteTest() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
    }
    @Test
    public void nullResponseWhenGettingAllAddress() throws InterruptedException {
//        Mockito.doReturn(null).when(spyAddressService).findAll();
        Mockito.doReturn(null).when(spyAddressService).findAll(any(Boolean.class)); //ApiClientben findAllWithDeleted

//        MockitoAnnotations.openMocks(this);
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        checkNotificationText("Error happened while getting addresses");
        assertEquals(0, countVisibleGridDataRows(gridXpath));
        assertEquals(0, countHiddenGridDataRows(gridXpath, showDeletedChecBoxXpath));
    }

    @Test
    public void nullResponseWhenGettingStreetTypes() throws InterruptedException {
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
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Country code", "Error happened while getting countries");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    public void nullResponseFromServiceWhenModify() throws InterruptedException {
        Mockito.doReturn(null).when(spyAddressService).update(any(Address.class));
//        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Address modifying failed: internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void nullResponseFromServiceWhenCreate() throws InterruptedException {
        Mockito.doReturn(null).when(spyAddressService).save(any(Address.class));
//        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "Address saving failed: internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @AfterClass
    public void afterClass() throws InterruptedException {
        logout();
    }

}
