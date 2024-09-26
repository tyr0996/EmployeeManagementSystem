package hu.martin.ems.base;

import hu.martin.ems.PaginatorComponents;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.PaginationData;
import hu.martin.ems.UITests.UIXpaths;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GridTestingUtil {

    public static WebDriver driver;

    public static WebElement goToPageInPaginatedGrid(String gridXpath, int requiredPageNumber) throws InterruptedException {
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        PaginatorComponents paginatorComponents = new PaginatorComponents(grid, driver);
        int needMoves = requiredPageNumber - paginatorComponents.getCurrentPageNumber();
        if (needMoves > 0){
            for(int i = 0; i < needMoves; i++){
                if(paginatorComponents.getNextButton().isEnabled()){
                    try{
                        paginatorComponents.getNextButton().click();
                    } catch (Exception e) {}
                }
                paginatorComponents.refresh();
                Thread.sleep(50);
            }
        }
        else if(needMoves < 0){
            for(int i = 0; i < Math.abs(needMoves); i++){
                if(paginatorComponents.getPreviousButton().isEnabled()){
                    try{
                        paginatorComponents.getPreviousButton().click();
                    } catch (Exception e) {}
                }
                paginatorComponents.refresh();
                Thread.sleep(50);
            }
        }
        return grid;
    }

    public static WebElement getRowAtPosition(String gridXpath, ElementLocation location){
        return getVisibleGridRow(gridXpath, location.getRowIndex());
    }

    public static void navigateMenu(String mainUIXpath, String subIXpath){
        findClickableElementWithXpath(UIXpaths.SIDE_MENU);
        WebElement adminMenu = findClickableElementWithXpathWithWaiting(mainUIXpath);
        adminMenu.click();
        WebElement employeeSubMenu = findClickableElementWithXpathWithWaiting(subIXpath);
        employeeSubMenu.click();
    }

    public static int getGridColumnNumber(String gridXpath){
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        WebElement e2 = grid.getShadowRoot().findElement(By.id("scroller"));
        WebElement e3 = e2.findElement(By.id("table"));
        WebElement e4 = e3.findElement(By.id("header"));
        WebElement e5 = e4.findElements(By.xpath(".//tr")).get(0);
        List<WebElement> headers = e5.findElements(By.xpath(".//th"));
        return headers.size();
    }

    public static WebElement getVisibleGridRow(String gridXpath, int rowIndex){
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        WebElement e2 = grid.getShadowRoot().findElement(By.id("scroller"));
        WebElement e3 = e2.findElement(By.id("table"));
        WebElement e4 = e3.findElement(By.id("items"));
        List<WebElement> allRows = e4.findElements(By.tagName("tr"));
        allRows = allRows.stream().filter(v -> v.isDisplayed()).toList();
        if(rowIndex < allRows.size()){
            return allRows.get(rowIndex);
        }
        return null;
    }

    public static PaginationData getGridPaginationData(String gridXpath){
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        WebElement parent = TestingUtils.getParent(grid);
        WebElement paginationComponent = parent.findElement(By.tagName("span")).findElement(By.tagName("lit-pagination"));
        Integer total = Integer.parseInt(paginationComponent.getDomAttribute("total"));
        Integer currentPage = Integer.parseInt(paginationComponent.getDomAttribute("page"));
        Integer pageSize = Integer.parseInt(paginationComponent.getDomAttribute("limit"));
        Integer numberOfPages = (int) Math.ceil((double) total / (double) pageSize);
        return new PaginationData(pageSize, total, currentPage, numberOfPages);
    }



    public static WebElement getVisibleGridCell(String gridXpath, int rowIndex, int columnIndex){
        WebElement row = getVisibleGridRow(gridXpath, rowIndex);
        List<WebElement> rowElements = row.findElements(By.xpath(".//td"));
        return rowElements.get(columnIndex);
    }

    public static WebElement getVisibleGridCell(WebElement row, int columnIndex){
        List<WebElement> rowElements = row.findElements(By.xpath(".//td"));
        return rowElements.get(columnIndex);
    }

    public static WebElement getDeleteButton(String gridXpath, int rowIndex){
        try{
            WebElement grid = findVisibleElementWithXpath(gridXpath);
            int optionsColumnIndex = getGridColumnNumber(gridXpath) - 1;
            WebElement optionsCell = getVisibleGridCell(gridXpath, rowIndex, optionsColumnIndex);
            WebElement deleteButton = optionsCell.findElements(By.xpath("//vaadin-icon[@icon='vaadin:trash']/parent::vaadin-button")).get(rowIndex);
            return deleteButton;
        }
        catch (Exception e){
            return null;
        }
    }

    public static WebElement getModifyButton(String gridXpath, int rowIndex){
        try{
            WebElement grid = findVisibleElementWithXpath(gridXpath);
            int optionsColumnIndex = getGridColumnNumber(gridXpath) - 1;
            WebElement optionsCell = getVisibleGridCell(gridXpath, rowIndex, optionsColumnIndex);
            WebElement modifyButton = optionsCell.findElements(By.xpath("//vaadin-icon[contains(@src, 'edit')]")).get(rowIndex);
            return modifyButton;
        }
        catch (Exception e){
            return null;
        }
    }

    public static WebElement getPermanentlyDeleteButton(String gridXpath, int rowIndex){
        try{
            WebElement grid = findVisibleElementWithXpath(gridXpath);
            int optionsColumnIndex = getGridColumnNumber(gridXpath) - 1;
            WebElement optionsCell = getVisibleGridCell(gridXpath, rowIndex, optionsColumnIndex);
            WebElement permanentlyDeleteButton = optionsCell.findElements(By.xpath("//vaadin-icon[contains(@src, 'clear')]")).get(0);
            return permanentlyDeleteButton;
        }
        catch (Exception e){
            return null;
        }
    }

    public static WebElement getRestoreButton(String gridXpath, int rowIndex){
        try{
            WebElement grid = findVisibleElementWithXpath(gridXpath);
            int optionsColumnIndex = getGridColumnNumber(gridXpath) - 1;
//            WebElement optionsCell = getVisibleGridCell(gridXpath, rowIndex, optionsColumnIndex).findElements(By.xpath(".//*")).get(0);
//            //WebElement restoreButton = optionsCell.findElements(By.xpath("//vaadin-icon[@icon='vaadin:backwards']/parent::vaadin-button")).get(rowIndex);
//            List<WebElement> buttons = optionsCell.findElements(By.xpath(".//*"));
//            for(int i = 0; i < buttons.size(); i++){
//                if(buttons.get(i).findElement(By.tagName("vaadin-icon")).getDomAttribute("icon").equals("vaadin:backwards")){
//                    return buttons.get(i);
//                }
//            }
//
//            Oszlopok = 4;
//            sorindex = 4;
//            optionscolumnIndex = 3;
//
//            2*oszlopok (üres) + 1*oszlopok (fejléc) + sorindex * oszlopok + oszlopindex + 1
//
            int gridCellIndex = (3 + rowIndex) * getGridColumnNumber(gridXpath) + optionsColumnIndex + 1;

            return findClickableElementWithXpath("/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid/vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[2]");
            //return null;
        }
        catch (Exception e){
            return null;
        }
    }

    public static WebElement findClickableElementWithXpath(String xpath){
        try{
            return driver.findElement(By.xpath(xpath));
        }
        catch (NoSuchElementException | TimeoutException e){
            return null;
        }
    }

    public static WebElement findClickableElementWithXpathWithWaiting(String xpath){
        try{
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(100));
            WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
            return registerButton;
        }
        catch (NoSuchElementException | TimeoutException e){
            return null;
        }
    }

    public static WebElement findVisibleElementWithXpath(String xpath) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(200));
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        } catch (Exception e) {
            return null;
        }
    }

    public static int countVisibleGridDataRows(String gridXpath, String showDeletedXpath) throws InterruptedException {
        WebElement grid = findVisibleElementWithXpath(gridXpath);
//        WebElement showDeletedButton = findVisibleElementWithXpath(showDeletedXpath);
//        if(showDeletedButton.isSelected()){
//            showDeletedButton.click();
//        }
        WebElement parent = grid.findElement(By.xpath("./.."));
        Thread.sleep(100);
        String total = parent.findElement(By.tagName("span")).findElement(By.tagName("lit-pagination")).getDomAttribute("total");
        return Integer.parseInt(total);
    }

    public static int countHiddenGridDataRows(String gridXpath, String showDeletedXpath) throws InterruptedException {
        WebElement showDeletedElement = findClickableElementWithXpath(showDeletedXpath);
        int visible = countVisibleGridDataRows(gridXpath, showDeletedXpath);
        showDeletedElement.click();
        int visibleWithHidden = countVisibleGridDataRows(gridXpath, showDeletedXpath);
        showDeletedElement.click();
        return visibleWithHidden - visible;
    }

    public static int countVisibleGridDataRowsOnPage(String gridXpath){
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        WebElement e2 = grid.getShadowRoot().findElement(By.id("scroller"));
        WebElement e3 = e2.findElement(By.id("table"));
        WebElement e4 = e3.findElement(By.id("items"));
        List<WebElement> e5 = e4.findElements(By.tagName("tr"));
        return e5.stream().filter(v -> v.isDisplayed()).toList().size();
    }

    public static ElementLocation getRandomLocationFromGrid(String gridXpath, String showDeletedXpath) throws InterruptedException {
        int elementNumber = countVisibleGridDataRows(gridXpath, showDeletedXpath);
        Random rnd = new Random();
        Integer selectedElementIndex;
        if(elementNumber == 0){
            return null;
        }
        else if(elementNumber > 1){
            selectedElementIndex = rnd.nextInt(0, elementNumber - 1); //TODO lehet, hogy ezt ki kell venni
        }
        else{
            return new ElementLocation(1, 0);
        }
        int pageNumber;
        if(selectedElementIndex != 0){
            pageNumber = (int) Math.ceil((double)selectedElementIndex / (double) getGridPaginationData(gridXpath).getPageSize());
        }
        else{
            pageNumber = 1;
        }

        int rowIndex = selectedElementIndex % getGridPaginationData(gridXpath).getPageSize();

        return new ElementLocation(pageNumber, rowIndex);
    }

    public static ElementLocation getRandomLocationDeletedStatusFromGrid(String gridXpath, String showDeletedXpath) throws InterruptedException {
        int originalElements = countVisibleGridDataRows(gridXpath, showDeletedXpath);
        int deletedElements = countHiddenGridDataRows(gridXpath, showDeletedXpath);
        WebElement showDeleted = findClickableElementWithXpathWithWaiting(showDeletedXpath);
        boolean originalShowDeleted = getCheckboxStatus(showDeletedXpath);
        if(!originalShowDeleted){
            showDeleted.click();
            Thread.sleep(100);
        }
        if(deletedElements == 0){
            return null;
        }
        List<ElementLocation> allDeleted = getDeletedRowsInGrid(gridXpath);
        ElementLocation selected = null;
        if(allDeleted.size() == 0){
            throw new RuntimeException("There isn't any deleted rows in the grid.\n\nSolution1: delete an element first");
        }
        else if(allDeleted.size() == 1){
            selected = allDeleted.get(0);
        }
        else{
            Random rnd = new Random();
            selected = allDeleted.get(rnd.nextInt(0, allDeleted.size()));
        }

        if(!originalShowDeleted){
            showDeleted.click();
        }
        return selected;
    }

    private static List<ElementLocation> getDeletedRowsInGrid(String gridXpath) throws InterruptedException {
        int maxPage = getGridPaginationData(gridXpath).getNumberOfPages();
        List<ElementLocation> allDeleted = new ArrayList<>();
        for(int currentPageNumber = 1; currentPageNumber <= maxPage; currentPageNumber++){
            allDeleted.addAll(getDeletedRowsOnPage(gridXpath, currentPageNumber));
        }
        return allDeleted;
    }

    private static List<ElementLocation> getDeletedRowsOnPage(String gridXpath, int currentPageNumber) throws InterruptedException {
        goToPageInPaginatedGrid(gridXpath, currentPageNumber);
        WebElement grid = findVisibleElementWithXpath(gridXpath);

        WebElement e2 = grid.getShadowRoot().findElement(By.id("scroller"));
        WebElement e3 = e2.findElement(By.id("table"));
        WebElement e4 = e3.findElement(By.id("items"));
        List<WebElement> allRows = e4.findElements(By.tagName("tr"));
        allRows = allRows.stream().filter(v -> v.isDisplayed()).toList();
        int rowNumber = allRows.size();
        List<ElementLocation> deletedRows = new ArrayList<>();
        for(int i = 0; i < rowNumber; i++){
            if(getVisibleGridCell(allRows.get(i), 0).getDomAttribute("part").contains("deleted")){
                deletedRows.add(new ElementLocation(currentPageNumber, i));
            }
        }
        return deletedRows;
    }

    public static boolean isDeletedRow(String gridXpath, WebElement row){
        int cols = getGridColumnNumber(gridXpath);
        Boolean isDeleted = getVisibleGridCell(row, 0).getDomAttribute("part").contains("deleted");
        for(int i = 1; i < cols; i++){
            if(getVisibleGridCell(row, i).getDomAttribute("part").contains("deleted") != isDeleted){
                throw new RuntimeException("Nem dönthető el biztosan, hogy egy a row az törölt, vagy nem");
            }
        }
        return isDeleted;
    }

    public static void fillElementWithRandom(WebElement element) throws InterruptedException {
        switch (element.getTagName()){
            case "vaadin-text-field": element.sendKeys(RandomGenerator.generateRandomOnlyLetterString()); break;
            case "vaadin-number-field": element.sendKeys(RandomGenerator.generateRandomInteger().toString()); break;
            case "vaadin-combo-box": selectRandomFromComboBox(element); break;
            case "vaadin-button", "style": break;
            default : System.err.println("Nem jó a filed teg-name-je ahhoz, hogy adatot generáljunk: " + element.getTagName()); break;
        }
    }

    public static void setShowDeletedCheckboxStatus(String checkboxXpath, boolean selected) throws InterruptedException {
        if(getCheckboxStatus(checkboxXpath) != selected){
            findClickableElementWithXpath(checkboxXpath).click();
            Thread.sleep(100);
        }
    }

    public static void selectElementByTextFromComboBox(WebElement comboBox, String text) throws InterruptedException {
        comboBox.click();
        Thread.sleep(200);
        List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-combo-box-item"));
        if(comboBoxOptions.size() == 0){
            System.err.println("Nincs elem a combo boxban!");
        }
        else{
            for(WebElement comboBoxElement : comboBoxOptions){
                if(comboBoxElement.getText().equals(text)){
                    comboBoxElement.click();
                    break;
                }
            }
        }
    }

    public static void selectRandomFromComboBox(WebElement comboBox) throws InterruptedException {
        comboBox.click();
        Thread.sleep(200);
        List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-combo-box-item"));
        if(comboBoxOptions.size() == 0){
            System.err.println("Nincs elem a combo boxban!");
        }
        else if(comboBoxOptions.size() == 1){
            comboBoxOptions.get(0).click();
        }
        else{
            Random rnd = new Random();
            Integer selectedIndex = rnd.nextInt(0, comboBoxOptions.size() - 1);
            comboBoxOptions.get(selectedIndex).click();
        }
    }

    public static ElementLocation lookingForElementInGrid(String gridXpath, String... attributes) throws InterruptedException {
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        for(int i = 1; i <= getGridPaginationData(gridXpath).getNumberOfPages(); i++){
            ElementLocation el = lookingForElementOnPage(gridXpath, i, attributes);
            if(el != null){
                return el;
            }
        }
        return null;
    }

    public static ElementLocation lookingForElementOnPage(String gridXpath, int pageIndex, String... attributes) throws InterruptedException {
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        goToPageInPaginatedGrid(gridXpath, pageIndex);
        int rowLimit = countVisibleGridDataRowsOnPage(gridXpath);
        for(int rowIndex = 0; rowIndex < rowLimit; rowIndex++){
            WebElement row = getVisibleGridRow(gridXpath, rowIndex);
            if(checkRowContent(row, attributes)){
                return new ElementLocation(pageIndex, rowIndex);
            }
        }
        return null;
    }

    public static boolean checkRowContent(WebElement row, String... attributes){
        for(int i = 0; i < attributes.length; i++){
            if(!attributes[i].equals("skip") && !attributes[i].equals(getVisibleGridCell(row, i).getText())) {
                return false;
            }
        }
        return true;
    }

    public static void checkNotificationText(String excepted){
        WebElement notification = findVisibleElementWithXpath("/html/body/vaadin-notification-container/vaadin-notification-card");
        assertEquals(excepted, notification.getText());
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].remove();", notification);
    }

    public static void checkNotificationContainsTexts(String text){
        WebElement notification = findVisibleElementWithXpath("/html/body/vaadin-notification-container/vaadin-notification-card");
        Assert.assertThat(notification.getText(), CoreMatchers.containsString(text));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].remove();", notification);
    }

    public static String[] getDataFromRowLocation(String gridXpath, ElementLocation location) throws InterruptedException {
        int columnNumber =  getGridColumnNumber(gridXpath);
        goToPageInPaginatedGrid(gridXpath, location.getPageNumber());
        String[] result = new String[columnNumber];
        for(int i = 0; i < columnNumber - 1; i++){
            result[i] = getVisibleGridCell(gridXpath, location.getRowIndex(), i).getText();
        }
        result[columnNumber - 1] = "skip";
        return result;

    }

    public static boolean getCheckboxStatus(String checboxXpath){
        return findClickableElementWithXpath(checboxXpath).getDomAttribute("selected") != null;
    }

    public static void printToConsole(WebElement e){
        System.out.println(e.getAttribute("outerHTML"));
    }
}
