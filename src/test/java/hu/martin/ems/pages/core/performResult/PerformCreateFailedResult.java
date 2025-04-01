package hu.martin.ems.pages.core.performResult;

import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PerformCreateFailedResult extends PerformCreateResult {
    private List<FailedVaadinFillableComponent> failedFields;
    private String notificationText;
}
