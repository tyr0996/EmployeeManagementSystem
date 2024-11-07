package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
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

    public EmsResponse findAlRoleXPermissionByRole(Role r){
        initWebClient();
        String jsonResponse = webClient.get()
                .uri("findAlRoleXPermissionByRole?roleId=" + r.getId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try{
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse, RoleXPermission.class), "");
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException while findAlRoleXPermissionByRole");
            return new EmsResponse(500, "JsonProcessingException");
        }
    }

    public EmsResponse findAllRoleXPermissionByPermission(Permission p){
        initWebClient();
        String jsonResponse = webClient.get()
                .uri("findAllRoleXPermissionByPermission?permissionId=" + p.getId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse, RoleXPermission.class), "");
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException while findAllRoleXPermissionByPermission");
            return new EmsResponse(500, "JsonProcessingException");
        }
    }

    public EmsResponse findAllPairedRoleTo(Permission p){
        initWebClient();
        String jsonResponse = webClient.get()
                .uri("findAllPairedRoleTo?permissionId=" + p.getId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse, Role.class), "");
        } catch (JsonProcessingException e) {

            logger.error("JsonProcessingException while findAllPairedRoleTo");
            return new EmsResponse(500, "JsonProcessingException");
        }
    }

    public EmsResponse findAllPairedPermissionsTo(Role r){
        initWebClient();
        String jsonResponse = webClient.get()
                .uri("findAllPairedPermissionsTo?roleId=" + r.getId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse, Permission.class), "");
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException while findAllPairedPermissionsTo");
            return new EmsResponse(500, "JsonProcessingException");
        }
    }

    public void removeAllRolesFrom(Permission p){
        initWebClient();
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
        initWebClient();
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

    public EmsResponse findAllWithUnused(Boolean withDeleted) {
        initWebClient();
        String jsonResponse = webClient.get()
                .uri("findAllWithUnused?withDeleted=" + withDeleted)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException while findAllWithUnused");
            return new EmsResponse(500, "JsonProcessingException");
        }
    }
}
