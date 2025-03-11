package hu.martin.ems.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.controller.AdminToolsController;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@NeedCleanCoding
public class AdminToolsService {

    private final ApplicationContext applicationContext;

    private final Logger logger = LoggerFactory.getLogger(AdminToolsController.class);

    @Autowired
    public AdminToolsService(@Qualifier("setupWebContext") ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void clearAllDatabaseTable() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider serviceProvider = new ClassPathScanningCandidateComponentProvider(false);
        serviceProvider.addIncludeFilter(new AssignableTypeFilter(BaseService.class));
        Set<BeanDefinition> services = serviceProvider.findCandidateComponents("hu/martin/ems/service");
        clearDatabaseTables(services.stream().toList());
    }

    private void clearDatabaseTables(List<BeanDefinition> services) throws ClassNotFoundException {
        for(int i = 0; i < services.size(); i++){
            BaseService service = (BaseService) BeanProvider.getBean(Class.forName(services.get(i).getBeanClassName()));
            System.out.println("Kiskutya");
//            service.clearDatabaseTable();
        }
    }
}
