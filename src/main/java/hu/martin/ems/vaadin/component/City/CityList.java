package hu.martin.ems.vaadin.component.City;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.City;
import hu.martin.ems.service.CityService;
import hu.martin.ems.vaadin.MainView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "city/list", layout = MainView.class)
public class CityList extends VerticalLayout {

    private final CityService cityService;
    private boolean showDeleted = false;
    private Grid<CityVO> grid;

    @Autowired
    public CityList(CityService cityService) {
        this.cityService = cityService;

        this.grid = new Grid<>(CityVO.class);
        List<City> citys = cityService.findAll(false);
        List<CityVO> data = citys.stream().map(CityVO::new).toList();
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");
        this.grid.removeColumnByKey("deleted");
        this.grid.removeColumnByKey("Id");

        //region Options column
        this.grid.addComponentColumn(city -> {
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            Button restoreButton = new Button("Restore");
            Button permanentDeleteButton = new Button("Permanently Delete");

            editButton.addClickListener(event -> {
                CityCreate.c = city.getOriginal();
                UI.getCurrent().navigate("city/create");
            });

            restoreButton.addClickListener(event -> {
                this.cityService.restore(city.getOriginal());
                Notification.show("City restored: " + city.getOriginal().getName());
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.cityService.delete(city.getOriginal());
                Notification.show("City deleted: " + city.getOriginal().getName());
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.cityService.permanentlyDelete(city.getOriginal());
                Notification.show("City permanently deleted: " + city.getOriginal().getName());
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if(city.getOriginal().getDeleted() == 0){ actions.add(editButton, deleteButton); }
            else if(city.getOriginal().getDeleted() == 1){ actions.add(permanentDeleteButton, restoreButton); }
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

    private void updateGridItems() {
        List<City> citys = this.cityService.findAll(showDeleted);
        this.grid.setItems(citys.stream().map(CityVO::new).toList());
    }

    @Getter
    public class CityVO {
        private City original;
        private Long deleted;
        private Long id;
        private String countryCode;
        private String name;
        private String zipCode;

        public CityVO(City city) {
            this.original = city;
            this.id = city.getId();
            this.deleted = city.getDeleted();
            this.name = original.getName();
            this.countryCode = original.getCountryCode().getName();
            this.zipCode = original.getZipCode();
        }
    }
}
