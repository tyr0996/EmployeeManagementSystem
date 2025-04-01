package hu.martin.ems.pages.core.doTestData;

import hu.martin.ems.pages.core.performResult.PerformUpdateFailedResult;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoUpdateFailedTestData extends DoFailedTestData {
    private PerformUpdateFailedResult result;
}

