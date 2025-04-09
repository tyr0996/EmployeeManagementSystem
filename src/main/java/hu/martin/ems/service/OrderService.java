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
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.file.XLSX;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.core.service.DataConverter;
import hu.martin.ems.core.sftp.SftpSender;
import hu.martin.ems.documentmodel.CustomerDM;
import hu.martin.ems.documentmodel.OrderDM;
import hu.martin.ems.documentmodel.OrderElementDM;
import hu.martin.ems.exception.CurrencyException;
import hu.martin.ems.model.Order;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.repository.OrderRepository;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@NeedCleanCoding
public class OrderService extends BaseService<Order, OrderRepository> {
    public OrderService(OrderRepository orderRepository){
        super(orderRepository);
        registry = XDocReportRegistry.getRegistry();
        xlsx = new XLSX();
    }

//    public List<OrderElement> getOrderElements(Long orderId) {
//        return this.repo.getOrderElements(orderId);
//    }

    @Autowired
    private CurrencyService currencyService;

    @Setter
    @Autowired
    private SftpSender sender;

    @Setter
    public XDocReportRegistry registry;

    @Setter
    @Getter
    XLSX xlsx;

    public List<Order> getOrdersBetween(LocalDate from, LocalDate to){
        LocalDateTime ldtFrom = LocalDateTime.of(from, LocalTime.of(0, 0, 0, 0));
        LocalDateTime ldtTo = LocalDateTime.of(to, LocalTime.of(23, 59, 59, 999999999));

        return this.repo.getOrdersByTimeOfOrderBetween(ldtFrom, ldtTo);
    }

    public List<Order> getOrdersAt(LocalDate date){ return getOrdersBetween(date, date); }

    public InputStream getTemplate(OrderDM order) {
        if (order.getTemplate() == null) {
            return getDefaultTemplate();
//            return null;
        } else {
            return new ByteArrayInputStream(order.getTemplate());
        }
    }

    @Nullable
    public static InputStream getDefaultTemplate() {
        return Order.class.getResourceAsStream("Empty.odt");
    }

    public byte[] createDocumentAsPDF(Order o, OutputStream out) throws IOException, XDocReportException {
        return getOrderDocumentExport(o, out, "PDF");
    }

    public byte[] createDocumentAsODT(Order o, OutputStream out) throws IOException, XDocReportException {
        return getOrderDocumentExport(o, out, "ODT");
    }

    public boolean sendReportSFTPToAccountant(LocalDate from, LocalDate to) throws IOException {
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
                for(OrderElement oe : order.getOrderElements()){
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

        byte[] res = xlsx.createExcelFile(sheetNames, tableDatas);
        boolean sent = sender.send(res,  "orders_" + from + "_" + to + ".xlsx");
        return sent;
    }



    public byte[] getOrderDocumentExport(Order o, OutputStream out, String fileType) throws IOException, XDocReportException { //TODO FileType can be enum
        InputStream in = getTemplate(new OrderDM(o));
        IXDocReport report = registry.loadReport(in, TemplateEngineKind.Freemarker);

        FieldsMetadata metadata = report.createFieldsMetadata();
        metadata.load("r", OrderElementDM.class, true);
        IContext ctx = report.createContext();
        OrderDM order = new OrderDM(o);
        String currency = o.getCurrency().getName();
        ctx.put("r", o.getOrderElements().stream().map(v -> {
            Double unitNetPrice = null;
            Double netPrice = null;
            Double tax = null;
            Double grossPrice = null;
            try {
                netPrice = currencyService.convert(v.getProduct().getSellingPriceCurrency().getName(), currency, v.getNetPrice().doubleValue());
                tax = currencyService.convert(v.getProduct().getSellingPriceCurrency().getName(), currency, v.getTaxPrice().doubleValue());
                grossPrice = currencyService.convert(v.getProduct().getSellingPriceCurrency().getName(), currency, v.getGrossPrice().doubleValue());
                unitNetPrice = currencyService.convert(v.getProduct().getSellingPriceCurrency().getName(), currency, v.getUnitNetPrice().doubleValue());
            } catch (CurrencyException e) {
                throw new RuntimeException(e);
            }
            order.addToTotalGross(grossPrice);
            return new OrderElementDM(v).extend(unitNetPrice, netPrice, tax, grossPrice, currency);
        }).toList());
        ctx.put("order", order);
        ctx.put("to", new CustomerDM(o.getCustomer()));
        if(fileType.equals("PDF")){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            report.convert(ctx, Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.ODFDOM), baos);
            out.write(baos.toByteArray());
            return baos.toByteArray();
        }
        else if(fileType.equals("ODT")){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            report.process(ctx, baos);
            out.write(baos.toByteArray());
            return baos.toByteArray();
        }
        return null;
    }

    public String generateHTMLEmail(Long orderId){
        Order o = findById(orderId);
        String customerName = o.getCustomer().getName();
        String orderNumber = o.getId().toString();
        String orderDate = o.getTimeOfOrder().toString();

        String htmlContent =
            "<!DOCTYPE html>\n"
            + "<html lang=\"hu\">\n"
            + "<head>\n"
            + "    <meta charset=\"UTF-8\">\n"
            + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
            + "    <title>Megrendelési értesítés</title>\n"
            + "    <style>\n"
            + "        body {\n"
            + "            font-family: Arial, sans-serif;\n"
            + "            line-height: 1.6;\n"
            + "            background-color: #f4f4f4;\n"
            + "            padding: 20px;\n"
            + "        }\n"
            + "        .container {\n"
            + "            max-width: 600px;\n"
            + "            margin: 0 auto;\n"
            + "            background-color: #ffffff;\n"
            + "            padding: 30px;\n"
            + "            border-radius: 8px;\n"
            + "            box-shadow: 0 0 10px rgba(0,0,0,0.1);\n"
            + "        }\n"
            + "        h2 {\n"
            + "            color: #333333;\n"
            + "        }\n"
            + "        p {\n"
            + "            color: #666666;\n"
            + "        }\n"
            + "        .footer {\n"
            + "            margin-top: 20px;\n"
            + "            text-align: center;\n"
            + "            color: #999999;\n"
            + "        }\n"
            + "    </style>\n"
            + "</head>\n"
            + "<body>\n"
            + "    <div class=\"container\">\n"
            + "        <h2>Megrendelési értesítés</h2>\n"
            + "        <p>Kedves " + customerName + "!</p>\n"
            + "        <p>Ezúton értesítjük, hogy sikeresen fogadtuk megrendelését az alábbi részletekkel:</p>\n"
            + "            <strong>Megrendelés száma:</strong> " + orderNumber + "\n"
            + "            <strong>Dátum:</strong> " + orderDate + "\n"
            + "        <p>A rendelésének részletes leírását a mellékelt PDF fájl tartalmazza.</p>"
            + "        <p>Köszönjük, hogy minket választott! Kérdése esetén forduljon hozzánk bizalommal.</p>\n"
            + "        <div class=\"footer\">\n"
            + "            <p>Ez egy automatikus értesítés, kérjük ne válaszoljon erre az e-mailre.</p>\n"
            + "        </div>\n"
            + "    </div>\n"
            + "</body>\n"
            + "</html>";
        return htmlContent;
    }
}

