package hu.martin.ems.vaadin.component.Product;

import com.google.gson.Gson;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.CodeStoreIds;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.EmsFilterableGridComponent;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.*;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.*;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import hu.martin.ems.vaadin.core.EmsDialog;
import hu.martin.ems.vaadin.core.IEmsOptionColumnBaseDialogCreationForm;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
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
public class ProductList extends EmsFilterableGridComponent implements Creatable<Product>, IEmsOptionColumnBaseDialogCreationForm<Product, ProductList.ProductVO> {

    //TODO a Unit és a nem tudom mi helyett inkább lehetne packaging (kiszerelés)
    @Getter
    private final ProductApiClient apiClient = BeanProvider.getBean(ProductApiClient.class);
    private final CustomerApiClient customerApi = BeanProvider.getBean(CustomerApiClient.class);
    private final OrderElementApiClient orderElementApi = BeanProvider.getBean(OrderElementApiClient.class);
    private final SupplierApiClient supplierApi = BeanProvider.getBean(SupplierApiClient.class);
    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private boolean showDeleted = false;
    @Getter
    private PaginatedGrid<ProductVO, String> grid;

    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Grid.Column<ProductVO> extraData;

    private List<ProductVO> productVOS;

    private TextFilteringHeaderCell amountFilter;
    private TextFilteringHeaderCell amountUnitFilter;
    private TextFilteringHeaderCell buyingPriceCurrencyFilter;
    private TextFilteringHeaderCell buyingPriceNetFilter;
    private TextFilteringHeaderCell nameFilter;
    private TextFilteringHeaderCell sellingPriceCurrencyFilter;
    private TextFilteringHeaderCell sellingPriceNetFilter;


    private Grid.Column<ProductVO> amountColumn;
    private Grid.Column<ProductVO> amountUnitColumn;
    private Grid.Column<ProductVO> buyingPriceCurrencyColumn;
    private Grid.Column<ProductVO> buyingPriceNetColumn;
    private Grid.Column<ProductVO> nameColumn;
    private Grid.Column<ProductVO> sellingPriceCurrencyColumn;
    private Grid.Column<ProductVO> sellingPriceNetColumn;

    private Logger logger = LoggerFactory.getLogger(Product.class);

    List<Product> productList;
    List<Customer> customerList;
    List<CodeStore> amountUnitList;
    List<CodeStore> currencyList;
    List<CodeStore> taxKeyList;
    List<Supplier> supplierList;

    private final Gson gson = BeanProvider.getBean(Gson.class);

    @Autowired
    public ProductList(PaginationSetting paginationSetting) {
        ProductVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(ProductVO.class);
        setEntities();


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

        extraData = this.grid.addComponentColumn(productVo -> {
            
            Button orderButton = new Button("Order");
            Button sellButton = new Button("Sell");
            orderButton.addClickListener(event -> {
                getOrderFromSupplierDialog(productVo).open();
            });
            sellButton.addClickListener(event -> {
                getSellToCustomerDialog(productVo).open();
            });
            return createOptionColumn("Product", productVo, new Button[]{sellButton, orderButton});
        }).setAutoWidth(true).setFlexGrow(0);

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

            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        setFilteringHeaderRow();
        productVOS = productList.stream().map(ProductVO::new).collect(Collectors.toList());
        this.grid.setItems(productVOS);

        add(hl, grid);
    }

