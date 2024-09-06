package hu.martin.ems.controller;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.repository.CodeStoreRepository;
import hu.martin.ems.service.CodeStoreService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/codeStore")
@AnonymousAllowed
public class CodeStoreController extends BaseController<CodeStore, CodeStoreService, CodeStoreRepository> {

    public CodeStoreController(CodeStoreService service) {
        super(service);
    }
}
