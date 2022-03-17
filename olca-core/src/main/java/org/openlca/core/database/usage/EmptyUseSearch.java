package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.Set;

import gnu.trove.set.TLongSet;
import org.openlca.core.model.descriptors.RootDescriptor;

record EmptyUseSearch() implements UsageSearch {
	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		return Collections.emptySet();
	}
}
