package hu.martin.ems.pages.core.doTestData;

import hu.martin.ems.pages.core.performResult.PerformRestoreResult;
import lombok.Getter;

public class DoRestoreTestData extends DoTestData {
    public DoRestoreTestData(Integer originalNonDeletedRowNumber,
                             Integer originalDeletedRowNumber,
                             Integer originalNonDeletedOnlyDeletable,
                             Integer originalDeletedOnlyDeletable,
                             String notificationWhenPerform,
                             PerformRestoreResult result,
                             Integer nonDeletedRowNumberAfterMethod,
                             Integer deletedRowNumberAfterMethod,
                             Integer nonDeletedRowNumberOnlyDeletableAfterMethod,
                             Integer deletedRowNumberOnlyDeletableAfterMethod) {
        super(originalNonDeletedRowNumber, originalDeletedRowNumber, originalNonDeletedOnlyDeletable, originalDeletedOnlyDeletable, notificationWhenPerform, nonDeletedRowNumberAfterMethod, deletedRowNumberAfterMethod, nonDeletedRowNumberOnlyDeletableAfterMethod, deletedRowNumberOnlyDeletableAfterMethod);
        this.result = result;
    }

    @Getter
    PerformRestoreResult result;
}
