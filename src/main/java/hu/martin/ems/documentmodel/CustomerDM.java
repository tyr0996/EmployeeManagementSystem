package hu.martin.ems.documentmodel;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.model.Customer;
import lombok.Getter;

@Getter
@NeedCleanCoding
public class CustomerDM extends AbstractDM {
    private String name;

    public CustomerDM(Customer c) {
        super();
        this.name = c.getName() + " (" + c.getAddress().getName() + ")";
    }
}
