package hu.martin.ems.vaadin.component.Order;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.CodeStoreIds;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.model.Customer;
import hu.martin.ems.model.Order;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CodeStoreApiClient;
import hu.martin.ems.vaadin.api.CustomerApiClient;
import hu.martin.ems.vaadin.api.OrderApiClient;
import hu.martin.ems.vaadin.api.OrderElementApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route(value = "order/create/customer", layout = MainView.class)
@RolesAllowed("ROLE_OrderCreateMenuOpenPermission")
@NeedCleanCoding
public class OrderCreateToCustomer extends VerticalLayout implements BeforeEnterObserver {

    public Order editObject;
    private final OrderApiClient orderApi = BeanProvider.getBean(OrderApiClient.class);
    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private final CustomerApiClient customerApi = BeanProvider.getBean(CustomerApiClient.class);
    private final OrderElementApiClient orderElementApi = BeanProvider.getBean(OrderElementApiClient.class);

    private List<OrderElementVO> orderElementVOS;

    private PaginatedGrid<OrderElementVO, String> grid;

    private Boolean showPreviously = false;

    ComboBox<CodeStore> paymentTypes;
    ComboBox<CodeStore> currencies;

    private Logger logger = LoggerFactory.getLogger(Order.class);

    List<Customer> customerList;
    List<CodeStore> paymentTypeList;
    List<CodeStore> currencyList;

    ComboBox<Customer> customers;
    Checkbox showPreviouslyOrderedElements;

