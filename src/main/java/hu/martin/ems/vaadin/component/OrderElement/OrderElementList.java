package hu.martin.ems.vaadin.component.OrderElement;


import com.google.gson.Gson;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.EmsFilterableGridComponent;
import hu.martin.ems.core.vaadin.NumberFilteringHeaderCell;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.Customer;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.model.Product;
import hu.martin.ems.model.Supplier;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CustomerApiClient;
import hu.martin.ems.vaadin.api.OrderElementApiClient;
import hu.martin.ems.vaadin.api.ProductApiClient;
import hu.martin.ems.vaadin.api.SupplierApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import hu.martin.ems.vaadin.core.EmsComboBox;
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

@Route(value = "orderElement/list", layout = MainView.class)
@CssImport("./styles/ButtonVariant.css")
@RolesAllowed("ROLE_OrderElementMenuOpenPermission")
@CssImport("./styles/grid.css")
@NeedCleanCoding
public class OrderElementList extends EmsFilterableGridComponent implements Creatable<OrderElement>, IEmsOptionColumnBaseDialogCreationForm<OrderElement, OrderElementList.OrderElementVO> {

    @Getter
    private final OrderElementApiClient apiClient = BeanProvider.getBean(OrderElementApiClient.class);
    private final ProductApiClient productApi = BeanProvider.getBean(ProductApiClient.class);
    private final CustomerApiClient customerApi = BeanProvider.getBean(CustomerApiClient.class);
    private final SupplierApiClient supplierApi = BeanProvider.getBean(SupplierApiClient.class);
    private final Gson gson = BeanProvider.getBean(Gson.class);
    private boolean showDeleted = false;

    @Getter
    private PaginatedGrid<OrderElementVO, String> grid;

    private List<OrderElementVO> orderElementVOS;

    private Grid.Column<OrderElementVO> idColumn;
    private Grid.Column<OrderElementVO> customerOrSupplierName;
    private Grid.Column<OrderElementVO> grossPriceColumn;
    private Grid.Column<OrderElementVO> netPriceColumn;
    private Grid.Column<OrderElementVO> orderColumn;
    private Grid.Column<OrderElementVO> productColumn;
    private Grid.Column<OrderElementVO> taxKeyColumn;
    private Grid.Column<OrderElementVO> unitColumn;
    private Grid.Column<OrderElementVO> unitNetPriceColumn;
    private Grid.Column<OrderElementVO> extraData;

    private TextFilteringHeaderCell grossPriceFilter;
    private TextFilteringHeaderCell netPriceFilter;
    private TextFilteringHeaderCell orderFilter;
    private TextFilteringHeaderCell productFilter;
    private TextFilteringHeaderCell taxKeyFilter;
    private TextFilteringHeaderCell unitFilter;
    private TextFilteringHeaderCell unitNetPriceFilter;
    private TextFilteringHeaderCell customerOrSupplierNameFilter;
    private NumberFilteringHeaderCell idFilter;


    List<OrderElement> orderElementList;
    List<Product> productList;
    List<Customer> customerList;
    List<Supplier> supplierList;
    Logger logger = LoggerFactory.getLogger(OrderElement.class);

    @Autowired
    public OrderElementList(PaginationSetting paginationSetting) {
        OrderElementVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(OrderElementVO.class);

        setEntities();
        if (orderElementList == null) {
            Notification.show("Getting order elements failed")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            orderElementList = new ArrayList<>();
        }

        idColumn = grid.addColumn(v -> v.id);
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

        extraData = this.grid.addComponentColumn(orderElement -> createOptionColumn("OrderElement", orderElement));

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
            setEntities();
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        setFilteringHeaderRow();
        updateGridItems();

        add(hl, grid);
    }
    
    public void setEntities() {
        EmsResponse response = apiClient.findAllWithDeleted();
        switch (response.getCode()) {
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
                filterField(idFilter, orderElementVO.id.doubleValue()) &&
                filterField(customerOrSupplierNameFilter, orderElementVO.customerOrSupplierName) &&
                filterField(grossPriceFilter, orderElementVO.grossPrice.toString()) &&
                filterField(netPriceFilter, orderElementVO.netPrice.toString()) &&
                filterFieldWithNullFilter(orderFilter, orderElementVO.order) &&
                filterField(productFilter, orderElementVO.product) &&
                filterField(taxKeyFilter, orderElementVO.taxKey) &&
                filterField(unitFilter, orderElementVO.unit.toString()) &&
                filterField(unitNetPriceFilter, orderElementVO.unitNetPrice.toString()) &&
                orderElementVO.filterExtraData()
        );
    }

