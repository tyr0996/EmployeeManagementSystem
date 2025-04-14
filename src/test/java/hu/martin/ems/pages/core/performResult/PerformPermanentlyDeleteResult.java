package hu.martin.ems.pages.core.performResult;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerformPermanentlyDeleteResult {
    String[] permanentlyDeletedData;
    String notificationWhenPerform;
}
