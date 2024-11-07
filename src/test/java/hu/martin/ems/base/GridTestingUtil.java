package hu.martin.ems.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.martin.ems.PaginatorComponents;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.PaginationData;
import hu.martin.ems.UITests.UIXpaths;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GridTestingUtil {

    public static WebDriver driver;

    public static void checkNoMoreNotificationsVisible(){
        WebElement notification = findVisibleElementWithXpath("/html/body/vaadin-notification-container/vaadin-notification-card");
        String notificationText = "";
        if(notification != null){
            notificationText = notification.getText();
        }
        assertEquals(null, notification, "No more notification expected but there was one or more. The last one was \"" + notificationText + "\"");
    }

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
                Thread.sleep(5);
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
                Thread.sleep(5);
            }
        }
        return grid;
    }

    public static WebElement getRowAtPosition(String gridXpath, ElementLocation location){
        return getVisibleGridRow(gridXpath, location.getRowIndex());
    }

    public static void navigateMenu(String mainUIXpath, String subIXpath) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        try{
            Thread.sleep(200);
        } catch (InterruptedException e){}
        findClickableElementWithXpathWithWaiting(UIXpaths.SIDE_MENU);
        WebElement menu = findClickableElementWithXpath(mainUIXpath);
        js.executeScript("arguments[0].click()", menu);
        try{
            Thread.sleep(200);
        } catch (InterruptedException e) {}
        WebElement subMenu = findClickableElementWithXpath(subIXpath);
        js.executeScript("arguments[0].click()", subMenu);
        try{
            Thread.sleep(200);
        }
        catch (InterruptedException e){}
        menu = findClickableElementWithXpath(mainUIXpath);
        js.executeScript("arguments[0].click()", menu);
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

