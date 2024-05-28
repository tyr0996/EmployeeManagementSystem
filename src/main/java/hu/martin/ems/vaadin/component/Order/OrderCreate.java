package hu.martin.ems.vaadin.component.Order;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.model.Customer;
import hu.martin.ems.model.Order;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.service.OrderService;
import hu.martin.ems.vaadin.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Objects;

@Route(value = "order/create", layout = MainView.class)
public class OrderCreate extends VerticalLayout {

    public static Order o;
    private final OrderService orderService;

    @Autowired
    public OrderCreate(OrderService orderService) {
        this.orderService = orderService;
        FormLayout formLayout = new FormLayout();

        MultiSelectComboBox<OrderElement> orderElementss = new MultiSelectComboBox<>("Order elements");
        ComboBox.ItemFilter<OrderElement> orderElementsFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        orderElementss.setItems(orderElementsFilter, new ArrayList<>()); //TODO add here a list
        orderElementss.setItemLabelGenerator(OrderElement::getName);
        
        ComboBox<CodeStore> states = new ComboBox<>("State");
        ComboBox.ItemFilter<CodeStore> stateFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        states.setItems(stateFilter);
        states.setItemLabelGenerator(CodeStore::getName);
        
        DateTimePicker timeOfOrderField = new DateTimePicker("Time of order");
        
        ComboBox<Customer> customers = new ComboBox<>("Customer");
        ComboBox.ItemFilter<Customer> customerFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        customers.setItems(customerFilter);
        customers.setItemLabelGenerator(Customer::getName);
        
        ComboBox<CodeStore> paymentTypes = new ComboBox<>("Payment type");
        ComboBox.ItemFilter<CodeStore> paymentTypeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        paymentTypes.setItems(paymentTypeFilter);
        paymentTypes.setItemLabelGenerator(CodeStore::getName);
        
        Button saveButton = new Button("Save");

        if(o != null){
            states.setValue(o.getState());
            timeOfOrderField.setValue(o.getTimeOfOrder());
            customers.setValue(o.getCustomer());
            paymentTypes.setValue(o.getPaymentType());
        }

        saveButton.addClickListener(event -> {
            Order order = Objects.requireNonNullElseGet(o, Order::new);
            order.setState(states.getValue());
            order.setTimeOfOrder(timeOfOrderField.getValue());
            order.setCustomer(customers.getValue());
            order.setPaymentType(paymentTypes.getValue());
            order.setDeleted(0L);
            this.orderService.saveOrUpdate(order);

            Notification.show("Order saved: " + order);
            orderElementss.clear();
            states.clear();
            timeOfOrderField.clear();
            customers.clear();
            paymentTypes.clear();
        });

        formLayout.add(orderElementss, states, timeOfOrderField, customers, paymentTypes, saveButton);
        add(formLayout);
    }
}
