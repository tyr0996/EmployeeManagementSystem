package hu.martin.ems.core.controller;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.config.StaticDatas;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/api/eps")
@AnonymousAllowed
public class EndpointController {
    @Qualifier("webApplicationContext")
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Gson gson;

    @GetMapping(path = "/eps", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> getAllEndpoints() {
//        ApplicationContext ctx = new AnnotationConfigApplicationContext(EmployeeManagementSystemApplication.class);
        RequestMappingHandlerMapping handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        List<RestEndPoint> eps = new ArrayList<>();
        handlerMethods.forEach((key, value) -> {
            eps.add(processKey(key.toString()));
        });
        return new ResponseEntity<>(gson.toJson(eps), HttpStatus.OK);
    }

    private RestEndPoint processKey(String input){
        // Regular expression minta
        String regex = "(\\w+)\\s\\[(\\/[^\\s]+)\\]";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String method = matcher.group(1);
            String url = matcher.group(2);
            if(!url.equals("/api/eps/eps")){
                return new RestEndPoint(method, url);
            }
            else{
                return null;

            }
        } else {
            System.out.println("No match found. Input: " + input);
            return null;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public class RestEndPoint{

        @Expose
        public String method;
        @Expose
        public String URL;

        @Override
        public String toString(){
            return gson.toJson(this);
        }
    }
}