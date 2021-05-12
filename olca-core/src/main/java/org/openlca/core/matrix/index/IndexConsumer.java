package org.openlca.core.matrix.index;

/**
 * Like the normal consumer function but with an index of the respective object
 * as first argument.
 */
@FunctionalInterface
public interface IndexConsumer<T> {

	void accept(int index, T obj);

}
