package hu.martin.ems.vaadin.api;

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
        return new EmsResponse(200, convertResponseToEntity(jsonResponse), "");
    }

    public EmsResponse findByUsername(String userName) {
        initWebClient();
        String jsonResponse = webClient.get()
                .uri("getByUsername?username=" + userName)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return new EmsResponse(200, convertResponseToEntity(jsonResponse), "");
    }
}
