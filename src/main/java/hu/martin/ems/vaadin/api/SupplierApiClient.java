package hu.martin.ems.vaadin.api;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.model.Supplier;
import org.springframework.stereotype.Component;

@Component
@NeedCleanCoding
public class SupplierApiClient extends EmsApiClient<Supplier> {
    public SupplierApiClient() {
        super(Supplier.class);
    }
}
