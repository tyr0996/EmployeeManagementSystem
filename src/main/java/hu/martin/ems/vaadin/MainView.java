package hu.martin.ems.vaadin;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinServletService;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.vaadin.component.AccessManagement.AccessManagement;
import hu.martin.ems.vaadin.component.Address.AddressList;
import hu.martin.ems.vaadin.component.AdminTools.AdminTools;
import hu.martin.ems.vaadin.component.City.CityList;
import hu.martin.ems.vaadin.component.CodeStore.CodeStoreList;
import hu.martin.ems.vaadin.component.Currency.CurrencyList;
import hu.martin.ems.vaadin.component.Customer.CustomerList;
import hu.martin.ems.vaadin.component.Employee.EmployeeList;
import hu.martin.ems.vaadin.component.Order.OrderCreate;
import hu.martin.ems.vaadin.component.Order.OrderList;
import hu.martin.ems.vaadin.component.OrderElement.OrderElementList;
import hu.martin.ems.vaadin.component.Product.ProductList;
import hu.martin.ems.vaadin.component.Supplier.SupplierList;
import hu.martin.ems.vaadin.component.User.UserList;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Route("")
@CssImport("./styles/shared-styles.css")
@PermitAll
@NeedCleanCoding
public class MainView extends HorizontalLayout implements RouterLayout {

    private VerticalLayout menuLayout;

    public MainView() {
        menuLayout = new VerticalLayout();
        menuLayout.setClassName("side-menu");

        Div contentLayout = new Div();
        contentLayout.setClassName("content-layout");

        addMenu(menuLayout, "Admin", "Employee", EmployeeList.class);
        addMenu(menuLayout, "Admin", "Access management", AccessManagement.class);
        addMenu(menuLayout, "Admin", "Codestore", CodeStoreList.class);
        addMenu(menuLayout, "Admin", "City", CityList.class);
        addMenu(menuLayout, "Admin", "Address", AddressList.class);
        addMenu(menuLayout, "Admin", "Customer", CustomerList.class);
        addMenu(menuLayout, "Admin", "Product", ProductList.class);
        addMenu(menuLayout, "Admin", "Supplier", SupplierList.class);
        addMenu(menuLayout, "Admin", "Currency", CurrencyList.class);
        addMenu(menuLayout, "Admin", "Users", UserList.class);
        addMenu(menuLayout, "Admin", "Admin tools", AdminTools.class);
        addMenu(menuLayout, "Orders", "OrderElement", OrderElementList.class);
        addMenu(menuLayout, "Orders", "Order", OrderCreate.class, OrderList.class);

        addLogoutButton();

        addClassName("main-view");
        add(menuLayout, contentLayout);
        setSizeFull();
    }

    private void addLogoutButton() {
        Button logoutButton = new Button("Log out", event -> {
            HttpServletRequest request = (HttpServletRequest) VaadinServletService.getCurrentRequest();
            HttpServletResponse response = VaadinServletService.getCurrentResponse();

            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());

            UI.getCurrent().getPage().setLocation("logout");
            Notification.show("Logging out successfully!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        logoutButton.addClassNames("logout-button");
        menuLayout.add(logoutButton);
    }

    private void addMenu(VerticalLayout menuLayout, String mainMenuName,
                         String subMenuName, Class listClass) {
        Span mainMenu = (Span) menuLayout.getChildren()
                .filter(component -> component instanceof Span && ((Span) component).getText().equals(mainMenuName))
                .findFirst()
                .orElseGet(() -> {
                    Span newMainMenu = new Span(mainMenuName);
                    menuLayout.add(newMainMenu);
                    return newMainMenu;
                });

        if (listClass != null) {
            RouterLink listLink = new RouterLink(subMenuName, listClass);
            HorizontalLayout listMenu = new HorizontalLayout(listLink);
            listMenu.setVisible(false);
            mainMenu.getElement().addEventListener("click", e -> {
                listMenu.setVisible(!listMenu.isVisible());
            });

            menuLayout.add(listMenu);
        }
    }

    @Deprecated
    private void addMenu(VerticalLayout menuLayout, String mainMenuName,
                         String subMenuName, Class createClass, Class listClass) {
        Span mainMenu = (Span) menuLayout.getChildren()
                .filter(component -> component instanceof Span && ((Span) component).getText().equals(mainMenuName))
                .findFirst()
                .orElseGet(() -> {
                    Span newMainMenu = new Span(mainMenuName);
                    menuLayout.add(newMainMenu);
                    return newMainMenu;
                });

        if (listClass != null) {
            RouterLink listLink = new RouterLink(subMenuName, listClass);
            HorizontalLayout listMenu = new HorizontalLayout(listLink);
            listMenu.setVisible(false);
            mainMenu.getElement().addEventListener("click", e -> {
                listMenu.setVisible(!listMenu.isVisible());
            });

            menuLayout.add(listMenu);
        }
        if (createClass != null) {
            RouterLink createLink = new RouterLink(subMenuName + " create", createClass);
            HorizontalLayout createMenu = new HorizontalLayout(createLink);
            createMenu.setVisible(false);
            mainMenu.getElement().addEventListener("click", e -> {
                createMenu.setVisible(!createMenu.isVisible());
            });

            menuLayout.add(createMenu);
        }
    }
}
