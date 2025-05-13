package hu.martin.ems.pages.core.component;

import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.PaginationData;
import jakarta.annotation.Nullable;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class VaadinGridComponent extends VaadinBaseComponent {

    public VaadinGridComponent(WebDriver driver, By provider){
        super(driver, provider);
    }

    protected ElementLocation convertIndexToElementLocation(Integer v) {
        int pageSize = getPaginationData().getPageSize();
        int page = (v / pageSize) + 1;
        int row = v % pageSize;
        return new ElementLocation(page, row);
    }

    public int countVisibleDataRows(){
        return Integer.parseInt(getParentWebElement(element).findElement(By.tagName("span")).findElement(By.tagName("lit-pagination")).getDomAttribute("total"));
    }

    public int getColumnNumber(){
        WebElement e2 = element.getShadowRoot().findElement(By.id("scroller"));
        WebElement e3 = e2.findElement(By.id("table"));
        WebElement e4 = e3.findElement(By.id("header"));
        WebElement e5 = e4.findElements(By.xpath(".//tr")).get(0);
        List<WebElement> headers = e5.findElements(By.xpath(".//th"));
        return headers.size();
    }

    /**
     * Counts all elements in grid (not only the current page)
     * @return
     */
    protected int getCurrentRowNumber(){
        return getPaginationData().getTotalElements();
    }

    public String[] getRandomDataDeletedStatus(VaadinCheckboxComponent showDeleted){
        boolean originalShowDeleted = showDeleted.getStatus();
        showDeleted.setStatus(true);
//        LinkedHashMap<String, List<String>> extraFilter = new LinkedHashMap<>();
//        extraFilter.put("deleted", Arrays.asList("1"));
        setExtraDataFilter("{\"deleted\":[\"1\"]}");
        List<ElementLocation> deletedRows = getDeletedRows();
        ElementLocation e = deletedRows.get(new Random().nextInt(0, deletedRows.size()));
        String[] res = getDataFromRowLocation(e, true);
        showDeleted.setStatus(originalShowDeleted);
        clearExtraDataFilter();
        return res;
    }

    public void applyFilter(String... attributes) {
        List<WebElement> filterInputs = getHeaderFilterInputFields();
        int max = Math.min(filterInputs.size(), attributes.length);
        for(int i = 0; i < max; i++){
            String role = filterInputs.get(i).getDomAttribute("role");
            String id = filterInputs.get(i).getDomAttribute("id"); //.contains("multi-select-combo-box")
            if(role == null){
                filterInputs.get(i).sendKeys(attributes[i]);
                filterInputs.get(i).sendKeys(Keys.ENTER);
            }
            else if(role.equals("combobox")){
                if(id.contains("multi-select-combo-box")){
                    VaadinMultipleSelectDropdownComponent comboBox = new VaadinMultipleSelectDropdownComponent(getDriver(), filterInputs.get(i), By.xpath("./.."), 0);
                    String[] data = attributes[i].split(", ");
                    comboBox.selectElements(data);
                }
                else {
                    VaadinDropdownComponent comboBox = new VaadinDropdownComponent(getDriver(), getParentWebElement(filterInputs.get(i)), By.xpath("."), 0);
                    comboBox.fillWith(attributes[i]);
                }
            }
        }
        this.waitForRefresh();
    }

    public List<ElementLocation> getDeletedRows(){
        int maxPage = this.getPaginationData().getNumberOfPages();
        List<ElementLocation> allDeleted = new ArrayList<>();
        for(int currentPageNumber = 1; currentPageNumber <= maxPage; currentPageNumber++){
            allDeleted.addAll(getDeletedRowsOnPage(currentPageNumber));
        }
        return allDeleted;
    }

    private List<ElementLocation> getDeletedRowsOnPage(int currentPageNumber) {
        goToPage(currentPageNumber);
        WebElement e2 = this.element.getShadowRoot().findElement(By.id("scroller"));
        WebElement e3 = e2.findElement(By.id("table"));
        WebElement e4 = e3.findElement(By.id("items"));
        List<WebElement> allRows = e4.findElements(By.tagName("tr"));
        allRows = allRows.stream().filter(v -> v.isDisplayed()).toList();
        int rowNumber = allRows.size();
        List<ElementLocation> deletedRows = new ArrayList<>();
        for(int i = 0; i < rowNumber; i++){
            if(getVisibleCell(allRows.get(i), 0).getDomAttribute("part").contains("deleted")){
                deletedRows.add(new ElementLocation(currentPageNumber, i));
            }
        }
        return deletedRows;
    }

    public void setExtraDataFilter(String content){
        getHeaderFilterInputFields().getLast().sendKeys(content);
        getHeaderFilterInputFields().getLast().sendKeys(Keys.ENTER);
    }

    public void clearExtraDataFilter() {
        getHeaderFilterInputFields().getLast().sendKeys(Keys.chord(Keys.CONTROL, "a"));
        getHeaderFilterInputFields().getLast().sendKeys(Keys.DELETE);
        getHeaderFilterInputFields().getLast().sendKeys(Keys.ENTER);
    }

    /**
     * Counts all elements in grid (not only the current page)
     * @return
     */
    public int getTotalRowNumber(){
        return getCurrentRowNumber();
//        boolean originalStatus = showDeleted.getStatus();
//        showDeleted.setStatus(true);
//        int result = getCurrentRowNumber();
//        showDeleted.setStatus(originalStatus);
//        return result;

    }

    public int getTotalDeletedRowNumber(VaadinCheckboxComponent showDeleted){
        assert showDeleted != null : "getTotalDeletedRowNumber is incomprehensible when showDeleted is null!";
        boolean originalStatus = showDeleted.getStatus();
        showDeleted.setStatus(false);
        waitForRefresh();
        int nonDeleted = getCurrentRowNumber();
        showDeleted.setStatus(true);
        waitForRefresh();
        int withDeleted = getCurrentRowNumber();
        showDeleted.setStatus(originalStatus);
        return withDeleted - nonDeleted;
    }

    public int getTotalNonDeletedRowNumber(@Nullable VaadinCheckboxComponent showDeleted){
        if(showDeleted != null){
            boolean originalStatus = showDeleted.getStatus();
            showDeleted.setStatus(false);
            waitForRefresh();
            int result = getCurrentRowNumber();
            showDeleted.setStatus(originalStatus);
            waitForRefresh();
            return result;
        }
        else{
            return getCurrentRowNumber();
        }
    }

    public List<Integer> getRandomIndexes(int count){
        List<Integer> selectedElements = new ArrayList<>();
        Random random = new Random();
        //TODO: Itt a

        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < getCurrentRowNumber(); i++) {
            numbers.add(i);
        }
        if (count > getCurrentRowNumber()) {
            return numbers;
        }
        Collections.shuffle(numbers, random);
        return numbers.subList(0, count);
    }

    public void goToPage(int requiredNumber){
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        js.executeScript("arguments[0].parentNode.querySelectorAll('span')[0].querySelectorAll('lit-pagination')[0].page=" + requiredNumber, element);
    }

    public WebElement getVisibleRow(int rowIndex){
        WebElement e2 = element.getShadowRoot().findElement(By.id("scroller"));
        WebElement e3 = e2.findElement(By.id("table"));
        WebElement e4 = e3.findElement(By.id("items"));
        List<WebElement> allRows = e4.findElements(By.tagName("tr"));
        allRows = allRows.stream().filter(v -> v.isDisplayed()).toList();
        if(rowIndex < allRows.size()){
            return allRows.get(rowIndex);
        }
        return null;
    }

    public PaginationData getPaginationData(){
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        String jsScript = "return document.querySelector('flow-container-root-2521314 vaadin-horizontal-layout vaadin-vertical-layout:nth-of-type(2) span lit-pagination')";
        Integer total = Integer.parseInt(js.executeScript(jsScript + ".total;").toString());
        Integer currentPage = Integer.parseInt(js.executeScript(jsScript + ".page").toString());
        Integer pageSize = Integer.parseInt(js.executeScript(jsScript + ".limit").toString());
        Integer numberOfPages = (int) Math.ceil((double) total / (double) pageSize);
        return new PaginationData(pageSize, total, currentPage, numberOfPages);
    }

    public WebElement getVisibleCell(int rowIndex, int columnIndex){
        WebElement row = getVisibleRow(rowIndex);
        List<WebElement> rowElements = getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(row, By.xpath(".//td")));
        return rowElements.get(columnIndex);
    }

    public WebElement getVisibleCell(WebElement row, int columnIndex){
        List<WebElement> rowElements = getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(row, By.xpath(".//td")));
        return rowElements.get(columnIndex);
    }

    public VaadinButtonComponent getDeleteButton(int rowIndex){
        try{
            int optionsColumnIndex = getColumnNumber() - 1;
            WebElement optionsCell = getVisibleCell(rowIndex, optionsColumnIndex);
//            WebElement deleteButton = optionsCell.findElements(By.xpath("//vaadin-icon[@icon='vaadin:trash']/parent::vaadin-button")).get(rowIndex);
//            WebElement deleteButton = optionsCell.findElements(By.xpath("//vaadin-icon[@icon='vaadin:trash']")).get(rowIndex);
//            WebElement deleteButtonProvider = getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(optionsCell, By.xpath("//vaadin-icon[@icon='vaadin:trash']"))).get(rowIndex);
            return new VaadinButtonComponent(getDriver(), optionsCell, By.xpath("//vaadin-icon[@icon='vaadin:trash']"), rowIndex);
        }
        catch (Exception e){
            return null;
        }
    }

    public List<String[]> getAllFullLines(Boolean hasOptionColumn) {
        PaginationData pd = this.getPaginationData();
        List<String[]> allFullData = new ArrayList<>();

        for(int i = 0; i < pd.getTotalElements(); i++){
            ElementLocation el = new ElementLocation((i / pd.getPageSize()) + 1, i % pd.getPageSize());
            String[] data = getDataFromRowLocation(el, hasOptionColumn);
            Boolean goodData = true;
            for(int j = 0; j < data.length; j++){
                if(data[j].equals("") || data[j] == null){
                    goodData = false;
                    break;
                }
            }
            if(goodData){
                allFullData.add(data);
            }
        }

        return allFullData;
    }

    public List<String[]> getAllLackingLines(Boolean hasOptionColumn) {
        PaginationData pd = getPaginationData();
        List<String[]> allLinesWithEmpty = new ArrayList<>();

        for(int i = 0; i < pd.getTotalElements(); i++){
            ElementLocation elementLocation = new ElementLocation((i / pd.getPageSize()) + 1, i % pd.getPageSize());
            String[] data = getDataFromRowLocation(elementLocation, hasOptionColumn);
            Boolean goodData = false;
            for(int j = 0; j < data.length; j++){
                if(data[j].equals("") || data[j] == null){
                    goodData = true;
                    break;
                }
            }
            if(goodData){
                allLinesWithEmpty.add(data);
            }
        }
        return allLinesWithEmpty;
    }

    public VaadinButtonComponent getModifyButton(int rowIndex){
        try{
            int optionsColumnIndex = getColumnNumber() - 1;
            WebElement optionsCell = getVisibleCell(rowIndex, optionsColumnIndex);
//            WebElement modifyButton = getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(optionsCell, By.xpath("//vaadin-icon[contains(@src, 'edit')]"))).get(rowIndex);
            return new VaadinButtonComponent(getDriver(), optionsCell, By.xpath("//vaadin-icon[contains(@src, 'edit')]"), rowIndex);
        }
        catch (Exception e){
            return null;
        }
    }

    /**
     * Fájlok letöltésénél használjuk.
     * @param el
     * @param index
     * @return
     */
    public VaadinButtonComponent getOptionAnchorButton(ElementLocation el, int index){
        int optionsColumnIndex = getColumnNumber() - 1;
//            2*oszlopok (üres) + 1*oszlopok (fejléc) + sorindex * oszlopok + oszlopindex + 1
//
        int gridCellIndex = (3 + el.getRowIndex()) * getColumnNumber() + optionsColumnIndex + 1;
//        WebElement buttonElement = getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(element, By.xpath("./vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/a[" + index + "]/vaadin-button"))).get(0);

        return new VaadinButtonComponent(getDriver(), element, By.xpath("./vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/a[" + index + "]/vaadin-button"), 0);

        //return findClickableElementWithXpathWithWaiting("/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid/vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[2]");
        //return null;
    }

    public VaadinButtonComponent getOptionColumnButton(ElementLocation el, int index){
        int optionsColumnIndex = getColumnNumber() - 1;
//            2*oszlopok (üres) + 1*oszlopok (fejléc) + sorindex * oszlopok + oszlopindex + 1
//
        int gridCellIndex = (3 + el.getRowIndex()) * getColumnNumber() + optionsColumnIndex + 1;

//        return findClickableElementWithXpathWithWaiting(gridXpath + "/vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[" + index + "]");

//        WebElement buttonElement = getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(element, By.xpath("./vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[" + index + "]"))).get(0);

        return new VaadinButtonComponent(getDriver(), element, By.xpath("./vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[" + index + "]"), 0);


        //return findClickableElementWithXpathWithWaiting("/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid/vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[2]");
        //return null;
    }


    public VaadinButtonComponent getPermanentlyDeleteButton(int rowIndex){
        try{
            int optionsColumnIndex = getColumnNumber() - 1;
            WebElement optionsCell = getVisibleCell(rowIndex, optionsColumnIndex);
//            WebElement permanentlyDeleteButton = optionsCell.findElements(By.xpath("//vaadin-icon[contains(@src, 'clear')]")).get(0);
//            WebElement permanentlyDeleteButton = getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(optionsCell, By.xpath("//vaadin-icon[contains(@src, 'clear')]"))).get(rowIndex);
            return new VaadinButtonComponent(getDriver(), optionsCell, By.xpath("//vaadin-icon[contains(@src, 'clear')]"), rowIndex);
        }
        catch (Exception e){
            return null;
        }
    }

    public WebElement getRestoreButton(int rowIndex){
        try{
            int gridCellIndex = (3 + rowIndex) * getColumnNumber() + getColumnNumber();
            return getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(element, By.xpath(".//vaadin-grid-cell-content[" + gridCellIndex  + "]/vaadin-horizontal-layout/vaadin-button[2]"))).get(0);
        }
        catch (Exception e){
            return null;
        }
    }

    public ElementLocation getRandomLocation() {
        int elementNumber = getPaginationData().getTotalElements();
        Random rnd = new Random();
        Integer selectedElementIndex;
        if(elementNumber == 0){
            return null;
        }
        else if(elementNumber > 1){
            selectedElementIndex = rnd.nextInt(0, elementNumber);
        }
        else{
            return new ElementLocation(1, 0);
        }
        int pageNumber;
        if(selectedElementIndex != 0){
            pageNumber = (int) Math.ceil((double)selectedElementIndex / (double) getPaginationData().getPageSize());
        }
        else{
            pageNumber = 1;
        }

        int rowIndex = selectedElementIndex % getPaginationData().getPageSize();

        return new ElementLocation(pageNumber, rowIndex);
    }

    public String[] getDataFromRowLocation(ElementLocation location, Boolean hasOptionColumn) {
        int columnNumber =  getColumnNumber();
        goToPage(location.getPageNumber());

        String[] result = new String[columnNumber];
        int dataColumnNumber = columnNumber;
        if(hasOptionColumn){
            result = new String[columnNumber - 1];
            dataColumnNumber = columnNumber - 1;
        }

        for(int i = 0; i < dataColumnNumber; i++){
            result[i] = getVisibleCell(location.getRowIndex(), i).getText();
        }
        return result;
    }

    public List<WebElement> getHeaderFilterInputFields(){
        List<WebElement> all = element.findElements(By.xpath("./*"));
        List<WebElement> headerFilterInputs = new ArrayList<>();
        for(int i = 0; i < all.size(); i++) {
            List<WebElement> we = all.get(i).findElements(By.cssSelector("input[slot='input']"));
            if (we.size() != 0){
                headerFilterInputs.addAll(we);
            }
        }
        return headerFilterInputs;
    }

    public String getHeaderFilterInputFieldErrorMessage(int index){
        return getParentWebElement(getHeaderFilterInputFields().get(index)).findElement(By.tagName("div")).getText();
    }

    public void resetFilter(){
        getHeaderFilterInputFields().forEach(v -> {
            String type = getParentWebElement(v).getTagName();
            switch (type){
                case "vaadin-combo-box" : {
                    VaadinDropdownComponent dropdownFilter = new VaadinDropdownComponent(getDriver(), v, By.xpath("./.."), 0);
                    dropdownFilter.clear();
                    break;
                }
                case "vaadin-multi-select-combo-box": {
                    VaadinMultipleSelectDropdownComponent multiSelectDropboxFilter = new VaadinMultipleSelectDropdownComponent(getDriver(), v, By.xpath("."), 0);
                    multiSelectDropboxFilter.deselectAll();
                    break;
                }
                default: {
                    v.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
                    v.sendKeys(Keys.ENTER);
                }
            }
        });
        this.waitForRefresh();
    }
}
