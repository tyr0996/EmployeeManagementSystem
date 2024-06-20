package hu.martin.ems.documentmodel;

import hu.martin.ems.model.Order;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class OrderDM extends AbstractDM {
    private String total;
    private String timeOfOrder;

    public OrderDM(Order o) {
        super("Order.odt");
        this.total = ""; //TODO
        this.timeOfOrder = o.getTimeOfOrder().format(DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm:ss"));
    }
}
