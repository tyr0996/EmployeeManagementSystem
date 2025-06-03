package hu.martin.ems.core.controller;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/api/eps")
@AnonymousAllowed
public class EndpointController {
    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    @Getter
    private RequestMappingHandlerMapping handlerMapping;

    @Autowired
    private Gson gson;


    @GetMapping(path = "/exportApis", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> getAllEndpoints() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = getHandlerMapping().getHandlerMethods();
        List<RestEndPoint> eps = new ArrayList<>();
        handlerMethods.forEach((key, value) -> {
            eps.add(processKey(key.toString()));
        });

        return new ResponseEntity<>(gson.toJson(eps).getBytes(StandardCharsets.UTF_8), HttpStatus.OK);
    }

    private RestEndPoint processKey(String input) {
        String regex = "(\\w+)\\s\\[(\\/[^\\s]+)\\]";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String method = matcher.group(1);
            String url = matcher.group(2);
            if (!url.equals("/api/eps/exportApis")) {
                return new RestEndPoint(method, url);
            } else {
                return null;
            }
        } else {
            System.out.println("No match found. Input: " + input);
            return null;
        }
    }

    @AllArgsConstructor
    public class RestEndPoint {

        @Expose
        public String method;
        @Expose
        public String URL;
    }
}