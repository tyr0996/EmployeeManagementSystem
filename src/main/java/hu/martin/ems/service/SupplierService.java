package hu.martin.ems.service;

import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Supplier;
import hu.martin.ems.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SupplierService extends BaseService<Supplier, SupplierRepository> {

    public SupplierService(SupplierRepository supplierRepository) {
        super(supplierRepository);
    }
}