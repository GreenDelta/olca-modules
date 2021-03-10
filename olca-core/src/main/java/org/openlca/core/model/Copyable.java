package org.openlca.core.model;

public interface Copyable<T extends Copyable<T>> {

	T copy();

}
