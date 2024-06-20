package hu.martin.ems.vaadin.component.CodeStore;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.service.CodeStoreService;
import hu.martin.ems.vaadin.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "codestore/list", layout = MainView.class)
public class CodeStoreList extends VerticalLayout {

    private final CodeStoreService codeStoreService;
    private boolean showDeleted = false;
    private Grid<CodeStore> grid;

    @Autowired
    public CodeStoreList(CodeStoreService codeStoreService) {
        this.codeStoreService = codeStoreService;

        this.grid = new Grid<>(CodeStore.class);
        List<CodeStore> codeStores = codeStoreService.findAll(false);
        this.grid.setItems(codeStores);

        this.grid.getColumns().forEach(grid::removeColumn);
        this.grid.addColumn(CodeStore::getName).setHeader("Name");
        this.grid.addColumn(codeStore -> {
            CodeStore parent = codeStore.getParentCodeStore();
            return parent != null ? parent.getName() : "";
        }).setHeader("Parent");

        this.grid.addComponentColumn(codeStore -> {
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            Button restoreButton = new Button("Restore");
            Button permanentDeleteButton = new Button("Permanently Delete");

            editButton.addClickListener(event -> {
                CodeStoreCreate.staticCodeStore = codeStore;
                UI.getCurrent().navigate("employee/create");
            });

            restoreButton.addClickListener(event -> {
                codeStoreService.restore(codeStore);
                Notification.show("CodeStore restored: " + codeStore.getName());
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                codeStoreService.delete(codeStore);
                Notification.show("CodeStore deleted: " + codeStore.getName());
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                codeStoreService.permanentlyDelete(codeStore);
                Notification.show("CodeStore permanently deleted: " + codeStore.getName());
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

        // Toggle switch for showing deleted items
        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            updateGridItems();
        });

        add(showDeletedCheckbox, grid);
    }

    private void updateGridItems() {
        List<CodeStore> employees = codeStoreService.findAll(showDeleted);
        this.grid.setItems(employees);
    }

}
