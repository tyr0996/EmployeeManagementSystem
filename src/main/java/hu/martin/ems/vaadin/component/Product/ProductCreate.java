package hu.martin.ems.vaadin.component.Product;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.model.Product;
import hu.martin.ems.service.CodeStoreService;
import hu.martin.ems.service.ProductService;
import hu.martin.ems.vaadin.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "product/create", layout = MainView.class)
public class ProductCreate extends VerticalLayout {

    public static Product p;
    private final ProductService productService;
    private final CodeStoreService codeStoreService;

    @Autowired
    public ProductCreate(ProductService productService,
                         CodeStoreService codeStoreService) {
        this.productService = productService;
        this.codeStoreService = codeStoreService;

        FormLayout formLayout = new FormLayout();

        TextField nameField = new TextField("Name");

        NumberField buyingPriceNetField = new NumberField("Buying price net");

        ComboBox<CodeStore> buyingPriceCurrencys = new ComboBox<>("Buying price currency");
        ComboBox.ItemFilter<CodeStore> buyingPriceCurrencyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        buyingPriceCurrencys.setItems(buyingPriceCurrencyFilter, codeStoreService.getChildren(StaticDatas.CURRENCIES_CODESTORE_ID));
        buyingPriceCurrencys.setItemLabelGenerator(CodeStore::getName);

        NumberField sellingPriceNetField = new NumberField("Selling price net");

        ComboBox<CodeStore> sellingPriceCurrencys = new ComboBox<>("Selling price currency");
        ComboBox.ItemFilter<CodeStore> sellingPriceCurrencyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        sellingPriceCurrencys.setItems(sellingPriceCurrencyFilter, codeStoreService.getChildren(StaticDatas.CURRENCIES_CODESTORE_ID));
        sellingPriceCurrencys.setItemLabelGenerator(CodeStore::getName);

        ComboBox<CodeStore> taxKeys = new ComboBox<>("Tax key");
        ComboBox.ItemFilter<CodeStore> taxKeyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        sellingPriceCurrencys.setItems(taxKeyFilter, codeStoreService.getChildren(StaticDatas.TAXKEYS_CODESTORE_ID));
        sellingPriceCurrencys.setItemLabelGenerator(CodeStore::getName);

        ComboBox<CodeStore> amountUnits = new ComboBox<>("Amount unit");
        ComboBox.ItemFilter<CodeStore> amountUnitFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        amountUnits.setItems(amountUnitFilter, codeStoreService.getChildren(StaticDatas.AMOUNTUNITS_CODESTORE_ID));
        amountUnits.setItemLabelGenerator(CodeStore::getName);

        NumberField amountField = new NumberField("Amount");

        Button saveButton = new Button("Save");

        if (p != null) {
            nameField.setValue(p.getName());
            buyingPriceNetField.setValue(p.getBuyingPriceNet());
            buyingPriceCurrencys.setValue(p.getBuyingPriceCurrency());
            sellingPriceNetField.setValue(p.getSellingPriceNet());
            sellingPriceCurrencys.setValue(p.getSellingPriceCurrency());
            amountUnits.setValue(p.getAmountUnit());
            amountField.setValue(p.getAmount());
            taxKeys.setValue(p.getTaxKey());
        }

        saveButton.addClickListener(event -> {
            Product product = Objects.requireNonNullElseGet(p, Product::new);
            product.setName(nameField.getValue());
            product.setBuyingPriceNet(buyingPriceNetField.getValue());
            product.setBuyingPriceCurrency(buyingPriceCurrencys.getValue());
            product.setSellingPriceNet(sellingPriceNetField.getValue());
            product.setSellingPriceCurrency(sellingPriceCurrencys.getValue());
            product.setAmountUnit(amountUnits.getValue());
            product.setAmount(amountField.getValue());
            product.setDeleted(0L);
            product.setTaxKey(taxKeys.getValue());
            this.productService.saveOrUpdate(product);

            Notification.show("Product saved: " + product);
            nameField.clear();
            buyingPriceNetField.clear();
            buyingPriceCurrencys.clear();
            sellingPriceNetField.clear();
            sellingPriceCurrencys.clear();
            amountUnits.clear();
            amountField.clear();
            taxKeys.clear();
        });

        formLayout.add(nameField, buyingPriceNetField, buyingPriceCurrencys, sellingPriceNetField, sellingPriceCurrencys, amountUnits, amountField, taxKeys, saveButton);
        add(formLayout);
    }
}
