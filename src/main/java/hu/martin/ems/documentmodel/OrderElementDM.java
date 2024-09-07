package hu.martin.ems.documentmodel;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.model.OrderElement;
import lombok.Getter;

@Getter
@NeedCleanCoding
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
        this.unitNetPrice = null;
        this.netPrice = null;
        this.taxKey = oe.getTaxKey().getName() + "%";
        this.tax = null;
        this.grossPrice = null;
    }

    public OrderElementDM extend(Double unitNetPrice, Double netPrice, Double tax, Double grossPrice, String currency){
        this.unitNetPrice = unitNetPrice.toString() + " " + currency;
        this.netPrice = netPrice.toString() + " " + currency;
        this.tax = tax.toString() + " " + currency;
        this.grossPrice = grossPrice.toString() + " " + currency;
        return this;
    }
}
