package hu.martin.ems.vaadin.api;

import hu.martin.ems.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductApiClient extends EmsApiClient<Product> {
    public ProductApiClient() {
        super(Product.class);
    }
}
