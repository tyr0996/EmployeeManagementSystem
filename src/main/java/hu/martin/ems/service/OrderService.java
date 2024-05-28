package hu.martin.ems.service;

import com.vaadin.flow.component.notification.Notification;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import hu.martin.ems.core.file.XLSX;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.core.service.DataConverter;
import hu.martin.ems.core.sftp.SftpSender;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class OrderService extends BaseService<Order, OrderRepository> {
    public OrderService(OrderRepository orderRepository,
                        CurrencyService currencyService,
                        SftpSender sender) {
        super(orderRepository);
        this.currencyService = currencyService;
        this.sender = sender;
    }

    public List<OrderElement> getOrderElements(Order o) {
        return this.repo.getOrderElements(o.getId());
    }

    private final CurrencyService currencyService;
    private final SftpSender sender;
    @Override
    public Order saveOrUpdate(Order o) {
        if ((o.getCustomer() != null && o.getSupplier() == null) || (o.getCustomer() == null || o.getSupplier() != null)) {
            return super.saveOrUpdate(o);
        } else {
            return null;
        }
    }

    public List<Order> getOrdersBetween(LocalDate from, LocalDate to){
        LocalDateTime ldtFrom = LocalDateTime.of(from, LocalTime.of(0, 0, 0, 0));
        LocalDateTime ldtTo = LocalDateTime.of(to, LocalTime.of(23, 59, 59, 999999999));

        return this.repo.getOrdersByTimeOfOrderBetween(ldtFrom, ldtTo);
    }

    public List<Order> getOrdersAt(LocalDate date){ return getOrdersBetween(date, date); }

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
            getOrderDocumentExport(o, out, "PDF");
        } catch (IOException | XDocReportException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeAsOdt(Order o, OutputStream out) {
        try {
            getOrderDocumentExport(o, out, "ODT");
        } catch (IOException | XDocReportException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendReport(LocalDate from, LocalDate to){
        LocalDate current = from;
        List<String> sheetNames = new ArrayList<>();
        List<String[][]> tableDatas = new ArrayList<>();
        while(current.isBefore(to) || current.isEqual(to)){
            List<Order> orders = getOrdersAt(current);
            String[] colNames = new String[] {"Order name", "Product", "Gross buying price", "Gross selling price", "Sold amount", "Profit"};
            List<List<String>> lines = new ArrayList<>();
            lines.add(Arrays.stream(colNames).toList());
            for (Order order : orders) {
                List<List<String>> linesOfOrder = new ArrayList<>();
                for(OrderElement oe : getOrderElements(order)){
                    List<String> line = new ArrayList<>();
                    line.add(order.getName());
                    line.add(oe.getProduct().getName());
                    Double buyingPriceGross = oe.getProduct().getBuyingPriceNet() * ((Integer.parseInt(oe.getProduct().getTaxKey().getName()) / 100.0)+ 1) * oe.getUnit();
                    Double sellingPriceGross = oe.getProduct().getSellingPriceNet() * ((Integer.parseInt(oe.getProduct().getTaxKey().getName()) / 100.0) + 1) * oe.getUnit();
                    Double profit = sellingPriceGross - buyingPriceGross;

                    line.add(buyingPriceGross + " " + oe.getProduct().getBuyingPriceCurrency().getName());
                    line.add(sellingPriceGross + " " + oe.getProduct().getSellingPriceCurrency().getName());
                    line.add((oe.getUnit() * oe.getProduct().getAmount()) + " " + oe.getProduct().getAmountUnit().getName());
                    line.add(profit.toString());
                    linesOfOrder.add(line);
                }
                lines.addAll(linesOfOrder);
            }
            String[][] table = DataConverter.convertListToArray2(lines);

            sheetNames.add(current.toString());
            tableDatas.add(table);
            current = current.plusDays(1);
        }

        byte[] res = XLSX.createExcelFile(sheetNames, tableDatas);
        boolean sent = sender.send(res,  "orders_" + from + "_" + to + ".xlsx");
        Notification.show(sent ? "Report sent" : "Report unable to send! Check logs!");
    }


    private void getOrderDocumentExport(Order o, OutputStream out, String fileType) throws IOException, XDocReportException { //TODO FileType can be enum
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
            report.convert(ctx, Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.ODFDOM), out);
            out.close();
        }
        else if(fileType.equals("ODT")){
            report.process(ctx, out);
        }
    }
}
