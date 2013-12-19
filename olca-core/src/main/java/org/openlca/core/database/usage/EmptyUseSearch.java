package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.List;

import org.openlca.core.model.descriptors.BaseDescriptor;

/**
 * Default implementation of the usage search that returns always an empty list.
 * This is useful for model types like projects that cannot be used in other
 * entities.
 */
class EmptyUseSearch<T extends BaseDescriptor> implements IUseSearch<T> {

	@Override
	public List<BaseDescriptor> findUses(T entity) {
		return Collections.emptyList();
	}

}
