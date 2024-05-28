package hu.martin.ems.service;

import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.repository.OrderElementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderElementService extends BaseService<OrderElement, OrderElementRepository>{
    public OrderElementService(OrderElementRepository orderElementRepository) { super(orderElementRepository); }
}
