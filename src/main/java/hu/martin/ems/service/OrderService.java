package hu.martin.ems.service;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.file.XLSX;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.core.sftp.SftpSender;
import hu.martin.ems.documentmodel.CustomerDM;
import hu.martin.ems.documentmodel.OrderDM;
import hu.martin.ems.documentmodel.OrderElementDM;
import hu.martin.ems.exception.CurrencyException;
import hu.martin.ems.model.Order;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.repository.OrderRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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


    public byte[] createDocumentAsPDF(Order o, OutputStream out) throws IOException, XDocReportException, CurrencyException {
        return getOrderDocumentExport(o, out, OrderDocumentFileType.PDF);
    }

    public byte[] createDocumentAsODT(Order o, OutputStream out) throws IOException, XDocReportException, CurrencyException {
        return getOrderDocumentExport(o, out, OrderDocumentFileType.ODT);
    }

    public boolean sendReportSFTPToAccountant(LocalDate from, LocalDate to) throws IOException {
        LocalDate current = from;
        List<String> sheetNames = new ArrayList<>();
        List<String[][]> tableDatas = new ArrayList<>();
        while(!current.isAfter(to)){
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
            String[][] table = convertListToArray2(lines);

            sheetNames.add(current.toString());
            tableDatas.add(table);
            current = current.plusDays(1);
        }

        byte[] res = xlsx.createExcelFile(sheetNames, tableDatas);
        return sender.send(res,  "orders_" + from + "_" + to + ".xlsx");
    }

    private String[][] convertListToArray2(List<List<String>> data) {
        String[][] array = new String[data.size()][];

        for (int i = 0; i < data.size(); i++) {
            List<String> row = data.get(i);
            array[i] = row.toArray(new String[0]);
        }

        return array;
    }



    public byte[] getOrderDocumentExport(Order o, OutputStream out, OrderDocumentFileType fileType) throws IOException, XDocReportException, CurrencyException { //TODO FileType can be enum
        InputStream in = new OrderDM(o).getTemplate();
        IXDocReport report = registry.loadReport(in, TemplateEngineKind.Freemarker);

        FieldsMetadata metadata = report.createFieldsMetadata();
        metadata.load("r", OrderElementDM.class, true);
        IContext ctx = report.createContext();
        OrderDM order = new OrderDM(o);
        String currency = o.getCurrency().getName();

        List<OrderElementDM> orderElementDMs = new ArrayList<>();
        for (OrderElement v : o.getOrderElements()) {
            String fromCurrency = v.getProduct().getSellingPriceCurrency().getName();
            Double unitNetPrice = currencyService.convert(fromCurrency, currency, v.getUnitNetPrice().doubleValue());
            Double netPrice = currencyService.convert(fromCurrency, currency, v.getNetPrice().doubleValue());
            Double tax = currencyService.convert(fromCurrency, currency, v.getTaxPrice().doubleValue());
            Double grossPrice = currencyService.convert(fromCurrency, currency, v.getGrossPrice().doubleValue());

            order.addToTotalGross(grossPrice);
            orderElementDMs.add(new OrderElementDM(v).extend(unitNetPrice, netPrice, tax, grossPrice, currency));
        }
        ctx.put("r", orderElementDMs);

        ctx.put("order", order);
        ctx.put("to", new CustomerDM(o.getCustomer()));

        fileType.generate(report, ctx, out);
//        if(fileType.equals("PDF")){
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            report.convert(ctx, Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.ODFDOM), baos);
//            out.write(baos.toByteArray());
//            return baos.toByteArray();
//        }
//        else if(fileType.equals("ODT")){
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            report.process(ctx, baos);
//            out.write(baos.toByteArray());
//            return baos.toByteArray();
//        }
        return null;
    }

    public String generateHTMLEmail(Long orderId){
        Order o = findById(orderId);
        String customerName = o.getCustomer().getName();
        String orderNumber = o.getId().toString();
        String orderDate = o.getTimeOfOrder().toString();

        String htmlContent = """
<!DOCTYPE html>
<html lang="hu">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Megrendelési értesítés</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            background-color: #f4f4f4;
            padding: 20px;
        }
        .container {
            max-width: 600px;
            margin: 0 auto;
            background-color: #ffffff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        h2 {
            color: #333333;
        }
        p {
            color: #666666;
        }
        .footer {
            margin-top: 20px;
            text-align: center;
            color: #999999;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>Megrendelési értesítés</h2>
        <p>Kedves %s!</p>
        <p>Ezúton értesítjük, hogy sikeresen fogadtuk megrendelését az alábbi részletekkel:</p>
            <strong>Megrendelés száma:</strong> %s
            <strong>Dátum:</strong> %s
        <p>A rendelésének részletes leírását a mellékelt PDF fájl tartalmazza.</p>"
            + "        <p>Köszönjük, hogy minket választott! Kérdése esetén forduljon hozzánk bizalommal.</p>
        <div class="footer">
            <p>Ez egy automatikus értesítés, kérjük ne válaszoljon erre az e-mailre.</p>
        </div>
    </div>
</body>
</html>
""";
        return String.format(htmlContent, customerName, orderNumber, orderDate);
    }
}

