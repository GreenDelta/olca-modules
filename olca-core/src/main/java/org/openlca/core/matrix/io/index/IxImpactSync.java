package org.openlca.core.matrix.io.index;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.descriptors.ImpactDescriptors;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.model.descriptors.ImpactDescriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

class IxImpactSync {

	private final IDatabase db;
	private final List<IxImpactItem> items;

	private IxImpactSync(IDatabase db, List<IxImpactItem> items) {
		this.db = db;
		this.items = items;
	}

	static Optional<ImpactIndex> sync(IDatabase db, IxImpactIndex idx) {
		return db == null || idx == null || idx.isEmpty()
				? Optional.empty()
				: new IxImpactSync(db, idx.items()).sync();
	}

	private Optional<ImpactIndex> sync() {

		// index impact categories of the database
		var ids = new HashSet<String>();
		for (var item : items) {
			ids.add(item.impact().id());
		}
		var impacts = new HashMap<String, ImpactDescriptor>();
		var ixs = ImpactDescriptors.of(db);
		ixs.getAll(row -> ids.contains(ixs.getRefIf(row)))
				.forEach(d -> impacts.put(d.refId, d));

		// construct the index
		var idx = new ImpactIndex();
		for (var item : items) {
			var impact = impacts.get(item.impact().id());
			if (impact == null)
				return Optional.empty();
			idx.add(impact);
		}

		return idx.isEmpty()
				? Optional.empty()
				: Optional.of(idx);
	}
}
