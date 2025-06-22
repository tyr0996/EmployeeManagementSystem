package hu.martin.ems.pages;

import hu.martin.ems.pages.core.ISimpleVaadinGridPage;
import hu.martin.ems.pages.core.SimpleVaadinGridPage;
import hu.martin.ems.pages.core.component.*;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OrderPage extends SimpleVaadinGridPage<OrderPage> implements ISimpleVaadinGridPage<OrderPage> {
    private static final String fromDatePickerXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-horizontal-layout/vaadin-date-picker[1]";
    private static final String toDatePickerXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-horizontal-layout/vaadin-date-picker[2]";
    private static final String showDeletedSwitchXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/switch";

    private static final String sendToAccountantSFTPButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-button";


    @Getter
    private VaadinDatePickerComponent fromDatePicker;
    @Getter
    private VaadinDatePickerComponent toDatePicker;
    @Getter
    private VaadinButtonComponent sendToAccountantSftpButton;
//    @Getter private VaadinCheckboxComponent showDeletedSwitch;
//    @Getter private VaadinGridComponent grid;

    public OrderPage(WebDriver driver, int port) {
        super(driver, port, "Order", null);
        initWebElements();
    }

    @Override
    public OrderPage initWebElements() {
        fromDatePicker = new VaadinDatePickerComponent(getDriver(), By.xpath(fromDatePickerXpath));
        toDatePicker = new VaadinDatePickerComponent(getDriver(), By.xpath(toDatePickerXpath));
        sendToAccountantSftpButton = new VaadinButtonComponent(getDriver(), By.xpath(sendToAccountantSFTPButtonXpath));
//        showDeletedCheckBox = new VaadinCheckboxComponent(getDriver(), By.xpath(showDeletedSwitchXpath));
        showDeletedSwitch = new VaadinSwitchComponent(getDriver(), By.xpath(showDeletedSwitchXpath));
        grid = new VaadinGridComponent(getDriver(), By.xpath(gridXpath));
        return this;
    }
}
