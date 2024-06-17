package hu.martin.ems.vaadin.component.Product;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Customer;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.model.Product;
import hu.martin.ems.model.Supplier;
import hu.martin.ems.service.CustomerService;
import hu.martin.ems.service.OrderElementService;
import hu.martin.ems.service.ProductService;
import hu.martin.ems.service.SupplierService;
import hu.martin.ems.vaadin.MainView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "product/list", layout = MainView.class)
public class ProductList extends VerticalLayout {

    private final ProductService productService;
    private final CustomerService customerService;
    private final OrderElementService orderElementService;
    private final SupplierService supplierService;
    private boolean showDeleted = false;
    private Grid<ProductVO> grid;

    @Autowired
    public ProductList(ProductService productService,
                       CustomerService customerService,
                       OrderElementService orderElementService,
                       SupplierService supplierService) {
        this.productService = productService;
        this.customerService = customerService;
        this.orderElementService = orderElementService;
        this.supplierService = supplierService;

        this.grid = new Grid<>(ProductVO.class);
        List<Product> products = productService.findAll(false);
        List<ProductVO> data = products.stream().map(ProductVO::new).toList();
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");
        this.grid.removeColumnByKey("id");
        this.grid.removeColumnByKey("deleted");

        //region Options column
        this.grid.addComponentColumn(product -> {
            //region sellDialog
            Dialog sellDialog = new Dialog();
            Product p = product.original;
            Button sellToCustomerButton = new Button("Sell to customer");
            ComboBox<Customer> customers = new ComboBox<>("Customer");
            NumberField unitField = new NumberField("Quantity");
            ComboBox.ItemFilter<Customer> filterCustomer = (customer, filterString) ->
                    customer.getName().toLowerCase().contains(filterString.toLowerCase());
            customers.setItems(filterCustomer, customerService.findAll(false));
            customers.setItemLabelGenerator(Customer::getName);
            sellDialog.add(customers, unitField, sellToCustomerButton);

            sellToCustomerButton.addClickListener(event -> {
                OrderElement orderElement = new OrderElement();
                orderElement.setCustomer(customers.getValue());
                orderElement.setUnit(unitField.getValue().intValue());
                orderElement.setName(p.getName());
                orderElement.setProduct(p);
                orderElement.setDeleted(0L);
                orderElement.setTaxKey(p.getTaxKey());
                orderElement.setUnitNetPrice(p.getSellingPriceNet().intValue());
                orderElement.setNetPrice(p.getSellingPriceNet().intValue() * unitField.getValue().intValue());
                orderElement.setTaxPrice((orderElement.getNetPrice() / 100.0) * Integer.parseInt(p.getTaxKey().getName()));
                orderElement.setGrossPrice(orderElement.getNetPrice() + orderElement.getTaxPrice().intValue());
                String s = orderElementService.saveOrUpdate(orderElement) != null ? "Rendelési tétel létrehozva a vásárlóhoz!" : "Érvénytelen rendelés!";
                Notification.show(s);
                sellDialog.close();
            });
            //endregion
            //region orderDialog
            Dialog orderDialog = new Dialog();
            Button buyFromSupplierButton = new Button("Order from Supllier");
            ComboBox<Supplier> suppliers = new ComboBox<>("Supplier");
            NumberField buyingUnitField = new NumberField("Quantity");
            ComboBox.ItemFilter<Supplier> filterSupplier = (supplier, filterString) ->
                    supplier.getName().toLowerCase().contains(filterString.toLowerCase());
            suppliers.setItems(filterSupplier, supplierService.findAll(false));
            suppliers.setItemLabelGenerator(Supplier::getName);
            orderDialog.add(suppliers, buyingUnitField, buyFromSupplierButton);

            buyFromSupplierButton.addClickListener(event -> {
                OrderElement orderElement = new OrderElement();
                orderElement.setSupplier(suppliers.getValue());
                orderElement.setUnit(buyingUnitField.getValue().intValue());
                orderElement.setName(p.getName());
                orderElement.setProduct(p);
                orderElement.setDeleted(0L);
                orderElement.setTaxKey(p.getTaxKey());
                orderElement.setUnitNetPrice(p.getSellingPriceNet().intValue());
                orderElement.setNetPrice(p.getSellingPriceNet().intValue() * buyingUnitField.getValue().intValue());
                orderElement.setTaxPrice((orderElement.getNetPrice() / 100.0) * Integer.parseInt(p.getTaxKey().getName()));
                orderElement.setGrossPrice(orderElement.getNetPrice() + orderElement.getTaxPrice().intValue());
                String s = orderElementService.saveOrUpdate(orderElement) != null ? "Rendelési tétel előkészítve a szállítónak!" : "Érvénytelen rendelés!";
                Notification.show(s);
                orderDialog.close();
            });
            //endregion

            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            Button orderButton = new Button("Order");
            Button sellButton = new Button("Sell");
            Button restoreButton = new Button("Restore");
            Button permanentDeleteButton = new Button("Permanently Delete");

            orderButton.addClickListener(event -> { orderDialog.open(); });
            sellButton.addClickListener(event -> { sellDialog.open(); });
            editButton.addClickListener(event -> {
                ProductCreate.p = product.getOriginal();
                UI.getCurrent().navigate("product/create");
            });

            restoreButton.addClickListener(event -> {
                this.productService.restore(product.getOriginal());
                Notification.show("Product restored: " + product.getOriginal().getName());
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.productService.delete(product.getOriginal());
                Notification.show("Product deleted: " + product.getOriginal().getName());
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.productService.permanentlyDelete(product.getOriginal());
                Notification.show("Product permanently deleted: " + product.getOriginal().getName());
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if(product.getOriginal().getDeleted() == 0){ actions.add(editButton, deleteButton, sellButton, orderButton); }
            else if(product.getOriginal().getDeleted() == 1){ actions.add(permanentDeleteButton, restoreButton); }
            return actions;
        }).setHeader("Options");

        //endregion

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            updateGridItems();
        });

        add(showDeletedCheckbox, grid);
    }

    private void updateGridItems() {
        List<Product> products = this.productService.findAll(showDeleted);
        this.grid.setItems(products.stream().map(ProductVO::new).toList());
    }

    @Getter
    public class ProductVO {
        private Product original;
        private Long deleted;
        private Long id;
        private String buyingPriceCurrency;
        private String sellingPriceCurrency;
        private String amountUnit;
        private String name;
        private Double buyingPriceNet;
        private Double sellingPriceNet;
        private Double amount;

        public ProductVO(Product product) {
            this.original = product;
            this.id = product.getId();
            this.deleted = product.getDeleted();
            this.name = original.getName();
            this.buyingPriceNet = original.getBuyingPriceNet();
            this.buyingPriceCurrency = original.getBuyingPriceCurrency().getName();
            this.sellingPriceNet = original.getSellingPriceNet();
            this.sellingPriceCurrency = original.getSellingPriceCurrency().getName();
            this.amountUnit = original.getAmountUnit().getName();
            this.amount = original.getAmount();
        }
    }
}
