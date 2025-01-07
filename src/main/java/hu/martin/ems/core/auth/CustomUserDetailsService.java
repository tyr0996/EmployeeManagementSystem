package hu.martin.ems.core.auth;

import hu.martin.ems.core.service.UserService;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
//import hu.martin.ems.service.RoleXPermissionService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;
//    private final RoleXPermissionService roleXPermissionService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        hu.martin.ems.core.model.User user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return User.withUsername(user.getUsername())
                .password("{noop}" + user.getPassword()) //TODO Titkosítást figyelembe venni
                .roles(getRolesAsString(user.getRoleRole()))
                .accountLocked(user.getRoleRole().getName().equals("NO_ROLE"))
                .build();
    }


    private String[] getRolesAsString(Role r){
//        List<Permission> permissions = roleXPermissionService.findAllPermission(r.getId());
        List<Permission> permissions = r.getPermissions().stream().toList();
        String[] ret = new String[permissions.size() + 1];
        ret[0] = r.getName();
        for(int i = 1; i < permissions.size() + 1; i++){
            ret[i] = permissions.get(i-1).getName();
        }
        return ret;
    }
}