//            2*oszlopok (üres) + 1*oszlopok (fejléc) + sorindex * oszlopok + oszlopindex + 1
            int gridCellIndex = (3 + rowIndex) * getGridColumnNumber(gridXpath) + optionsColumnIndex + 1;

            return findClickableElementWithXpath(gridXpath + "/vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[2]");
            //return findClickableElementWithXpath("/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid/vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[2]");
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

    public static WebElement findGrid(String grid){
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(20000));
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(grid)));
        } catch (Exception e) {
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

    public static int countVisibleGridDataRows(String gridXpath) throws InterruptedException {
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        WebElement parent = grid.findElement(By.xpath("./.."));
        Thread.sleep(100);
        String total = parent.findElement(By.tagName("span")).findElement(By.tagName("lit-pagination")).getDomAttribute("total");
        return Integer.parseInt(total);
    }

    public static int countHiddenGridDataRows(String gridXpath, String showDeletedXpath) throws InterruptedException {
        if(showDeletedXpath == null){
            return 0;
        }
        WebElement showDeletedElement = findClickableElementWithXpath(showDeletedXpath);
        setCheckboxStatus(showDeletedXpath, false);
        int visible = countVisibleGridDataRows(gridXpath);
        setCheckboxStatus(showDeletedXpath, true);
        int visibleWithHidden = countVisibleGridDataRows(gridXpath);
        setCheckboxStatus(showDeletedXpath, false);
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

    public static ElementLocation getRandomLocationFromGrid(String gridXpath) throws InterruptedException {
        int elementNumber = countVisibleGridDataRows(gridXpath);
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


    public static String[] getRandomDataDeletedStatusFromGrid(String gridXpath, String showDeletedXpath) throws InterruptedException {
        boolean originalShowDeleted = getCheckboxStatus(showDeletedXpath);
        setCheckboxStatus(showDeletedXpath, true);
        LinkedHashMap<String, List<String>> extraFilter = new LinkedHashMap<>();
        extraFilter.put("deleted", Arrays.asList("1"));
        setExtraDataFilterValue(gridXpath, extraFilter, null);
        List<ElementLocation> deletedRows = getDeletedRowsInGrid(gridXpath);
        ElementLocation e = deletedRows.get(new Random().nextInt(0, deletedRows.size()));
        String[] res = getDataFromRowLocation(gridXpath, e);
        setCheckboxStatus(showDeletedXpath, originalShowDeleted);
        clearExtraDataFilter(gridXpath);
        return res;
    }

    @Deprecated
    public static ElementLocation getRandomLocationDeletedStatusFromGrid(String gridXpath, String showDeletedXpath) throws InterruptedException {
        int originalElements = countVisibleGridDataRows(gridXpath);
        int deletedElements = countHiddenGridDataRows(gridXpath, showDeletedXpath);
        WebElement showDeleted = findClickableElementWithXpathWithWaiting(showDeletedXpath);
        boolean originalShowDeleted = getCheckboxStatus(showDeletedXpath);
        setCheckboxStatus(showDeletedXpath, true);
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

        setCheckboxStatus(showDeletedXpath, false);
        return selected;
    }

    private static List<ElementLocation> getDeletedRowsInGrid(String gridXpath) throws InterruptedException {
        findVisibleElementWithXpath(gridXpath);
        Thread.sleep(200);
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

    public static String fillElementWith(WebElement element, Boolean hasDeletableField, String previousPasswordFieldValue) throws InterruptedException {
        return fillElementWith(element, hasDeletableField, previousPasswordFieldValue, null);
    }

    public static String fillElementWith(WebElement element, Boolean hasDeletableField, String previousPasswordFieldValue, String withData) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;


        switch (element.getTagName()){
            case "vaadin-text-field": {
                js.executeScript("arguments[0].value = '';", element);
                element.sendKeys(withData == null ? RandomGenerator.generateRandomOnlyLetterString() : withData);
                return null;
            }
            case "vaadin-password-field": {
                js.executeScript("arguments[0].value = '';", element);
                String passwordToBeSend = withData == null ? (previousPasswordFieldValue == null ? RandomGenerator.generateRandomOnlyLetterString() : previousPasswordFieldValue) : withData;
                element.sendKeys(passwordToBeSend);
                return passwordToBeSend;
            }
            case "vaadin-number-field":{
                js.executeScript("arguments[0].value = '';", element);
                element.sendKeys(withData == null ? RandomGenerator.generateRandomInteger().toString() : withData);
                return null;
            }
            case "vaadin-combo-box":{
                if(withData == null){
                    selectRandomFromComboBox(element);
                }
                else {
                    selectElementByTextFromComboBox(element, withData);
                }
                return null;
            }
            case "vaadin-multi-select-combo-box":
                selectRandomFromMultiSelectComboBox(element, false);
                return null;
            case "vaadin-button", "style": return null;
            case "vaadin-email-field": {
                String email = withData == null ? RandomGenerator.generateRandomEmailAddress() : withData;
                WebElement emailInput = element.findElement(By.xpath("./input"));
                js.executeScript("arguments[0].value = '';", element);

                printToConsole(emailInput);
                emailInput.sendKeys(email, Keys.ENTER);

                //element.sendKeys(email);
                return null;
            }
            case "vaadin-checkbox":{
                if(element.findElement(By.tagName("label")).getText().toLowerCase().contains("deletable") &&
                    hasDeletableField){
                    js.executeScript("arguments[0].click()", element);
                }
                else if(new Random().nextBoolean()){
                    js.executeScript("arguments[0].click()", element);
                }
                return null;
            }
            default : System.err.println("Nem jó a filed teg-name-je ahhoz, hogy adatot generáljunk: " + element.getTagName()); return null;
        }
    }

    public static String getFieldErrorMessage(WebElement webElement) {
        return webElement.findElement(By.tagName("div")).getText();
    }

    public static Boolean isEnabled(WebElement element){
        return element.getDomAttribute("disabled") == null;
    }

    public static void setCheckboxStatus(String checkboxXpath, boolean selected) throws InterruptedException {
        Boolean checkboxStatus = getCheckboxStatus(checkboxXpath);
        Thread.sleep(100);
        if(checkboxStatus != selected){
            findClickableElementWithXpathWithWaiting(checkboxXpath).click();
        }
        Thread.sleep(100);
    }

    /**
     *
     * @param comboBox Az xpath fontos, hogy ne legyen a végén a //div
     * @param text
     * @throws InterruptedException
     */
    public static void selectElementByTextFromComboBox(WebElement comboBox, String text) throws InterruptedException {
        if(text != ""){
            comboBox.click();
            Thread.sleep(200);
            List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-combo-box-item"));
            while(comboBoxOptions.size() == 0){
                System.err.println("Nincs elem a combo boxban! újrapróbálom");
                Thread.sleep(1000);
                comboBoxOptions = driver.findElements(By.cssSelector("vaadin-combo-box-item"));
            }
            for(WebElement comboBoxElement : comboBoxOptions){
                if(comboBoxElement.getText().equals(text)){
                    JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                    jsExecutor.executeScript("arguments[0].click();", comboBoxElement);
                    break;
                }
            }

        }
    }

    /**
     * Az az elemet adja vissza, amit kiválasztott
     * @param comboBox
     * @return
     * @throws InterruptedException
     */
    public static String selectRandomFromComboBox(WebElement comboBox) throws InterruptedException {
        comboBox.click();
        Thread.sleep(200);
        List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-combo-box-item"));
        if(comboBoxOptions.size() == 0){
            System.err.println("Nincs elem a combo boxban!");
            printToConsole(comboBox);
            return null;
        }
        else if(comboBoxOptions.size() == 1){
            comboBoxOptions.get(0).click();
            return comboBoxOptions.get(0).getText();
        }
        else {
            Random rnd = new Random();
            Integer selectedIndex = rnd.nextInt(0, comboBoxOptions.size() - 1);
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("arguments[0].click();", comboBoxOptions.get(selectedIndex));
            return comboBoxOptions.get(selectedIndex).getText();
        }
    }

    public static void selectRandomFromMultiSelectComboBox(WebElement multiSelectComboBox, boolean allowEmpty) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement toggleButton = (WebElement) js.executeScript("return arguments[0].querySelectorAll('*')[6].querySelectorAll('*')[5];", multiSelectComboBox.getShadowRoot());

        toggleButton.click();
        Thread.sleep(200);

        List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-multi-select-combo-box-item"));
        if(comboBoxOptions.size() == 0){
            System.err.println("Nincs elem a multiselect combo boxban!");
            toggleButton.click();
        }
        else if(comboBoxOptions.size() == 1){
            comboBoxOptions.get(0).click();
            toggleButton.click();
        }
        else {
            for(int i = 0; i < comboBoxOptions.size(); i++){
                Random rnd = new Random();
                if(rnd.nextBoolean()){
                    JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                    jsExecutor.executeScript("arguments[0].click();", comboBoxOptions.get(i));
                }
            }
            toggleButton.click();
            String elements = multiSelectComboBox.getDomAttribute("placeholder");
            if(!allowEmpty && elements.isEmpty()){
                System.out.println("Nem választott element a multiSelectComboBox-ból, ezért újra futtatjuk a függvényt");
                selectRandomFromMultiSelectComboBox(multiSelectComboBox, false);
            }
        }
    }

    public static void selectElementsFromMultiSelectComboBox(WebElement multiSelectComboBox, String... elements) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement toggleButton = (WebElement) js.executeScript("return arguments[0].querySelectorAll('*')[6].querySelectorAll('*')[5];", multiSelectComboBox.getShadowRoot());

        toggleButton.click();
        Thread.sleep(200);

        List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-multi-select-combo-box-item"));
        if(comboBoxOptions.size() == 0){
            System.err.println("Nincs elem a multiselect combo boxban!");
            toggleButton.click();
        }

        else {
            for(WebElement comboBoxElement : comboBoxOptions){
                if(Arrays.asList(elements).contains(comboBoxElement.getText())){
                    JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                    jsExecutor.executeScript("arguments[0].click();", comboBoxElement);
                    break;
                }
            }

            toggleButton.click();
        }
    }

    public static int countElementResultsFromGridWithFilter(String gridXPath, String... attributes) throws InterruptedException {
        applyFilter(gridXPath, attributes);
        int result = countVisibleGridDataRows(gridXPath);
        resetFilter(gridXPath);
        return result;
    }

    /**
     *
     * @param gridXpath
     * @param el
     * @param index the index of the button. Important! It starts from 0, but the 0th and the 1st is the CRUD buttons!
     * @return
     */
    public static WebElement getOptionButton(String gridXpath, ElementLocation el, int index){
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        int optionsColumnIndex = getGridColumnNumber(gridXpath) - 1;
//            2*oszlopok (üres) + 1*oszlopok (fejléc) + sorindex * oszlopok + oszlopindex + 1
//
        int gridCellIndex = (3 + el.getRowIndex()) * getGridColumnNumber(gridXpath) + optionsColumnIndex + 1;

        return findClickableElementWithXpath(gridXpath + "/vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[" + index + "]");
        //return findClickableElementWithXpath("/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid/vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[2]");
        //return null;
    }

    public static WebElement getOptionDownloadButton(String gridXpath, ElementLocation el, int index){
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        int optionsColumnIndex = getGridColumnNumber(gridXpath) - 1;
//            2*oszlopok (üres) + 1*oszlopok (fejléc) + sorindex * oszlopok + oszlopindex + 1
//
        int gridCellIndex = (3 + el.getRowIndex()) * getGridColumnNumber(gridXpath) + optionsColumnIndex + 1;

        return findClickableElementWithXpath(gridXpath + "/vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/a[" + index + "]/vaadin-button");

        //return findClickableElementWithXpath("/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid/vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[2]");
        //return null;
    }

    public static WebElement getOptionColumnButton(String gridXpath, ElementLocation el, int index){
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        int optionsColumnIndex = getGridColumnNumber(gridXpath) - 1;
//            2*oszlopok (üres) + 1*oszlopok (fejléc) + sorindex * oszlopok + oszlopindex + 1
//
        int gridCellIndex = (3 + el.getRowIndex()) * getGridColumnNumber(gridXpath) + optionsColumnIndex + 1;

        return findClickableElementWithXpath(gridXpath + "/vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[" + index + "]");


        //return findClickableElementWithXpath("/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid/vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[2]");
        //return null;
    }

    public static void resetFilter(String gridXPath) throws InterruptedException {
        getHeaderFilterInputFields(gridXPath).forEach(v -> {
            if(v.getDomAttribute("role") == null){
                v.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
                v.sendKeys(Keys.ENTER);
            }
            else {
                deselectAllFromMultiSelectComboBox(v);
            }
        });
        Thread.sleep(200);
        findVisibleElementWithXpath(gridXPath);
    }

    private static void deselectAllFromMultiSelectComboBox(WebElement v){
        JavascriptExecutor js = (JavascriptExecutor) driver;
        printToConsole(TestingUtils.getParent(v));
        WebElement toggleButton = (WebElement) js.executeScript("return arguments[0].querySelectorAll('*')[6].querySelectorAll('*')[5];", TestingUtils.getParent(v).getShadowRoot());

        toggleButton.click();
        sleep(200);

        List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-multi-select-combo-box-item"));
        if(comboBoxOptions.size() == 0){
            System.err.println("Nincs elem a multiselect combo boxban!");
            toggleButton.click();
        }

        else {
            for(WebElement comboBoxElement : comboBoxOptions){
                if(comboBoxElement.getDomAttribute("selected") != null){
                    js.executeScript("arguments[0].click()", comboBoxElement);
                }
            }

            toggleButton.click();
        }
    }

    private static void sleep(long millis){
        try{
            Thread.sleep(millis);
        }
        catch (InterruptedException ex){

        }
    }

    public static void applyFilter(String gridXpath, String... attributes) throws InterruptedException {
        List<WebElement> filterInputs = getHeaderFilterInputFields(gridXpath);
        int max = Math.min(filterInputs.size(), attributes.length);
        for(int i = 0; i < max; i++){
            String role = filterInputs.get(i).getDomAttribute("role");
            if(role == null){
                filterInputs.get(i).sendKeys(attributes[i]);
                filterInputs.get(i).sendKeys(Keys.ENTER);
            }
            else if(role.equals("combobox")){
                String[] data = attributes[i].split(", ");

                selectElementsFromMultiSelectComboBox(TestingUtils.getParent(filterInputs.get(i)), data);
            }
        }
        Thread.sleep(200);
    }


    public static List<WebElement> getHeaderFilterInputFields(String gridXpath){
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        List<WebElement> all = grid.findElements(By.xpath("./*"));
        List<WebElement> headerFilterInputs = new ArrayList<>();
        for(int i = 0; i < all.size(); i++) {
            List<WebElement> we = all.get(i).findElements(By.cssSelector("input[slot='input']"));
            if (we.size() != 0){
                headerFilterInputs.addAll(we);
            }
        }
        return headerFilterInputs;
    }

    private static WebElement getHeaderExtraDataFilter(String gridXpath) {
        return getHeaderFilterInputFields(gridXpath).getLast();
    }


    public static void setExtraDataFilterValue(String gridXpath, String content, NotificationCheck notificationCheck){
        getHeaderExtraDataFilter(gridXpath).sendKeys(content);
        getHeaderExtraDataFilter(gridXpath).sendKeys(Keys.ENTER);
        if(notificationCheck != null && notificationCheck.getAfterFillExtraDataFilter() != null){
            checkNotificationText(notificationCheck.getAfterFillExtraDataFilter());
        }
    }

    public static void setExtraDataFilterValue(String gridXpath, LinkedHashMap<String, List<String>> content, NotificationCheck notificationCheck) {
        try{
            String contentString = new ObjectMapper().writeValueAsString(content);
            setExtraDataFilterValue(gridXpath, contentString, notificationCheck);
        }
        catch (JsonProcessingException ex){
            throw new RuntimeException("Parsing map to json failed due to unknown error!");
        }
    }

    public static void clearExtraDataFilter(String gridXpath) throws InterruptedException {
        getHeaderExtraDataFilter(gridXpath).sendKeys(Keys.chord(Keys.CONTROL, "a"));
        getHeaderExtraDataFilter(gridXpath).sendKeys(Keys.DELETE);
        getHeaderExtraDataFilter(gridXpath).sendKeys(Keys.ENTER);
        Thread.sleep(100);
    }

    public static void checkNotificationText(String excepted){
        WebElement notification = findVisibleElementWithXpath("/html/body/vaadin-notification-container/vaadin-notification-card");
        assertEquals(excepted, notification.getText());
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].remove();", notification);
        sleep(100);
    }

    public static void checkNotificationContainsTexts(String text){
        WebElement notification = findVisibleElementWithXpath("/html/body/vaadin-notification-container/vaadin-notification-card");
        Assert.assertThat(notification.getText().toLowerCase(), CoreMatchers.containsString(text.toLowerCase()));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].remove();", notification);
    }

    public static void checkNotificationContainsTexts(String text, long timeoutInMillis){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(timeoutInMillis));
        WebElement notification = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/vaadin-notification-container/vaadin-notification-card")));
        Assert.assertThat(notification.getText(), CoreMatchers.containsString(text));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].remove();", notification);
    }

    public static void closeNotification(long timeoutInMillis){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(timeoutInMillis));
        WebElement notification = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/vaadin-notification-container/vaadin-notification-card")));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].remove();", notification);
    }

    public static String[] getDataFromRowLocation(String gridXpath, ElementLocation location, Boolean hasOptionColumn) throws InterruptedException {
        int columnNumber =  getGridColumnNumber(gridXpath);
        goToPageInPaginatedGrid(gridXpath, location.getPageNumber());

        String[] result = new String[columnNumber];
        int dataColumnNumber = columnNumber;
        if(hasOptionColumn){
            result = new String[columnNumber - 1];
            dataColumnNumber = columnNumber - 1;
        }

        for(int i = 0; i < dataColumnNumber; i++){
            result[i] = getVisibleGridCell(gridXpath, location.getRowIndex(), i).getText();
        }
        return result;
    }

    public static String[] getDataFromRowLocation(String gridXpath, ElementLocation location) throws InterruptedException {
        return getDataFromRowLocation(gridXpath, location, true);
    }

    public static boolean getCheckboxStatus(String checboxXpath){
        return findClickableElementWithXpathWithWaiting(checboxXpath).getDomAttribute("checked") != null;
    }

    public static void printToConsole(WebElement e){
        System.out.println(e.getAttribute("outerHTML"));
    }

    public static void selectDateFromDatePicker(String datePickerXpath, LocalDate date){

        WebElement datePicker = findVisibleElementWithXpath(datePickerXpath);
        datePicker.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        if(date != null){
            String todayString = date.format(DateTimeFormatter.ofPattern("yyyy. MM. dd."));
            datePicker.sendKeys(todayString);
        }
        datePicker.sendKeys(Keys.ENTER);
    }

    public static LocalDate getDateFromDatePicker(String datePickerXpath, String dateFormat){
        String value = findVisibleElementWithXpath(datePickerXpath).getAttribute("value");
        if(value.equals("")){
            return null;
        }
        return LocalDate.parse(value, DateTimeFormatter.ofPattern(dateFormat));
    }


    private static void selectElementsFromMultipleSelectionGrid(String gridXpath, List<Integer> indexesToBeSelected) throws InterruptedException {
        findVisibleElementWithXpath(gridXpath);
        int rows = countVisibleGridDataRows(gridXpath);
        if(rows == 0){
            return;
        }
        for(int rowIndex = 0; rowIndex < rows; rowIndex++){
            if(indexesToBeSelected.contains(rowIndex)){
                int gridCellIndex = (3 + rowIndex) * getGridColumnNumber(gridXpath) + 0 + 1;
                WebElement e = findVisibleElementWithXpath(gridXpath + "/vaadin-grid-cell-content["+gridCellIndex+"]");
                e.click();
            }
        }
    }

    public static void selectMultipleElementsFromMultibleSelectionGrid(String gridXpath, int selectElementNumber) throws InterruptedException {
        int gridRows = countVisibleGridDataRows(gridXpath);
        deselectAllFromGrid(gridXpath);
        selectElementsFromMultipleSelectionGrid(gridXpath, getRandomIndexes(gridRows, selectElementNumber));
    }

    private static void deselectAllFromGrid(String gridXpath) throws InterruptedException {
        findVisibleElementWithXpath(gridXpath);
        WebElement elementCheckbox = getHeaderFilterInputFields(gridXpath).get(0);
        WebElement parent = TestingUtils.getParent(elementCheckbox);
        if(parent.getDomAttribute("checked") != null){
            if(parent.getDomAttribute("indeterminate") != null){
                elementCheckbox.click();
                Thread.sleep(1);
            }
            elementCheckbox.click();
        }
        Thread.sleep(1);
    }

    public static List<Integer> getRandomIndexes(int max, int count) {
        List<Integer> selectedElements = new ArrayList<>();
        Random random = new Random();

        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            numbers.add(i);
        }
        if (count > max) {
            return numbers;
        }
        Collections.shuffle(numbers, random);
        return numbers.subList(0, count);
    }
}
