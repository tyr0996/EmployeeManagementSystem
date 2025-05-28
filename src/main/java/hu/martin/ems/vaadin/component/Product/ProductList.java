package hu.martin.ems.vaadin.component.Product;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.CodeStoreIds;
import hu.martin.ems.core.config.IconProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.*;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.*;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Route(value = "product/list", layout = MainView.class)
@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@RolesAllowed("ROLE_ProductMenuOpenPermission")
@NeedCleanCoding
public class ProductList extends VerticalLayout implements Creatable<Product> {

    //TODO a Unit és a nem tudom mi helyett inkább lehetne packaging (kiszerelés)
    private final ProductApiClient productApi = BeanProvider.getBean(ProductApiClient.class);
    private final CustomerApiClient customerApi = BeanProvider.getBean(CustomerApiClient.class);
    private final OrderElementApiClient orderElementApi = BeanProvider.getBean(OrderElementApiClient.class);
    private final SupplierApiClient supplierApi = BeanProvider.getBean(SupplierApiClient.class);
    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private boolean showDeleted = false;
    private PaginatedGrid<ProductVO, String> grid;

    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Grid.Column<ProductVO> extraData;

    private List<ProductVO> productVOS;

    private static String amountFilterText = "";
    private static String amountUnitFilterText = "";
    private static String buyingPriceCurrencyFilterText = "";
    private static String buyingPriceNetFilterText = "";
    private static String nameFilterText = "";
    private static String sellingPriceCurrencyFilterText = "";
    private static String sellingPriceNetFilterText = "";


    private Grid.Column<ProductVO> amountColumn;
    private Grid.Column<ProductVO> amountUnitColumn;
    private Grid.Column<ProductVO> buyingPriceCurrencyColumn;
    private Grid.Column<ProductVO> buyingPriceNetColumn;
    private Grid.Column<ProductVO> nameColumn;
    private Grid.Column<ProductVO> sellingPriceCurrencyColumn;
    private Grid.Column<ProductVO> sellingPriceNetColumn;
    
    
    
    private final PaginationSetting paginationSetting;

    private Logger logger = LoggerFactory.getLogger(Product.class);

    List<Product> productList;
    List<Customer> customerList;
    List<CodeStore> amountUnitList;
    List<CodeStore> currencyList;
    List<CodeStore> taxKeyList;
    List<Supplier> supplierList;

    private final Gson gson = BeanProvider.getBean(Gson.class);
    private MainView mainView;

    @Autowired
    public ProductList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;
        this.mainView = mainView;

        ProductVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(ProductVO.class);
        setupProducts();
        productVOS = productList.stream().map(ProductVO::new).collect(Collectors.toList());
        this.grid.setItems(productVOS);

       amountColumn = grid.addColumn(v -> v.amount);
       amountUnitColumn = grid.addColumn(v -> v.amountUnit);
       buyingPriceCurrencyColumn = grid.addColumn(v -> v.buyingPriceCurrency);
       buyingPriceNetColumn = grid.addColumn(v -> v.buyingPriceNet);
       nameColumn = grid.addColumn(v -> v.name);
       sellingPriceCurrencyColumn = grid.addColumn(v -> v.sellingPriceCurrency);
       sellingPriceNetColumn = grid.addColumn(v -> v.sellingPriceNet);
        
        grid.addClassName("styling");
        grid.setPartNameGenerator(productVO -> productVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());
        


