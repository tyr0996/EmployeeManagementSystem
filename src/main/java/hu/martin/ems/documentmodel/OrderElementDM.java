package hu.martin.ems.documentmodel;

import hu.martin.ems.model.OrderElement;
import lombok.Getter;

@Getter
public class OrderElementDM extends AbstractDM {
    private String product;
    private String amount;
    private String amountUnit;
    private String unitNetPrice;
    private String netPrice;
    private String taxKey;
    private String tax;
    private String grossPrice;

    public OrderElementDM(OrderElement oe) {
        super(null);
        this.product = oe.getProduct().getName();
        this.amount = oe.getUnit().toString();
        this.amountUnit = oe.getProduct().getAmountUnit().getName();
        this.unitNetPrice = ""; //TODO
        this.netPrice = ""; //TODO
        this.taxKey = oe.getTaxKey().getName() + "%";
        this.tax = ""; //TODO
        this.grossPrice = ""; //TODO

    }
}
