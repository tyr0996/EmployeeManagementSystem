package hu.martin.ems.core.auth;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
import java.util.Arrays;

@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true, securedEnabled = true)
@NeedCleanCoding
public class SecurityConfiguration extends VaadinWebSecurity {
    private final SecurityService securityService;


    public SecurityConfiguration(SecurityService securityService){
        this.securityService = securityService;
    }

    @Autowired
    public CustomUserDetailsService userDetailsService;

    @Autowired
    public UserService userService;

    @Value("${rememberme.key}")
    private String key;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
//        configure(http);
        http
                /*
                TODO érdemes lenne használni a csrf-t, de jelenleg különben bejelentkezni sem enged
                */
                .cors(cors -> {
                    cors.configurationSource(corsConfigurationSource());
                })
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/login?invalid-session")
                        .maximumSessions(1)
                        .expiredUrl("/login?session-expired"))
//                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests
//                        .requestMatchers(new AntPathRequestMatcher("/public/**")).permitAll()
                        .requestMatchers("/api/**").permitAll()//TODO: only for testing!
                        .requestMatchers("/login**").permitAll()
                        .requestMatchers("/access-denied").permitAll()
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> {
                    exception.accessDeniedPage("/accesss-denied")
                            .configure(http);
                })
                .formLogin(form -> form.loginPage("/login"))
                .logout(logout -> logout
                        .permitAll()
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));

        return http.build();
    }

//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }

//    @Bean
//    public AccessDeniedHandler accessDeniedHandler() {
//        return (request, response, accessDeniedException) ->
//                response.sendRedirect("/access-denied");
//    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(new BCryptPasswordEncoder());

        return new ProviderManager(authProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
//        http.rememberMe(me -> {
//            me.alwaysRemember(true);
//            me.key(key);
//            me.rememberMeCookieName("rememberMe");
//            me.tokenValiditySeconds(86400);
//            me.userDetailsService(userDetailsService);
//        });
//        setLoginView(http, LoginView.class);
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
        configuration.setAllowCredentials(true);
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.setAllowedHeaders(Arrays.asList("Accept","Access-Control-Request-Method","Access-Control-Request-Headers",
                "Accept-Language","Authorization","Content-Type","Request-Name","Request-Surname","Origin","X-Request-AppVersion",
                "X-Request-OsVersion", "X-Request-Device", "X-Requested-With"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsManager userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager();
        manager.setDataSource(dataSource);
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}