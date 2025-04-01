package hu.martin.ems.pages.core;

public interface ILoggedInPage<T> {
    String contentLayoutXpath = "//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout[2]";
    String sideMenuXpath = "//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout";
    T initWebElements();

    void logout();
}