        //region Options column
        extraData = this.grid.addComponentColumn(productVo -> {
            Button editButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).EDIT_ICON));
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button orderButton = new Button("Order");
            Button sellButton = new Button("Sell");
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).PERMANENTLY_DELETE_ICON));
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            orderButton.addClickListener(event -> {
                getOrderFromSupplierDialog(productVo).open();
            });
            sellButton.addClickListener(event -> {
                getSellToCustomerDialog(productVo).open();
            });
            editButton.addClickListener(event -> {
                Dialog dialog = getSaveOrUpdateDialog(productVo.original);
                dialog.open();
            });

            restoreButton.addClickListener(event -> {
                this.productApi.restore(productVo.original);
                Notification.show("Product restored: " + productVo.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                EmsResponse resp = this.productApi.delete(productVo.original);
                switch (resp.getCode()){
                    case 200: {
                        Notification.show("Product deleted: " + productVo.original.getName())
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        updateGridItems();
                        break;
                    }
                    default: {
                        Notification.show(resp.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
                setupProducts();
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.productApi.permanentlyDelete(productVo.original.getId());
                Notification.show("Product permanently deleted: " + productVo.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (productVo.original.getDeleted() == 0) {
                actions.add(editButton, deleteButton, sellButton, orderButton);
            } else {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        }).setAutoWidth(true).setFlexGrow(0);

        setFilteringHeaderRow();

        //endregion

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            ProductVO.showDeletedCheckboxFilter.replace("deleted", newValue);

            updateGridItems();;
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, grid);
    }

    private void setupProducts() {
        EmsResponse response = productApi.findAll();
        switch (response.getCode()){
            case 200:
                productList = (List<Product>) response.getResponseData();
                break;
            default:
                productList = new ArrayList<>();
                logger.error("Product findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                Notification.show("EmsError happened while getting products: " + response.getDescription())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
        }
    }

    public Dialog getSellToCustomerDialog(ProductVO productVO){
        Dialog sellDialog = new Dialog("Sell to customer");
        appendCloseButton(sellDialog);
        FormLayout formLayout = new FormLayout();
        Product p = productVO.original;
        Button sellToCustomerButton = new Button("Sell to customer");
        ComboBox<Customer> customers = new ComboBox<>("Customer");
        NumberField unitField = new NumberField("Quantity");
        ComboBox.ItemFilter<Customer> filterCustomer = (customer, filterString) ->
                customer.getName().toLowerCase().contains(filterString.toLowerCase());
        setupCustomers();
        if(customerList == null){
            customers.setInvalid(true);
            customers.setErrorMessage("EmsError happened while getting customers");
            customers.setEnabled(false);
            sellToCustomerButton.setEnabled(false);
        }
        else{
            sellToCustomerButton.setEnabled(true);
            customers.setInvalid(false);
            customers.setEnabled(true);
            customers.setItems(filterCustomer, customerList);
            customers.setItemLabelGenerator(Customer::getName);
        }

        formLayout.add(customers, unitField, sellToCustomerButton);
        sellDialog.add(formLayout);

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
            orderElementApi.save(orderElement);
            Notification.show("Order element successfully paired to customer!")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            sellDialog.close();
        });
        return sellDialog;
    }

    private void setupCustomers() {
        EmsResponse response = customerApi.findAll();
        switch (response.getCode()){
            case 200:
                customerList = (List<Customer>) response.getResponseData();
                break;
            default:
                customerList = null;
                logger.error("Customer findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
//                Notification.show("")
//                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
        }
    }

    private void appendCloseButton(Dialog d){
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> d.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        d.getHeader().add(closeButton);
    }

    public Dialog getOrderFromSupplierDialog(ProductVO productVO){
        Dialog orderDialog = new Dialog("Order from Supplier");
        appendCloseButton(orderDialog);
        FormLayout formLayout = new FormLayout();
        Button buyFromSupplierButton = new Button("Order from Supplier");
        ComboBox<Supplier> suppliers = new ComboBox<>("Supplier");
        NumberField buyingUnitField = new NumberField("Quantity");
        ComboBox.ItemFilter<Supplier> filterSupplier = (supplier, filterString) ->
                supplier.getName().toLowerCase().contains(filterString.toLowerCase());
        setupSuppliers();
        if(supplierList == null){
            suppliers.setInvalid(true);
            suppliers.setErrorMessage("EmsError happened while getting suppliers");
            suppliers.setEnabled(false);
            buyFromSupplierButton.setEnabled(false);
        }
        else{
            buyFromSupplierButton.setEnabled(true);
            suppliers.setInvalid(false);
            suppliers.setEnabled(true);
            suppliers.setItems(filterSupplier, supplierList);
            suppliers.setItemLabelGenerator(Supplier::getName);
        }

        formLayout.add(suppliers, buyingUnitField, buyFromSupplierButton);
        orderDialog.add(formLayout);

        buyFromSupplierButton.addClickListener(event -> {
            OrderElement orderElement = new OrderElement();
            orderElement.setSupplier(suppliers.getValue());
            orderElement.setUnit(buyingUnitField.getValue().intValue());
            orderElement.setName(productVO.original.getName());
            orderElement.setProduct(productVO.original);
            orderElement.setDeleted(0L);
            orderElement.setTaxKey(productVO.original.getTaxKey());
            orderElement.setUnitNetPrice(productVO.original.getSellingPriceNet().intValue());
            orderElement.setNetPrice(productVO.original.getSellingPriceNet().intValue() * buyingUnitField.getValue().intValue());
            orderElement.setTaxPrice((orderElement.getNetPrice() / 100.0) * Integer.parseInt(productVO.original.getTaxKey().getName()));
            orderElement.setGrossPrice(orderElement.getNetPrice() + orderElement.getTaxPrice().intValue());
            orderElementApi.save(orderElement);
            Notification.show("Order element successfully paired to supplier!")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            orderDialog.close();
        });
        return orderDialog;
    }

    private void setupSuppliers() {
        EmsResponse response = supplierApi.findAll();
        switch (response.getCode()){
            case 200:
                supplierList = (List<Supplier>) response.getResponseData();
                break;
            default:
                supplierList = null;
                logger.error("Supplier findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
//                Notification.show("EmsError happened while getting suppliers")
//                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
        }
    }

    private Stream<ProductVO> getFilteredStream() {
        return productVOS.stream().filter(productVO ->
                (amountFilterText.isEmpty() || productVO.amount.toString().toLowerCase().contains(amountFilterText.toLowerCase())) &&
                (amountUnitFilterText.isEmpty() || productVO.amountUnit.toLowerCase().contains(amountUnitFilterText.toLowerCase())) &&
                (buyingPriceCurrencyFilterText.isEmpty() || productVO.buyingPriceCurrency.toLowerCase().contains(buyingPriceCurrencyFilterText.toLowerCase())) &&
                (buyingPriceNetFilterText.isEmpty() || productVO.buyingPriceNet.toString().toLowerCase().contains(buyingPriceNetFilterText.toLowerCase())) &&
                (nameFilterText.isEmpty() || productVO.name.toLowerCase().contains(nameFilterText.toLowerCase())) &&
                (sellingPriceCurrencyFilterText.isEmpty() || productVO.sellingPriceCurrency.toLowerCase().contains(sellingPriceCurrencyFilterText.toLowerCase())) &&
                (sellingPriceNetFilterText.isEmpty() || productVO.sellingPriceNet.toString().toLowerCase().contains(sellingPriceNetFilterText.toLowerCase())) &&
                productVO.filterExtraData()
        );
    }

    private Component filterField(TextField filterField, String title){
        VerticalLayout res = new VerticalLayout();
        res.getStyle().set("padding", "0px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");
        filterField.getStyle().set("display", "flex").set("width", "100%");
        NativeLabel titleLabel = new NativeLabel(title);
        res.add(titleLabel, filterField);
        res.setClassName("vaadin-header-cell-content");
        return res;
    }

    private void setFilteringHeaderRow(){
        TextField amountFilter = new TextField();
        amountFilter.setPlaceholder("Search amount...");
        amountFilter.setClearButtonVisible(true);
        amountFilter.addValueChangeListener(event -> {
            amountFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField amountUnitFilter = new TextField();
        amountUnitFilter.setPlaceholder("Search amount unit...");
        amountUnitFilter.setClearButtonVisible(true);
        amountUnitFilter.addValueChangeListener(event -> {
            amountUnitFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField buyingPriceCurrencyFilter = new TextField();
        buyingPriceCurrencyFilter.setPlaceholder("Search buying price currency...");
        buyingPriceCurrencyFilter.setClearButtonVisible(true);
        buyingPriceCurrencyFilter.addValueChangeListener(event -> {
            buyingPriceCurrencyFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField buyingPriceNetFilter = new TextField();
        buyingPriceNetFilter.setPlaceholder("Search buying price net...");
        buyingPriceNetFilter.setClearButtonVisible(true);
        buyingPriceNetFilter.addValueChangeListener(event -> {
            buyingPriceNetFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField nameFilter = new TextField();
        nameFilter.setPlaceholder("Search name...");
        nameFilter.setClearButtonVisible(true);
        nameFilter.addValueChangeListener(event -> {
            nameFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField sellingPriceCurrencyFilter = new TextField();
        sellingPriceCurrencyFilter.setPlaceholder("Search selling price currency...");
        sellingPriceCurrencyFilter.setClearButtonVisible(true);
        sellingPriceCurrencyFilter.addValueChangeListener(event -> {
            sellingPriceCurrencyFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField sellingPriceNetFilter = new TextField();
        sellingPriceNetFilter.setPlaceholder("Search selling price net...");
        sellingPriceNetFilter.setClearButtonVisible(true);
        sellingPriceNetFilter.addValueChangeListener(event -> {
            sellingPriceNetFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                ProductVO.extraDataFilterMap.clear();
            }
            else{
                ProductVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });


        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();;
        filterRow.getCell(amountColumn).setComponent(filterField(amountFilter, "Amount"));
        filterRow.getCell(amountUnitColumn).setComponent(filterField(amountUnitFilter, "Amount unit"));
        filterRow.getCell(buyingPriceCurrencyColumn).setComponent(filterField(buyingPriceCurrencyFilter, "Buying price currency"));
        filterRow.getCell(buyingPriceNetColumn).setComponent(filterField(buyingPriceNetFilter, "Buying price net"));
        filterRow.getCell(nameColumn).setComponent(filterField(nameFilter, "Name"));
        filterRow.getCell(sellingPriceCurrencyColumn).setComponent(filterField(sellingPriceCurrencyFilter, "Selling price currency"));
        filterRow.getCell(sellingPriceNetColumn).setComponent(filterField(sellingPriceNetFilter, "Selling price net"));
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
    }

    private void updateGridItems() {
        EmsResponse response = productApi.findAllWithDeleted();
        List<Product> products;
        switch (response.getCode()){
            case 200:
                products = (List<Product>) response.getResponseData();
                break;
            default:
                Notification.show("EmsError happened while getting products: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                products = new ArrayList<>();
                break;
        }
        productVOS = products.stream().map(ProductVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
    }

    public Dialog getSaveOrUpdateDialog(Product entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " product");
        appendCloseButton(createDialog);
        FormLayout formLayout = new FormLayout();

        Button saveButton = new Button("Save");

        TextField nameField = new TextField("Name");

        NumberField buyingPriceNetField = new NumberField("Buying price net");


        ComboBox<CodeStore> buyingPriceCurrencys = new ComboBox<>("Buying price currency");
        ComboBox.ItemFilter<CodeStore> buyingPriceCurrencyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        setupCurrencies();
        if(currencyList == null){
            buyingPriceCurrencys.setInvalid(true);
            buyingPriceCurrencys.setErrorMessage("EmsError happened while getting currencies");
            buyingPriceCurrencys.setEnabled(false);
            saveButton.setEnabled(false);
        }
        else {
            buyingPriceCurrencys.setInvalid(false);
            buyingPriceCurrencys.setEnabled(true);
            buyingPriceCurrencys.setItems(buyingPriceCurrencyFilter, currencyList);
            buyingPriceCurrencys.setItemLabelGenerator(CodeStore::getName);
        }

        NumberField sellingPriceNetField = new NumberField("Selling price net");

        ComboBox<CodeStore> sellingPriceCurrencies = new ComboBox<>("Selling price currency");
        ComboBox.ItemFilter<CodeStore> sellingPriceCurrencyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if(currencyList == null){
            sellingPriceCurrencies.setInvalid(true);
            sellingPriceCurrencies.setErrorMessage("EmsError happened while getting currencies");
            sellingPriceCurrencies.setEnabled(false);
            saveButton.setEnabled(false);
        }
        else {
            sellingPriceCurrencies.setInvalid(false);
            sellingPriceCurrencies.setEnabled(true);
            sellingPriceCurrencies.setItems(sellingPriceCurrencyFilter, currencyList);
            sellingPriceCurrencies.setItemLabelGenerator(CodeStore::getName);
        }

        ComboBox<CodeStore> taxKeys = new ComboBox<>("Tax key");
        ComboBox.ItemFilter<CodeStore> taxKeyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        setupTaxKeys();
        if(taxKeyList == null){
            taxKeys.setInvalid(true);
            taxKeys.setErrorMessage("EmsError happened while getting tax keys");
            taxKeys.setEnabled(false);
            saveButton.setEnabled(false);
        }
        else {
            taxKeys.setInvalid(false);
            taxKeys.setEnabled(true);
            taxKeys.setItems(taxKeyFilter, taxKeyList);
            taxKeys.setItemLabelGenerator(CodeStore::getName);
        }

        ComboBox<CodeStore> amountUnits = new ComboBox<>("Amount unit");
        ComboBox.ItemFilter<CodeStore> amountUnitFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        setupAmountUnits();
        if(amountUnitList == null){
            amountUnits.setInvalid(true);
            amountUnits.setErrorMessage("EmsError happened while getting amount units");
            amountUnits.setEnabled(false);
            saveButton.setEnabled(false);
        }
        else {
            amountUnits.setInvalid(false);
            amountUnits.setEnabled(true);
            amountUnits.setItems(amountUnitFilter, amountUnitList);
            amountUnits.setItemLabelGenerator(CodeStore::getName);
        }

        NumberField amountField = new NumberField("Amount");

        if (entity != null) {
            nameField.setValue(entity.getName());
            buyingPriceNetField.setValue(entity.getBuyingPriceNet());
            buyingPriceCurrencys.setValue(entity.getBuyingPriceCurrency());
            sellingPriceNetField.setValue(entity.getSellingPriceNet());
            sellingPriceCurrencies.setValue(entity.getSellingPriceCurrency());
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
            product.setSellingPriceCurrency(sellingPriceCurrencies.getValue());
            product.setAmountUnit(amountUnits.getValue());
            product.setAmount(amountField.getValue());
            product.setDeleted(0L);
            product.setTaxKey(taxKeys.getValue());
            EmsResponse response = null;
            if(entity != null){
                response = productApi.update(product);
            }
            else{
                response = productApi.save(product);
            }
            switch (response.getCode()){
                case 200: {
                    Notification.show("Product  " + (entity == null ? "saved: " : "updated: ") + ((Product) response.getResponseData()).getName())
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    break;
                }
                default: {
                    Notification.show("Product " + (entity == null ? "saving " : "modifying " ) + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    createDialog.close();
                    updateGridItems();
                    return;
                }
            }

            updateGridItems();

            nameField.clear();
            buyingPriceNetField.clear();
            buyingPriceCurrencys.clear();
            sellingPriceNetField.clear();
            sellingPriceCurrencies.clear();
            amountUnits.clear();
            amountField.clear();
            taxKeys.clear();
            createDialog.close();
        });

        formLayout.add(nameField, buyingPriceNetField, buyingPriceCurrencys, sellingPriceNetField, sellingPriceCurrencies, amountUnits, amountField, taxKeys, saveButton);
        createDialog.add(formLayout);
        updateGridItems();
        return createDialog;
    }

    private void setupAmountUnits() {
        EmsResponse response = codeStoreApi.getChildren(CodeStoreIds.AMOUNTUNITS_CODESTORE_ID);
        switch (response.getCode()){
            case 200:
                amountUnitList = (List<CodeStore>) response.getResponseData();
                break;
            default:
                amountUnitList = null;
                logger.error("CodeStore getChildrenError [amount unit]. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private void setupTaxKeys() {
        EmsResponse response = codeStoreApi.getChildren(CodeStoreIds.TAXKEYS_CODESTORE_ID);
        switch (response.getCode()){
            case 200:
                taxKeyList = (List<CodeStore>) response.getResponseData();
                break;
            default:
                taxKeyList = null;
                logger.error("CodeStore getChildrenError [tax key]. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private void setupCurrencies() {
        EmsResponse response = codeStoreApi.getChildren(CodeStoreIds.CURRENCIES_CODESTORE_ID);
        switch (response.getCode()){
            case 200:
                currencyList = (List<CodeStore>) response.getResponseData();
                break;
            default:
                currencyList = null;
                logger.error("CodeStore getChildrenError [currency]. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    @NeedCleanCoding
public class ProductVO extends BaseVO {
        private Product original;
        private String buyingPriceCurrency;
        private String sellingPriceCurrency;
        private String amountUnit;
        private String name;
        private Double buyingPriceNet;
        private Double sellingPriceNet;
        private Double amount;

        public ProductVO(Product product) {
            super(product.id, product.getDeleted());
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
