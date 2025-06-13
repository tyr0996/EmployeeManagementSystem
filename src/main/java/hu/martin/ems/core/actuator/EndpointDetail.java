package hu.martin.ems.core.actuator;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EndpointDetail {
    @Expose
    private String method; //TODO lehetne enum
    @Expose
    private String path;
}
