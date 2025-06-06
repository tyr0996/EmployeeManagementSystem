package hu.martin.ems.core.auth;

import hu.martin.ems.core.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
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

    public static String getLoggedInUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return null;
        }
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return convert(userService.findByUsername(username));
    }

    public UserDetails convert(hu.martin.ems.core.model.User user) {
        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRoles().split(","))
                .accountLocked(user.getRoleRole().getName().equals("NO_ROLE"))
                .disabled(!user.isEnabled())
                .build();
    }
}
