package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.List;

import gnu.trove.set.TLongSet;
import org.openlca.core.model.descriptors.RootDescriptor;

record EmptyUseSearch() implements IUseSearch {
	@Override
	public List<RootDescriptor> find(TLongSet ids) {
		return Collections.emptyList();
	}
}
