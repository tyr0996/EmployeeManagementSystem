package hu.martin.ems.vaadin.component;

public abstract class BaseVOWithDeletable<T> extends BaseVO<T> {
    public Boolean deletable;

    public BaseVOWithDeletable(Long id, Long deleted, Boolean deletable, T original){
        super(id, deleted, original);
        this.deletable = deletable;
    }
}
