package hu.martin.ems.vaadin;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import hu.martin.ems.vaadin.component.Address.AddressCreate;
import hu.martin.ems.vaadin.component.Address.AddressList;
import hu.martin.ems.vaadin.component.City.CityCreate;
import hu.martin.ems.vaadin.component.City.CityList;
import hu.martin.ems.vaadin.component.CodeStore.CodeStoreCreate;
import hu.martin.ems.vaadin.component.CodeStore.CodeStoreList;
import hu.martin.ems.vaadin.component.Currency.CurrencyList;
import hu.martin.ems.vaadin.component.Customer.CustomerCreate;
import hu.martin.ems.vaadin.component.Customer.CustomerList;
import hu.martin.ems.vaadin.component.Employee.EmployeeCreate;
import hu.martin.ems.vaadin.component.Employee.EmployeeList;
import hu.martin.ems.vaadin.component.Order.OrderCreate;
import hu.martin.ems.vaadin.component.Order.OrderList;
import hu.martin.ems.vaadin.component.OrderElement.OrderElementCreate;
import hu.martin.ems.vaadin.component.OrderElement.OrderElementList;
import hu.martin.ems.vaadin.component.Permission.PermissionCreate;
import hu.martin.ems.vaadin.component.Permission.PermissionList;
import hu.martin.ems.vaadin.component.Product.ProductCreate;
import hu.martin.ems.vaadin.component.Product.ProductList;
import hu.martin.ems.vaadin.component.Role.RoleCreate;
import hu.martin.ems.vaadin.component.Role.RoleList;
import hu.martin.ems.vaadin.component.RoleXPermission.RoleXPermissionCreate;
import hu.martin.ems.vaadin.component.Supplier.SupplierCreate;
import hu.martin.ems.vaadin.component.Supplier.SupplierList;

@Route("")
@CssImport("./styles/shared-styles.css")
public class MainView extends HorizontalLayout implements RouterLayout {

    public MainView() {
        VerticalLayout menuLayout = new VerticalLayout();
        menuLayout.setWidth("250px");

        Div contentLayout = new Div();
        contentLayout.setClassName("content-layout");

        // Dinamikusan hozzáadott menük
        addMenu(menuLayout, "Admin", "Employee", EmployeeCreate.class, EmployeeList.class);
        addMenu(menuLayout, "Admin", "Role", RoleCreate.class, RoleList.class);
        addMenu(menuLayout, "Admin", "Permission", PermissionCreate.class, PermissionList.class);
        addMenu(menuLayout, "Admin", "Role-permission pairing", RoleXPermissionCreate.class, null);
        addMenu(menuLayout, "Admin", "Codestore", CodeStoreCreate.class, CodeStoreList.class);
        addMenu(menuLayout, "Admin", "City", CityCreate.class, CityList.class);
        addMenu(menuLayout, "Admin", "Address", AddressCreate.class, AddressList.class);
        addMenu(menuLayout, "Admin", "Customer", CustomerCreate.class, CustomerList.class);
        addMenu(menuLayout, "Admin", "Product", ProductCreate.class, ProductList.class);
        addMenu(menuLayout, "Admin", "Supplier", SupplierCreate.class, SupplierList.class);
        addMenu(menuLayout, "Orders", "OrderElement", OrderElementCreate.class, OrderElementList.class);
        addMenu(menuLayout, "Orders", "Order", OrderCreate.class, OrderList.class);
        addMenu(menuLayout, "Admin", "Currency", null, CurrencyList.class);

        // Hozzáadva a meglévő kinézethez
        addClassName("main-view");
        add(menuLayout, contentLayout);
        setSizeFull();
    }

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
            RouterLink listLink = new RouterLink(subMenuName + " list", listClass);
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
