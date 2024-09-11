package hu.martin.ems;

import lombok.Getter;
import org.openqa.selenium.*;

import java.util.List;

public class PaginatorComponents {
    @Getter
    private WebElement firstButton;

    @Getter
    private WebElement previousButton;

    @Getter
    private WebElement nextButton;

    @Getter
    private WebElement lastButton;

    @Getter
    private WebElement currentPageIndex;

    private WebElement grid;
    private WebDriver driver;

    public PaginatorComponents(WebElement grid, WebDriver driver){
        this.grid = grid;
        this.driver = driver;
        WebElement span = TestingUtils.getParent(grid).findElement(By.tagName("span"));
        WebElement litPagination = span.findElement(By.tagName("lit-pagination"));
        SearchContext shadow = litPagination.getShadowRoot();
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        List<WebElement> paperIconButtons = (List<WebElement>) executor.executeScript("return arguments[0].querySelectorAll('div[hidden] paper-icon-button')", shadow);
        paperIconButtons.forEach(v -> {
            switch (v.getDomAttribute("id")){
                case "fastRewindId" : this.firstButton = v; break;
                case "navigateBeforeId" : this.previousButton = v; break;
                case "navigateNextId" : this.nextButton = v; break;
                case "fastForwardId" : this.lastButton = v; break;
                default: throw new RuntimeException("Unknown paper-icon-button id: " + v.getDomAttribute("id"));
            }
        });
        List<WebElement> paperButtons = (List<WebElement>) executor.executeScript("return arguments[0].querySelectorAll('div[hidden] paper-button')", shadow);
        paperButtons.forEach(v -> {
            if(v.getDomAttribute("elevation").equals("1")){
                this.currentPageIndex = v;
            }
        });
    }

    public void refresh(){
        WebElement span = TestingUtils.getParent(grid).findElement(By.tagName("span"));
        WebElement litPagination = span.findElement(By.tagName("lit-pagination"));
        SearchContext shadow = litPagination.getShadowRoot();
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        List<WebElement> paperButtons = (List<WebElement>) executor.executeScript("return arguments[0].querySelectorAll('div[hidden] paper-button')", shadow);
        paperButtons.forEach(v -> {
            if(v.getDomAttribute("elevation").equals("0")){
                this.currentPageIndex = v;
            }
        });
    }

    public Integer getCurrentPageNumber() {
        refresh();
        return Integer.parseInt(this.currentPageIndex.getText());
    }
}
