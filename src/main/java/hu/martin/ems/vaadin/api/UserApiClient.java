package hu.martin.ems.vaadin.api;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@NeedCleanCoding
public class UserApiClient extends EmsApiClient<User> {
    public UserApiClient() {
        super(User.class);
    }

    public EmsResponse userExists(String userName){
        WebClient webClient = webClientProvider.initWebClient(entityName);
        String jsonResponse = webClient.get()
                .uri("userExists?username=" + userName)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return new EmsResponse(200, convertResponseToEntity(jsonResponse), "");
    }

    @AnonymousAllowed
    public EmsResponse findByUsername(String userName) {
        WebClient webClient = webClientProvider.initWebClient(entityName);
        try{
            String jsonResponse = webClient.get()
                    .uri("getByUsername?username=" + userName)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, convertResponseToEntity(jsonResponse), "");
        }
        catch (WebClientResponseException e){
            System.out.println(e.getStatusCode());
            System.out.println(e.getResponseBodyAsString());
            return new EmsResponse(e.getStatusCode().value(), e.getResponseBodyAsString(), "");
        }
    }
}
