package hu.martin.ems.core.actuator;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HealthStatusResponse {
    @Expose
    private String status;
}
