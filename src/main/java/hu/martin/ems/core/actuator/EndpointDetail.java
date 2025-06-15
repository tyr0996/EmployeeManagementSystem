package hu.martin.ems.core.actuator;

import com.google.gson.annotations.Expose;
import org.springframework.http.HttpMethod;

public class EndpointDetail {
    @Expose
    private String path;
    @Expose
    private String method;

    public EndpointDetail(String path, HttpMethod method){
        this.path = path;
        this.method = method.name().trim();
    }
}
