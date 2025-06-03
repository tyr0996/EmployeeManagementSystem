package hu.martin.ems.vaadin.api.base;

import hu.martin.ems.core.auth.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class WebClientProvider {

    protected SecurityService securityService;

    @Lazy
    private ServletWebServerApplicationContext webServerAppCtxt;
    public WebClientProvider(@Autowired SecurityService securityService,
                             @Autowired ServletWebServerApplicationContext webServerAppCtxt) {
        this.securityService = securityService;
        this.webServerAppCtxt = webServerAppCtxt;
    }

    public WebClient initCsrfWebClient(String entityName) {
        String baseUrl = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/api/" + entityName + "/";
        return WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                        .build())

                .defaultCookie("X-XSRF-TOKEN", securityService.getXsrfToken())
                .defaultCookie("JSESSIONID", securityService.getSessionId())
                .filter(logRequest())
                .build();
    }

    public WebClient initBaseUrlWebClient() {
        String url = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/";
        return WebClient.builder()
                .baseUrl(url)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                        .build())

                .defaultCookie("JSESSIONID", securityService.getSessionId())
                .filter(logRequest())
                .build();
    }

    public WebClient initWebClient(String entityName) {
        String baseUrl = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/api/" + entityName + "/";
        return WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                        .build())

                .defaultCookie("JSESSIONID", securityService.getSessionId())
                .filter(logRequest())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            StringBuilder sb = new StringBuilder(">>> Request >>> ");
            sb.append(clientRequest.method()).append(" ").append(clientRequest.url()).append("");

            clientRequest.headers().forEach((name, values) -> {
                values.forEach(value -> sb.append(name).append(": ").append(value).append(""));
            });

            log.info(sb.toString());
            return Mono.just(clientRequest);
        });
    }
}
