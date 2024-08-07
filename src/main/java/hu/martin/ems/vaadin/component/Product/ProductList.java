package hu.martin.ems.vaadin.component.Product;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.*;
import hu.martin.ems.service.*;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.component.Creatable;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@Route(value = "product/list", layout = MainView.class)
@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
public class ProductList extends VerticalLayout implements Creatable<Product> {

    private final ProductService productService;
    private final CustomerService customerService;
    private final OrderElementService orderElementService;
    private final SupplierService supplierService;
    private final CodeStoreService codeStoreService;
    private boolean showDeleted = false;
    private PaginatedGrid<ProductVO, String> grid;
    private final PaginationSetting paginationSetting;

    @Autowired
    public ProductList(ProductService productService,
                       CustomerService customerService,
                       OrderElementService orderElementService,
                       SupplierService supplierService,
                       CodeStoreService codeStoreService,
                       PaginationSetting paginationSetting) {
        this.productService = productService;
        this.customerService = customerService;
        this.orderElementService = orderElementService;
        this.codeStoreService = codeStoreService;
        this.supplierService = supplierService;
        this.paginationSetting = paginationSetting;

        this.grid = new PaginatedGrid<>(ProductVO.class);
        List<Product> products = productService.findAll(false);
        List<ProductVO> data = products.stream().map(ProductVO::new).collect(Collectors.toList());
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");
        this.grid.removeColumnByKey("id");
        this.grid.removeColumnByKey("deleted");
        grid.addClassName("styling");
        grid.setPartNameGenerator(productVO -> productVO.getDeleted() != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

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
                OrderElement oe = orderElementService.saveOrUpdate(orderElement);
                Notification.show(oe != null ? "Order element successfully paired to customer!" : "Invalid ordering!")
                        .addThemeVariants(oe != null ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR);
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
                OrderElement oe = orderElementService.saveOrUpdate(orderElement);
                Notification.show(oe != null ? "Order element successfully paired to supplier!" : "Invalid ordering!")
                        .addThemeVariants(oe != null ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR);
                orderDialog.close();
            });
            //endregion

            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button orderButton = new Button("Order");
            Button sellButton = new Button("Sell");
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            orderButton.addClickListener(event -> {
                orderDialog.open();
            });
            sellButton.addClickListener(event -> {
                sellDialog.open();
            });
            editButton.addClickListener(event -> {
                Dialog dialog = getSaveOrUpdateDialog(product.getOriginal());
                dialog.open();
            });

            restoreButton.addClickListener(event -> {
                this.productService.restore(product.getOriginal());
                Notification.show("Product restored: " + product.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.productService.delete(product.getOriginal());
                Notification.show("Product deleted: " + product.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.productService.permanentlyDelete(product.getOriginal());
                Notification.show("Product permanently deleted: " + product.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (product.getOriginal().getDeleted() == 0) {
                actions.add(editButton, deleteButton, sellButton, orderButton);
            } else if (product.getOriginal().getDeleted() == 1) {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        }).setHeader("Options");

        //endregion

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, grid);
    }

    private void updateGridItems() {
        List<Product> products = this.productService.findAll(showDeleted);
        this.grid.setItems(products.stream().map(ProductVO::new).collect(Collectors.toList()));
    }

    @Override
    public Dialog getSaveOrUpdateDialog(Product entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " product");
        FormLayout formLayout = new FormLayout();

        TextField nameField = new TextField("Name");

        NumberField buyingPriceNetField = new NumberField("Buying price net");

        ComboBox<CodeStore> buyingPriceCurrencys = new ComboBox<>("Buying price currency");
        ComboBox.ItemFilter<CodeStore> buyingPriceCurrencyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        buyingPriceCurrencys.setItems(buyingPriceCurrencyFilter, codeStoreService.getChildren(StaticDatas.CURRENCIES_CODESTORE_ID));
        buyingPriceCurrencys.setItemLabelGenerator(CodeStore::getName);

        NumberField sellingPriceNetField = new NumberField("Selling price net");

        ComboBox<CodeStore> sellingPriceCurrencys = new ComboBox<>("Selling price currency");
        ComboBox.ItemFilter<CodeStore> sellingPriceCurrencyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        sellingPriceCurrencys.setItems(sellingPriceCurrencyFilter, codeStoreService.getChildren(StaticDatas.CURRENCIES_CODESTORE_ID));
        sellingPriceCurrencys.setItemLabelGenerator(CodeStore::getName);

        ComboBox<CodeStore> taxKeys = new ComboBox<>("Tax key");
        ComboBox.ItemFilter<CodeStore> taxKeyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        sellingPriceCurrencys.setItems(taxKeyFilter, codeStoreService.getChildren(StaticDatas.TAXKEYS_CODESTORE_ID));
        sellingPriceCurrencys.setItemLabelGenerator(CodeStore::getName);

        ComboBox<CodeStore> amountUnits = new ComboBox<>("Amount unit");
        ComboBox.ItemFilter<CodeStore> amountUnitFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        amountUnits.setItems(amountUnitFilter, codeStoreService.getChildren(StaticDatas.AMOUNTUNITS_CODESTORE_ID));
        amountUnits.setItemLabelGenerator(CodeStore::getName);

        NumberField amountField = new NumberField("Amount");

        Button saveButton = new Button("Save");

        if (entity != null) {
            nameField.setValue(entity.getName());
            buyingPriceNetField.setValue(entity.getBuyingPriceNet());
            buyingPriceCurrencys.setValue(entity.getBuyingPriceCurrency());
            sellingPriceNetField.setValue(entity.getSellingPriceNet());
            sellingPriceCurrencys.setValue(entity.getSellingPriceCurrency());
            amountUnits.setValue(entity.getAmountUnit());
            amountField.setValue(entity.getAmount());
            taxKeys.setValue(entity.getTaxKey());
        }

        saveButton.addClickListener(event -> {
            Product product = Objects.requireNonNullElseGet(entity, Product::new);
            product.setName(nameField.getValue());
            product.setBuyingPriceNet(buyingPriceNetField.getValue());
            product.setBuyingPriceCurrency(buyingPriceCurrencys.getValue());
            product.setSellingPriceNet(sellingPriceNetField.getValue());
            product.setSellingPriceCurrency(sellingPriceCurrencys.getValue());
            product.setAmountUnit(amountUnits.getValue());
            product.setAmount(amountField.getValue());
            product.setDeleted(0L);
            product.setTaxKey(taxKeys.getValue());
            this.productService.saveOrUpdate(product);

            Notification.show("Product saved: " + product)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            nameField.clear();
            buyingPriceNetField.clear();
            buyingPriceCurrencys.clear();
            sellingPriceNetField.clear();
            sellingPriceCurrencys.clear();
            amountUnits.clear();
            amountField.clear();
            taxKeys.clear();
            createDialog.close();
        });

        formLayout.add(nameField, buyingPriceNetField, buyingPriceCurrencys, sellingPriceNetField, sellingPriceCurrencys, amountUnits, amountField, taxKeys, saveButton);
        createDialog.add(formLayout);
        return createDialog;
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
