package hu.martin.ems.pages.core.doTestData;

import hu.martin.ems.pages.core.performResult.PerformPermanentlyDeleteResult;
import lombok.Getter;

public class DoPermanentlyDeleteTestData extends DoTestData {
    public DoPermanentlyDeleteTestData(Integer originalNonDeletedRowNumber,
                            Integer originalDeletedRowNumber,
                            Integer originalNonDeletedOnlyDeletable,
                            Integer originalDeletedOnlyDeletable,
                            String notificationWhenPerform,
                            PerformPermanentlyDeleteResult result,
                            Integer nonDeletedRowNumberAfterMethod,
                            Integer deletedRowNumberAfterMethod,
                            Integer nonDeletedRowNumberOnlyDeletableAfterMethod,
                            Integer deletedRowNumberOnlyDeletableAfterMethod) {
        super(originalNonDeletedRowNumber, originalDeletedRowNumber, originalNonDeletedOnlyDeletable, originalDeletedOnlyDeletable, notificationWhenPerform, nonDeletedRowNumberAfterMethod, deletedRowNumberAfterMethod, nonDeletedRowNumberOnlyDeletableAfterMethod, deletedRowNumberOnlyDeletableAfterMethod);
        this.result = result;
    }

    @Getter
    private PerformPermanentlyDeleteResult result;
}
