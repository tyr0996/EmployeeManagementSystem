package hu.martin.ems.base;

import hu.martin.ems.UITests.ElementLocation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static hu.martin.ems.base.GridTestingUtil.*;
import static hu.martin.ems.base.RandomGenerator.generateRandomOnlyLetterString;
import static org.junit.jupiter.api.Assertions.*;

public class CrudTestingUtil {

    public final WebDriver driver;

    private final String showDeletedCheckBoxXpath;
    private final String gridXpath;
    private final String createButtonXpath;
    private LinkedHashMap<String, String> createOrUpdateDialogPartXpaths;
    private final String className;

    public CrudTestingUtil(WebDriver driver,
                           String className,
                           String showDeletedCheckBoxXpath,
                           String gridXpath,
                           String createButtonXpath){
        this.driver = driver;
        this.className = className;
        this.createButtonXpath = createButtonXpath;
        GridTestingUtil.driver = driver;

        this.showDeletedCheckBoxXpath = showDeletedCheckBoxXpath;
        this.gridXpath = gridXpath;
        this.createOrUpdateDialogPartXpaths = new LinkedHashMap<>();
    }

    public void updateTest() throws InterruptedException {
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath, showDeletedCheckBoxXpath);
        if(rowLocation == null){
            createTest();
            rowLocation = new ElementLocation(1, 0);
        }
        goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());
        WebElement modifyButton = getModifyButton(gridXpath, rowLocation.getRowIndex());
        Thread.sleep(200);

        WebElement firstNameCell = getVisibleGridCell(gridXpath, rowLocation.getRowIndex(), 0);
        WebElement lastNameCell = getVisibleGridCell(gridXpath, rowLocation.getRowIndex(), 1);
        WebElement roleCell = getVisibleGridCell(gridXpath, rowLocation.getRowIndex(), 2);
        WebElement salaryCell = getVisibleGridCell(gridXpath, rowLocation.getRowIndex(), 3);


        //TODO azt kell megcsinálni, hogy random válasszon egyet (vagy többet) a bemeneti mezőkből, és azok alapján generáljon adatot
        String originalFirstName = firstNameCell.getText();
        String originalLastName = lastNameCell.getText();
        modifyButton.click();

        WebElement dialog = findVisibleElementWithXpath("//*[@id=\"overlay\"]");
        WebElement saveEmployeeButton = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        List<WebElement> fields = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout").findElements(By.xpath("./*"));
        for(int i = 0; i < fields.size(); i++){
            fillElementWithRandom(fields.get(i));
        }

        WebElement firstNameField = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-text-field[1]/input");
        WebElement lastNameField = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-text-field[2]/input");
        WebElement salaryField = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-number-field/input");
        WebElement roleComboBox = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-combo-box/input");

        assertNotEquals(firstNameCell.getText(), firstNameField.getText());
        assertNotEquals(lastNameCell.getText(), lastNameField.getText());
        assertNotEquals(salaryCell.getText(), salaryField.getText());
        assertNotEquals(roleCell.getText(), roleComboBox.getText());

        String newFirstName = generateRandomOnlyLetterString();
        firstNameField.clear();
        firstNameField.sendKeys(newFirstName);

        saveEmployeeButton.click();
        checkNotificationContainsTexts(className + " saved: ");
        Thread.sleep(200);
//        assertNull(lookingForElementInGrid(gridXpath, originalFirstName, originalLastName, "Martin", "20500"));
//        Thread.sleep(2000);
//        assertNotNull(lookingForElementInGrid(gridXpath, newFirstName, originalLastName, "Martin", "20500"));
    }

    public void createTest() throws InterruptedException {
        int originalVisible = countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalInvisible = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);

        WebElement createButton = findClickableElementWithXpath(createButtonXpath);
        createButton.click();


        List<WebElement> fields = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout").findElements(By.xpath("./*"));
        List<String> generatedData = new ArrayList<>();
        for(int i = 0; i < fields.size(); i++){
            fillElementWithRandom(fields.get(i));
        }

        WebElement saveEmployeeButton = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        saveEmployeeButton.click();
        checkNotificationContainsTexts(className + " saved: ");
        //assertNotNull(lookingForElementInGrid(gridXpath, firstName, lastName, "Martin", "20500"));
        Thread.sleep(2000);
        assertEquals(originalVisible + 1, countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        assertEquals(originalInvisible, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }

    public void deleteTest() throws InterruptedException {
        int originalVisible = countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalInvisible = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);

        if(originalVisible == 0){
            createTest();
            originalVisible++;
        }
        WebElement grid = findVisibleElementWithXpath(gridXpath);
        ElementLocation rowLocation = getRandomLocationFromGrid(gridXpath, showDeletedCheckBoxXpath);
        if(rowLocation == null){
            createTest();
            rowLocation = new ElementLocation(1, 0);
        }
        goToPageInPaginatedGrid(gridXpath, rowLocation.getPageNumber());


        WebElement showDeletedCheckBox = findVisibleElementWithXpath(showDeletedCheckBoxXpath);

        Thread.sleep(200);
//        WebElement firstName = getVisibleGridCell(gridXpath, rowLocation.getRowIndex(), 0);
//        WebElement lastName = getVisibleGridCell(gridXpath, rowLocation.getRowIndex(), 1);
//        String name = firstName.getText() + " " + lastName.getText();
        WebElement deleteButton = getDeleteButton(gridXpath, rowLocation.getRowIndex());
        deleteButton.click();
        checkNotificationContainsTexts(className + " deleted: ");
        Thread.sleep(2000);

        grid = findVisibleElementWithXpath(gridXpath);
        assertEquals(originalVisible - 1, countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        assertEquals(originalInvisible + 1, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        showDeletedCheckBox.click();
        assertEquals(originalInvisible + originalVisible, countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath));
        showDeletedCheckBox.click();
    }

    public void permanentlyDeleteTest() throws InterruptedException {
        int originalVisible = countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalInvisible = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        if(originalVisible == 0 && originalInvisible == 0){
            createTest();
            Thread.sleep(500);
            originalVisible++;
        }
        if(originalInvisible == 0){
            deleteTest();
            Thread.sleep(500);
            originalVisible--;
            originalInvisible++;
        }
        ElementLocation el = getRandomLocationDeletedStatusFromGrid(gridXpath, showDeletedCheckBoxXpath);
        WebElement showDeleted = findClickableElementWithXpath(showDeletedCheckBoxXpath);
        showDeleted.click();
        Thread.sleep(500);
        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());
        String[] selectedData = getDataFromRowLocation(gridXpath, el);
        assertNotNull(lookingForElementInGrid(gridXpath, selectedData));
        goToPageInPaginatedGrid(gridXpath, el.getPageNumber());
        WebElement pDeleteButton = getPermanentlyDeleteButton(gridXpath, el.getRowIndex());
        pDeleteButton.click();
        checkNotificationContainsTexts(className + " permanently deleted: ");
        Thread.sleep(500);
        assertNull(lookingForElementInGrid(gridXpath, selectedData));
        showDeleted = findClickableElementWithXpathWithWaiting(showDeletedCheckBoxXpath);
        showDeleted.click();
        assertNull(lookingForElementInGrid(gridXpath, selectedData));

        assertEquals(originalInvisible - 1, countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
    }

    public void readTest(){
//        lookingForElementInGrid(grid, createdEmployee.getFirstName(),
//                createdEmployee.getLastName(),
//                createdEmployee.getRoleName(),
//                createdEmployee.getSalary(),
//                "skip");
    }

    public void restoreTest() throws InterruptedException {
        int originalVisibleRows = countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int originalInvisibleRows = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        WebElement showDeletedButton = findClickableElementWithXpath(showDeletedCheckBoxXpath);

        if(originalInvisibleRows == 0){
            createTest();
            deleteTest();
            originalInvisibleRows++;
        }

        if(!showDeletedButton.isSelected()){
            showDeletedButton.click();
        }
        WebElement restoreButton;
        ElementLocation location;
        do {
            location = getRandomLocationFromGrid(gridXpath, showDeletedCheckBoxXpath);
            goToPageInPaginatedGrid(gridXpath, location.getPageNumber());
            restoreButton = getRestoreButton(gridXpath, location.getRowIndex());
        } while (restoreButton == null);
        String[] originalData = getDataFromRowLocation(gridXpath, location);
        showDeletedButton = findClickableElementWithXpath(showDeletedCheckBoxXpath);
        showDeletedButton.click();
        Thread.sleep(1000);
        //assertNull(lookingForElementInGrid(gridXpath, originalData)); //TODO
        findClickableElementWithXpath(showDeletedCheckBoxXpath).click();
        assertNotNull(lookingForElementInGrid(gridXpath, originalData));
        goToPageInPaginatedGrid(gridXpath, location.getPageNumber());
        restoreButton = getRestoreButton(gridXpath, location.getRowIndex());
        restoreButton.click();
        Thread.sleep(1000);
        findClickableElementWithXpath(showDeletedCheckBoxXpath).click();
        assertNotNull(lookingForElementInGrid(gridXpath, originalData));

        int newVisibleRows = countVisibleGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        int newInvisibleRows = countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath);
        assertEquals(originalVisibleRows + 1, newVisibleRows);
        assertEquals(originalInvisibleRows - 1, newInvisibleRows);
    }
}
