package hu.martin.ems.vaadin.component;

import jakarta.validation.constraints.NotNull;

import java.util.*;

public abstract class BaseVO<T> {
    @NotNull
    public Long id;
    @NotNull
    public Long deleted;
    @NotNull
    public T original;

    public BaseVO(Long id, Long deleted, T original) {
        this.id = id;
        this.deleted = deleted;
        this.original = original;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseVO that = (BaseVO) o;
        return Objects.equals(this.id, that.id);
    }

}
