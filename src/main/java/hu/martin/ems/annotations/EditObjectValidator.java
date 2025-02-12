package hu.martin.ems.annotations;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("hu.martin.ems.annotations.EditObject") // Az annotáció neve
@SupportedSourceVersion(SourceVersion.RELEASE_21) // Java 8 vagy újabb verziók támogatása
public class EditObjectValidator extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(EditObject.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@EditObject can only be applied to fields", element);
                return true;
            }

            VariableElement field = (VariableElement) element;

            if (field.getModifiers().contains(Modifier.STATIC)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "You cann't annotate static field with @EditObject" , field);
                return false;
            }
            TypeElement enclosingClass = (TypeElement) field.getEnclosingElement();
            long annotatedFieldsCount = enclosingClass.getEnclosedElements().stream()
                    .filter(e -> e.getKind() == ElementKind.FIELD)
                    .filter(e -> e.getAnnotation(EditObject.class) != null)
                    .count();

            if (annotatedFieldsCount > 1) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Only one field can be annotated with @EditObject in class " + enclosingClass.getSimpleName(), enclosingClass);
                return true;
            }
        }

        return true;
    }
}
