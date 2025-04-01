package hu.martin.ems.pages.core.doTestData;

import hu.martin.ems.pages.core.performResult.PerformDeleteResult;
import lombok.Getter;

public class DoDeleteTestData extends DoTestData {
    public DoDeleteTestData(Integer originalNonDeletedRowNumber,
                            Integer originalDeletedRowNumber,
                            Integer originalNonDeletedOnlyDeletable,
                            Integer originalDeletedOnlyDeletable,
                            String notificationWhenPerform,
                            PerformDeleteResult performDeleteResult,
                            Integer nonDeletedRowNumberAfterMethod,
                            Integer deletedRowNumberAfterMethod,
                            Integer nonDeletedRowNumberOnlyDeletableAfterMethod,
                            Integer deletedRowNumberOnlyDeletableAfterMethod) {
        super(originalNonDeletedRowNumber, originalDeletedRowNumber, originalNonDeletedOnlyDeletable, originalDeletedOnlyDeletable, notificationWhenPerform, nonDeletedRowNumberAfterMethod, deletedRowNumberAfterMethod, nonDeletedRowNumberOnlyDeletableAfterMethod, deletedRowNumberOnlyDeletableAfterMethod);
        this.result = performDeleteResult;
    }

    @Getter
    private PerformDeleteResult result;
}
