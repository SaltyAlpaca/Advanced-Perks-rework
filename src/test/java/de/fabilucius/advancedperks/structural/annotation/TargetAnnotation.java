package de.fabilucius.advancedperks.structural.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // Ensures the annotation is available at runtime for reflection
@Target(ElementType.TYPE)           // Specifies that this annotation can be used on classes
public @interface TargetAnnotation {
    // Add any elements here if needed, for example, a description or value property
}
