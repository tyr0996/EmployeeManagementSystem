package hu.martin.ems.pages.core.component;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class VaadinMultipleSelectGridComponent extends VaadinGridComponent {
    public VaadinMultipleSelectGridComponent(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public VaadinGridComponent selectElements(int selectElementNumber){
//        int gridRows = countVisibleDataRows();
        deselectAll();
        selectElements(getRandomIndexes(selectElementNumber));
        return this;
    }

    private VaadinGridComponent selectElements(List<Integer> indexesToBeSelected){
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
        return new VaadinCheckboxComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(element, By.xpath("./vaadin-grid-cell-content[1]/vaadin-checkbox"))).get(0));
    }

    public VaadinGridComponent deselectAll(){
        WebElement selectAllCheckBoxWebElement = getSelectAllCheckbox().element;
        WebElement parent = getParentWebElement(selectAllCheckBoxWebElement);
        if(parent.getDomAttribute("checked") != null){
            if(parent.getDomAttribute("indeterminate") != null){
                getSelectAllCheckbox().element.click();
            }
            getSelectAllCheckbox().element.click();
        }
        return this;
    }


}
