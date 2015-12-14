package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openlca.core.model.descriptors.CategorizedDescriptor;

/**
 * Default implementation of the usage search that returns always an empty list.
 * This is useful for model types like projects that cannot be used in other
 * entities.
 */
class EmptyUseSearch<T extends CategorizedDescriptor> extends BaseUseSearch<T> {

	EmptyUseSearch() {
		super(null);
	}

	@Override
	public List<CategorizedDescriptor> findUses(Set<Long> ids) {
		return Collections.emptyList();
	}

}
