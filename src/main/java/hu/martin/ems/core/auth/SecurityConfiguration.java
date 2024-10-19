package hu.martin.ems.core.auth;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.vaadin.component.Login.LoginView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@EnableWebSecurity
@Configuration
@NeedCleanCoding
public class SecurityConfiguration extends VaadinWebSecurity {
    private final SecurityService securityService;


    public SecurityConfiguration(SecurityService securityService){
        this.securityService = securityService;
    }

    @Autowired
    public UserDetailsService userDetailsService;

    @Value("${rememberme.key}")
    private String key;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                /*
                TODO érdemes lenne használni a csrf-t, de jelenleg különben bejelentkezni sem enged
                */
                .cors(cors -> {
                    cors.configurationSource(corsConfigurationSource());
                })
                .sessionManagement((sessionManagement) -> {
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                    sessionManagement.invalidSessionUrl("/login?invalid-session");
                    sessionManagement.maximumSessions(1);
                })
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(new AntPathRequestMatcher("/public/**")).permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/*").permitAll()//TODO: only for testing!
                        .requestMatchers("/api/*/*").permitAll() //TODO: only for testing!
                        .requestMatchers("/api/*/*/*").permitAll()
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.loginPage("/login"))
                .logout(logout -> logout
                        .permitAll()
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.rememberMe(me -> {
            me.alwaysRemember(true);
            me.key(key);
            me.rememberMeCookieName("rememberMe");
            me.tokenValiditySeconds(86400);
            me.userDetailsService(userDetailsService);
        });
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
}