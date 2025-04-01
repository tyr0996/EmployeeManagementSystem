package hu.martin.ems.pages.core.component;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class VaadinDatePickerComponent extends VaadinBaseComponent {
    public VaadinDatePickerComponent(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public LocalDate getDate(){
//        return LocalDate.parse(element.getAttribute("value"), DateTimeFormatter.ofPattern("yyyy. MM. dd"));
        String value = element.getAttribute("value");
        return value == null || value.equals("") ? null : LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public void selectDate(LocalDate date){
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        if(date != null){
            String todayString = date.format(DateTimeFormatter.ofPattern("yyyy. MM. dd."));
            element.sendKeys(todayString);
        }
        element.sendKeys(Keys.ENTER);
    }

    public void selectDate(String dateString){
        element.sendKeys(dateString, Keys.ENTER);
    }

    public void clear() {
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
    }
}
