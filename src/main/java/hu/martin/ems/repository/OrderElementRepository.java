package hu.martin.ems.repository;

import hu.martin.ems.core.repository.BaseRepository;
import hu.martin.ems.model.OrderElement;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface OrderElementRepository extends BaseRepository<OrderElement, Long> {


    @Query("SELECT oe FROM OrderElement oe " +
            "WHERE oe.customer.id = :customerId")
    List<OrderElement> getByCustomer(@Param("customerId") Long customerId);

    @Modifying
    @Query("UPDATE OrderElement oe " +
            "SET oe.deleted = 2 " +
            "WHERE oe.orderObject.id = :orderId")
    void customPermanentlyDeleteByOrder(@Param("orderId") Long orderId);

    @Query("SELECT oe FROM OrderElement oe " +
            "WHERE oe.supplier.id = :supplierId")
    List<OrderElement> getBySupplier(Long supplierId);
}
