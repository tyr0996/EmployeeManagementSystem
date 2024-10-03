package hu.martin.ems.vaadin.component.AccessManagement;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.vaadin.api.PermissionApiClient;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.RoleXPermissionApiClient;
import hu.martin.ems.vaadin.component.City.CityList;
import hu.martin.ems.vaadin.component.Creatable;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/grid.css")
@AnonymousAllowed
@NeedCleanCoding
public class PermissionList extends VerticalLayout implements Creatable<Permission> {
    private boolean withDeleted = false;
    private PaginatedGrid<PermissionVO, String> grid;
    private final PaginationSetting paginationSetting;
    private final PermissionApiClient permissionApi = BeanProvider.getBean(PermissionApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);
    private final RoleXPermissionApiClient roleXPermissionApi = BeanProvider.getBean(RoleXPermissionApiClient.class);

    private Grid.Column<PermissionVO> idColumn;
    private Grid.Column<PermissionVO> nameColumn;

    private static String idFilterText = "";
    private static String nameFilterText = "";
    List<PermissionVO> permissionVOS;

    public PermissionList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;

        this.grid = new PaginatedGrid<>(PermissionVO.class);
        List<Permission> permissions = permissionApi.findAll();
        permissionVOS = permissions.stream().map(PermissionVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().toList());

        idColumn = grid.addColumn(v -> v.id);
        nameColumn = grid.addColumn(v -> v.name);

        //this.grid.setColumns("id", "name");
        grid.addClassName("styling");
        grid.setPartNameGenerator(permissionVO -> permissionVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        setFilteringHeaderRow();

        this.grid.addComponentColumn(permission -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog dialog = getSaveOrUpdateDialog(permission.original);
                dialog.open();
            });

            restoreButton.addClickListener(event -> {
                permissionApi.restore(permission.original);
                Notification.show("Permission restored: " + permission.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                permissionApi.delete(permission.original);
                Notification.show("Permission deleted: " + permission.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                permissionApi.permanentlyDelete(permission.id);
                Notification.show("Permission permanently deleted: " + permission.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (permission.deleted == 0) {
                actions.add(editButton, deleteButton);
            } else if (permission.deleted == 1) {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        }).setHeader("Options");

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        Checkbox withDeletedCheckbox = new Checkbox("Show deleted");
        withDeletedCheckbox.addValueChangeListener(event -> {
            withDeleted = event.getValue();
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(withDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, withDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, grid);
    }

    private void updateGridItems() {
        List<Permission> permissions = permissionApi.findAllWithDeleted();
        permissionVOS = permissions.stream().map(PermissionVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
    }

    public Dialog getSaveOrUpdateDialog(Permission entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " permission");
        FormLayout formLayout = new FormLayout();

        TextField nameField = new TextField("Name");

        MultiSelectComboBox<Role> roles = new MultiSelectComboBox<>("Roles");
        ComboBox.ItemFilter<Role> filterRole = (role, filterString) ->
                role.getName().toLowerCase().contains(filterString.toLowerCase());
        List<Role> savedRoles = roleApi.findAll();
        roles.setItems(filterRole, savedRoles);
        roles.setItemLabelGenerator(Role::getName);

        Button saveButton = new Button("Save");

        if (entity != null) {
            nameField.setValue(entity.getName());
            List<Role> pairedRoles = roleXPermissionApi.findAllPairedRoleTo(entity);
            roles.setValue(pairedRoles);
        }

        saveButton.addClickListener(event -> {
            Permission permission = Objects.requireNonNullElseGet(entity, Permission::new);
            permission.setDeleted(0L);
            permission.setName(nameField.getValue());
            if(entity != null){
                permission = permissionApi.update(permission);
            }
            else{
                permission = permissionApi.save(permission);
            }
            roleXPermissionApi.removeAllRolesFrom(entity);
            List<Role> selectedRoles = roles.getSelectedItems().stream().toList();
            for(int i = 0; i < selectedRoles.size(); i++){
                RoleXPermission rxp = new RoleXPermission(selectedRoles.get(i), permission);
                rxp.setDeleted(0L);
                if(entity != null){
                    roleXPermissionApi.update(rxp);
                }
                else{
                    roleXPermissionApi.save(rxp);
                }
            }

            Notification.show("Permission " + (entity == null ? "saved: " : "updated: ") + permission.getName())
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            nameField.clear();
            createDialog.close();
            updateGridItems();
        });

        formLayout.add(nameField, roles, saveButton);
        createDialog.add(formLayout);
        return createDialog;
    }

    private Stream<PermissionVO> getFilteredStream() {
        return permissionVOS.stream().filter(permissionVO ->
                (idFilterText.isEmpty() || permissionVO.id.toString().toLowerCase().contains(idFilterText.toLowerCase())) &&
                        (nameFilterText.isEmpty() || permissionVO.name.toLowerCase().contains(nameFilterText.toLowerCase())) &&
                        (withDeleted ? (permissionVO.deleted == 0 || permissionVO.deleted == 1) : permissionVO.deleted == 0)
        );
    }


    private void setFilteringHeaderRow(){
        TextField idColumnFilter = new TextField();
        idColumnFilter.setPlaceholder("Search id...");
        idColumnFilter.setClearButtonVisible(true);
        idColumnFilter.addValueChangeListener(event -> {
            idFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField nameColumnFilter = new TextField();
        nameColumnFilter.setPlaceholder("Search name...");
        nameColumnFilter.setClearButtonVisible(true);
        nameColumnFilter.addValueChangeListener(event -> {
            nameFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();;
        filterRow.getCell(idColumn).setComponent(filterField(idColumnFilter, "ID"));
        filterRow.getCell(nameColumn).setComponent(filterField(nameColumnFilter, "Name"));
    }

    private Component filterField(TextField filterField, String title){
        VerticalLayout res = new VerticalLayout();
        res.getStyle().set("padding", "0px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");
        filterField.getStyle().set("display", "flex").set("width", "100%");
        NativeLabel titleLabel = new NativeLabel(title);
        res.add(titleLabel, filterField);
        res.setClassName("vaadin-header-cell-content");
        return res;
    }


    public class PermissionVO{
        private Permission original;
        private Long deleted;
        private Long id;
        private String name;

        public PermissionVO(Permission permission){
            this.original = permission;
            this.deleted = permission.getDeleted();
            this.id = permission.getId();
            this.name = permission.getName();
        }
    }




}