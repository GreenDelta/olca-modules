package org.openlca.core.model;

/**
 * The target of a calculation setup can be an instance of {@link Process} or
 * {@link ProductSystem}. This is a tagging interface to mark these classes as
 * calculable.
 *
 * TODO: make this a sealed interface and only permit Process and ProductSystem
 * to implement that interface when we are on Java 17
 */
public interface CalculationTarget {
}
