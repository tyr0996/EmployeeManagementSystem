package hu.martin.ems.service;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class OrderService extends BaseService<Order, OrderRepository> {
    public OrderService(OrderRepository orderRepository,
                        CurrencyService currencyService) {
        super(orderRepository);
        this.currencyService = currencyService;
    }

    public List<OrderElement> getOrderElements(Order o) {
        return this.repo.getOrderElements(o.getId());
    }

    private final CurrencyService currencyService;

    @Override
    public Order saveOrUpdate(Order o) {
        if ((o.getCustomer() != null && o.getSupplier() == null) || (o.getCustomer() == null || o.getSupplier() != null)) {
            return super.saveOrUpdate(o);
        } else {
            return null;
        }
    }

    public void writeAsOdt(Order o, OutputStream out) {
        try {
            getReport(o, out, "ODT");
        } catch (IOException | XDocReportException e) {
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

    public void writeAsPdf(Order o, OutputStream out) {
        try {
            getReport(o, out, "PDF");
        } catch (IOException | XDocReportException e) {
            throw new RuntimeException(e);
        }
    }


    private void getReport(Order o, OutputStream out, String fileType) throws IOException, XDocReportException { //TODO FileType can be enum
        InputStream in = getTemplate(new OrderDM(o));
        IXDocReport report = XDocReportRegistry.getRegistry().
                loadReport(in, TemplateEngineKind.Freemarker);

        FieldsMetadata metadata = report.createFieldsMetadata();
        metadata.load("r", OrderElementDM.class, true);
        IContext ctx = report.createContext();
        OrderDM order = new OrderDM(o);
        String currency = o.getCurrency().getName();
        ctx.put("r", getOrderElements(o).stream().map(v -> {
            Double unitNetPrice = currencyService.convert(LocalDate.now(), v.getProduct().getSellingPriceCurrency().getName(), currency, v.getUnitNetPrice().doubleValue());
            Double netPrice = currencyService.convert(LocalDate.now(), v.getProduct().getSellingPriceCurrency().getName(), currency, v.getNetPrice().doubleValue());
            Double tax = currencyService.convert(LocalDate.now(), v.getProduct().getSellingPriceCurrency().getName(), currency, v.getTaxPrice().doubleValue());
            Double grossPrice = currencyService.convert(LocalDate.now(), v.getProduct().getSellingPriceCurrency().getName(), currency, v.getGrossPrice().doubleValue());
            order.addToTotalGross(grossPrice);
            return new OrderElementDM(v).extend(unitNetPrice, netPrice, tax, grossPrice, currency);
        }).toList());
        ctx.put("order", order);
        ctx.put("to", new CustomerDM(o.getCustomer()));
        if(fileType.equals("PDF")){
            Options options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.ODFDOM);

            report.convert(ctx, options, out);
            out.close();
        }
        else if(fileType.equals("ODT")){
            report.process(ctx, out);
        }
    }
}
