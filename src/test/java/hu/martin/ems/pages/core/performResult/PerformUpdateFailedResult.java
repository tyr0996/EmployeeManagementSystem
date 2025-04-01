package hu.martin.ems.pages.core.performResult;


import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class PerformUpdateFailedResult extends PerformCreateResult {
    private String[] originalModifiedData;
    private List<FailedVaadinFillableComponent> failedFields;
    private String notificationText;
}