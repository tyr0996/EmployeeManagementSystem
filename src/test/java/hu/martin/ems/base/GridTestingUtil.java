package hu.martin.ems.base;

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
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static void navigateMenu(String mainUIXpath, String subIXpath){
        findClickableElementWithXpathWithWaiting(UIXpaths.SIDE_MENU);
        WebElement adminMenu = findClickableElementWithXpathWithWaiting(mainUIXpath);
        adminMenu.click();
        WebElement subMenu = findClickableElementWithXpathWithWaiting(subIXpath);
        subMenu.click();
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
        Instant start = Instant.now();
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
        Instant stop = Instant.now();
        System.out.println("Eltelt idő: " + Duration.between(start, stop).toMillis() + "ms");
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

    public static String fillElementWithRandom(WebElement element, Boolean hasDeletableField, String previousPasswordFieldValue) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        switch (element.getTagName()){
            case "vaadin-text-field": {
                js.executeScript("arguments[0].value = '';", element);
                element.sendKeys(RandomGenerator.generateRandomOnlyLetterString());
                return null;
            }
            case "vaadin-password-field": {
                js.executeScript("arguments[0].value = '';", element);
                String passwordToBeSend = previousPasswordFieldValue == null ? RandomGenerator.generateRandomOnlyLetterString() : previousPasswordFieldValue;
                element.sendKeys(passwordToBeSend);
                return passwordToBeSend;
            }
            case "vaadin-number-field":{
                js.executeScript("arguments[0].value = '';", element);
                element.sendKeys(RandomGenerator.generateRandomInteger().toString());
                return null;
            }
            case "vaadin-combo-box": selectRandomFromComboBox(element); return null;
            case "vaadin-multi-select-combo-box": selectRandomFromMultiSelectComboBox(element); return null;
            case "vaadin-button", "style": return null;
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

    public static void setCheckboxStatus(String checkboxXpath, boolean selected) throws InterruptedException {
        Boolean checkboxStatus = getCheckboxStatus(checkboxXpath);
        if(checkboxStatus != selected){
            //HA false-ra akarom álítani, akkor valamiért nem jó
            findClickableElementWithXpathWithWaiting(checkboxXpath).click();
        }
        Thread.sleep(100);
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
                    JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                    jsExecutor.executeScript("arguments[0].click();", comboBoxElement);
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
        else {
            Random rnd = new Random();
            Integer selectedIndex = rnd.nextInt(0, comboBoxOptions.size() - 1);
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("arguments[0].click();", comboBoxOptions.get(selectedIndex));
        }
    }

    public static void selectRandomFromMultiSelectComboBox(WebElement multiSelectComboBox) throws InterruptedException {
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
        }
    }

    public static int countElementResultsFromGridWithFilter(String gridXPath, String... attributes) throws InterruptedException {
        applyFilter(gridXPath, attributes);
        int result = countVisibleGridDataRows(gridXPath);
        resetFilter(gridXPath);
        return result;
    }

    public static void resetFilter(String gridXPath) throws InterruptedException {
        getHeaderFilterInputFields(gridXPath).forEach(v -> {
            v.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            v.sendKeys(Keys.ENTER);
        });
        Thread.sleep(200);
        findVisibleElementWithXpath(gridXPath);
    }

    public static void applyFilter(String gridXpath, String... attributes) throws InterruptedException {
        List<WebElement> filterInputs = getHeaderFilterInputFields(gridXpath);
        if(filterInputs.size() != attributes.length){
            throw new ArrayIndexOutOfBoundsException("The number of filter fields doesn't match the given attributes.");
        }
        for(int i = 0; i < filterInputs.size(); i++){
            filterInputs.get(i).sendKeys(attributes[i]);
            filterInputs.get(i).sendKeys(Keys.ENTER);
        }
        Thread.sleep(200);
    }


    private static List<WebElement> getHeaderFilterInputFields(String gridXpath){
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


    //átlagos 133
    public static ElementLocation lookingForElementInGridOptimized2(String gridXpath, String... attributes) throws InterruptedException {
        StringBuilder allGrid = new StringBuilder();
        int numberOfPages = getGridPaginationData(gridXpath).getNumberOfPages();
        int gridColumnNumber = getGridColumnNumber(gridXpath);
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        Instant start = Instant.now();
        for(int i = 1; i <= numberOfPages; i++){
            goToPageInPaginatedGrid(gridXpath, i);
            allGrid.append(grid.getAttribute("outerHTML"));
        }
        Instant eoPaging = Instant.now();
        String regexForGettingCellContent = "(?<=<vaadin-grid-cell-content slot=\"vaadin-grid-cell-content-)[0-9]*\">\\S*(?=</vaadin-grid-cell-content>)";
        Pattern pattern = Pattern.compile(regexForGettingCellContent);
        Matcher matcher = pattern.matcher(allGrid);
        List<String> data = new ArrayList<>();
        while(matcher.find()){
            String row = matcher.group();
            if(!row.endsWith("\">")){
                String value = row.split("\">")[1];
                data.add(value);
            }
        }
        List<String[]> correctedData = chunkList(data, gridColumnNumber - 1);
        for(int j = 0; j < correctedData.size(); j++){
            if(Arrays.equals(attributes, correctedData.get(j))){
                System.out.println("Az optimalizált futás sikeresen megtalálta az elemet");
                return new ElementLocation(getGridPaginationData(gridXpath).getCurrentPage(), j);
            }
        }
        Instant end = Instant.now();
        System.out.println("Az oldal lekérése általánosan " + Duration.between(start, eoPaging).toMillis()/numberOfPages);
        System.out.println("Átlagos oldalvizsgálati idő: " + Duration.between(start, end).toMillis()/numberOfPages);
        System.out.println("Az optimalizált futás nem találja az elemet");


        return null;
    }


        //átlag: 155
    public static ElementLocation lookingForElementInGridOptimized(String gridXpath, String... attributes) throws InterruptedException {
        System.out.println("Optimalizált futás elindult");
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        //String regexForGettingCellContent = "(?<=<vaadin-grid-cell-content slot=\"vaadin-grid-cell-content-)(.*)(?=</vaadin-grid-cell-content>)"; //Itt még benne lesz az index-e, majd "> string, és utána jön az érdemi dolog
        String regexForGettingCellContent = "(?<=<vaadin-grid-cell-content slot=\"vaadin-grid-cell-content-)[0-9]*\">\\S*(?=</vaadin-grid-cell-content>)";
        int gridColumnNumber = getGridColumnNumber(gridXpath);
        long totalMilis = 0;
        for(int i = 1; i <= getGridPaginationData(gridXpath).getNumberOfPages(); i++){
            Instant start = Instant.now();
            goToPageInPaginatedGrid(gridXpath, i);
            String pageHtml = grid.getAttribute("outerHTML");
            Pattern pattern = Pattern.compile(regexForGettingCellContent);
            Matcher matcher = pattern.matcher(pageHtml);
            List<String> data = new ArrayList<>();
            int needToDrop = getGridColumnNumber(gridXpath) * 2;
            for(int j = 0; j < needToDrop; j++){
                matcher.find();
            }
            while(matcher.find()){
                String row = matcher.group();
                if(!row.endsWith("\">")){
                    String value = row.split("\">")[1];
                    data.add(value);
                }
            }
            List<String[]> correctedData = chunkList(data, gridColumnNumber - 1);
            for(int j = 0; j < correctedData.size(); j++){
                if(Arrays.equals(attributes, correctedData.get(j))){
                    System.out.println("Az optimalizált futás sikeresen megtalálta az elemet");
                    return new ElementLocation(getGridPaginationData(gridXpath).getCurrentPage(), j);
                }
            }
            Instant end = Instant.now();
            long milis = Duration.between(start, end).toMillis();
            totalMilis += milis;
            System.out.println("Az oldal megvizsgálása " + milis + " ms telt el.");
        }
        System.out.println("Átlagos oldalvizsgálati idő: " + totalMilis/getGridPaginationData(gridXpath).getNumberOfPages());
        System.out.println("Az optimalizált futás nem találja az elemet");
        return null;
    }

    public static List<String[]> chunkList(List<String> originalList, int chunkSize) {
        List<String[]> chunkedList = new ArrayList<>();
        for (int i = 0; i < originalList.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, originalList.size());
            String[] chunk = new String[end - i + 1];
            for (int j = 0; j < chunk.length - 1; j++) {
                chunk[j] = originalList.get(i + j);
            }
            chunk[chunk.length-1] = "skip";
            chunkedList.add(chunk);
        }
        return chunkedList;
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
        String[] result = new String[columnNumber - 1];
        for(int i = 0; i < columnNumber - 1; i++){
            result[i] = getVisibleGridCell(gridXpath, location.getRowIndex(), i).getText();
        }
        return result;

    }

    public static boolean getCheckboxStatus(String checboxXpath){
        return findClickableElementWithXpathWithWaiting(checboxXpath).getDomAttribute("checked") != null;
    }

    public static void printToConsole(WebElement e){
        System.out.println(e.getAttribute("outerHTML"));
    }
}
