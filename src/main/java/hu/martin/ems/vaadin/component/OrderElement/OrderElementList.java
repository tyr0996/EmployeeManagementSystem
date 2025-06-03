package hu.martin.ems.vaadin.component.OrderElement;


import com.google.gson.Gson;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.IconProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.EmsFilterableGridComponent;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.Customer;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.model.Product;
import hu.martin.ems.model.Supplier;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.*;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
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
public class OrderElementList extends EmsFilterableGridComponent implements Creatable<OrderElement> {

    private final OrderElementApiClient orderElementApi = BeanProvider.getBean(OrderElementApiClient.class);
    private final OrderApiClient orderApi = BeanProvider.getBean(OrderApiClient.class);
    private final ProductApiClient productApi = BeanProvider.getBean(ProductApiClient.class);
    private final CustomerApiClient customerApi = BeanProvider.getBean(CustomerApiClient.class);
    private final SupplierApiClient supplierApi = BeanProvider.getBean(SupplierApiClient.class);
    private final Gson gson = BeanProvider.getBean(Gson.class);
    private boolean showDeleted = false;

    @Getter
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

    private TextFilteringHeaderCell grossPriceFilter;
    private TextFilteringHeaderCell netPriceFilter;
    private TextFilteringHeaderCell orderFilter;
    private TextFilteringHeaderCell productFilter;
    private TextFilteringHeaderCell taxKeyFilter;
    private TextFilteringHeaderCell unitFilter;
    private TextFilteringHeaderCell unitNetPriceFilter;
    private TextFilteringHeaderCell customerOrSupplierNameFilter;


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
        if (orderElementList == null) {
            Notification.show("Getting order elements failed")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            orderElementList = new ArrayList<>();
        }


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
            Button editButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).EDIT_ICON));
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).PERMANENTLY_DELETE_ICON));
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
                switch (resp.getCode()) {
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

        setFilteringHeaderRow();
        updateGridItems();

        add(hl, grid);
    }

    private void setupOrderElements() {
        EmsResponse response = orderElementApi.findAllWithDeleted();
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

    private void appendCloseButton(Dialog d) {
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> d.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        d.getHeader().add(closeButton);
    }


    public Dialog getSaveOrUpdateDialog(OrderElement entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " order element");
        appendCloseButton(createDialog);
        FormLayout formLayout = new FormLayout();

        Button saveButton = new Button("Save");

        setupProducts();
        ComboBox<Product> products = new ComboBox<>("Product");
        ComboBox.ItemFilter<Product> productFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if (productList == null) {
            products.setInvalid(true);
            products.setEnabled(false);
            products.setErrorMessage("EmsError happened while getting products");
            saveButton.setEnabled(false);
        } else {
            products.setItems(productFilter, productList);
            products.setItemLabelGenerator(Product::getName);
        }

        setupCustomers();
        ComboBox<Customer> customer = new ComboBox<>("Customer");
        ComboBox.ItemFilter<Customer> customerFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());

        if (customerList == null) {
            customer.setInvalid(true);
            customer.setEnabled(false);
            customer.setErrorMessage("EmsError happened while getting customers");
            saveButton.setEnabled(false);
        } else {
            customer.setItems(customerFilter, customerList);
            customer.setItemLabelGenerator(Customer::getName);
        }

        setupSuppliers();
        ComboBox<Supplier> supplier = new ComboBox<>("Supplier");
        ComboBox.ItemFilter<Supplier> supplierFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if (supplierList == null) {
            supplier.setInvalid(true);
            supplier.setEnabled(false);
            supplier.setErrorMessage("EmsError happened while getting suppliers");
            saveButton.setEnabled(false);
        } else {
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
                response = orderElementApi.update(orderElement);
            } else {
                response = orderElementApi.save(orderElement);
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
            setupOrderElements();
            updateGridItems();
        });

        formLayout.add(products, unitField, supplier, customer, saveButton);
        createDialog.add(formLayout);
        return createDialog;
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
                Notification.show("EmsError happened while getting suppliers")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
        }
    }

    private void setupCustomers() {
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
    }

    private void setupProducts() {
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
