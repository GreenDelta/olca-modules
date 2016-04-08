package org.openlca.core.database.references;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openlca.core.model.descriptors.CategorizedDescriptor;

/**
 * Default implementation of the usage search that returns always an empty list.
 * This is useful for model types like projects that cannot be used in other
 * entities.
 */
class EmptyReferenceSearch<T extends CategorizedDescriptor> extends BaseReferenceSearch<T> {

	EmptyReferenceSearch() {
		super(null, null);
	}

	@Override
	public List<Reference> findReferences(Set<Long> ids) {
		return Collections.emptyList();
	}

}
