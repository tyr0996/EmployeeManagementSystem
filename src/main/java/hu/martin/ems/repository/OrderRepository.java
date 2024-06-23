package hu.martin.ems.repository;

import hu.martin.ems.core.repository.BaseRepository;
import hu.martin.ems.model.Order;
import hu.martin.ems.model.OrderElement;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public interface OrderRepository extends BaseRepository<Order, Long> {
    @Query("SELECT oe FROM OrderElement oe WHERE " +
            "oe.order.id = :orderId")
    List<OrderElement> getOrderElements(@Param("orderId") Long orderId);

    @Query("SELECT o FROM Order o WHERE o.deleted = 0 AND o.timeOfOrder BETWEEN :from AND :to")
    List<Order> getOrdersByTimeOfOrderBetween(LocalDateTime from, LocalDateTime to);
}
