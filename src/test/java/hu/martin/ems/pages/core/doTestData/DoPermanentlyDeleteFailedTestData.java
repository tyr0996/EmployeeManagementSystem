package hu.martin.ems.pages.core.doTestData;

import hu.martin.ems.pages.core.performResult.PerformPermanentlyDeleteFailedResult;
import org.jetbrains.annotations.Nullable;

public class DoPermanentlyDeleteFailedTestData extends DoFailedTestData {
    private PerformPermanentlyDeleteFailedResult result;

    public DoPermanentlyDeleteFailedTestData(Integer originalNonDeletedRowNumber,
                                             Integer originalDeletedRowNumber,
                                             @Nullable Integer originalNonDeletedOnlyDeletable,
                                             @Nullable Integer originalDeletedOnlyDeletable,
                                             String notificationWhenPerform,
                                             PerformPermanentlyDeleteFailedResult pdr,
                                             Integer nonDeletedRowNumberAfterMethod,
                                             Integer deletedRowNumberAfterMethod,
                                             @Nullable Integer nonDeletedRowNumberOnlyDeletableAfterMethod,
                                             Integer deletedRowNumberOnlyDeletableAfterMethod) {
        super(originalNonDeletedRowNumber, originalDeletedRowNumber, originalNonDeletedOnlyDeletable, originalDeletedOnlyDeletable, notificationWhenPerform, nonDeletedRowNumberAfterMethod, deletedRowNumberAfterMethod, nonDeletedRowNumberOnlyDeletableAfterMethod, deletedRowNumberOnlyDeletableAfterMethod);
        this.result = pdr;
    }
}
