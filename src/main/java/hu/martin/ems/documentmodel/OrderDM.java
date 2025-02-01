package hu.martin.ems.documentmodel;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.model.Order;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@NeedCleanCoding
public class OrderDM extends AbstractDM {
    private String total;
    private String timeOfOrder;

    public OrderDM(Order o) {
        super("Order.odt");
        this.total = "0.0 " + o.getCurrency().getName();
        this.timeOfOrder = o.getTimeOfOrder().format(DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm:ss"));
    }

    public void addToTotalGross(Double amount){
        String[] price = this.total.split(" ");
        this.total = Double.valueOf(amount + Double.parseDouble(price[0])) + " " + price[1];
    }
}
