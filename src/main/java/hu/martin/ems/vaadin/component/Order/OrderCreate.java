package hu.martin.ems.vaadin.component.Order;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.model.Customer;
import hu.martin.ems.model.Order;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CodeStoreApiClient;
import hu.martin.ems.vaadin.api.CustomerApiClient;
import hu.martin.ems.vaadin.api.OrderApiClient;
import hu.martin.ems.vaadin.api.OrderElementApiClient;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

@Route(value = "order/create", layout = MainView.class)
@AnonymousAllowed
public class OrderCreate extends VerticalLayout {

    public static Order o;
    private final OrderApiClient orderApi = BeanProvider.getBean(OrderApiClient.class);
    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private final CustomerApiClient customerApi = BeanProvider.getBean(CustomerApiClient.class);
    private final OrderElementApiClient orderElementApi = BeanProvider.getBean(OrderElementApiClient.class);

    public OrderCreate() {
        FormLayout formLayout = new FormLayout();

        Grid<OrderElementVO> grid = new Grid<>(OrderElementVO.class);
        grid.setItems(new ArrayList<>());
        grid.removeColumnByKey("original");
        grid.removeColumnByKey("deleted");
        grid.removeColumnByKey("id");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.asMultiSelect();

        ComboBox<Customer> customers = new ComboBox<>("Customer");
        ComboBox.ItemFilter<Customer> customerFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        customers.setItems(customerFilter, customerApi.findAll());
        customers.setItemLabelGenerator(Customer::getName);

        customers.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                grid.setItems(orderElementApi.getByCustomer(event.getValue()).stream().map(OrderElementVO::new).collect(Collectors.toList()));
            } else {
                grid.setItems(new ArrayList<>());
            }
        });

        ComboBox<CodeStore> paymentTypes = new ComboBox<>("Payment type");
        ComboBox.ItemFilter<CodeStore> paymentTypeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        paymentTypes.setItems(paymentTypeFilter, codeStoreApi.getChildren(StaticDatas.PAYMENT_TYPES_CODESTORE_ID));
        paymentTypes.setItemLabelGenerator(CodeStore::getName);

        ComboBox<CodeStore> currencies = new ComboBox<>("Currency");
        ComboBox.ItemFilter<CodeStore> currencyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        currencies.setItems(currencyFilter, codeStoreApi.getChildren(StaticDatas.CURRENCIES_CODESTORE_ID));
        currencies.setItemLabelGenerator(CodeStore::getName);

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
                orderApi.update(order);
            }
            else{
                orderApi.save(order);
            }
            grid.getSelectedItems().stream().forEach(v -> {
                OrderElement oe = v.original;
                oe.setOrder(order);
                orderElementApi.save(oe);
            });
            Notification.show("Order saved: " + order)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            customers.clear();
            paymentTypes.clear();
        });

        formLayout.add(customers);
        FormLayout formLayout1 = new FormLayout();
        formLayout1.add(currencies, paymentTypes);
        formLayout1.add(saveButton);
        add(formLayout, grid, formLayout1);
    }

    @Getter
    public class OrderElementVO {
        private OrderElement original;
        private Long deleted;
        private Long id;
        private String product;
        private String order;
        private String taxKey;
        private Integer unit;
        private Integer unitNetPrice;
        private Integer netPrice;
        private Integer grossPrice;

        public OrderElementVO(OrderElement orderElement) {
            this.original = orderElement;
            this.id = orderElement.getId();
            this.deleted = orderElement.getDeleted();
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
