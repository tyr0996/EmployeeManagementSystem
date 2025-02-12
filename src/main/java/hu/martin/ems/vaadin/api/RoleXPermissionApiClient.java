//package hu.martin.ems.vaadin.api;
//
//import hu.martin.ems.annotations.NeedCleanCoding;
//import hu.martin.ems.core.model.EmsResponse;
//import hu.martin.ems.model.Permission;
//import hu.martin.ems.model.Role;
//import hu.martin.ems.model.RoleXPermission;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//
//@Component
//@NeedCleanCoding
//public class RoleXPermissionApiClient extends EmsApiClient<RoleXPermission> {
//    public RoleXPermissionApiClient() {
//        super(RoleXPermission.class);
//    }
//
//
//    public EmsResponse findAlRoleXPermissionByRole(Role r){
//        webClientProvider.initWebClient(entityName);
//        String jsonResponse = webClient.get()
//                .uri("findAlRoleXPermissionByRole?roleId=" + r.getId())
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//        return new EmsResponse(200, convertResponseToEntityList(jsonResponse, RoleXPermission.class), "");
//
//    }
//
//    public EmsResponse findAllPairedRoleTo(Permission p){
//        webClientProvider.initWebClient(entityName);
//        String jsonResponse = webClient.get()
//                .uri("findAllPairedRoleTo?permissionId=" + p.getId())
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//        return new EmsResponse(200, convertResponseToEntityList(jsonResponse, Role.class), "");
//
//    }
//
//    public EmsResponse findAllPairedPermissionsTo(Role r){
//        webClientProvider.initWebClient(entityName);
//        String jsonResponse = webClient.get()
//                .uri("findAllPairedPermissionsTo?roleId=" + r.getId())
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//        return new EmsResponse(200, convertResponseToEntityList(jsonResponse, Permission.class), "");
//
//    }
//
//    public void removeAllRolesFrom(Permission p){
//        webClientProvider.initWebClient(entityName);
//        webClient.put()
//                .uri("removeAllRolesFrom")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(gson.toJson(p))
//                .retrieve()
//                .bodyToMono(Void.class)
//                .block();
//
//    }
//
//    public String removeAllPermissionsFrom(Role r){
//        webClientProvider.initWebClient(entityName);
//        String response = webClient.put()
//                .uri("removeAllPermissionsFrom")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(gson.toJson(r))
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//        return response;
//
//    }
//
//    public EmsResponse findAllWithUnused() {
//        webClientProvider.initWebClient(entityName);
//        String jsonResponse = webClient.get()
//                .uri("findAllWithUnused")
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//        return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
//
//    }
//}
