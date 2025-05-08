package hu.martin.ems.core.actuator;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HealthStatusResponse {
    @Expose
    private String status;
}
