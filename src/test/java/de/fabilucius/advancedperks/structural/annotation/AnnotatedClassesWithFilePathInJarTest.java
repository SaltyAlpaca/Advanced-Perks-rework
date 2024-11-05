package de.fabilucius.advancedperks.structural.annotation;

import de.fabilucius.advancedperks.structural.AbstractStructureTest;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Set;

class AnnotatedClassesWithFilePathInJarTest extends AbstractStructureTest {

    private static final Class<? extends Annotation> TARGET_ANNOTATION = TargetAnnotation.class;

    @Test
    void checkConfigurationClasses() throws IOException {
        Reflections reflections = new Reflections("de.fabilucius.advancedperks");

        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(TARGET_ANNOTATION);

        boolean allPublic = annotatedClasses.stream().allMatch(clazz -> {
            boolean isPublic = Modifier.isPublic(clazz.getModifiers());
            if (!isPublic) {
                System.err.println("Class " + clazz.getName() + " is not public.");
            }
            return isPublic;
        });

        org.junit.jupiter.api.Assertions.assertTrue(allPublic, "All annotated classes should be public");
        org.junit.jupiter.api.Assertions.assertFalse(annotatedClasses.isEmpty(), "No classes found with the specified annotation");
    }
}
