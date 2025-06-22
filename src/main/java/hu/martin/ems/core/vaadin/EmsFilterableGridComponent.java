package hu.martin.ems.core.vaadin;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import hu.martin.ems.vaadin.component.BaseVO;

public abstract class EmsFilterableGridComponent<T extends BaseVO> extends VerticalLayout implements IEmsFilterableGridPage<T> {

}
