package hu.martin.ems.pages.core.performResult;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerformDeleteResult {
    private String[] originalDeletedData;
    private String notificationWhenPerform;
}
