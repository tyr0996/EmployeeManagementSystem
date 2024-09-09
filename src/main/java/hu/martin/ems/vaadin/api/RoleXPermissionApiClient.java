package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@NeedCleanCoding
public class RoleXPermissionApiClient extends EmsApiClient<RoleXPermission> {
    public RoleXPermissionApiClient() {
        super(RoleXPermission.class);
    }

    private final RoleApiClient roleApiClient = BeanProvider.getBean(RoleApiClient.class);

    public List<Role> findAllPairedRoleTo(Permission p){
        String jsonResponse = webClient.get()
                .uri("findAllPairedRoleTo?permissionId=" + p.getId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return convertResponseToEntityList(jsonResponse, Role.class);
    }

    public List<Permission> findAllPairedPermissionsTo(Role r){
        String jsonResponse = webClient.get()
                .uri("findAllPairedPermissionsTo?roleId=" + r.getId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return convertResponseToEntityList(jsonResponse, Permission.class);
    }

    public void removeAllRolesFrom(Permission p){
        try {
            webClient.put()
                    .uri("removeAllRolesFrom")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(om.writeValueAsString(p))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (JsonProcessingException ex) {
            logger.error("Removing permissions from paired role failed due to failing convert it to json");
            //TODO
            ex.printStackTrace();
        }
    }

    public String removeAllPermissionsFrom(Role r){
        try {
            String response = webClient.put()
                    .uri("removeAllPermissionsFrom")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(om.writeValueAsString(r))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return response;
        } catch (JsonProcessingException ex) {
            logger.error("Removing permissions from paired role failed due to failing convert it to json");
            //TODO
            ex.printStackTrace();
            return "null";
        }
    }

    public List<RoleXPermission> findAllWithUnused() {
        String jsonResponse = webClient.get()
                .uri("findAllWithUnused")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return convertResponseToEntityList(jsonResponse);
    }
}
