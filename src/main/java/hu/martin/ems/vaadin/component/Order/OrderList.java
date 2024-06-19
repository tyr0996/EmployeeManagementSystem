package hu.martin.ems.vaadin.component.Order;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import hu.martin.ems.core.service.DataConverter;
import hu.martin.ems.core.util.WordTemplateProcessor;
import hu.martin.ems.model.Order;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.service.CurrencyService;
import hu.martin.ems.service.OrderService;
import hu.martin.ems.vaadin.MainView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "order/list", layout = MainView.class)
public class OrderList extends VerticalLayout {

    private final OrderService orderService;
    private final CurrencyService currencyService;
    private boolean showDeleted = false;
    private Grid<OrderVO> grid;

    @Autowired
    public OrderList(OrderService orderService,
                     CurrencyService currencyService) {
        this.orderService = orderService;
        this.currencyService = currencyService;

        this.grid = new Grid<>(OrderVO.class);
        List<Order> orders = orderService.findAll(false);
        List<OrderVO> data = orders.stream().map(OrderVO::new).collect(Collectors.toList());
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");
        this.grid.removeColumnByKey("id");
        this.grid.removeColumnByKey("deleted");

        //region Options column
        this.grid.addComponentColumn(order -> {
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            Button restoreButton = new Button("Restore");
            Button permanentDeleteButton = new Button("Permanently Delete");
            Button downloadPdfButton = new Button("Download pdf");

            editButton.addClickListener(event -> {
                OrderCreate.o = order.getOriginal();
                UI.getCurrent().navigate("order/create");
            });

            restoreButton.addClickListener(event -> {
                this.orderService.restore(order.getOriginal());
                Notification.show("Order restored: " + order.getOriginal().getName());
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.orderService.delete(order.getOriginal());
                Notification.show("Order deleted: " + order.getOriginal().getName());
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.orderService.permanentlyDelete(order.getOriginal());
                Notification.show("Order permanently deleted: " + order.getOriginal().getName());
                updateGridItems();
            });

            downloadPdfButton.addClickListener(event -> {
                byte[] pdfData = generatePDFData(order);

                StreamResource resource = new StreamResource("generated.pdf", () -> new ByteArrayInputStream(pdfData));
                resource.setContentType("application/pdf");

                // Wrap the button with FileDownloadWrapper
                // FileDownloadWrapper buttonWrapper = new FileDownloadWrapper(resource);
                // buttonWrapper.wrapComponent(downloadPdfButton);
            });
            //TODO: megcsinálni, hogy letöltse a cuccot.

            HorizontalLayout actions = new HorizontalLayout();
            actions.add(downloadPdfButton);
            if (order.getOriginal().getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else if (order.getOriginal().getDeleted() == 1) {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        }).setHeader("Options");

        //endregion

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            updateGridItems();
        });

        add(showDeletedCheckbox, grid);
    }

    private byte[] generatePDFData(OrderVO order){
        HashMap<String, Object> docxData = new HashMap<>();
        docxData.put("buyerName", order.getOriginal().getCustomer().getName());
        docxData.put("buyerAddress", order.getOriginal().getCustomer().getAddress().getName());
        List<List<String>> tableData = new ArrayList<>();
        tableData.add(List.of("Termék", "Mennyiség", "Mennyiségi egység", "Nettó egységár", "Nettó ár",
                "ÁFA kulcs", "ÁFA", "Bruttó ár"));
        Double totalGross = 0.0;
        //TODO: figyelni kell, mert nem biztos, hogy van customer. Lehet, hogy supplier van, és akkor nem lesz jó.
        String currency = order.getOriginal().getCurrency().getName();
        for(OrderElement oe : orderService.getOrderElements(order.getOriginal())){
            List<String> row = List.of(oe.getName(), oe.getUnit().toString(), oe.getProduct().getAmountUnit().getName(),
                    currencyService.convert(LocalDate.now(), oe.getProduct().getSellingPriceCurrency().getName(), currency, oe.getUnitNetPrice().doubleValue()) + " " + currency,
                    currencyService.convert(LocalDate.now(), oe.getProduct().getSellingPriceCurrency().getName(), currency, oe.getNetPrice().doubleValue()) + " " + currency,
                    oe.getTaxKey().getName(),
                    currencyService.convert(LocalDate.now(), oe.getProduct().getSellingPriceCurrency().getName(), currency, oe.getTaxPrice().doubleValue()) + " " + currency,
                    currencyService.convert(LocalDate.now(), oe.getProduct().getSellingPriceCurrency().getName(), currency, oe.getGrossPrice().doubleValue()) + " " + currency
            );
            totalGross += currencyService.convert(LocalDate.now(), oe.getProduct().getSellingPriceCurrency().getName(), currency, oe.getGrossPrice().doubleValue());
            tableData.add(row);
        }
        docxData.put("table", DataConverter.convertListToArray2(tableData));
        docxData.put("totalGross", totalGross);
        docxData.put("currency", order.getOriginal().getCurrency().getName());
        byte[] documentData = new byte[0];
        try {
            documentData = WordTemplateProcessor.fillDocx("Order.docx", docxData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return documentData;
    }

    private void updateGridItems() {
        List<Order> orders = this.orderService.findAll(showDeleted);
        this.grid.setItems(orders.stream().map(OrderVO::new).collect(Collectors.toList()));
    }

    @Getter
    public class OrderVO {
        private Order original;
        private Long deleted;
        private Long id;
        private String state;
        private String customer;
        private String paymentType;
        private String timeOfOrder;

        public OrderVO(Order order) {
            this.original = order;
            this.id = order.getId();
            this.deleted = order.getDeleted();
            this.state = original.getState().getName();
            this.timeOfOrder = original.getTimeOfOrder().format(DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm:ss"));
            this.customer = original.getCustomer().getName();
            this.paymentType = original.getPaymentType().getName();
        }
    }
}
