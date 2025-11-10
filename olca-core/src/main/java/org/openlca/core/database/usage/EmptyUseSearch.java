package org.openlca.core.database.usage;

import java.util.Collections;
import java.util.Set;

import org.openlca.core.model.descriptors.RootDescriptor;

import gnu.trove.set.TLongSet;

record EmptyUseSearch() implements UsageSearch {
	@Override
	public Set<? extends RootDescriptor> find(TLongSet ids) {
		return Collections.emptySet();
	}
}
