package org.openlca.ilcd.commons.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Free text with an unlimited length.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface FreeText {
}
