package hu.martin.ems.pages.core.dialog.saveOrUpdateDialog;

import hu.martin.ems.pages.core.component.Fillable;

public interface MultiFillable<T, S> extends Fillable {
    T fillWithRandom(int numberOfElement);

    T fillWith(S... values);
}
