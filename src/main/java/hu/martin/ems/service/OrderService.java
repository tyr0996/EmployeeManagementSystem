package hu.martin.ems.service;

import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.documentmodel.CustomerDM;
import hu.martin.ems.documentmodel.OrderDM;
import hu.martin.ems.documentmodel.OrderElementDM;
import hu.martin.ems.model.Order;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class OrderService extends BaseService<Order, OrderRepository> {
    public OrderService(OrderRepository orderRepository) {
        super(orderRepository);
    }

    public List<OrderElement> getOrderElements(Order o) {
        return this.repo.getOrderElements(o.getId());
    }

    @Override
    public Order saveOrUpdate(Order o) {
        if ((o.getCustomer() != null && o.getSupplier() == null) || (o.getCustomer() == null || o.getSupplier() != null)) {
            return super.saveOrUpdate(o);
        } else {
            return null;
        }
    }

    public void writeAsOdt(Order o, OutputStream out) {
        Locale.setDefault(new Locale("hu"));
        try {
            InputStream in = getTemplate(new OrderDM(o));
            IXDocReport report = XDocReportRegistry.getRegistry().
                    loadReport(in, TemplateEngineKind.Freemarker);

            FieldsMetadata metadata = report.createFieldsMetadata();
            metadata.load("r", OrderElementDM.class, true);
            IContext ctx = report.createContext();
            ctx.put("order", new OrderDM(o));
            ctx.put("r", getOrderElements(o).stream().map(v -> new OrderElementDM(v)).toList());
            ctx.put("to", new CustomerDM(o.getCustomer()));

            report.process(ctx, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static InputStream getTemplate(OrderDM order) {
        if (order.getTemplate() == null) {
            return getDefaultTemplate();
        } else {
            return new ByteArrayInputStream(order.getTemplate());
        }
    }

    public static InputStream getDefaultTemplate() {
        return Order.class.getResourceAsStream("Empty.odt");
    }
}