    private void setFilteringHeaderRow() {
        grossPriceFilter = new TextFilteringHeaderCell("Search gross price...", this);
        netPriceFilter = new TextFilteringHeaderCell("Search net price...", this);
        orderFilter = new TextFilteringHeaderCell("Search order...", this);
        productFilter = new TextFilteringHeaderCell("Search product...", this);
        taxKeyFilter = new TextFilteringHeaderCell("Search tax key...", this);
        unitFilter = new TextFilteringHeaderCell("Search unit...", this);
        unitNetPriceFilter = new TextFilteringHeaderCell("Search unit net price...", this);
        customerOrSupplierNameFilter = new TextFilteringHeaderCell("Search customer/supplier...", this);
        idFilter = new NumberFilteringHeaderCell("Search id...", this);

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if (extraDataFilter.getValue().isEmpty()) {
                OrderElementVO.extraDataFilterMap.clear();
            } else {
                OrderElementVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        HeaderRow filterRow = grid.appendHeaderRow();

        filterRow.getCell(idColumn).setComponent(styleFilterField(idFilter, "ID"));
        filterRow.getCell(grossPriceColumn).setComponent(styleFilterField(grossPriceFilter, "Gross price"));
        filterRow.getCell(netPriceColumn).setComponent(styleFilterField(netPriceFilter, "Net price"));
        filterRow.getCell(orderColumn).setComponent(styleFilterField(orderFilter, "Order"));
        filterRow.getCell(productColumn).setComponent(styleFilterField(productFilter, "Product"));
        filterRow.getCell(taxKeyColumn).setComponent(styleFilterField(taxKeyFilter, "Tax key"));
        filterRow.getCell(unitColumn).setComponent(styleFilterField(unitFilter, "Unit"));
        filterRow.getCell(unitNetPriceColumn).setComponent(styleFilterField(unitNetPriceFilter, "Unit net price"));
        filterRow.getCell(customerOrSupplierName).setComponent(styleFilterField(customerOrSupplierNameFilter, "Customer/Supplier"));
        filterRow.getCell(extraData).setComponent(styleFilterField(extraDataFilter, ""));
    }

    public void updateGridItems() {
        if (orderElementList == null) {
            Notification.show("Getting order elements failed").addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            orderElementVOS = orderElementList.stream().map(OrderElementVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        }
    }

    public EmsDialog getSaveOrUpdateDialog(OrderElementVO entity) {
        EmsDialog createDialog = new EmsDialog((entity == null ? "Create" : "Modify") + " order element");

        FormLayout formLayout = new FormLayout();

        Button saveButton = new Button("Save");

        EmsComboBox<Product> products = new EmsComboBox<>("Product", this::setupProducts, saveButton, "EmsError happened while getting products");
        EmsComboBox<Customer> customer = new EmsComboBox<>("Customer", this::setupCustomers, saveButton, "EmsError happened while getting customers");
        EmsComboBox<Supplier> supplier = new EmsComboBox<>("Supplier", this::setupSuppliers, saveButton, "EmsError happened while getting suppliers");

        NumberField unitField = new NumberField("Unit");


        if (entity != null) {
            products.setValue(entity.original.getProduct());
//            orders.setValue(entity.getOrder());
            unitField.setValue(entity.original.getUnit().doubleValue());
        }

        saveButton.addClickListener(event -> {
            OrderElement orderElement = Optional.ofNullable(entity)
                    .map(e -> e.original)
                    .orElseGet(OrderElement::new);
            orderElement.setProduct(products.getValue());
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
            if (entity != null) {
                response = apiClient.update(orderElement);
            } else {
                response = apiClient.save(orderElement);
            }
            switch (response.getCode()) {
                case 200: {
                    Notification.show("OrderElement " + (entity == null ? "saved: " : "updated: ") + orderElement)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    break;
                }

                default: {
                    Notification.show("OrderElement " + (entity == null ? "saving " : "modifying ") + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    break;
                }
            }


            products.setValue(null);
            unitField.setValue(null);
            createDialog.close();
            setEntities();
            updateGridItems();
        });

        formLayout.add(products, unitField, supplier, customer, saveButton);
        createDialog.add(formLayout);
        return createDialog;
    }

    private List<Supplier> setupSuppliers() {
        EmsResponse response = supplierApi.findAll();
        switch (response.getCode()) {
            case 200:
                supplierList = (List<Supplier>) response.getResponseData();
                break;
            default:
                supplierList = null;
                logger.error("Supplier findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                Notification.show("EmsError happened while getting suppliers")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
        }
        return supplierList;
    }

    private List<Customer> setupCustomers() {
        EmsResponse response = customerApi.findAll();
        switch (response.getCode()) {
            case 200:
                customerList = (List<Customer>) response.getResponseData();
                break;
            default:
                customerList = null;
                logger.error("Customers findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
        return customerList;
    }

    private List<Product> setupProducts() {
        EmsResponse response = productApi.findAll();
        switch (response.getCode()) {
            case 200:
                productList = (List<Product>) response.getResponseData();
                break;
            default:
                productList = null;
                logger.error("Product findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
        return productList;
    }

    @NeedCleanCoding
    public class OrderElementVO extends BaseVO<OrderElement> {
        private String product;
        private String order;
        private String taxKey;
        private Integer unit;
        private Integer unitNetPrice;
        private Integer netPrice;
        private Integer grossPrice;
        private String customerOrSupplierName;

        public OrderElementVO(OrderElement orderElement) {
            super(orderElement.id, orderElement.getDeleted(), orderElement);
            this.product = orderElement.getProduct().getName();
            this.order = orderElement.getOrderId() == null ? "" : orderElement.getOrderId().toString();
            this.unit = orderElement.getUnit();
            this.unitNetPrice = orderElement.getUnitNetPrice();
            this.taxKey = orderElement.getTaxKey().getName() + "%";
            this.netPrice = orderElement.getNetPrice();
            this.grossPrice = orderElement.getGrossPrice();
            this.customerOrSupplierName = generateCustomerOrSupplierName(orderElement);
        }

        private String generateCustomerOrSupplierName(OrderElement orderElement) {
            if (orderElement.getCustomer() != null && orderElement.getSupplier() != null) {
                return "(S) " + orderElement.getSupplier().getName() + ", (C) " + orderElement.getCustomer().getName();
            } else if (orderElement.getCustomer() == null) {
                return "(S) " + orderElement.getSupplier().getName();
            } else {
                return "(C) " + orderElement.getCustomer().getName();
            }
        }
    }
}
