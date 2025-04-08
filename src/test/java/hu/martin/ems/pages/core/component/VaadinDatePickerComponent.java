package hu.martin.ems.pages.core.component;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class VaadinDatePickerComponent extends VaadinBaseComponent {
    public VaadinDatePickerComponent(WebDriver driver, By provider) {
        super(driver, provider);
    }

    public LocalDate getDate(){
//        return LocalDate.parse(element.getAttribute("value"), DateTimeFormatter.ofPattern("yyyy. MM. dd"));
        String value = element.getAttribute("value");
        boolean isEmpty = value == null || value.equals("");
        LocalDate date = null;
        if(!isEmpty && value.contains("-")){
            date = LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        else if(!isEmpty && value.contains(".")){
            date = LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy. MM. dd"));
        }
        else {
            System.err.println("Unsupported date format: " + value);
        }

        return value == null || value.equals("") ? null : date;
    }

    public void selectDate(LocalDate date){
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        if(date != null){
            String todayString = date.format(DateTimeFormatter.ofPattern("yyyy. MM. dd."));
            element.sendKeys(todayString);
        }
        element.sendKeys(Keys.ENTER);
        waitForRefresh();
    }

    public void selectDate(String dateString){
        element.sendKeys(dateString, Keys.ENTER);
    }

    public void clear() {
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
    }
}
