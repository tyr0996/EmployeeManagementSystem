package hu.martin.ems.repository;

import hu.martin.ems.core.repository.BaseRepository;
import hu.martin.ems.model.Currency;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Repository
@Transactional
public interface CurrencyRepository extends BaseRepository<Currency, Long> {
    @Query("SELECT c FROM Currency c WHERE c.validDate = :date")
    Currency findByDate(@Param("date") LocalDate date);
}
