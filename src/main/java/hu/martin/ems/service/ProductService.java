package hu.martin.ems.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Product;
import hu.martin.ems.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
@NeedCleanCoding
public class ProductService extends BaseService<Product, ProductRepository> {
    public ProductService(ProductRepository productRepository) {
        super(productRepository);
    }
}
