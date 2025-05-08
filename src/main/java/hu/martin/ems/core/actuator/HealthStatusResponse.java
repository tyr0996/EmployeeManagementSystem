package hu.martin.ems.core.actuator;

import com.google.gson.annotations.Expose;
import lombok.Getter;

@Getter
public class HealthStatusResponse {
    @Expose
    private String status;
}
