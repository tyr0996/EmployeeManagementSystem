package hu.martin.ems.vaadin.api.base;

import hu.martin.ems.core.auth.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientProvider {

    protected SecurityService securityService;

    @Lazy
    private ServletWebServerApplicationContext webServerAppCtxt;

    public WebClientProvider(@Autowired SecurityService securityService,
                             @Autowired ServletWebServerApplicationContext webServerAppCtxt) {
        this.securityService = securityService;
        this.webServerAppCtxt = webServerAppCtxt;
    }

    public WebClient initCsrfWebClient(String entityName){
        String baseUrl = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/api/" + entityName + "/";
        return WebClient.builder()
                .baseUrl(baseUrl)
//                    .filter(logRequest())
                .defaultCookie("X-XSRF-TOKEN", securityService.getXsrfToken())
                .defaultCookie("JSESSIONID", securityService.getSessionId())
                .build();
    }

    public WebClient initWebClient(String entityName){
        String baseUrl = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/api/" + entityName + "/";
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultCookie("JSESSIONID", securityService.getSessionId())
//                    .filter(logRequest())
                .build();
    }
}
