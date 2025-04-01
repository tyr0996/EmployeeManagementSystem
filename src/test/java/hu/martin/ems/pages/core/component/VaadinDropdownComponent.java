package hu.martin.ems.pages.core.component;

import hu.martin.ems.pages.core.component.saveOrUpdateDialog.SingleFillable;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VaadinDropdownComponent extends VaadinFillableComponent implements SingleFillable<VaadinDropdownComponent, String> {

    @Getter protected WebElement toggleButton;
    public VaadinDropdownComponent(WebDriver driver, WebElement element) {
        super(driver, element);
        initWebElements();
    }

    public String getSelectedElement(){
        return this.getElement().getText();
    }

    public void initWebElements(){
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
//        toggleButton = (WebElement) js.executeScript("return arguments[0].querySelectorAll('*')[6].querySelectorAll('*')[5];", element.getShadowRoot());
        toggleButton = (WebElement) js.executeScript("return arguments[0].querySelector('div').querySelector('vaadin-input-container').querySelectorAll('div')[1]", element.getShadowRoot());
//        printToConsole(toggleButton);
        assert toggleButton != null;
    }

    public int getElementNumber(){
        JavascriptExecutor js = (JavascriptExecutor) driver;
        var a = js.executeScript("return Array.from(arguments)[0].__data.filteredItems.length", element);
        System.out.println("sanyi");
        return Integer.parseInt(a.toString());
//        assertEquals(true, this.isEnabled(), "The combo box is not enabled: " + element.getText());
//        element.click();
//        try {
//            Thread.sleep(50);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        return driver.findElements(By.cssSelector("vaadin-combo-box-item")).size();
    }

    public void fillWith(int index){
        assert this.isEnabled() : "The combo box is not enabled: " + element.getText();
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//        js.executeScript("arguments[0].selectedItem=Array.from(arguments[0])[0].__data.filteredItems[" + index + "]", element); //TODO ez nem jó, mert nem tudja a __data-t kiolvasni.


        element.click();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-combo-box-item"));
        int i = 0;
        while(comboBoxOptions.size() == 0){
            System.err.println("Nincs elem a combo boxban! ");
//            printToConsole(comboBox);
        }
        if(comboBoxOptions.size() == 1){
            comboBoxOptions.get(0).click();
//            return comboBoxOptions.get(0).getText();
        }
        else {
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("arguments[0].click();", comboBoxOptions.get(index));
        }
    }

    @Override
    public VaadinDropdownComponent fillWithRandom() {
        assertEquals(true, this.isEnabled(), "The combo box is not enabled: " + element.getText());
        element.click();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-combo-box-item"));
        int i = 0;
        while(comboBoxOptions.size() == 0){
            System.err.println("Nincs elem a combo boxban! ");
//            printToConsole(comboBox);
        }
        if(comboBoxOptions.size() == 1){
            comboBoxOptions.get(0).click();
            return this;
//            return comboBoxOptions.get(0).getText();
        }
        else {
            Random rnd = new Random();
            Integer selectedIndex = rnd.nextInt(0, comboBoxOptions.size() - 1);
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("arguments[0].click();", comboBoxOptions.get(selectedIndex));
//            return comboBoxOptions.get(selectedIndex).getText();
            return this;
        }
    }

    @Override
    public VaadinDropdownComponent fillWith(String value) {
        assertEquals(true, this.isEnabled(), "The combo box is not enabled: " + element.getText());
        element.click();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-combo-box-item"));
//        comboBoxOptions.forEach(v -> printToConsole(v));
        comboBoxOptions.stream().filter(v -> v.getText().equals(value)).toList().get(0).click();
        return this;
//        int i = 0;
//        while(comboBoxOptions.size() == 0){
//            System.err.println("Nincs elem a combo boxban! ");
////            printToConsole(comboBox);
//        }
//        if(comboBoxOptions.size() == 1){
//            comboBoxOptions.get(0).click();
//            return this;
////            return comboBoxOptions.get(0).getText();
//        }
//        else {
//            Random rnd = new Random();
//            Integer selectedIndex = rnd.nextInt(0, comboBoxOptions.size() - 1);
//            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
//            jsExecutor.executeScript("arguments[0].click();", comboBoxOptions.get(selectedIndex));
////            return comboBoxOptions.get(selectedIndex).getText();
//            return this;
//        }
//    }
} }
