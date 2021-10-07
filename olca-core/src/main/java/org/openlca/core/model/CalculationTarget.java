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

	default boolean isProcess() {
		return this instanceof Process;
	}

	default Process asProcess() {
		return (Process) this;
	}

	default boolean isProductSystem() {
		return this instanceof ProductSystem;
	}

	default ProductSystem asProductSystem() {
		return (ProductSystem) this;
	}

	default Exchange quantitativeReference() {
		if (isProcess())
			return asProcess().quantitativeReference;
		if (isProductSystem())
			return asProductSystem().referenceExchange;
		return null;
	}
}
