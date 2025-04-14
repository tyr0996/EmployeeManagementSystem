package hu.martin.ems.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.repository.OrderElementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@NeedCleanCoding
public class OrderElementService extends BaseService<OrderElement, OrderElementRepository> {
    public OrderElementService(OrderElementRepository orderElementRepository) {
        super(orderElementRepository);
    }

    public List<OrderElement> getByCustomer(Long customerId) {
        return this.repo.getByCustomer(customerId);
    }

    public void permanentlyDeleteByOrder(Long orderId){
        this.repo.customPermanentlyDeleteByOrder(orderId);
    }

    public List<OrderElement> getBySupplier(Long supplierId) { return this.repo.getBySupplier(supplierId); }
}
