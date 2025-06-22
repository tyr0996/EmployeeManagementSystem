package hu.martin.ems.core.vaadin;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

@Tag("switch")
@CssImport("./styles/switch.css")
public class Switch extends Component {

    public enum Size {
        SMALL("small"), MEDIUM("medium"), LARGE("large");
        private final String cssClass;
        Size(String cssClass) {
            this.cssClass = cssClass;
        }
        public String getCssClass() {
            return cssClass;
        }
    }

    private final Span knob;
    private final Span onLabel;
    private final Span offLabel;
    private final Span mainLabel;
    private final Span switchElement;
    private boolean value = false;

    public Boolean getValue(){
        return this.value;
    }
    public void setValue(Boolean value){
        this.value = value;
        updateVisuals();
//        if(value != this.value){
//            toggle();
//        }
    }

    public Switch(String labelText, boolean showLabels, Size size, Boolean status) {
        HorizontalLayout container = new HorizontalLayout();
        container.setSpacing(true);
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        getElement().appendChild(container.getElement());

        mainLabel = new Span(labelText);
        mainLabel.getElement().getClassList().add("switch-main-label");
        container.add(mainLabel);

        switchElement = new Span();
        switchElement.getElement().getClassList().add("custom-switch");
        switchElement.getElement().getClassList().add(size.getCssClass());

        knob = new Span();
        knob.getElement().getClassList().add("knob");

        onLabel = new Span("I");
        onLabel.getElement().getClassList().add("label");
        onLabel.getElement().getClassList().add("on-label");

        offLabel = new Span("O");
        offLabel.getElement().getClassList().add("label");
        offLabel.getElement().getClassList().add("off-label");

//        if (showLabels) {
            switchElement.getElement().appendChild(onLabel.getElement(), offLabel.getElement());
//        }

        switchElement.getElement().appendChild(knob.getElement());

        container.add(switchElement);
        if(status){
            toggle();
        }

        switchElement.addClickListener(e -> {
            toggle();
        });
        updateVisuals();
    }
    public Switch(String label){
        this(label, true, Size.MEDIUM, false);
    }

    private void toggle() {
        this.value = !this.value;
        updateVisuals();
    }

    private void updateVisuals() {
        if (this.value) {
            switchElement.getElement().getClassList().add("on");
        } else {
            switchElement.getElement().getClassList().remove("on");
        }
    }

    public void addClickListener(ComponentEventListener<ClickEvent<Switch>> listener) {
        addListener(ClickEvent.class, (ComponentEventListener) listener);
    }
}