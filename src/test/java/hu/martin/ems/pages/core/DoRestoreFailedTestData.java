package hu.martin.ems.pages.core;

import hu.martin.ems.pages.core.doTestData.DoTestData;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DoRestoreFailedTestData extends DoTestData {
    public DoRestoreFailedTestData(Integer originalNonDeletedRowNumber,
                                   Integer originalDeletedRowNumber,
                                   Integer originalNonDeletedOnlyDeletable,
                                   Integer originalDeletedOnlyDeletable,
                                   String notificationWhenPerform,
                                   PerformRestoreFailedResult result,
                                   Integer nonDeletedRowNumberAfterMethod,
                                   Integer deletedRowNumberAfterMethod,
                                   Integer nonDeletedRowNumberOnlyDeletableAfterMethod,
                                   Integer deletedRowNumberOnlyDeletableAfterMethod) {
        super(originalNonDeletedRowNumber, originalDeletedRowNumber, originalNonDeletedOnlyDeletable, originalDeletedOnlyDeletable, notificationWhenPerform, nonDeletedRowNumberAfterMethod, deletedRowNumberAfterMethod, nonDeletedRowNumberOnlyDeletableAfterMethod, deletedRowNumberOnlyDeletableAfterMethod);
        this.result = result;
    }

    @Getter
    PerformRestoreFailedResult result;
}