    public void setEntities() {
        EmsResponse response = apiClient.findAll();
        switch (response.getCode()) {
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

    public EmsDialog getSellToCustomerDialog(ProductVO productVO) {
        EmsDialog sellDialog = new EmsDialog("Sell to customer");

        FormLayout formLayout = new FormLayout();
        Product p = productVO.original;
        Button sellToCustomerButton = new Button("Sell to customer");
        ComboBox<Customer> customers = new ComboBox<>("Customer");
        NumberField unitField = new NumberField("Quantity");
        ComboBox.ItemFilter<Customer> filterCustomer = (customer, filterString) ->
                customer.getName().toLowerCase().contains(filterString.toLowerCase());
        setupCustomers();
        if (customerList == null) {
            customers.setInvalid(true);
            customers.setErrorMessage("EmsError happened while getting customers");
            customers.setEnabled(false);
            sellToCustomerButton.setEnabled(false);
        } else {
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
        switch (response.getCode()) {
            case 200:
                customerList = (List<Customer>) response.getResponseData();
                break;
            default:
                customerList = null;
                logger.error("Customer findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }


    public EmsDialog getOrderFromSupplierDialog(ProductVO productVO) {
        EmsDialog orderDialog = new EmsDialog("Order from Supplier");

        FormLayout formLayout = new FormLayout();
        Button buyFromSupplierButton = new Button("Order from Supplier");
        ComboBox<Supplier> suppliers = new ComboBox<>("Supplier");
        NumberField buyingUnitField = new NumberField("Quantity");
        ComboBox.ItemFilter<Supplier> filterSupplier = (supplier, filterString) ->
                supplier.getName().toLowerCase().contains(filterString.toLowerCase());
        setupSuppliers();
        if (supplierList == null) {
            suppliers.setInvalid(true);
            suppliers.setErrorMessage("EmsError happened while getting suppliers");
            suppliers.setEnabled(false);
            buyFromSupplierButton.setEnabled(false);
        } else {
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
        switch (response.getCode()) {
            case 200:
                supplierList = (List<Supplier>) response.getResponseData();
                break;
            default:
                supplierList = null;
                logger.error("Supplier findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private Stream<ProductVO> getFilteredStream() {
        return productVOS.stream().filter(productVO ->
                filterField(amountFilter, productVO.amount.toString()) &&
                filterField(amountUnitFilter, productVO.amountUnit.toString()) &&
                filterField(buyingPriceCurrencyFilter, productVO.buyingPriceCurrency.toString()) &&
                filterField(buyingPriceNetFilter, productVO.buyingPriceNet.toString()) &&
                filterField(nameFilter, productVO.name.toString()) &&
                filterField(sellingPriceCurrencyFilter, productVO.sellingPriceCurrency.toString()) &&
                filterField(sellingPriceNetFilter, productVO.sellingPriceNet.toString()) &&
                productVO.filterExtraData());
    }

    private void setFilteringHeaderRow() {
        amountFilter = new TextFilteringHeaderCell("Search amount", this);
        amountUnitFilter = new TextFilteringHeaderCell("Search amount unit...", this);
        buyingPriceCurrencyFilter = new TextFilteringHeaderCell("Search buying price currency...", this);
        buyingPriceNetFilter = new TextFilteringHeaderCell("Search buying price net...", this);
        nameFilter = new TextFilteringHeaderCell("Search name...", this);
        sellingPriceCurrencyFilter = new TextFilteringHeaderCell("Search selling price currency...", this);
        sellingPriceNetFilter = new TextFilteringHeaderCell("Search selling price net...", this);

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if (extraDataFilter.getValue().isEmpty()) {
                ProductVO.extraDataFilterMap.clear();
            } else {
                ProductVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        HeaderRow filterRow = grid.appendHeaderRow();

        filterRow.getCell(amountColumn).setComponent(styleFilterField(amountFilter, "Amount"));
        filterRow.getCell(amountUnitColumn).setComponent(styleFilterField(amountUnitFilter, "Amount unit"));
        filterRow.getCell(buyingPriceCurrencyColumn).setComponent(styleFilterField(buyingPriceCurrencyFilter, "Buying price currency"));
        filterRow.getCell(buyingPriceNetColumn).setComponent(styleFilterField(buyingPriceNetFilter, "Buying price net"));
        filterRow.getCell(nameColumn).setComponent(styleFilterField(nameFilter, "Name"));
        filterRow.getCell(sellingPriceCurrencyColumn).setComponent(styleFilterField(sellingPriceCurrencyFilter, "Selling price currency"));
        filterRow.getCell(sellingPriceNetColumn).setComponent(styleFilterField(sellingPriceNetFilter, "Selling price net"));
        filterRow.getCell(extraData).setComponent(styleFilterField(extraDataFilter, ""));
    }

    public void updateGridItems() {
        EmsResponse response = apiClient.findAllWithDeleted();
        List<Product> products;
        switch (response.getCode()) {
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

    public EmsDialog getSaveOrUpdateDialog(ProductVO entity) {
        EmsDialog createDialog = new EmsDialog((entity == null ? "Create" : "Modify") + " product");

        FormLayout formLayout = new FormLayout();

        Button saveButton = new Button("Save");

        TextField nameField = new TextField("Name");

        NumberField buyingPriceNetField = new NumberField("Buying price net");


        ComboBox<CodeStore> buyingPriceCurrencys = new ComboBox<>("Buying price currency");
        ComboBox.ItemFilter<CodeStore> buyingPriceCurrencyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        setupCurrencies();
        if (currencyList == null) {
            buyingPriceCurrencys.setInvalid(true);
            buyingPriceCurrencys.setErrorMessage("EmsError happened while getting currencies");
            buyingPriceCurrencys.setEnabled(false);
            saveButton.setEnabled(false);
        } else {
            buyingPriceCurrencys.setInvalid(false);
            buyingPriceCurrencys.setEnabled(true);
            buyingPriceCurrencys.setItems(buyingPriceCurrencyFilter, currencyList);
            buyingPriceCurrencys.setItemLabelGenerator(CodeStore::getName);
        }

        NumberField sellingPriceNetField = new NumberField("Selling price net");

        ComboBox<CodeStore> sellingPriceCurrencies = new ComboBox<>("Selling price currency");
        ComboBox.ItemFilter<CodeStore> sellingPriceCurrencyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if (currencyList == null) {
            sellingPriceCurrencies.setInvalid(true);
            sellingPriceCurrencies.setErrorMessage("EmsError happened while getting currencies");
            sellingPriceCurrencies.setEnabled(false);
            saveButton.setEnabled(false);
        } else {
            sellingPriceCurrencies.setInvalid(false);
            sellingPriceCurrencies.setEnabled(true);
            sellingPriceCurrencies.setItems(sellingPriceCurrencyFilter, currencyList);
            sellingPriceCurrencies.setItemLabelGenerator(CodeStore::getName);
        }

        ComboBox<CodeStore> taxKeys = new ComboBox<>("Tax key");
        ComboBox.ItemFilter<CodeStore> taxKeyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        setupTaxKeys();
        if (taxKeyList == null) {
            taxKeys.setInvalid(true);
            taxKeys.setErrorMessage("EmsError happened while getting tax keys");
            taxKeys.setEnabled(false);
            saveButton.setEnabled(false);
        } else {
            taxKeys.setInvalid(false);
            taxKeys.setEnabled(true);
            taxKeys.setItems(taxKeyFilter, taxKeyList);
            taxKeys.setItemLabelGenerator(CodeStore::getName);
        }

        ComboBox<CodeStore> amountUnits = new ComboBox<>("Amount unit");
        ComboBox.ItemFilter<CodeStore> amountUnitFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        setupAmountUnits();
        if (amountUnitList == null) {
            amountUnits.setInvalid(true);
            amountUnits.setErrorMessage("EmsError happened while getting amount units");
            amountUnits.setEnabled(false);
            saveButton.setEnabled(false);
        } else {
            amountUnits.setInvalid(false);
            amountUnits.setEnabled(true);
            amountUnits.setItems(amountUnitFilter, amountUnitList);
            amountUnits.setItemLabelGenerator(CodeStore::getName);
        }

        NumberField amountField = new NumberField("Amount");

        if (entity != null) {
            nameField.setValue(entity.original.getName());
            buyingPriceNetField.setValue(entity.original.getBuyingPriceNet());
            buyingPriceCurrencys.setValue(entity.original.getBuyingPriceCurrency());
            sellingPriceNetField.setValue(entity.original.getSellingPriceNet());
            sellingPriceCurrencies.setValue(entity.original.getSellingPriceCurrency());
            amountUnits.setValue(entity.original.getAmountUnit());
            amountField.setValue(entity.original.getAmount());
            taxKeys.setValue(entity.original.getTaxKey());
        }

        saveButton.addClickListener(event -> {
            Product product = Optional.ofNullable(entity)
                    .map(e -> e.original)
                    .orElseGet(Product::new);
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
            if (entity != null) {
                response = apiClient.update(product);
            } else {
                response = apiClient.save(product);
            }
            switch (response.getCode()) {
                case 200: {
                    Notification.show("Product  " + (entity == null ? "saved: " : "updated: ") + ((Product) response.getResponseData()).getName())
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    break;
                }
                default: {
                    Notification.show("Product " + (entity == null ? "saving " : "modifying ") + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
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
        switch (response.getCode()) {
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
        switch (response.getCode()) {
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
        switch (response.getCode()) {
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
    public class ProductVO extends BaseVO<Product> {
        private String buyingPriceCurrency;
        private String sellingPriceCurrency;
        private String amountUnit;
        private String name;
        private Double buyingPriceNet;
        private Double sellingPriceNet;
        private Double amount;

        public ProductVO(Product product) {
            super(product.id, product.getDeleted(), product);
            this.name = product.getName();
            this.buyingPriceNet = product.getBuyingPriceNet();
            this.buyingPriceCurrency = product.getBuyingPriceCurrency().getName();
            this.sellingPriceNet = product.getSellingPriceNet();
            this.sellingPriceCurrency = product.getSellingPriceCurrency().getName();
            this.amountUnit = product.getAmountUnit().getName();
            this.amount = product.getAmount();
        }
    }
}
