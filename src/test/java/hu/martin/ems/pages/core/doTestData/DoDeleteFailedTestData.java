package hu.martin.ems.pages.core.doTestData;

import hu.martin.ems.pages.core.performResult.PerformDeleteResult;
import org.jetbrains.annotations.Nullable;

public class DoDeleteFailedTestData extends DoFailedTestData {
    private PerformDeleteResult result;

    public DoDeleteFailedTestData(Integer originalNonDeletedRowNumber,
                                  Integer originalDeletedRowNumber,
                                  @Nullable Integer originalNonDeletedOnlyDeletable,
                                  @Nullable Integer originalDeletedOnlyDeletable,
                                  String notificationWhenPerform,
                                  PerformDeleteResult pdr,
                                  Integer nonDeletedRowNumberAfterMethod,
                                  Integer deletedRowNumberAfterMethod,
                                  @Nullable Integer nonDeletedRowNumberOnlyDeletableAfterMethod,
                                  Integer deletedRowNumberOnlyDeletableAfterMethod) {
        super(originalNonDeletedRowNumber, originalDeletedRowNumber, originalNonDeletedOnlyDeletable, originalDeletedOnlyDeletable, notificationWhenPerform, nonDeletedRowNumberAfterMethod, deletedRowNumberAfterMethod, nonDeletedRowNumberOnlyDeletableAfterMethod, deletedRowNumberOnlyDeletableAfterMethod);
        this.result = pdr;
    }
}
