package hu.martin.ems.vaadin.component.Order;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.annotations.EditObject;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.*;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CodeStoreApiClient;
import hu.martin.ems.vaadin.api.CustomerApiClient;
import hu.martin.ems.vaadin.api.OrderApiClient;
import hu.martin.ems.vaadin.api.OrderElementApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import com.vaadin.flow.component.UI;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route(value = "order/create", layout = MainView.class)
@AnonymousAllowed
@NeedCleanCoding
public class OrderCreate extends VerticalLayout {

    @EditObject
    public static Order editObject;
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

    //The autowired here is need because the MainView addmenu function
    @Autowired
    public OrderCreate(PaginationSetting paginationSetting) {
        FormLayout formLayout = new FormLayout();

        Button saveButton = new Button("Create order");

        grid = new PaginatedGrid<>(OrderElementVO.class);
        grid.setItems(new ArrayList<>());
        grid.removeColumnByKey("original");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.asMultiSelect();

        ComboBox<Customer> customers = new ComboBox<>("Customer");
        ComboBox.ItemFilter<Customer> customerFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        setupCustomers();
        if(customerList == null){
            customers.setEnabled(false);
            customers.setInvalid(true);
            customers.setErrorMessage("Error happened while getting customers");
            saveButton.setEnabled(false);
        }
        else{
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

        Checkbox showPreviouslyOrderedElements = new Checkbox("Show previously ordered elements");
        showPreviouslyOrderedElements.addValueChangeListener(event -> {
            showPreviously = !showPreviously;
            if(showPreviously){
                grid.setSelectionMode(Grid.SelectionMode.NONE);
            }
            else{
                grid.setSelectionMode(Grid.SelectionMode.MULTI);
            }
            orderElementVOS = customers.getValue() == null ?
                    new ArrayList<>() :
                    getOrderElementsByCustomer(customers.getValue()).stream().map(OrderElementVO::new).toList();
            if(editObject != null){
                grid.setSelectionMode(Grid.SelectionMode.MULTI);
            }
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showPreviouslyOrderedElements);
        hl.setAlignSelf(Alignment.CENTER, showPreviouslyOrderedElements);

        setupPaymentTypes();
        paymentTypes = new ComboBox<>("Payment type");
        ComboBox.ItemFilter<CodeStore> paymentTypeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if(paymentTypeList == null){
            paymentTypes.setErrorMessage("Error happened while getting payment methods");
            paymentTypes.setEnabled(false);
            paymentTypes.setInvalid(true);
            saveButton.setEnabled(false);
        }
        else{
            paymentTypes.setItems(paymentTypeFilter, paymentTypeList);
            paymentTypes.setItemLabelGenerator(CodeStore::getName);
        }

        setupCurrencies();
        currencies = new ComboBox<>("Currency");
        ComboBox.ItemFilter<CodeStore> currencyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if(currencyList == null){
            currencies.setEnabled(false);
            currencies.setInvalid(true);
            currencies.setErrorMessage("Error happened while getting currencies");
            saveButton.setEnabled(false);
        }
        else{
            currencies.setItems(currencyFilter, currencyList);
            currencies.setItemLabelGenerator(CodeStore::getName);
        }


        if(editObject != null){
            customers.setValue(editObject.getCustomer());
            showPreviouslyOrderedElements.setValue(true);
            paymentTypes.setValue(editObject.getPaymentType());
            currencies.setValue(editObject.getCurrency());
            orderElementVOS = getOrderElementsByCustomer(editObject.getCustomer()).stream().map(OrderElementVO::new).collect(Collectors.toList());
            updateGridItems();
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
        }


        saveButton.addClickListener(event -> {
            CodeStore pending = getPendingCodeStore();
            if(pending == null){
                Notification.show("Error happened while getting \"Pending\" status").addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            List<OrderElement> originalOrderElements = null;
            Order originalOrder = null;
            if(editObject != null){
                originalOrderElements = orderApi.getOrderElements(editObject.getId());
                originalOrder = new Order();
                originalOrder.setTimeOfOrder(editObject.getTimeOfOrder());
                originalOrder.setState(editObject.getState());
                originalOrder.setCurrency(editObject.getCurrency());
                originalOrder.setCustomer(editObject.getCustomer());
                originalOrder.setDeleted(editObject.getDeleted());
                originalOrder.setSupplier(editObject.getSupplier());
                originalOrder.setPaymentType(editObject.getPaymentType());
                originalOrder.setName(editObject.getName());
                originalOrder.id = editObject.getId();
            }

            Order order = Objects.requireNonNullElseGet(editObject, Order::new);
            order.setState(pending);
            order.setTimeOfOrder(LocalDateTime.now());
            order.setCustomer(customers.getValue());
            order.setPaymentType(paymentTypes.getValue());
            order.setDeleted(0L);
            order.setCurrency(currencies.getValue());
            EmsResponse response = null;
            if(editObject != null){
                response = orderApi.update(order);
            }
            else{
                response =  orderApi.save(order);
            }
            switch (response.getCode()){
                case 200:
                    order = (Order) response.getResponseData();
                    break;
                case 500:
                    Notification.show("Order saving failed").addThemeVariants(NotificationVariant.LUMO_ERROR);
                    updateGridItems();
                    return;
                default:
                    Notification.show("Not expected status-code in saving").addThemeVariants(NotificationVariant.LUMO_WARNING);
                    updateGridItems();
                    return;
            }
            List<OrderElement> savedOrderElements = new ArrayList<>();
            for(OrderElementVO oeVo : grid.getSelectedItems()){
                OrderElement oe = oeVo.getOriginal();
                oe.setOrder((Order)response.getResponseData());
                EmsResponse responseOrderElement = orderElementApi.update(oe);
                switch (responseOrderElement.getCode()){
                    case 200:
                        savedOrderElements.add((OrderElement) responseOrderElement.getResponseData());
                        break;
                    case 500:
                        Notification.show("Order element " + (editObject == null ? "saving" : "modifying") + " failed");
                        if(editObject != null){
                            undoUpdate(originalOrder, originalOrderElements);
                        }
                        else {
                            undoSave(order);
                        }
                        updateGridItems();
                        return;
                    default:
                        Notification.show("Not expected status-code in saving order element").addThemeVariants(NotificationVariant.LUMO_ERROR);
                        if(editObject != null){
                            undoUpdate(originalOrder, originalOrderElements);
                        }
                        else {
                            undoSave(order);
                        }
                        updateGridItems();
                        return;
                }
            }

            Notification.show("Order " + (editObject == null ? "saved: " : "updated: ") + order)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            customers.setValue(customers.getEmptyValue());
            paymentTypes.setValue(paymentTypes.getEmptyValue());
        });

        formLayout.add(customers);
        FormLayout formLayout1 = new FormLayout();
        formLayout1.add(currencies, paymentTypes);
        formLayout1.add(saveButton);
        add(formLayout, hl, grid, formLayout1);
    }

    private void setupCustomers() {
        EmsResponse response = customerApi.findAll();
        switch (response.getCode()){
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
        switch (response.getCode()){
            case 200:
                return ((List<CodeStore>) response.getResponseData()).get(0);
            default:
                logger.error("CodeStore getChildrenError. [status] Code: {}, Description: {}", response.getCode(), response.getDescription());
                return null;
        }
    }

    private List<OrderElement> getOrderElementsByCustomer(Customer customer) {
        EmsResponse response = orderElementApi.getByCustomer(customer);
        switch (response.getCode()){
            case 200:
                return (List<OrderElement>) response.getResponseData();
            default:
                logger.error("OrderElement getOrderElementsByCustomerError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                Notification.show("Error happened while getting order elements to the customer")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return new ArrayList<>();
        }

    }

    private void setupPaymentTypes() {
        EmsResponse response = codeStoreApi.getChildren(StaticDatas.PAYMENT_TYPES_CODESTORE_ID);
        switch (response.getCode()){
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
        EmsResponse response = codeStoreApi.getChildren(StaticDatas.CURRENCIES_CODESTORE_ID);
        switch (response.getCode()){
            case 200:
                currencyList = (List<CodeStore>) response.getResponseData();
                break;
            default:
                currencyList = null;
                logger.error("CodeStore getChildrenError [currencies]. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private void undoUpdate(Order originalOrder, List<OrderElement> originalOrderElements) {
        orderApi.update(originalOrder);
        orderApi.getOrderElements(originalOrder.getId()).forEach(v -> {
            v.setOrder(null);
            orderElementApi.update(v);
        });
        originalOrderElements.forEach(v -> orderElementApi.update(v));
        logger.info("Undo order update successful");
        updateGridItems();
    }

    private void undoSave(Order order){
        List<OrderElement> orderElements = orderApi.getOrderElements(order.getId());
        orderElements.forEach(v -> {
            v.setOrder(null);
            orderElementApi.update(v);
        });
        orderApi.forcePermanentlyDelete(order.getId());
        logger.info("Undo order create successful");
        updateGridItems();
    }

    private Stream<OrderElementVO> getFilteredStream() {
        if(editObject != null){
            orderElementVOS = getOrderElementsByCustomer(editObject.getCustomer()).stream().map(OrderElementVO::new).collect(Collectors.toList());
        }

        return orderElementVOS.stream().filter(orderElementVO ->
            {
                OrderElement oe = orderElementVO.getOriginal();
                Order o = oe.getOrder();
                Boolean res = false;
                if(showPreviously){
                    res = o != null;
                }
                else{
                    res = o == null;
                }

                Boolean filter = orderElementVO.filterExtraData();
                return res && filter;
            });
    }


    public void updateGridItems(){
        orderElementVOS = getFilteredStream().toList();
        grid.setItems(orderElementVOS);
        if(editObject != null){
            List<OrderElementVO> selected = orderElementVOS.stream()
                    .filter(v -> {
                        Order o = v.getOriginal().getOrder();
                        if(o != null){
                            return o.getId().equals(editObject.getId());
                        }
                        else{
                            return false;
                        }
                    }).toList();
            UI.getCurrent().access(() -> {
                selected.forEach(v -> {
                    grid.getSelectionModel().select(v);
                });
            });
            //UI.getCurrent().push(); // Kliens-szerver szinkronizálás kényszerítése
        }
    }

    @Getter
    @NeedCleanCoding
public class OrderElementVO extends BaseVO {

        @NotNull
        private OrderElement original;
        private String product;
        private String order;
        private String taxKey;
        private Integer unit;
        private Integer unitNetPrice;
        private Integer netPrice;
        private Integer grossPrice;

        public OrderElementVO(OrderElement orderElement) {
            super(orderElement.id, orderElement.getDeleted());
            this.original = orderElement;
            this.product = original.getProduct().getName();
            this.order = original.getOrder() == null ? "" : original.getOrder().getName();
            this.unit = original.getUnit();
            this.unitNetPrice = original.getUnitNetPrice();
            this.taxKey = original.getTaxKey().getName() + "%";
            this.netPrice = original.getNetPrice();
            this.grossPrice = original.getGrossPrice();
        }
    }
}
