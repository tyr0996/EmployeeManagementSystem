package hu.martin.ems.pages.core.dialog.saveOrUpdateDialog;

import hu.martin.ems.pages.core.component.Fillable;

public interface SingleFillable<T, S> extends Fillable {
    T fillWith(S value);

    T fillWithRandom();
}
