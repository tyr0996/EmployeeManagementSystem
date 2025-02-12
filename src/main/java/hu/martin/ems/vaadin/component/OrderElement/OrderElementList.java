package hu.martin.ems.vaadin.component.OrderElement;


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
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Customer;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.model.Product;
import hu.martin.ems.model.Supplier;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.*;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import jakarta.annotation.security.RolesAllowed;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@Route(value = "orderElement/list", layout = MainView.class)
@CssImport("./styles/ButtonVariant.css")
@RolesAllowed("ROLE_OrderElementMenuOpenPermission")
@CssImport("./styles/grid.css")
@NeedCleanCoding
public class OrderElementList extends VerticalLayout implements Creatable<OrderElement> {

    private final OrderElementApiClient orderElementApi = BeanProvider.getBean(OrderElementApiClient.class);
    private final OrderApiClient orderApi = BeanProvider.getBean(OrderApiClient.class);
    private final ProductApiClient productApi = BeanProvider.getBean(ProductApiClient.class);
    private final CustomerApiClient customerApi = BeanProvider.getBean(CustomerApiClient.class);
    private final SupplierApiClient supplierApi = BeanProvider.getBean(SupplierApiClient.class);
    private final Gson gson = BeanProvider.getBean(Gson.class);
    private boolean showDeleted = false;
    private PaginatedGrid<OrderElementVO, String> grid;
    
    private List<OrderElementVO> orderElementVOS;

    private final PaginationSetting paginationSetting;
    private Grid.Column<OrderElementVO> customerOrSupplierName;
    private Grid.Column<OrderElementVO> grossPriceColumn;
    private Grid.Column<OrderElementVO> netPriceColumn;
    private Grid.Column<OrderElementVO> orderColumn;
    private Grid.Column<OrderElementVO> productColumn;
    private Grid.Column<OrderElementVO> taxKeyColumn;
    private Grid.Column<OrderElementVO> unitColumn;
    private Grid.Column<OrderElementVO> unitNetPriceColumn;

    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Grid.Column<OrderElementVO> extraData;

    private static String grossPriceFilterText = "";
    private static String netPriceFilterText = "";
    private static String orderFilterText = "";
    private static String productFilterText = "";
    private static String taxKeyFilterText = "";
    private static String unitFilterText = "";
    private static String unitNetPriceFilterText = "";
    private static String customerOrSupplierNameFilterText = "";
    List<OrderElement> orderElementList;
    List<Product> productList;
    List<Customer> customerList;
    List<Supplier> supplierList;
    Logger logger = LoggerFactory.getLogger(OrderElement.class);
    private MainView mainView;

    @Autowired
    public OrderElementList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;
        OrderElementVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(OrderElementVO.class);
        setupOrderElements();
        if(orderElementList == null){
            Notification.show("Error happened while getting order elements")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            orderElementList = new ArrayList<>();
        }

        updateGridItems();

        grossPriceColumn = grid.addColumn(v -> v.grossPrice);
        netPriceColumn = grid.addColumn(v -> v.netPrice);
        orderColumn = grid.addColumn(v -> v.order);
        productColumn = grid.addColumn(v -> v.product);
        taxKeyColumn = grid.addColumn(v -> v.taxKey);
        unitColumn = grid.addColumn(v -> v.unit);
        unitNetPriceColumn = grid.addColumn(v -> v.unitNetPrice);
        customerOrSupplierName = grid.addColumn(v -> v.customerOrSupplierName);

        grid.addClassName("styling");
        grid.setPartNameGenerator(orderElementVO -> orderElementVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());


