package hu.martin.ems.pages.core.doTestData;

import hu.martin.ems.pages.core.performResult.PerformCreateFailedResult;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoCreateFailedTestData extends DoFailedTestData {
    private PerformCreateFailedResult result;
}
