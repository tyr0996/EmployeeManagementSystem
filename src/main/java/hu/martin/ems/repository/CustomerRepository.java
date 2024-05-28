package hu.martin.ems.repository;

import hu.martin.ems.core.repository.BaseRepository;
import hu.martin.ems.model.Customer;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface CustomerRepository extends BaseRepository<Customer, Long> {
}