        //region Options column
        extraData = this.grid.addComponentColumn(orderElement -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog d = getSaveOrUpdateDialog(orderElement.original);
                d.open();
            });

            restoreButton.addClickListener(event -> {
                this.orderElementApi.restore(orderElement.original);
                Notification.show("OrderElement restored: " + orderElement.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setupOrderElements();
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                EmsResponse resp = this.orderElementApi.delete(orderElement.original);
                switch (resp.getCode()){
                    case 200: {
                        Notification.show("OrderElement deleted: " + orderElement.original.getName())
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        updateGridItems();
                        break;
                    }
                    default: {
                        Notification.show(resp.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
                setupOrderElements();
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.orderElementApi.permanentlyDelete(orderElement.original.getId());
                Notification.show("OrderElement permanently deleted: " + orderElement.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setupOrderElements();
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (orderElement.original.getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        });
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
            OrderElementVO.showDeletedCheckboxFilter.replace("deleted", newValue);
            setupOrderElements();
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, grid);
    }

    private void setupOrderElements() {
        EmsResponse response = orderElementApi.findAllWithDeleted();
        switch (response.getCode()){
            case 200:
                orderElementList = (List<OrderElement>) response.getResponseData();
                break;
            default:
                orderElementList = null;
                logger.error("OrderElement findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private Stream<OrderElementVO> getFilteredStream() {
        return orderElementVOS.stream().filter(orderElementVO ->
                filterField(customerOrSupplierNameFilterText, orderElementVO.customerOrSupplierName) &&
                filterField(grossPriceFilterText, orderElementVO.grossPrice.toString()) &&
                filterField(netPriceFilterText, orderElementVO.netPrice.toString()) &&
                filterFieldWithNullFilter(orderFilterText, orderElementVO.order) &&
                filterField(productFilterText, orderElementVO.product) &&
                filterField(taxKeyFilterText, orderElementVO.taxKey) &&
                filterField(unitFilterText, orderElementVO.unit.toString()) &&
                filterField(unitNetPriceFilterText, orderElementVO.unitNetPrice.toString()) &&
                orderElementVO.filterExtraData()

        );
    }


    //TODO megcsinálni a többinél is
    private boolean filterFieldWithNullFilter(String filterFieldText, String fieldValue){
        if(filterFieldText.toLowerCase().equals("null")){
            return fieldValue.isEmpty();
        }
        else{
            return filterField(filterFieldText, fieldValue);
        }
    }

    private boolean filterField(String filterFieldText, String fieldValue){
        return filterFieldText.isEmpty() || fieldValue.toLowerCase().contains(filterFieldText.toLowerCase());
    }
    
    private Component createFilterField(TextField filterField, String title){
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
        TextField grossPriceFilter = new TextField();
        grossPriceFilter.setPlaceholder("Search gross price...");
        grossPriceFilter.setClearButtonVisible(true);
        grossPriceFilter.addValueChangeListener(event -> {
            grossPriceFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField netPriceFilter = new TextField();
        netPriceFilter.setPlaceholder("Search net price...");
        netPriceFilter.setClearButtonVisible(true);
        netPriceFilter.addValueChangeListener(event -> {
            netPriceFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField orderFilter = new TextField();
        orderFilter.setPlaceholder("Search order...");
        orderFilter.setClearButtonVisible(true);
        orderFilter.addValueChangeListener(event -> {
            orderFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField productFilter = new TextField();
        productFilter.setPlaceholder("Search product...");
        productFilter.setClearButtonVisible(true);
        productFilter.addValueChangeListener(event -> {
            productFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField taxKeyFilter = new TextField();
        taxKeyFilter.setPlaceholder("Search tax key...");
        taxKeyFilter.setClearButtonVisible(true);
        taxKeyFilter.addValueChangeListener(event -> {
            taxKeyFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField unitFilter = new TextField();
        unitFilter.setPlaceholder("Search unit...");
        unitFilter.setClearButtonVisible(true);
        unitFilter.addValueChangeListener(event -> {
            unitFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField unitNetPriceFilter = new TextField();
        unitNetPriceFilter.setPlaceholder("Search unit net price...");
        unitNetPriceFilter.setClearButtonVisible(true);
        unitNetPriceFilter.addValueChangeListener(event -> {
            unitNetPriceFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField customerOrSupplierNameFilter = new TextField();
        customerOrSupplierNameFilter.setPlaceholder("Search customer/supplier...");
        customerOrSupplierNameFilter.setClearButtonVisible(true);
        customerOrSupplierNameFilter.addValueChangeListener(event -> {
            customerOrSupplierNameFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                OrderElementVO.extraDataFilterMap.clear();
            }
            else{
                OrderElementVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });


        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();;
        filterRow.getCell(grossPriceColumn).setComponent(createFilterField(grossPriceFilter, "Gross price"));
        filterRow.getCell(netPriceColumn).setComponent(createFilterField(netPriceFilter, "Net price"));
        filterRow.getCell(orderColumn).setComponent(createFilterField(orderFilter, "Order"));
        filterRow.getCell(productColumn).setComponent(createFilterField(productFilter, "Product"));
        filterRow.getCell(taxKeyColumn).setComponent(createFilterField(taxKeyFilter, "Tax key"));
        filterRow.getCell(unitColumn).setComponent(createFilterField(unitFilter, "Unit"));
        filterRow.getCell(unitNetPriceColumn).setComponent(createFilterField(unitNetPriceFilter, "Unit net price"));
        filterRow.getCell(customerOrSupplierName).setComponent(createFilterField(customerOrSupplierNameFilter, "Customer/Supplier"));
        filterRow.getCell(extraData).setComponent(createFilterField(extraDataFilter, ""));
    }

    private void updateGridItems() {
        if(orderElementList == null){
            Notification.show("Getting order elements failed").addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
        else{
            orderElementVOS = orderElementList.stream().map(OrderElementVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        }
    }

    public Dialog getSaveOrUpdateDialog(OrderElement entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " order element");
        FormLayout formLayout = new FormLayout();

        Button saveButton = new Button("Save");

        setupProducts();
        ComboBox<Product> products = new ComboBox<>("Product");
        ComboBox.ItemFilter<Product> productFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if(productList == null){
            products.setInvalid(true);
            products.setEnabled(false);
            products.setErrorMessage("Error happened while getting products");
            saveButton.setEnabled(false);
        }
        else {
            products.setItems(productFilter, productList);
            products.setItemLabelGenerator(Product::getName);
        }

        setupCustomers();
        ComboBox<Customer> customer = new ComboBox<>("Customer");
        ComboBox.ItemFilter<Customer> customerFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());

        if(customerList == null){
            customer.setInvalid(true);
            customer.setEnabled(false);
            customer.setErrorMessage("Error happened while getting customers");
            saveButton.setEnabled(false);
        }
        else{
            customer.setItems(customerFilter, customerList);
            customer.setItemLabelGenerator(Customer::getName);
        }

        setupSuppliers();
        ComboBox<Supplier> supplier = new ComboBox<>("Supplier");
        ComboBox.ItemFilter<Supplier> supplierFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if(supplierList == null){
            supplier.setInvalid(true);
            supplier.setEnabled(false);
            supplier.setErrorMessage("Error happened while getting suppliers");
            saveButton.setEnabled(false);
        }
        else{
            supplier.setItems(supplierFilter, supplierList);
            supplier.setItemLabelGenerator(Supplier::getName);
        }

        NumberField unitField = new NumberField("Unit");


        if (entity != null) {
            products.setValue(entity.getProduct());
//            orders.setValue(entity.getOrder());
            unitField.setValue(entity.getUnit().doubleValue());
        }

        saveButton.addClickListener(event -> {
            OrderElement orderElement = Objects.requireNonNullElseGet(entity, OrderElement::new);
            orderElement.setProduct(products.getValue());
            //orderElement.setOrder(orders.getValue());
            orderElement.setUnit(unitField.getValue().intValue());
            orderElement.setUnitNetPrice(products.getValue().getSellingPriceNet().intValue());
            orderElement.setTaxKey(products.getValue().getTaxKey());
            orderElement.setNetPrice(orderElement.getUnitNetPrice() * orderElement.getUnit());
            orderElement.setSupplier(supplier.getValue());
            orderElement.setCustomer(customer.getValue());
            double tax = (Double.parseDouble(orderElement.getTaxKey().getName()) / 100) + 1;
            orderElement.setGrossPrice(((Double) Math.ceil(tax * orderElement.getNetPrice())).intValue());
            orderElement.setDeleted(0L);
            orderElement.setName(products.getValue().getName());
            orderElement.setTaxPrice(orderElement.getGrossPrice().doubleValue() - orderElement.getNetPrice().doubleValue());
            EmsResponse response = null;
            if(entity != null){
                response = orderElementApi.update(orderElement);
            }
            else{
                response = orderElementApi.save(orderElement);
            }
            switch (response.getCode()){
                case 200: {
                    Notification.show("OrderElement " + (entity == null ? "saved: " : "updated: ") + orderElement)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    break; //TODO
                }

                default:{
                    Notification.show("OrderElement " + (entity == null ? "saving " : "modifying " ) + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    break; //TODO
                }
            }


            products.setValue(null);
            unitField.setValue(null);
            createDialog.close();
            setupOrderElements();
            updateGridItems();
        });

        formLayout.add(products, unitField, supplier, customer, saveButton);
        createDialog.add(formLayout);
        return createDialog;
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
                Notification.show("Error happened while getting suppliers")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
        }
    }

    private void setupCustomers() {
        EmsResponse response = customerApi.findAll();
        switch (response.getCode()){
            case 200:
                customerList = (List<Customer>) response.getResponseData();
                break;
            default:
                customerList = null;
                logger.error("Customers findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private void setupProducts() {
        EmsResponse response = productApi.findAll();
        switch (response.getCode()){
            case 200:
                productList = (List<Product>) response.getResponseData();
                break;
            default:
                productList = null;
                logger.error("Product findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    @NeedCleanCoding
public class OrderElementVO extends BaseVO {
        private OrderElement original;
        private String product;
        private String order;
        private String taxKey;
        private Integer unit;
        private Integer unitNetPrice;
        private Integer netPrice;
        private Integer grossPrice;
        private String customerOrSupplierName;

        public OrderElementVO(OrderElement orderElement) {
            super(orderElement.id, orderElement.getDeleted());
            this.original = orderElement;
            this.product = original.getProduct().getName();
            this.order = original.getOrderId() == null ? "" : original.getOrderId().toString();
            this.unit = original.getUnit();
            this.unitNetPrice = original.getUnitNetPrice();
            this.taxKey = original.getTaxKey().getName() + "%";
            this.netPrice = original.getNetPrice();
            this.grossPrice = original.getGrossPrice();
            this.customerOrSupplierName = orderElement.getCustomer() == null ? "(S) " + orderElement.getSupplier().getName() : "(C) " + orderElement.getCustomer().getName();
        }
    }
}
