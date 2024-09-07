package hu.martin.ems.controller;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Product;
import hu.martin.ems.repository.ProductRepository;
import hu.martin.ems.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/product")
@NeedCleanCoding
public class ProductController extends BaseController<Product, ProductService, ProductRepository> {
    public ProductController(ProductService service) {
        super(service);
    }
}