    @Autowired
    public OrderCreateToCustomer(PaginationSetting paginationSetting) {
        init(paginationSetting);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
        List<String> paramOrderId = params.get("orderId");
        if (paramOrderId != null) {
            setupCustomers();
            customers.setItems(customerList);
            setupCurrencies();
            currencies.setItems(currencyList);
            setupPaymentTypes();
            paymentTypes.setItems(paymentTypeList);
            Long orderId = Long.parseLong(paramOrderId.getFirst());
            EmsResponse response = orderApi.findById(orderId);
            switch (response.getCode()) {
                case 200: {
                    this.editObject = (Order) orderApi.findById(orderId).getResponseData();
                    loadEditObject();
                    break;
                }
                default: {
                    Notification.show(response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    break;
                }
            }
        }
    }

    private void loadEditObject() {
        customers.setEnabled(false);
        customers.setValue(editObject.getCustomer());
        showPreviouslyOrderedElements.setValue(true);
        showPreviously = true;
        paymentTypes.setValue(editObject.getPaymentType());
        currencies.setValue(editObject.getCurrency());
        orderElementVOS = getOrderElementsByCustomer(editObject.getCustomer()).stream().map(OrderElementVO::new).collect(Collectors.toList());
        updateGridItems();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
    }

    private void init(PaginationSetting paginationSetting) {
        FormLayout formLayout = new FormLayout();

        Button saveButton = new Button("Create order");

        grid = new PaginatedGrid<>(OrderElementVO.class);
        grid.setItems(new ArrayList<>());
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.asMultiSelect();
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        customers = new ComboBox<>("Customer");
        ComboBox.ItemFilter<Customer> customerFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        setupCustomers();
        if (customerList == null) {
            customers.setEnabled(false);
            customers.setInvalid(true);
            customers.setErrorMessage("EmsError happened while getting customers");
            saveButton.setEnabled(false);
        } else {
            customers.setItems(customerFilter, customerList);
            customers.setItemLabelGenerator(Customer::getName);
        }

        customers.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                orderElementVOS = getOrderElementsByCustomer(event.getValue()).stream().map(OrderElementVO::new).collect(Collectors.toList());
            } else {
                orderElementVOS = new ArrayList<>();
            }
            updateGridItems();
        });

        showPreviouslyOrderedElements = new Checkbox("Show previously ordered elements");
        showPreviouslyOrderedElements.addValueChangeListener(event -> {
            showPreviously = !showPreviously;
            orderElementVOS = customers.getValue() == null ?
                    new ArrayList<>() :
                    getOrderElementsByCustomer(customers.getValue()).stream().map(OrderElementVO::new).toList();

            if (showPreviously) {
                grid.setSelectionMode(Grid.SelectionMode.NONE);
                grid.setItems(getFilteredStream().filter(v -> v.original.getOrderId() != null).toList());
            } else {
                grid.setSelectionMode(Grid.SelectionMode.MULTI);
                grid.setItems(getFilteredStream().toList());
            }

            if (editObject != null) {
                grid.setSelectionMode(Grid.SelectionMode.MULTI);
            }
        });

        setupPaymentTypes();
        paymentTypes = new ComboBox<>("Payment type");
        ComboBox.ItemFilter<CodeStore> paymentTypeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if (paymentTypeList == null) {
            paymentTypes.setErrorMessage("EmsError happened while getting payment methods");
            paymentTypes.setEnabled(false);
            paymentTypes.setInvalid(true);
            saveButton.setEnabled(false);
        } else {
            paymentTypes.setItems(paymentTypeFilter, paymentTypeList);
            paymentTypes.setItemLabelGenerator(CodeStore::getName);
        }

        setupCurrencies();
        currencies = new ComboBox<>("Currency");
        ComboBox.ItemFilter<CodeStore> currencyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if (currencyList == null) {
            currencies.setEnabled(false);
            currencies.setInvalid(true);
            currencies.setErrorMessage("EmsError happened while getting currencies");
            saveButton.setEnabled(false);
        } else {
            currencies.setItems(currencyFilter, currencyList);
            currencies.setItemLabelGenerator(CodeStore::getName);
        }

        saveButton.addClickListener(event -> {
            CodeStore pending = getPendingCodeStore();
            if (pending == null) {
                Notification.show("EmsError happened while getting \"Pending\" status").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Order order = Objects.requireNonNullElseGet(editObject, Order::new);
            order.setState(pending);
            order.setTimeOfOrder(LocalDateTime.now());
            order.setCustomer(customers.getValue());
            order.setPaymentType(paymentTypes.getValue());
            order.setDeleted(0L);
            order.setCurrency(currencies.getValue());

            List<OrderElement> orderElements = new ArrayList<>();
            List<OrderElementVO> selected = grid.getSelectedItems().stream().toList();

            for (int i = 0; i < selected.size(); i++) {
                OrderElement oe = selected.get(i).original;
                orderElements.add(oe);
            }
            order.setOrderElements(orderElements);

            if (orderElements.isEmpty()) {
                Notification.show("Order must contains at least one order element!").addThemeVariants(NotificationVariant.LUMO_WARNING);
            } else {
                EmsResponse response;
                if (editObject == null) {
                    response = orderApi.save(order);
                } else {
                    response = orderApi.update(order);
                }
                switch (response.getCode()) {
                    case 200:
                        order = (Order) response.getResponseData();
                        Notification.show("Order " + (editObject == null ? "saved: " : "updated: ") + order)
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        break;
                    default:
                        Notification.show("Order " + (editObject == null ? "saving " : "modifying ") + " failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        updateGridItems();
                        return;
                }
                customers.setValue(customers.getEmptyValue());
                paymentTypes.setValue(paymentTypes.getEmptyValue());
            }
        });

        formLayout.add(customers, showPreviouslyOrderedElements);
        FormLayout formLayout1 = new FormLayout();
        formLayout1.add(currencies, paymentTypes);
        formLayout1.add(saveButton);
        add(formLayout, grid, formLayout1);
    }

    //region setup methods

    private void setupCustomers() {
        EmsResponse response = customerApi.findAll();
        switch (response.getCode()) {
            case 200:
                customerList = (List<Customer>) response.getResponseData();
                break;
            default:
                customerList = null;
                logger.error("Customer findAllError. [payment types] Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private CodeStore getPendingCodeStore() {
        EmsResponse response = codeStoreApi.getAllByName("Pending");
        switch (response.getCode()) {
            case 200:
                return ((List<CodeStore>) response.getResponseData()).get(0);
            default:
                logger.error("CodeStore getChildrenError. [status] Code: {}, Description: {}", response.getCode(), response.getDescription());
                return null;
        }
    }

    private List<OrderElement> getOrderElementsByCustomer(Customer customer) {
        EmsResponse response = orderElementApi.getByCustomer(customer);
        switch (response.getCode()) {
            case 200:
                return (List<OrderElement>) response.getResponseData();
            default:
                logger.error("OrderElement getOrderElementsByCustomerError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                Notification.show("EmsError happened while getting order elements to the customer")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return null;
        }

    }

    private void setupPaymentTypes() {
        EmsResponse response = codeStoreApi.getChildren(CodeStoreIds.PAYMENT_TYPES_CODESTORE_ID);
        switch (response.getCode()) {
            case 200:
                paymentTypeList = (List<CodeStore>) response.getResponseData();
                break;
            default:
                paymentTypeList = null;
                logger.error("CodeStore getChildrenError. [payment types] Code: {}, Description: {}", response.getCode(), response.getDescription());
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
                logger.error("CodeStore getChildrenError [currencies]. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    //endregion


    private Stream<OrderElementVO> getFilteredStream() {
        if (editObject != null) {
            orderElementVOS = getOrderElementsByCustomer(editObject.getCustomer()).stream().filter(v ->
                    (v.getOrderId() == null) || v.getOrderId().equals(editObject.getId())
            ).map(OrderElementVO::new).collect(Collectors.toList());
        }

        return orderElementVOS.stream().filter(orderElementVO ->
        {
            OrderElement oe = orderElementVO.original;
            boolean res = showPreviously || oe.getOrderId() == null;
//            Boolean filter = orderElementVO.filterExtraData();
            Boolean filter = oe.getDeleted() == 0;
            return res && filter;
        });
    }


    public void updateGridItems() {
        orderElementVOS = getFilteredStream().toList();
        grid.setItems(orderElementVOS);
        if (editObject != null) {
            List<OrderElementVO> selected = orderElementVOS.stream()
                    .filter(v -> {
                        if (v.original.getOrderId() != null) {
                            return v.original.getOrderId().equals(editObject.getId());
                        } else {
                            return false;
                        }
                    }).toList();
            UI.getCurrent().access(() -> {
                grid.deselectAll();
                selected.stream()
                        .distinct()
                        .forEach(v -> grid.getSelectionModel().select(v));
            });
        }
    }

    @Getter
    @NeedCleanCoding
    public class OrderElementVO extends BaseVO<OrderElement> {
        private String product;
        private String orderNumber;
        private String taxKey;
        private Integer unit;
        private Integer unitNetPrice;
        private Integer netPrice;
        private Integer grossPrice;

        public OrderElementVO(OrderElement orderElement) {
            super(orderElement.id, orderElement.getDeleted(), orderElement);
            this.product = orderElement.getProduct().getName();
            this.orderNumber = orderElement.getOrderId() == null ? "" : orderElement.getOrderId().toString();
            this.unit = orderElement.getUnit();
            this.unitNetPrice = orderElement.getUnitNetPrice();
            this.taxKey = orderElement.getTaxKey().getName() + "%";
            this.netPrice = orderElement.getNetPrice();
            this.grossPrice = orderElement.getGrossPrice();
        }
    }
}
