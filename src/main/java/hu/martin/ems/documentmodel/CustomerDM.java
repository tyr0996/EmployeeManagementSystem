package hu.martin.ems.documentmodel;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.model.Customer;
import lombok.Getter;

@Getter
@NeedCleanCoding
public class CustomerDM extends AbstractDM {
    private String name;

    public CustomerDM(Customer c) {
        super(null);
        this.name = c.getName() + " (" + c.getAddress().getName() + ")";
    }
}
