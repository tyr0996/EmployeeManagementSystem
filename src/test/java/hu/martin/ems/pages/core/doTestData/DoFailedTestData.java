package hu.martin.ems.pages.core.doTestData;

import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
public abstract class DoFailedTestData extends DoTestData {

    public DoFailedTestData(Integer originalNonDeletedRowNumber, Integer originalDeletedRowNumber, @Nullable Integer originalNonDeletedOnlyDeletable, @Nullable Integer originalDeletedOnlyDeletable, String notificationWhenPerform, Integer nonDeletedRowNumberAfterMethod, Integer deletedRowNumberAfterMethod, @Nullable Integer nonDeletedRowNumberOnlyDeletableAfterMethod, Integer deletedRowNumberOnlyDeletableAfterMethod) {
        super(originalNonDeletedRowNumber, originalDeletedRowNumber, originalNonDeletedOnlyDeletable, originalDeletedOnlyDeletable, notificationWhenPerform, nonDeletedRowNumberAfterMethod, deletedRowNumberAfterMethod, nonDeletedRowNumberOnlyDeletableAfterMethod, deletedRowNumberOnlyDeletableAfterMethod);
    }
}
