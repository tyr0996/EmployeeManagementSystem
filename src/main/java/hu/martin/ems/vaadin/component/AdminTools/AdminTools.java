package hu.martin.ems.vaadin.component.AdminTools;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.EmsApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "adminTools", layout = MainView.class)
@AnonymousAllowed
@NeedCleanCoding
public class AdminTools extends VerticalLayout {

    @Autowired
    private ApplicationContext applicationContext;

    public AdminTools(){
        Button b = new Button("b");
        add(b);

        b.addClickListener(v -> {
            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
            provider.addIncludeFilter(new AssignableTypeFilter(EmsApiClient.class));

            Set<BeanDefinition> components = provider.findCandidateComponents("hu/martin/ems/vaadin/api");
            for (BeanDefinition component : components)
            {
                try {
                    Class apiClient = Class.forName(component.getBeanClassName());
                    Object o = apiClient.getDeclaredConstructor().newInstance();
                    applicationContext.getAutowireCapableBeanFactory().autowireBean(o);
                    Method m = apiClient.getMethod("clearDatabaseTable");
                    m.invoke(o);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                // use class cls found
            }
        });
    }
}
