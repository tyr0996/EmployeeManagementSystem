package hu.martin.ems.core.auth;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import hu.martin.ems.annotations.NeedCleanCoding;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@NeedCleanCoding
public class SecurityService {

    @Autowired
    private WebServerApplicationContext webServerAppCtxt;

    public UserDetails getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) context.getAuthentication().getPrincipal();
        }
        return null;
    }

    public void logout() {
        WebClient c = WebClient.builder()
                .baseUrl("http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/")
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

        WrappedSession session = VaadinSession.getCurrent().getSession();
        if(session != null){
            session.invalidate();
        }
        VaadinSession.getCurrent().close();

        VaadinService.reinitializeSession(VaadinService.getCurrentRequest());

    }

    public String getSessionId() {
        HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
        return request.getSession(true).getId();
    }

    public String getXsrfToken() {
        HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("XSRF-TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}