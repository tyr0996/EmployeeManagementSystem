package hu.martin.ems;

import hu.martin.ems.base.GridTestingUtil;
import lombok.Getter;
import org.openqa.selenium.*;

import java.util.List;

@Deprecated
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
    private WebElement currentPageNumber;

    @Getter
    private WebElement lastPageNumber;

    private int nubmerOfPages;

    private WebElement grid;
    private WebDriver driver;

    public enum PAGE_CHANGE_EVENT {
        NEXT,
        PREVIOUS,
        FIRST,
        LAST
    }

    private int lastKnownPageNumber;
    private GridTestingUtil gridTestingUtil;

    public PaginatorComponents(WebElement grid, WebDriver driver, GridTestingUtil gridTestingUtil) {
        this.grid = grid;
        this.driver = driver;
        this.gridTestingUtil = gridTestingUtil;
        
        WebElement span = gridTestingUtil.getParent(grid).findElement(By.tagName("span"));
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
            if(v.getDomAttribute("aria-disabled").equals("true")){
                this.currentPageNumber = v;
            }
//            if(v.getDomAttribute("elevation").equals("1")){
//                this.currentPageIndex = v;
//            }
        });

        //Get max page number
//        WebElement parent = gridTestingUtil.getParent(grid);
//        WebElement paginationComponent = parent.findElement(By.tagName("span")).findElement(By.tagName("lit-pagination"));
//        Integer total = Integer.parseInt(paginationComponent.getDomAttribute("total"));
//        Integer currentPage = Integer.parseInt(paginationComponent.getDomAttribute("page"));
//        this.nubmerOfPages numberOfPages = (int) Math.ceil((double) total / (double) pageSize);
    }

    public void refresh(){
        WebElement span = gridTestingUtil.getParent(grid).findElement(By.tagName("span"));
        WebElement litPagination = span.findElement(By.tagName("lit-pagination"));
        SearchContext shadow = litPagination.getShadowRoot();
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        List<WebElement> paperButtons = (List<WebElement>) executor.executeScript("return arguments[0].querySelectorAll('div[hidden] paper-button')", shadow);
        paperButtons.forEach(v -> {
            if(v.getDomAttribute("aria-disabled").equals("true")){
                this.currentPageNumber = v;
                this.lastKnownPageNumber = Integer.parseInt(v.getText());
            }
        });
    }

    public Integer getCurrentPageNumber() {
        refresh();
        if(currentPageNumber == null){
            return lastKnownPageNumber;
        }
        return Integer.parseInt(this.currentPageNumber.getText());
    }

    public void changePage(PAGE_CHANGE_EVENT event){
        switch (event){
            case FIRST -> lastKnownPageNumber = 1;
            case LAST -> lastKnownPageNumber = 0;
            default -> lastKnownPageNumber = -1;
        }
    }
}
