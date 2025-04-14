package hu.martin.ems.pages.core.component;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VaadinMultipleSelectDropdownComponent extends VaadinDropdownComponent {
    public VaadinMultipleSelectDropdownComponent(WebDriver driver, By provider) {
        super(driver, provider);
    }

    public VaadinMultipleSelectDropdownComponent(WebDriver driver, WebElement scope, By provider, int index){
        super(driver, scope, provider, index);
    }

    private WebElement clearButton;

    @Override
    public void initWebElements(){
        JavascriptExecutor js = (JavascriptExecutor) getDriver();

//        printToConsole(element);
//        System.out.println(element.getTagName());
        if(!element.getTagName().equals("input")){
            this.toggleButton = (WebElement) js.executeScript("return arguments[0].querySelector('div').querySelector('vaadin-multi-select-combo-box-internal').querySelector('vaadin-multi-select-combo-box-container').querySelectorAll('div')[2]", element.getShadowRoot());
            this.clearButton = (WebElement) js.executeScript("return arguments[0].querySelector('div').querySelector('vaadin-multi-select-combo-box-internal').querySelector('vaadin-multi-select-combo-box-container').querySelectorAll('div')[1]", element.getShadowRoot());
        }
        else{
            this.toggleButton = (WebElement) js.executeScript(
                    "return document.evaluate('/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid/vaadin-grid-cell-content[2]/vaadin-vertical-layout/vaadin-multi-select-combo-box', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue"
                            + ".shadowRoot.querySelector('div')"
                            + ".querySelector('vaadin-multi-select-combo-box-internal vaadin-multi-select-combo-box-container')"
                            + ".querySelectorAll('div')[2];"
            );
            this.clearButton = (WebElement) js.executeScript(
                    "return document.evaluate('/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid/vaadin-grid-cell-content[2]/vaadin-vertical-layout/vaadin-multi-select-combo-box', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue"
                            + ".shadowRoot.querySelector('div')"
                            + ".querySelector('vaadin-multi-select-combo-box-internal vaadin-multi-select-combo-box-container')"
                            + ".querySelectorAll('div')[1];"
            );



        }

        assert toggleButton != null;
    }


    public void deselectAll(){
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        getToggleButton().click();
//        List<WebElement> comboBoxOptions;
//        List<WebElement> comboBoxOptions = getDriver().findElements(By.cssSelector("vaadin-multi-select-combo-box-item"));
//        if(element.getTagName().equals("input")){
//            printToConsole(element);
        List<WebElement> comboBoxOptions = (List<WebElement>) getWait().until(ExpectedConditions.jsReturnsValue("return document.querySelectorAll('vaadin-multi-select-combo-box-item')"));
//            comboBoxOptions = getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(element, By.cssSelector("vaadin-multi-select-combo-box-item")));
//        }
//        else{
//            printToConsole(element);
//            comboBoxOptions = getWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("vaadin-multi-select-combo-box-item")));
//        }

        if(comboBoxOptions.size() == 0){
            System.err.println("Nincs elem a multiselect combo boxban!");
            getToggleButton().click();
        }
        else {
            for(WebElement comboBoxElement : comboBoxOptions){
                if(comboBoxElement.getDomAttribute("selected") != null){
                    js.executeScript("arguments[0].click()", comboBoxElement);
                }
            }
            getToggleButton().click();
        }
    }

    @Override
    public VaadinDropdownComponent fillWithRandom() {
        assertEquals(true, this.isEnabled(), "The multi select combo box is not enabled: " + element.getText());
        element.click();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-multi-select-combo-box-item"));
        int i = 0;
        if(comboBoxOptions.size() == 0){
            System.err.println("Nincs elem a multiple select combo boxban! ");
            toggleButton.click();
            return this;
//            printToConsole(comboBox);
        }

        if(comboBoxOptions.size() == 1){
            comboBoxOptions.get(0).click();
            toggleButton.click();
            return this;
//            return comboBoxOptions.get(0).getText();
        }
        else {
            Random rnd = new Random();
            Integer selectedIndex = rnd.nextInt(0, comboBoxOptions.size() - 1);
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("arguments[0].click();", comboBoxOptions.get(selectedIndex));
//            return comboBoxOptions.get(selectedIndex).getText();
            toggleButton.click();
            return this;
        }

    }

    public void selectElements(String... elements) {
        JavascriptExecutor js = (JavascriptExecutor) getDriver();

        getToggleButton().click();

//        List<WebElement> comboBoxOptions = getDriver().findElements(By.cssSelector("vaadin-multi-select-combo-box-item"));
        List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-multi-select-combo-box-item"));
        int i = 0;
        while(comboBoxOptions.size() == 0){
            System.err.println("Nincs elem a multi select combo boxban! ");
            toggleButton.click();
//            printToConsole(comboBox);
        }
        if(comboBoxOptions.size() == 1){
            comboBoxOptions.get(0).click();
//            return comboBoxOptions.get(0).getText();
        }
        else {
            for(WebElement comboBoxElement : comboBoxOptions){
                if(Arrays.asList(elements).contains(comboBoxElement.getText())){
                    JavascriptExecutor jsExecutor = (JavascriptExecutor) getDriver();
                    jsExecutor.executeScript("arguments[0].click();", comboBoxElement);
                    break;
                }
            }

            getToggleButton().click();
        }
    }
}
