package org.openlca.ilcd.commons.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Multi-lang string with a maximum length of 500 characters.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface Label {
}
