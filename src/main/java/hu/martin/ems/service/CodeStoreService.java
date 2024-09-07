package hu.martin.ems.service;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.repository.CodeStoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@NeedCleanCoding
public class CodeStoreService extends BaseService<CodeStore, CodeStoreRepository> {
    @Autowired
    public CodeStoreService(CodeStoreRepository codeStoreRepository) {
        super(codeStoreRepository);
    }


    public List<CodeStore> getChildren(Long parentCodeStoreId) {
        return this.repo.getChildren(parentCodeStoreId);
    }

    public CodeStore findByName(String name) {
        return this.repo.findByName(name);
    }
}
