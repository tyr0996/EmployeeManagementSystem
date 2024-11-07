package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.User;
import org.springframework.stereotype.Component;

@Component
@NeedCleanCoding
public class UserApiClient extends EmsApiClient<User> {
    public UserApiClient() {
        super(User.class);
    }

    public EmsResponse userExists(String userName){
        initWebClient();
        String jsonResponse = webClient.get()
                .uri("userExists?username=" + userName)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            return new EmsResponse(200, convertResponseToEntity(jsonResponse), "");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public EmsResponse findByUsername(String userName) {
        initWebClient();
        String jsonResponse = webClient.get()
                .uri("getByUsername?username=" + userName)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            return new EmsResponse(200, convertResponseToEntity(jsonResponse), "");
        } catch (JsonProcessingException e) {
            return new EmsResponse(500, "Json processing exception");
        }
    }
}
