package hu.martin.ems.vaadin.component.CodeStore;

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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.service.CodeStoreService;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.component.Creatable;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.List;
import java.util.Objects;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "codestore/list", layout = MainView.class)
public class CodeStoreList extends VerticalLayout implements Creatable<CodeStore> {

    private final CodeStoreService codeStoreService;
    private boolean showDeleted = false;
    private PaginatedGrid<CodeStore, ?> grid;
    private final PaginationSetting paginationSetting;

    @Autowired
    public CodeStoreList(CodeStoreService codeStoreService,
                         PaginationSetting paginationSetting) {
        this.codeStoreService = codeStoreService;
        this.paginationSetting = paginationSetting;
        this.grid = new PaginatedGrid<>(CodeStore.class);

        List<CodeStore> codeStores = codeStoreService.findAll(false);
        this.grid.setItems(codeStores);


        this.grid.getColumns().forEach(grid::removeColumn);
        this.grid.addColumn(CodeStore::getName).setHeader("Name");
        this.grid.addColumn(codeStore -> {
            CodeStore parent = codeStore.getParentCodeStore();
            return parent != null ? parent.getName() : "";
        }).setHeader("Parent");
        grid.addClassName("styling");
        grid.setPartNameGenerator(codeStore -> codeStore.getDeleted() != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        this.grid.addComponentColumn(codeStore -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog dialog = getSaveOrUpdateDialog(codeStore);
                dialog.open();
            });

            restoreButton.addClickListener(event -> {
                codeStoreService.restore(codeStore);
                Notification.show("CodeStore restored: " + codeStore.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                codeStoreService.delete(codeStore);
                Notification.show("CodeStore deleted: " + codeStore.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                codeStoreService.permanentlyDelete(codeStore);
                Notification.show("CodeStore permanently deleted: " + codeStore.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });
            if (!codeStore.getDeletable()) {
                editButton.setEnabled(false);
                deleteButton.setEnabled(false);
                permanentDeleteButton.setEnabled(false);
                restoreButton.setEnabled(false);
            }

            HorizontalLayout actions = new HorizontalLayout();
            if (codeStore.getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else if (codeStore.getDeleted() == 1) {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        }).setHeader("Options");

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
        List<CodeStore> employees = codeStoreService.findAll(showDeleted);
        this.grid.setItems(employees);
    }

    @Override
    public Dialog getSaveOrUpdateDialog(CodeStore entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " codestore");
        FormLayout formLayout = new FormLayout();

        TextField nameTextField = new TextField("Name");
        ComboBox<CodeStore> parentCodeStore = new ComboBox<>("Parent");
        ComboBox.ItemFilter<CodeStore> filter = (codeStore, filterString) ->
                codeStore.getName().toLowerCase().contains(filterString.toLowerCase());
        parentCodeStore.setItems(filter, codeStoreService.findAll(false));
        parentCodeStore.setItemLabelGenerator(CodeStore::getName);

        Checkbox deletable = new Checkbox("Deletable");

        if (entity != null) {
            nameTextField.setValue(entity.getName());
            parentCodeStore.setValue(entity.getParentCodeStore());
        }

        Button saveButton = new Button("Save");

        saveButton.addClickListener(event -> {
            CodeStore codeStore = Objects.requireNonNullElseGet(entity, CodeStore::new);
            codeStore.setLinkName(codeStore.getLinkName() == null ? nameTextField.getValue() : codeStore.getLinkName());
            codeStore.setName(nameTextField.getValue());
            codeStore.setDeletable(deletable.getValue());
            codeStore.setDeleted(0L);
            codeStore.setParentCodeStore(parentCodeStore.getValue());
            codeStoreService.saveOrUpdate(codeStore);

            Notification.show("CodeStore saved: " + codeStore.getName())
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            nameTextField.clear();
            deletable.clear();
            parentCodeStore.clear();
            createDialog.close();
        });

        formLayout.add(nameTextField, parentCodeStore, deletable, saveButton);

        createDialog.add(formLayout);
        return createDialog;
    }
}
