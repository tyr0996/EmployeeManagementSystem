package hu.martin.ems.vaadin.component.CodeStore;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.service.CodeStoreService;
import hu.martin.ems.vaadin.MainView;

import java.util.Objects;

@Route(value = "codestore/create", layout = MainView.class)
public class CodeStoreCreate extends VerticalLayout {

    private final CodeStoreService codeStoreService;
    public static CodeStore staticCodeStore;

    public CodeStoreCreate(CodeStoreService codeStoreService){
        this.codeStoreService = codeStoreService;
        FormLayout formLayout = new FormLayout();

        TextField nameTextField = new TextField("Name");
        ComboBox<CodeStore> parentCodeStore = new ComboBox<>("Parent");
        ComboBox.ItemFilter<CodeStore> filter = (codeStore, filterString) ->
                codeStore.getName().toLowerCase().contains(filterString.toLowerCase());
        parentCodeStore.setItems(filter, codeStoreService.findAll(false));
        parentCodeStore.setItemLabelGenerator(CodeStore::getName);

        Checkbox deletable = new Checkbox("Deletable");

        if(staticCodeStore != null){
            nameTextField.setValue(staticCodeStore.getName());
            parentCodeStore.setValue(staticCodeStore.getParentCodeStore());
        }

        Button saveButton = new Button("Save");

        saveButton.addClickListener(event -> {
            CodeStore codeStore = Objects.requireNonNullElseGet(staticCodeStore, CodeStore::new);
            codeStore.setLinkName(codeStore.getLinkName() == null ? nameTextField.getValue() : codeStore.getLinkName());
            codeStore.setName(nameTextField.getValue());
            codeStore.setDeletable(deletable.getValue());
            codeStore.setDeleted(0L);
            codeStore.setParentCodeStore(parentCodeStore.getValue());
            codeStoreService.saveOrUpdate(codeStore);

            Notification.show("CodeStore saved: " + codeStore.getName());

            staticCodeStore = null;
            nameTextField.clear();
            deletable.clear();
            parentCodeStore.clear();
        });

        formLayout.add(nameTextField, parentCodeStore, deletable, saveButton);

        add(formLayout);
    }
}
