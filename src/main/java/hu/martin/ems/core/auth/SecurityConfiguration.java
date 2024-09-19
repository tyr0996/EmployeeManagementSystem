package hu.martin.ems.core.auth;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.service.UserService;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.service.RoleXPermissionService;
import hu.martin.ems.vaadin.component.Login.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;

@EnableWebSecurity
@Configuration
@NeedCleanCoding
public class SecurityConfiguration extends VaadinWebSecurity {

    private final UserService userService;
    private final RoleXPermissionService roleXPermissionService;

    public SecurityConfiguration(UserService userService,
                                 RoleXPermissionService roleXPermissionService){
        this.userService = userService;
        this.roleXPermissionService = roleXPermissionService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                /*
                TODO érdemes lenne használni a csrf-t, de jelenleg különben bejelentkezni sem enged
                */
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(new AntPathRequestMatcher("/public/**")).permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/*").permitAll()//TODO: only for testing!
                        .requestMatchers("/api/*/*").permitAll() //TODO: only for testing!
                        .requestMatchers("/api/*/*/*").permitAll()
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .logout((logout) -> logout.permitAll());

        return http.build();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.rememberMe(me -> me.alwaysRemember(false));

        // super.configure(HttpSecurity)
        super.configure(http);
        setLoginView(http, LoginView.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        web.ignoring()
                .requestMatchers("/VAADIN/**")
                .requestMatchers("/favicon.ico")
                .requestMatchers("/manifest.webmanifest", "/sw.js", "/offline.html")
                .requestMatchers("/icons/**")
                .requestMatchers("/images/**")
                .requestMatchers("/frontend/**")
                .requestMatchers("/webjars/**")
                .requestMatchers("/css/**", "/js/**");
    }


}