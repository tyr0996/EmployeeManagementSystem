package hu.martin.ems.vaadin.component.OrderElement;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.model.Order;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.model.Product;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.OrderElementApiClient;
import hu.martin.ems.vaadin.component.Creatable;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@Route(value = "orderElement/list", layout = MainView.class)
@CssImport("./styles/ButtonVariant.css")
@AnonymousAllowed
@CssImport("./styles/grid.css")
public class OrderElementList extends VerticalLayout implements Creatable<OrderElement> {

    private final OrderElementApiClient orderElementApi = BeanProvider.getBean(OrderElementApiClient.class);
    private boolean showDeleted = false;
    private PaginatedGrid<OrderElementVO, String> grid;
    private final PaginationSetting paginationSetting;

    @Autowired
    public OrderElementList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;

        this.grid = new PaginatedGrid<>(OrderElementVO.class);
        List<OrderElement> orderElements = orderElementApi.findAll();
        List<OrderElementVO> data = orderElements.stream().map(OrderElementVO::new).collect(Collectors.toList());
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");
        this.grid.removeColumnByKey("id");
        this.grid.removeColumnByKey("deleted");
        grid.addClassName("styling");
        grid.setPartNameGenerator(orderElementVO -> orderElementVO.getDeleted() != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        //region Options column
        this.grid.addComponentColumn(orderElement -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog d = getSaveOrUpdateDialog(orderElement.original);
                d.open();
            });

            restoreButton.addClickListener(event -> {
                this.orderElementApi.restore(orderElement.getOriginal());
                Notification.show("OrderElement restored: " + orderElement.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.orderElementApi.delete(orderElement.getOriginal());
                Notification.show("OrderElement deleted: " + orderElement.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.orderElementApi.permanentlyDelete(orderElement.getOriginal().getId());
                Notification.show("OrderElement permanently deleted: " + orderElement.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (orderElement.getOriginal().getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else if (orderElement.getOriginal().getDeleted() == 1) {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        }).setHeader("Options");

        //endregion

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, grid);
    }

    private void updateGridItems() {
        List<OrderElement> orderElements = showDeleted ? orderElementApi.findAllWithDeleted() : orderElementApi.findAll();
        this.grid.setItems(orderElements.stream().map(OrderElementVO::new).collect(Collectors.toList()));
    }

    public Dialog getSaveOrUpdateDialog(OrderElement entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " order element");
        FormLayout formLayout = new FormLayout();

        ComboBox<Product> products = new ComboBox<>("Product");
        ComboBox.ItemFilter<Product> productFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        products.setItems(productFilter);
        products.setItemLabelGenerator(Product::getName);

        ComboBox<Order> orders = new ComboBox<>("Order");
        ComboBox.ItemFilter<Order> orderFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        orders.setItems(orderFilter);
        orders.setItemLabelGenerator(Order::getName);

        NumberField unitField = new NumberField("Unit");

        NumberField unitNetPriceField = new NumberField("Unit net price");

        ComboBox<CodeStore> taxKeys = new ComboBox<>("Tax key");
        ComboBox.ItemFilter<CodeStore> taxKeyFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        taxKeys.setItems(taxKeyFilter);
        taxKeys.setItemLabelGenerator(CodeStore::getName);

        NumberField netPriceField = new NumberField("Net price");

        NumberField grossPriceField = new NumberField("Gross price");

        Button saveButton = new Button("Save");

        if (entity != null) {
            products.setValue(entity.getProduct());
            orders.setValue(entity.getOrder());
            unitField.setValue(entity.getUnit().doubleValue());
            unitNetPriceField.setValue(entity.getUnitNetPrice().doubleValue());
            taxKeys.setValue(entity.getTaxKey());
            netPriceField.setValue(entity.getNetPrice().doubleValue());
            grossPriceField.setValue(entity.getGrossPrice().doubleValue());
        }

        saveButton.addClickListener(event -> {
            OrderElement orderElement = Objects.requireNonNullElseGet(entity, OrderElement::new);
            orderElement.setProduct(products.getValue());
            orderElement.setOrder(orders.getValue());
            orderElement.setUnit(unitField.getValue().intValue());
            orderElement.setUnitNetPrice(unitNetPriceField.getValue().intValue());
            orderElement.setTaxKey(taxKeys.getValue());
            orderElement.setNetPrice(netPriceField.getValue().intValue());
            orderElement.setGrossPrice(grossPriceField.getValue().intValue());
            orderElement.setDeleted(0L);
            if(entity != null){
                orderElementApi.update(orderElement);
            }
            else{
                orderElementApi.save(orderElement);
            }

            Notification.show("OrderElement saved: " + orderElement)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            products.clear();
            orders.clear();
            unitField.clear();
            unitNetPriceField.clear();
            taxKeys.clear();
            netPriceField.clear();
            grossPriceField.clear();
            createDialog.close();
        });

        formLayout.add(products, orders, unitField, unitNetPriceField, taxKeys, netPriceField, grossPriceField, saveButton);
        createDialog.add(formLayout);
        return createDialog;
    }

    @Getter
    public class OrderElementVO {
        private OrderElement original;
        private Long deleted;
        private Long id;
        private String product;
        private String order;
        private String taxKey;
        private Integer unit;
        private Integer unitNetPrice;
        private Integer netPrice;
        private Integer grossPrice;

        public OrderElementVO(OrderElement orderElement) {
            this.original = orderElement;
            this.id = orderElement.getId();
            this.deleted = orderElement.getDeleted();
            this.product = original.getProduct().getName();
            this.order = original.getOrder() == null ? "" : original.getOrder().getName();
            this.unit = original.getUnit();
            this.unitNetPrice = original.getUnitNetPrice();
            this.taxKey = original.getTaxKey().getName() + "%";
            this.netPrice = original.getNetPrice();
            this.grossPrice = original.getGrossPrice();
        }
    }
}
