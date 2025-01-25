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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
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

    public void clearAllDatabaseTable() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ClassPathScanningCandidateComponentProvider serviceProvider = new ClassPathScanningCandidateComponentProvider(false);
        serviceProvider.addIncludeFilter(new AssignableTypeFilter(BaseService.class));
        Set<BeanDefinition> services = serviceProvider.findCandidateComponents("hu/martin/ems/service");

        HashMap<Class<?>, Class<?>> pair = mapServicesWithRepositories(services);

        clearDatabaseTables(pair);
    }

    private void clearDatabaseTables(HashMap<Class<?>, Class<?>> paired) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        for(int i = 0; i < paired.size(); i++){
            Class<?> service = (Class<?>) paired.keySet().toArray()[i];
            runClearDatabaseTableOnBeanClass(service, paired.get(service));
        }
    }


    private HashMap<Class<?>, Class<?>> mapServicesWithRepositories(Set<BeanDefinition> services) throws ClassNotFoundException {
        HashMap<Class<?>, Class<?>> serviceRepositoryMap = new HashMap<>();

        for (BeanDefinition serviceDef : services) {
            Class<?> serviceClass = Class.forName(serviceDef.getBeanClassName());
            Class<?> repositoryClass = serviceClass.getConstructors()[0].getParameterTypes()[0];
            serviceRepositoryMap.put(serviceClass, repositoryClass);
        }

        return serviceRepositoryMap;
    }

    private void runClearDatabaseTableOnBeanClass(Class<?> service, Class<?> repo) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object o = null;
        try{
            o = service.getDeclaredConstructor(repo).newInstance(BeanProvider.getBean(repo));
        }
        catch (NoSuchMethodException ex){
            logger.error("Service called " + service.getSimpleName() + " has no declaredConstructor with zero arguments. Define a no-args constructor in the service class");
            return;
        }

        applicationContext.getAutowireCapableBeanFactory().autowireBean(o);
        try{
            Method m = service.getMethod("clearDatabaseTable");
            m.invoke(o);
        }
        catch(NoSuchMethodException ex){
            logger.error("clearDatabaseTable method not found in service called " + service.getSimpleName() + ". Define this method in the service class.");
        }
    }

}
