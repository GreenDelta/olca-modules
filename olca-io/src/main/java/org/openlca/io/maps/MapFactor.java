package org.openlca.io.maps;

/**
 * Describes a conversion factor when mapping to a given entity type. For example
 * if there is a flow from a format A that is mapped to a format B  a
 * MapFactor<B> could describe the factor for conversion of amounts for the
 * flow in format A to the flow amounts in B.
 *
 * @param <T> the entity type
 */
public class MapFactor<T> {

	private final T entity;
	private final double factor;

	public MapFactor(T entity, double factor) {
		this.entity = entity;
		this.factor = factor;
	}

	public T getEntity() {
		return entity;
	}

	public double getFactor() {
		return factor;
	}

	/**
	 * Applies this factor to the given value. In the entity mappings conversion
	 * factors should be always applied by multiplication which is what this
	 * method does.
	 */
	public double apply(double value) {
		return factor * value;
	}

}
