package hu.martin.ems.vaadin.component.Order;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
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
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.StaticDatas;
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
import hu.martin.ems.vaadin.component.Employee.EmployeeList;
import hu.martin.ems.vaadin.component.OrderElement.OrderElementList;
import lombok.EqualsAndHashCode;
import com.vaadin.flow.component.UI;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route(value = "order/create", layout = MainView.class)
@AnonymousAllowed
@NeedCleanCoding
public class OrderCreate extends VerticalLayout {

    public static Order o;
    private final OrderApiClient orderApi = BeanProvider.getBean(OrderApiClient.class);
    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private final CustomerApiClient customerApi = BeanProvider.getBean(CustomerApiClient.class);
    private final OrderElementApiClient orderElementApi = BeanProvider.getBean(OrderElementApiClient.class);

    private List<OrderElementVO> orderElementVOS;
    private PaginatedGrid<OrderElementVO, String> grid;
    private Boolean showPreviously = false;

    ComboBox<CodeStore> paymentTypes;
    ComboBox<CodeStore> currencies;

    //The autowired here is need because the MainView addmenu function
    @Autowired
    public OrderCreate(PaginationSetting paginationSetting) {
        FormLayout formLayout = new FormLayout();

        grid = new PaginatedGrid<>(OrderElementVO.class);
        grid.setItems(new ArrayList<>());
        grid.removeColumnByKey("original");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.asMultiSelect();

        ComboBox<Customer> customers = new ComboBox<>("Customer");
        ComboBox.ItemFilter<Customer> customerFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        customers.setItems(customerFilter, customerApi.findAll());
        customers.setItemLabelGenerator(Customer::getName);

        customers.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                orderElementVOS = orderElementApi.getByCustomer(event.getValue()).stream().map(OrderElementVO::new).collect(Collectors.toList());
            } else {
                orderElementVOS = new ArrayList<>();
            }
            updateGridItems();
        });

        Checkbox showPreviouslyOrderedElements = new Checkbox("Show previously ordered elements");
        showPreviouslyOrderedElements.addValueChangeListener(event -> {
            showPreviously = event.getValue();
            grid.setSelectionMode(showPreviously ? Grid.SelectionMode.NONE : Grid.SelectionMode.MULTI);
            orderElementVOS = customers.getValue() == null ?
                    new ArrayList<>() :
                    orderElementApi.getByCustomer(customers.getValue()).stream().map(OrderElementVO::new).toList();
            if(o != null){
                grid.setSelectionMode(Grid.SelectionMode.MULTI);
            }
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showPreviouslyOrderedElements);
        hl.setAlignSelf(Alignment.CENTER, showPreviouslyOrderedElements);


        paymentTypes = new ComboBox<>("Payment type");
        ComboBox.ItemFilter<CodeStore> paymentTypeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        paymentTypes.setItems(paymentTypeFilter, codeStoreApi.getChildren(StaticDatas.PAYMENT_TYPES_CODESTORE_ID));
        paymentTypes.setItemLabelGenerator(CodeStore::getName);

        currencies = new ComboBox<>("Currency");
        ComboBox.ItemFilter<CodeStore> currencyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        currencies.setItems(currencyFilter, codeStoreApi.getChildren(StaticDatas.CURRENCIES_CODESTORE_ID));
        currencies.setItemLabelGenerator(CodeStore::getName);

        if(o != null){
            customers.setValue(o.getCustomer());
            showPreviouslyOrderedElements.setValue(true);
            paymentTypes.setValue(o.getPaymentType());
            currencies.setValue(o.getCurrency());
            orderElementVOS = orderElementApi.getByCustomer(o.getCustomer()).stream().map(OrderElementVO::new).collect(Collectors.toList());
            updateGridItems();
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
        }

        Button saveButton = new Button("Create order");

        saveButton.addClickListener(event -> {
            Order order = Objects.requireNonNullElseGet(o, Order::new);
            order.setState(codeStoreApi.getAllByName("Pending").get(0));
            order.setTimeOfOrder(LocalDateTime.now());
            order.setCustomer(customers.getValue());
            order.setPaymentType(paymentTypes.getValue());
            order.setDeleted(0L);
            order.setCurrency(currencies.getValue());
            if(order != null){
                order = orderApi.update(order);
            }
            else{
                order = orderApi.save(order);
            }
            for(OrderElementVO oeVo : grid.getSelectedItems()){
                OrderElement oe = oeVo.original;
                oe.setOrder(order);
                orderElementApi.save(oe);
            }
            Notification.show("Order " + (o == null ? "saved: " : "updated: ") + order)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            customers.clear();
            paymentTypes.clear();
        });

        formLayout.add(customers);
        FormLayout formLayout1 = new FormLayout();
        formLayout1.add(currencies, paymentTypes);
        formLayout1.add(saveButton);
        add(formLayout, hl, grid, formLayout1);
    }

    private Stream<OrderElementVO> getFilteredStream() {
        if(o != null){
            orderElementVOS = orderElementApi.getByCustomer(o.getCustomer()).stream().map(OrderElementVO::new).collect(Collectors.toList());
        }

        return orderElementVOS.stream().filter(orderElementVO ->
                showPreviously ? orderElementVO.original.getOrder() != null : orderElementVO.original.getOrder() == null &&
                orderElementVO.filterExtraData());
    }


    public void updateGridItems(){
        orderElementVOS = getFilteredStream().toList();
        grid.setItems(orderElementVOS);
        if(o != null){
            List<OrderElementVO> selected = orderElementVOS.stream()
                    .filter(v -> v.original.getOrder() != null && v.original.getOrder().getId() == o.getId()).toList();
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
