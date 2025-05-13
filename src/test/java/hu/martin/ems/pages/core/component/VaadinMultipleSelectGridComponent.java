package hu.martin.ems.pages.core.component;

import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.PaginationData;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VaadinMultipleSelectGridComponent extends VaadinGridComponent {
    public VaadinMultipleSelectGridComponent(WebDriver driver, By provider) {
        super(driver, provider);
    }

    public VaadinGridComponent selectElements(int selectElementNumber){
//        int gridRows = countVisibleDataRows();
//        System.out.println("Elemek, amiket kiválasztok: " + selectElementNumber);
        deselectAll();
        List<Integer> randomIndexes = getRandomIndexes(selectElementNumber);
        List<ElementLocation> locations = randomIndexes.stream().map(this::convertIndexToElementLocation).toList();
        System.out.println("Locations: ");
        locations.forEach(System.out::println);
//        System.out.println("Kiválasztott index-ek: ");
//        randomIndexes.forEach(System.out::println);
        selectElements(locations);
        return this;
    }

    private VaadinGridComponent selectElements(List<ElementLocation> locations){
//        locations.sort(Comparator.comparing(ElementLocation::getPageNumber));

//        List<ElementLocation> list = ... // a bemeneti lista

        Map<Integer, List<ElementLocation>> grouped = locations.stream()
                .collect(Collectors.groupingBy(ElementLocation::getPageNumber));
        List<List<ElementLocation>> result = new ArrayList<>(grouped.values());
        result.forEach(v -> {
            goToPage(v.get(0).getPageNumber());
            List<Integer> tempI = new ArrayList<>();
            v.forEach(l -> {
                tempI.add(l.getRowIndex());
            });
            selectIndexes(tempI);
        });
        return this;
    }

    private VaadinGridComponent selectIndexes(List<Integer> indexesToBeSelected){
        int rows = countVisibleDataRows();
        if(rows == 0){
            //TODO kivételt dobni!
//            return this;
        }
        for(int rowIndex = 0; rowIndex < rows; rowIndex++){
            if(indexesToBeSelected.contains(rowIndex)){
                int gridCellIndex = (3 + rowIndex) * getColumnNumber() + 0 + 1;
//                WebElement e = getWait().until(ExpectedConditions.visibilityOfElementLocated(this.element))
                WebElement e = this.element.findElement(By.xpath(".//vaadin-grid-cell-content[" + gridCellIndex + "]"));
                e.click();
            }
        }
        return this;
    }

    private VaadinCheckboxComponent getSelectAllCheckbox(){
        return new VaadinCheckboxComponent(getDriver(), element, By.xpath("./vaadin-grid-cell-content[1]/vaadin-checkbox"), 0);
    }

    public VaadinGridComponent deselectAll(){
//        WebElement selectAllCheckBoxWebElement = getSelectAllCheckbox().element;
        WebElement parent = getSelectAllCheckbox().element;
//        WebElement parent = getParentWebElement(selectAllCheckBoxWebElement);
//        printToConsole(parent);
        if(parent.getDomAttribute("checked") != null){
            if(parent.getDomAttribute("indeterminate") != null){
                getSelectAllCheckbox().element.click();
            }
            getSelectAllCheckbox().element.click();
        }
        this.waitForRefresh();
        return this;
    }

    @Override
    public PaginationData getPaginationData(){
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        String jsScript = "return document.querySelector('flow-container-root-2521314 vaadin-horizontal-layout vaadin-vertical-layout:nth-of-type(2) span lit-pagination')";
        Integer total = Integer.parseInt(js.executeScript(jsScript + ".total;").toString());
        Integer currentPage = Integer.parseInt(js.executeScript(jsScript + ".page").toString());
        Integer pageSize = Integer.parseInt(js.executeScript(jsScript + ".limit").toString());
        Integer numberOfPages = (int) Math.ceil((double) total / (double) pageSize);
        return new PaginationData(pageSize, total, currentPage, numberOfPages);
    }


    public boolean isMultiSelectEnabled() {
        List<WebElement> children = getAllChildren();
        List<WebElement> selectionColumns = children.stream().filter(v -> v.getAttribute("outerHTML").contains("<vaadin-grid-flow-selection-column>")).collect(Collectors.toList());
        return selectionColumns.size() > 0;
    }
}
