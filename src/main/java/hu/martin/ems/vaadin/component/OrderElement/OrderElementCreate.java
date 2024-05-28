package hu.martin.ems.vaadin.component.OrderElement;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.model.Order;
import hu.martin.ems.model.CodeStore;
import java.util.ArrayList;
import hu.martin.ems.model.Product;
import com.vaadin.flow.component.combobox.ComboBox;
import hu.martin.ems.service.OrderElementService;
import hu.martin.ems.vaadin.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Objects;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.NumberField;

@Route(value = "orderElement/create", layout = MainView.class)
public class OrderElementCreate extends VerticalLayout {

    public static OrderElement o;
    private final OrderElementService orderElementService;

    @Autowired
    public OrderElementCreate(OrderElementService orderElementService) {
        this.orderElementService = orderElementService;
        FormLayout formLayout = new FormLayout();

        ComboBox<Product> products = new ComboBox<>("Product");
        ComboBox.ItemFilter<Product> productFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        products.setItems(productFilter);
        products.setItemLabelGenerator(Product::getName);
        
        ComboBox<Order> orders = new ComboBox<>("Order");
        ComboBox.ItemFilter<Order> orderFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        orders.setItems(orderFilter);
        orders.setItemLabelGenerator(Order::getName);
        
        NumberField unitField = new NumberField("Unit");
        
        NumberField unitNetPriceField = new NumberField("Unit net price");
        
        ComboBox<CodeStore> taxKeys = new ComboBox<>("Tax key");
        ComboBox.ItemFilter<CodeStore> taxKeyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        taxKeys.setItems(taxKeyFilter);
        taxKeys.setItemLabelGenerator(CodeStore::getName);
        
        NumberField netPriceField = new NumberField("Net price");
        
        NumberField grossPriceField = new NumberField("Gross price");
        
        Button saveButton = new Button("Save");

        if(o != null){
            products.setValue(o.getProduct());
            orders.setValue(o.getOrder());
            unitField.setValue(o.getUnit().doubleValue());
            unitNetPriceField.setValue(o.getUnitNetPrice().doubleValue());
            taxKeys.setValue(o.getTaxKey());
            netPriceField.setValue(o.getNetPrice().doubleValue());
            grossPriceField.setValue(o.getGrossPrice().doubleValue());
        }

        saveButton.addClickListener(event -> {
            OrderElement orderElement = Objects.requireNonNullElseGet(o, OrderElement::new);
            orderElement.setProduct(products.getValue());
            orderElement.setOrder(orders.getValue());
            orderElement.setUnit(unitField.getValue().intValue());
            orderElement.setUnitNetPrice(unitNetPriceField.getValue().intValue());
            orderElement.setTaxKey(taxKeys.getValue());
            orderElement.setNetPrice(netPriceField.getValue().intValue());
            orderElement.setGrossPrice(grossPriceField.getValue().intValue());
            orderElement.setDeleted(0L);
            this.orderElementService.saveOrUpdate(orderElement);

            Notification.show("OrderElement saved: " + orderElement);
            products.clear();
            orders.clear();
            unitField.clear();
            unitNetPriceField.clear();
            taxKeys.clear();
            netPriceField.clear();
            grossPriceField.clear();
        });

        formLayout.add(products, orders, unitField, unitNetPriceField, taxKeys, netPriceField, grossPriceField, saveButton);
        add(formLayout);
    }
}
