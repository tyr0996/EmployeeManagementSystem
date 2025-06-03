package hu.martin.ems.pages.core.component;

import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.SingleFillable;
import jakarta.annotation.Nullable;
import lombok.Getter;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VaadinDropdownComponent extends VaadinFillableComponent implements SingleFillable<VaadinDropdownComponent, String> {

    @Getter
    protected WebElement toggleButton;
    @Getter
    private Boolean isHeaderField;

    public VaadinDropdownComponent(WebDriver driver, By provider) {
        this(driver, provider, false);
    }

    public VaadinDropdownComponent(WebDriver driver, By provider, boolean isHeaderField) {
        super(driver, provider);
        this.isHeaderField = isHeaderField;
        initWebElements();
    }

    public VaadinDropdownComponent(WebDriver driver, WebElement parent, By provider, int index) {
        super(driver, parent, provider, index);
        initWebElements();
    }

    public String getSelectedElement() {
        Object selectedElement = ((JavascriptExecutor) getDriver()).executeScript("return arguments[0].selectedItem ? arguments[0].selectedItem.label : null", element);
        return selectedElement.toString();
    }

    public void initWebElements() {
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        if (!element.getTagName().equals("vaadin-combo-box")) {
//            printToConsole();
            throw new IllegalArgumentException("Can't create ComboBox from an " + element.getTagName() + "!");
        }
        toggleButton = (WebElement) js.executeScript("return arguments[0].querySelector('div').querySelector('vaadin-input-container').querySelectorAll('div')[1]", element.getShadowRoot());
//
        assert toggleButton != null;
    }

    public int getElementNumber() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        var a = js.executeScript("return Array.from(arguments)[0].__data.filteredItems.length", element);
//        System.out.println("sanyi");
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

    public void fillWith(int index) {
        assert this.isEnabled() : "The combo box is not enabled: " + element.getText();

        element.click();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-combo-box-item"));
        int i = 0;
        if (comboBoxOptions.size() == 0) {
            System.err.println("Nincs elem a combo boxban! " + getTitle());
            element.click();
            fillWith(index);
        }
        if (comboBoxOptions.size() == 1) {
            comboBoxOptions.get(0).click();
        } else {
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
        List<WebElement> comboBoxOptions = getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(getScope(), By.cssSelector("vaadin-combo-box-item")));
//        List<WebElement> comboBoxOptions = driver.findElements(By.cssSelector("vaadin-combo-box-item"));
        int i = 0;
        if (comboBoxOptions.size() == 0) {
            System.err.println("Nincs elem a combo boxban! " + getTitle());
            element.click();
//            printToConsole(comboBox);
        }
        if (comboBoxOptions.size() == 1) {
            comboBoxOptions.get(0).click();
            this.waitForRefresh();
            return this;
        } else {
            Random rnd = new Random();
            Integer selectedIndex = rnd.nextInt(0, comboBoxOptions.size() - 1);
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("arguments[0].click();", comboBoxOptions.get(selectedIndex));
            this.waitForRefresh();
            return this;
        }
    }

    @Override
    public void clear() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].selectedItem=''", element);
        element.sendKeys(Keys.ENTER);
        waitForRefresh();
    }


    public void scrollDown(int pixels) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement scroller = (WebElement) js.executeScript("return arguments[0].shadowRoot.querySelector('vaadin-combo-box-overlay').shadowRoot.querySelector('vaadin-combo-box-dropdown-wrapper').shadowRoot.querySelector('vaadin-combo-box-scroller')", this.element);
        js.executeScript("arguments[0].scrollTop = arguments[1];", scroller, pixels);
    }

    @Override
    public VaadinDropdownComponent fillWith(@Nullable String value) {
        assertTrue(this.isEnabled(), "The combo box is not enabled: " + element.getText());
        if (value == null || value.equals("")) {
            return this;
        }
        element.click();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<WebElement> comboBoxOptions = getWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("vaadin-combo-box-item")));
        List<WebElement> filtered = comboBoxOptions.stream().filter(v -> v.getText().equals(value)).toList();
        if (filtered.size() == 0) {
            String elements = String.join(", ", comboBoxOptions.stream().map(v -> v.getText()).toList()).toString();
            throw new NoSuchElementException("Element " + value + " not found in " + element.getText() + ". Elements: " + elements);
        }
        filtered.get(0).click();
        return this;
    }

    @Override
    public String getTitle() {
        if (isHeaderField) {
            return getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(getParentWebElement(element), By.xpath("./label"))).get(0).getText();
        } else {
            return super.getTitle();
        }
    }
}
