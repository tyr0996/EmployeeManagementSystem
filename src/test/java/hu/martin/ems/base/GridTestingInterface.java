package hu.martin.ems.base;

import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.PaginationData;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

public class GridTestingInterface {
    
    GridTestingUtil gridTestingUtil;

    WebElement goToPageInPaginatedGrid(String gridXpath, int requiredPageNumber) throws InterruptedException {
		return gridTestingUtil.goToPageInPaginatedGrid(gridXpath, requiredPageNumber);
}

    WebElement getRowAtPosition(String gridXpath, ElementLocation location) {
		return gridTestingUtil.getRowAtPosition(gridXpath, location);
}

    void navigateMenu(String mainUIXpath, String subIXpath) {
		gridTestingUtil.navigateMenu(mainUIXpath, subIXpath);
}

    int getGridColumnNumber(String gridXpath) {
		return gridTestingUtil.getGridColumnNumber(gridXpath);
}

    WebElement getVisibleGridRow(String gridXpath, int rowIndex) {
		return gridTestingUtil.getVisibleGridRow(gridXpath, rowIndex);
}

    PaginationData getGridPaginationData(String gridXpath) {
		return gridTestingUtil.getGridPaginationData(gridXpath);
}

    WebElement getVisibleGridCell(String gridXpath, int rowIndex, int columnIndex) {
		return gridTestingUtil.getVisibleGridCell(gridXpath, rowIndex, columnIndex);
}

    WebElement getVisibleGridCell(WebElement row, int columnIndex) {
		return gridTestingUtil.getVisibleGridCell(row, columnIndex);
}

    WebElement getDeleteButton(String gridXpath, int rowIndex) {
		return gridTestingUtil.getDeleteButton(gridXpath, rowIndex);
}

    WebElement getModifyButton(String gridXpath, int rowIndex) {
		return gridTestingUtil.getModifyButton(gridXpath, rowIndex);
}

    WebElement getPermanentlyDeleteButton(String gridXpath, int rowIndex) {
		return gridTestingUtil.getPermanentlyDeleteButton(gridXpath, rowIndex);
}

    WebElement getRestoreButton(String gridXpath, int rowIndex) {
		return gridTestingUtil.getRestoreButton(gridXpath, rowIndex);
}

    WebElement findClickableElementWithXpath(String xpath) {
		return gridTestingUtil.findClickableElementWithXpath(xpath);
}

    WebElement findClickableElementWithXpathWithWaiting(String xpath) {
		return gridTestingUtil.findClickableElementWithXpathWithWaiting(xpath);
}

    WebElement findGrid(String grid) {
		return gridTestingUtil.findGrid(grid);
}

    WebElement findVisibleElementWithXpath(String xpath) {
		return gridTestingUtil.findVisibleElementWithXpath(xpath);
}

    int countVisibleGridDataRows(String gridXpath) throws InterruptedException {
		return gridTestingUtil.countVisibleGridDataRows(gridXpath);
}

