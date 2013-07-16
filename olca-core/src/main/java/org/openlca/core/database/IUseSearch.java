package org.openlca.core.database;

import java.util.List;

import org.openlca.core.model.lean.BaseDescriptor;

/** Search of entities where another entity is used. */
interface IUseSearch<T> {

	/**
	 * Returns a list of descriptors of entities where the given entity is used.
	 */
	public List<BaseDescriptor> findUses(T entity);

}
