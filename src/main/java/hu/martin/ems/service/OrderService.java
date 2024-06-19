package hu.martin.ems.service;

import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Order;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderService extends BaseService<Order, OrderRepository>{
    public OrderService(OrderRepository orderRepository) { super(orderRepository); }

    public List<OrderElement> getOrderElements(Order o){
        return this.repo.getOrderElements(o.getId());
    }

    @Override
    public Order saveOrUpdate(Order o){
        if((o.getCustomer() != null && o.getSupplier() == null) || (o.getCustomer() == null || o.getSupplier() != null)){
            return super.saveOrUpdate(o);
        }
        else{
            return null;
        }
    }
}
