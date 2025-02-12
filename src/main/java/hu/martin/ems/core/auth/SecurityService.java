package hu.martin.ems.core.auth;

import com.vaadin.flow.server.VaadinServletRequest;
import hu.martin.ems.annotations.NeedCleanCoding;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.VaadinService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@NeedCleanCoding
public class SecurityService {

    private static final String LOGOUT_SUCCESS_URL = "/";

    public UserDetails getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) context.getAuthentication().getPrincipal();
        }
        // Anonymous or no authentication.
        return null;
    }

    public void logout() {
        WebClient c = WebClient.builder()
                .baseUrl("http://localhost:8080/")
                .defaultCookie("X-XSRF-TOKEN", getXsrfToken())
                .defaultCookie("JSESSIONID", getSessionId())
                .build();
        c.post()
                .uri("logout")
                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(writeValueAsString(entity))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        VaadinSession.getCurrent().getSession().invalidate();
        VaadinSession.getCurrent().close(); // Fontos a lezárás is

        // Új session ID lekérése (új request indításával frissül)
        VaadinService.reinitializeSession(VaadinService.getCurrentRequest());


        //        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
//        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
//        logoutHandler.logout(
//                VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
//                null);
    }

    public String getSessionId() {
//        VaadinService.reinitializeSession(VaadinService.getCurrentRequest());

        HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
        return request.getSession(true).getId();
    }

    public String getXsrfToken() {
        HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("XSRF-TOKEN".equals(cookie.getName())) {
                    return cookie.getValue(); // Return the value of the XSRF-TOKEN cookie
                }
            }
        }
        return null; // Return null if the XSRF-TOKEN cookie is not found
    }
}