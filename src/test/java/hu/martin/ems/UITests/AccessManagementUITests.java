package hu.martin.ems.UITests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AccessManagementUITests {

    private WebDriver driver;

    private WebDriverWait notificationDisappearWait;

    @Before
    public void setup() {
        driver = new ChromeDriver();
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void unauthorizedCredidentalsTest() {
        loginWith("unauthorized", "unauthorized");
        assertEquals("http://localhost:8080/login?error", driver.getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
    }



    @Test
    public void authorizedCredidentalsTest() throws InterruptedException {
        loginWith("admin", "admin");
        Thread.sleep(2000);
        assertEquals("http://localhost:8080/", driver.getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
    }

    @Test
    public void sideMenuElementsTest() {
        loginWith("admin", "admin");
        findClickableElementWithXpath(UIXpaths.SIDE_MENU);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(1000));
        WebElement adminMenu = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(UIXpaths.ADMIN_MENU)));
        WebElement ordersMenu = findClickableElementWithXpath(UIXpaths.ORDERS_MENU);

        assertEquals(false, adminSubMenusVisible());
        assertEquals(false, ordersSubMenusVisible());

        adminMenu.click();
        assertEquals(true, adminSubMenusVisible());
        assertEquals(false, ordersSubMenusVisible());
        
        adminMenu.click();
        assertEquals(false, adminSubMenusVisible());
        assertEquals(false, ordersSubMenusVisible());

        ordersMenu.click();
        assertEquals(false, adminSubMenusVisible());
        assertEquals(true, ordersSubMenusVisible());

        ordersMenu.click();
        assertEquals(false, adminSubMenusVisible());
        assertEquals(false, ordersSubMenusVisible());

        ordersMenu.click();
        adminMenu.click();
        assertEquals(true, adminSubMenusVisible());
        assertEquals(true, ordersSubMenusVisible());
    }

    @Test
    public void employeeCrudTest() throws InterruptedException {
        loginWith("admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.EMPLOYEE_SUBMENU);

        WebElement showDeletedCheckBox = findClickableElementWithXpathWithWaiting("//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-horizontal-layout/vaadin-checkbox");
        WebElement createButton = findClickableElementWithXpath("//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-horizontal-layout/vaadin-button");
        WebElement grid = findVisibleEventWithXpath("//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid");

        int originalVisible = countVisibleGridDataRows(grid);
        int originalInvisible = countHiddenGridDataRows(grid, showDeletedCheckBox);
        createEmployee(createButton);
        assertEquals(originalVisible + 1, countVisibleGridDataRows(grid));

        deleteEmployee(grid);
        grid = findVisibleEventWithXpath("//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid");
        assertEquals(originalVisible, countVisibleGridDataRows(grid));
        assertEquals(originalInvisible + 1, countHiddenGridDataRows(grid, showDeletedCheckBox));
        showDeletedCheckBox.click();
        assertEquals(originalInvisible + originalVisible + 1, countVisibleGridDataRows(grid));
        showDeletedCheckBox.click();
    }

    private void createEmployee(WebElement createButton) throws InterruptedException {
        createButton.click();
        findVisibleEventWithXpath("//*[@id=\"overlay\"]");
        WebElement firstNameField = findVisibleEventWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-text-field[1]/input");
        WebElement lastNameField = findVisibleEventWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-text-field[2]/input");
        WebElement salaryField = findVisibleEventWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-number-field/input");
        WebElement roleComboBox = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-combo-box/input");
        WebElement saveEmployeeButton = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        String firstName = generateRandomOnlyLetterString();
        String lastName = generateRandomOnlyLetterString();

        firstNameField.sendKeys(firstName);
        lastNameField.sendKeys(lastName);
        salaryField.sendKeys("20500");
        roleComboBox.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(400));

        List<WebElement> comboBoxOptions = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("vaadin-combo-box-item"))
        );

        for (WebElement option : comboBoxOptions) {
            if (option.getText().equals("Martin")) {
                option.click();
                break;
            }
        }

        saveEmployeeButton.click();
        checkNotificationText("Employee saved: " + firstName + " " + lastName);
        notificationDisappearWait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("/html/body/vaadin-notification-container/vaadin-notification-card")));
        assertNull(findVisibleEventWithXpath("//*[@id=\"resizerContainer\"]"));
        Thread.sleep(2000);
    }

    private void checkNotificationText(String excepted){
        WebElement notification = findVisibleEventWithXpath("/html/body/vaadin-notification-container/vaadin-notification-card");
        assertEquals(excepted, notification.getText());
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].remove();", notification);
    }


    //@Test
    public void deleteEmployee(WebElement grid) throws InterruptedException {
        int visibleRows = countVisibleGridDataRows(grid);
        assertNotEquals(0, visibleRows);
        Integer selectedRowIndex;
        if(visibleRows != 1){
            Random r = new Random();
            selectedRowIndex = r.nextInt(0, visibleRows - 1) % getGridPaginationData(grid).getPageSize();
        }
        else{
            selectedRowIndex = 1;
        }

        WebElement deleteButton = getDeleteButton(grid, selectedRowIndex);
        Thread.sleep(200);
        WebElement firstName = getVisibleGridCell(grid, selectedRowIndex, 0);
        WebElement lastName = getVisibleGridCell(grid, selectedRowIndex, 1);
        String name = firstName.getText() + " " + lastName.getText();
        deleteButton.click();
        checkNotificationText("Employee deleted: " + name);
        Thread.sleep(2000);
    }

    private void loginWith(String username, String password) {
        driver.get("http://localhost:8080/login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"input-vaadin-text-field-6\"]")));
        WebElement passwordField = driver.findElement(By.xpath("//*[@id=\"input-vaadin-password-field-7\"]"));
        WebElement loginButton = driver.findElement(By.xpath("//*[@id=\"vaadinLoginFormWrapper\"]/vaadin-button[1]"));
        WebElement forgotPasswordButton = driver.findElement(By.xpath("//*[@id=\"vaadinLoginFormWrapper\"]/vaadin-button[2]"));
        WebElement registerButton = driver.findElement(By.xpath("//*[@id=\"ROOT-2521314\"]/vaadin-vertical-layout/vaadin-button"));

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
    }

    private WebElement findClickableElementWithXpath(String xpath){
        try{
            return driver.findElement(By.xpath(xpath));
        }
        catch (NoSuchElementException | TimeoutException e){
            return null;
        }
    }

    private WebElement findClickableElementWithXpathWithWaiting(String xpath){
        try{
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(100));
            WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
            return registerButton;
        }
        catch (NoSuchElementException | TimeoutException e){
            return null;
        }
    }

    private WebElement findVisibleEventWithXpath(String xpath) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(200));
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        } catch (Exception e) {
            return null;
        }
    }

    private int countVisibleGridDataRows(WebElement grid) throws InterruptedException {
        WebElement parent = grid.findElement(By.xpath("./.."));
        Thread.sleep(100);
        String total = parent.findElement(By.tagName("span")).findElement(By.tagName("lit-pagination")).getDomAttribute("total");
        return Integer.parseInt(total);
    }

    private int countHiddenGridDataRows(WebElement grid, WebElement showDeletedWebElement) throws InterruptedException {
        int visible = countVisibleGridDataRows(grid);
        showDeletedWebElement.click();
        int visibleWithHidden = countVisibleGridDataRows(grid);
        showDeletedWebElement.click();
        return visibleWithHidden - visible;
    }

    private boolean adminSubMenusVisible(){
        WebElement employeeSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.EMPLOYEE_SUBMENU);
        WebElement accessManagementSubMenu = findClickableElementWithXpath(UIXpaths.ACESS_MANAGEMENT_SUBMENU);
        WebElement codestoreSubMenu = findClickableElementWithXpath(UIXpaths.CODESTORE_SUBMENU);
        WebElement citySubMenu = findClickableElementWithXpath(UIXpaths.CITY_SUBMENU);
        WebElement addressSubMenu = findClickableElementWithXpath(UIXpaths.ADDRESS_SUBMENU);
        WebElement customerSubMenu = findClickableElementWithXpath(UIXpaths.CUSTOMER_SUBMENU);
        WebElement productSubMenu = findClickableElementWithXpath(UIXpaths.PRODUCT_SUBMENU);
        WebElement supplierSubMenu = findClickableElementWithXpath(UIXpaths.SUPPLIER_SUBMENU);
        WebElement currencySubMenu = findClickableElementWithXpath(UIXpaths.CURRENCY_SUBMENU);

        return employeeSubMenu != null &&
                accessManagementSubMenu != null &&
                codestoreSubMenu != null &&
                citySubMenu != null &&
                addressSubMenu != null &&
                customerSubMenu != null &&
                productSubMenu != null &&
                supplierSubMenu != null &&
                currencySubMenu != null;
    }
    
    private boolean ordersSubMenusVisible(){
        WebElement orderElementSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.ORDER_ELEMENT_SUBMENU);
        WebElement orderSubMenu = findClickableElementWithXpath(UIXpaths.ORDER_SUBMENU);
        WebElement orderCreateMenu = findClickableElementWithXpath(UIXpaths.ORDER_CREATE_SUBMENU);
        return orderElementSubMenu != null &&
                orderSubMenu != null &&
                orderCreateMenu != null;
    }

    @After
    public void destroy(){
        driver.close();
    }

    private void navigateMenu(String mainUIXpath, String subIXpath){
        findClickableElementWithXpath(UIXpaths.SIDE_MENU);
        WebElement adminMenu = findClickableElementWithXpathWithWaiting(mainUIXpath);
        adminMenu.click();
        WebElement employeeSubMenu = findClickableElementWithXpathWithWaiting(subIXpath);
        employeeSubMenu.click();
    }

    private int getGridColumnNumber(WebElement grid){
        WebElement e2 = grid.getShadowRoot().findElement(By.id("scroller"));
        WebElement e3 = e2.findElement(By.id("table"));
        WebElement e4 = e3.findElement(By.id("header"));
        WebElement e5 = e4.findElements(By.xpath(".//tr")).get(0);
        List<WebElement> headers = e5.findElements(By.xpath(".//th"));
        return headers.size();
    }

    private WebElement getVisibleGridRow(WebElement grid, int rowIndex){
        WebElement e2 = grid.getShadowRoot().findElement(By.id("scroller"));
        WebElement e3 = e2.findElement(By.id("table"));
        WebElement e4 = e3.findElement(By.id("items"));
        List<WebElement> allRows = e4.findElements(By.tagName("tr"));
        allRows = allRows.stream().filter(v -> v.isDisplayed()).toList();
        WebElement row = allRows.get(rowIndex);
        return row;
    }

    private PaginationData getGridPaginationData(WebElement grid){
        WebElement parent = getParent(grid);
        WebElement paginationComponent = parent.findElement(By.tagName("span")).findElement(By.tagName("lit-pagination"));
        Integer total = Integer.parseInt(paginationComponent.getDomAttribute("total"));
        Integer currentPage = Integer.parseInt(paginationComponent.getDomAttribute("page"));
        Integer pageSize = Integer.parseInt(paginationComponent.getDomAttribute("limit"));
        return new PaginationData(pageSize, total, currentPage);
    }

    private WebElement getParent(WebElement grid){
        return grid.findElement(By.xpath("./.."));
    }

    private WebElement getVisibleGridCell(WebElement grid, int rowIndex, int columnIndex){
        WebElement row = getVisibleGridRow(grid, rowIndex);
        List<WebElement> rowElements = row.findElements(By.xpath(".//td"));
        return rowElements.get(columnIndex);
    }

    private WebElement getDeleteButton(WebElement grid, int rowIndex){
        int optionsColumnIndex = getGridColumnNumber(grid) - 1;
        WebElement optionsCell = getVisibleGridCell(grid, rowIndex, optionsColumnIndex);
        WebElement deleteButton = optionsCell.findElements(By.xpath("//vaadin-icon[@icon='vaadin:trash']/parent::vaadin-button")).get(rowIndex);
        return deleteButton;
    }

    @Deprecated
    @Test
    public void testGridPagination(){
        loginWith("admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.EMPLOYEE_SUBMENU);
        
    }

    private String generateRandomOnlyLetterString(){
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }

//    //TODO megcsinálni, hogy tudjak lapozni a grid-ben.
//
//    private WebElement getNextPageButtonInPaginatedGrid(WebElement grid){
//        WebElement parent = grid.findElement(By.xpath("./.."));
//        WebElement paginationComponent = parent.findElement(By.tagName("span")).findElement(By.tagName("lit-pagination"));
//        WebElement paginatorComponent = paginationComponent.getShadowRoot().findElement(By.id("paginator-page-container"));
//        return paginatorComponent.findElement(By.id("navigateNextId"));
//    }
//
//    private WebElement getPreviousPageButtonInPaginatedGrid(WebElement grid){
//        WebElement parent = grid.findElement(By.xpath("./.."));
//        WebElement paginationComponent = parent.findElement(By.tagName("span")).findElement(By.tagName("lit-pagination"));
//        return paginationComponent.findElement(By.id("navigateBeforeId"));
//    }
//
//    private WebElement goToPageInPaginatedGrid(WebElement grid, int requiredPageNumber) throws InterruptedException {
//        if(getGridPaginationData(grid).getCurrentPage() < requiredPageNumber){
//            WebElement nextPageButton = getNextPageButtonInPaginatedGrid(grid);
//            while(getGridPaginationData(grid).getCurrentPage() != requiredPageNumber){
//                nextPageButton.click();
//                Thread.sleep(100);
//            }
//        }
//        if(getGridPaginationData(grid).getCurrentPage() > requiredPageNumber){
//            WebElement nextPageButton = getPreviousPageButtonInPaginatedGrid(grid);
//            while(getGridPaginationData(grid).getCurrentPage() != requiredPageNumber){
//                nextPageButton.click();
//                Thread.sleep(100);
//            }
//        }
//        return grid;
//    }
}
