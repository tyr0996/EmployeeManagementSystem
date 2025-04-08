package hu.martin.ems.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Use this annotation to indicate that the class should not be included in the coverage.
 */
@Target(ElementType.TYPE)
public @interface CoverageIgnored {
}
