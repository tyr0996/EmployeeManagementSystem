package hu.martin.ems.core.auth;

import hu.martin.ems.core.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;
//    private final RoleXPermissionService roleXPermissionService;

    public static String getLoggedInUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            System.out.println("Felhasználónév: " + ((UserDetails) authentication.getPrincipal()).getUsername());
            System.out.println("Jogosultságok : " + ((UserDetails) authentication.getPrincipal()).getAuthorities());
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        return null;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        hu.martin.ems.core.model.User user = userService.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return convert(user);
    }

    public UserDetails convert(hu.martin.ems.core.model.User user){
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRoles().split(","))
                .accountLocked(user.getRoleRole().getName().equals("NO_ROLE"))
                .disabled(!user.isEnabled())
                .build();
    }


//    private String[] getRolesAsString(Role r){
////        List<Permission> permissions = roleXPermissionService.findAllPermission(r.getId());
//        List<Permission> permissions = r.getPermissions().stream().toList();
//        String[] ret = new String[permissions.size() + 1];
//        ret[0] = r.getName();
//        for(int i = 1; i < permissions.size() + 1; i++){
//            ret[i] = permissions.get(i-1).getName();
//        }
//        return ret;
//    }
}