    int countHiddenGridDataRows(String gridXpath, String showDeletedXpath) throws InterruptedException {
		return gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedXpath);
}

    int countVisibleGridDataRowsOnPage(String gridXpath) {
		return gridTestingUtil.countVisibleGridDataRowsOnPage(gridXpath);
}

    ElementLocation getRandomLocationFromGrid(String gridXpath) throws InterruptedException {
		return gridTestingUtil.getRandomLocationFromGrid(gridXpath);
}

    String[] getRandomDataDeletedStatusFromGrid(String gridXpath, String showDeletedXpath) throws InterruptedException {
		return gridTestingUtil.getRandomDataDeletedStatusFromGrid(gridXpath, showDeletedXpath);
}

    @Deprecated
    ElementLocation getRandomLocationDeletedStatusFromGrid(String gridXpath, String showDeletedXpath) throws InterruptedException {
		return gridTestingUtil.getRandomLocationDeletedStatusFromGrid(gridXpath, showDeletedXpath);
}

    boolean isDeletedRow(String gridXpath, WebElement row) {
		return gridTestingUtil.isDeletedRow(gridXpath, row);
}

    String fillElementWith(WebElement element, Boolean hasDeletableField, String previousPasswordFieldValue) throws InterruptedException {
		return gridTestingUtil.fillElementWith(element, hasDeletableField, previousPasswordFieldValue);
}

    String fillElementWith(WebElement element, Boolean hasDeletableField, String previousPasswordFieldValue, String withData) throws InterruptedException {
		return gridTestingUtil.fillElementWith(element, hasDeletableField, previousPasswordFieldValue, withData);
}

    void setCheckboxStatus(String checkboxXpath, boolean selected) throws InterruptedException {
		gridTestingUtil.setCheckboxStatus(checkboxXpath, selected);
}

    void selectElementByTextFromComboBox(WebElement comboBox, String text) throws InterruptedException {
		gridTestingUtil.selectElementByTextFromComboBox(comboBox, text);
}

    String selectRandomFromComboBox(WebElement comboBox) throws InterruptedException {
		return gridTestingUtil.selectRandomFromComboBox(comboBox);
}

    void selectRandomFromMultiSelectComboBox(WebElement multiSelectComboBox) throws InterruptedException {
		gridTestingUtil.selectRandomFromMultiSelectComboBox(multiSelectComboBox);
}

    int countElementResultsFromGridWithFilter(String gridXPath, String... attributes) throws InterruptedException {
		return gridTestingUtil.countElementResultsFromGridWithFilter(gridXPath, attributes);
}

    WebElement getOptionButton(String gridXpath, ElementLocation el, int index) {
		return gridTestingUtil.getOptionButton(gridXpath, el, index);
}

    WebElement getOptionDownloadButton(String gridXpath, ElementLocation el, int index) {
		return gridTestingUtil.getOptionDownloadButton(gridXpath, el, index);
}

    WebElement getOptionColumnButton(String gridXpath, ElementLocation el, int index) {
		return gridTestingUtil.getOptionColumnButton(gridXpath, el, index);
}

    void resetFilter(String gridXPath) throws InterruptedException {
		gridTestingUtil.resetFilter(gridXPath);
}

    void applyFilter(String gridXpath, String... attributes) throws InterruptedException {
		gridTestingUtil.applyFilter(gridXpath, attributes);
}

    void setExtraDataFilterValue(String gridXpath, String content, NotificationCheck notificationCheck) {
		gridTestingUtil.setExtraDataFilterValue(gridXpath, content, notificationCheck);
}

    void setExtraDataFilterValue(String gridXpath, LinkedHashMap<String, List<String>> content, NotificationCheck notificationCheck) {
		gridTestingUtil.setExtraDataFilterValue(gridXpath, content, notificationCheck);
}

    void clearExtraDataFilter(String gridXpath) throws InterruptedException {
		gridTestingUtil.clearExtraDataFilter(gridXpath);
}

    void checkNotificationText(String excepted) {
		gridTestingUtil.checkNotificationText(excepted);
}

    void checkNotificationContainsTexts(String text) {
		gridTestingUtil.checkNotificationContainsTexts(text);
}

    void checkNotificationContainsTexts(String text, long timeoutInMillis) {
		gridTestingUtil.checkNotificationContainsTexts(text, timeoutInMillis);
}

    String[] getDataFromRowLocation(String gridXpath, ElementLocation location, Boolean hasOptionColumn) throws InterruptedException {
		return gridTestingUtil.getDataFromRowLocation(gridXpath, location, hasOptionColumn);
}

    String[] getDataFromRowLocation(String gridXpath, ElementLocation location) throws InterruptedException {
		return gridTestingUtil.getDataFromRowLocation(gridXpath, location);
}

    boolean getCheckboxStatus(String checboxXpath) {
		return gridTestingUtil.getCheckboxStatus(checboxXpath);
}

    void printToConsole(WebElement e) {
		gridTestingUtil.printToConsole(e);
}

    void selectDateFromDatePicker(String datePickerXpath, LocalDate date) {
		gridTestingUtil.selectDateFromDatePicker(datePickerXpath, date);
}

    LocalDate getDateFromDatePicker(String datePickerXpath, String dateFormat) {
		return gridTestingUtil.getDateFromDatePicker(datePickerXpath, dateFormat);
}

    void selectMultipleElementsFromMultibleSelectionGrid(String gridXpath, int selectElementNumber) throws InterruptedException {
		gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(gridXpath, selectElementNumber);
}

    List<Integer> getRandomIndexes(int max, int count) {
		return gridTestingUtil.getRandomIndexes(max, count);
}
}
