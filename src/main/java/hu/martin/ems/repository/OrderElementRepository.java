package hu.martin.ems.repository;

import hu.martin.ems.core.repository.BaseRepository;
import hu.martin.ems.model.OrderElement;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface OrderElementRepository extends BaseRepository<OrderElement, Long> {
}
