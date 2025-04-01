package hu.martin.ems.pages.core.doTestData;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Use this class to collect and restore data for testing asserts.
 */
@Getter
@Setter
@NoArgsConstructor
public class DoTestData {
    private Integer originalNonDeletedRowNumber;
    private Integer originalDeletedRowNumber;
    @Nullable
    private Integer originalNonDeletedOnlyDeletable;
    @Nullable private Integer originalDeletedOnlyDeletable;
    private String notificationWhenPerform;
    private Integer nonDeletedRowNumberAfterMethod;
    private Integer deletedRowNumberAfterMethod;
    @Nullable private Integer nonDeletedRowNumberOnlyDeletableAfterMethod;
    private Integer deletedRowNumberOnlyDeletableAfterMethod;

    public DoTestData(Integer originalNonDeletedRowNumber, Integer originalDeletedRowNumber, @Nullable Integer originalNonDeletedOnlyDeletable, @Nullable Integer originalDeletedOnlyDeletable, String notificationWhenPerform, Integer nonDeletedRowNumberAfterMethod, Integer deletedRowNumberAfterMethod, @Nullable Integer nonDeletedRowNumberOnlyDeletableAfterMethod, Integer deletedRowNumberOnlyDeletableAfterMethod) {
        this.originalNonDeletedRowNumber = originalNonDeletedRowNumber;
        this.originalDeletedRowNumber = originalDeletedRowNumber;
        this.originalNonDeletedOnlyDeletable = originalNonDeletedOnlyDeletable;
        this.originalDeletedOnlyDeletable = originalDeletedOnlyDeletable;
        this.notificationWhenPerform = notificationWhenPerform;
        this.nonDeletedRowNumberAfterMethod = nonDeletedRowNumberAfterMethod;
        this.deletedRowNumberAfterMethod = deletedRowNumberAfterMethod;
        this.nonDeletedRowNumberOnlyDeletableAfterMethod = nonDeletedRowNumberOnlyDeletableAfterMethod;
        this.deletedRowNumberOnlyDeletableAfterMethod = deletedRowNumberOnlyDeletableAfterMethod;
    }
}
