package org.openlca.core.matrix.io.index;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.descriptors.FlowDescriptors;
import org.openlca.core.database.descriptors.LocationDescriptors;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.commons.Strings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

class IxEnviSync {

	private final IDatabase db;
	private final List<IxEnviItem> items;

	private IxEnviSync(IDatabase db, List<IxEnviItem> items) {
		this.db = db;
		this.items = items;
	}

	static Optional<EnviIndex> sync(IDatabase db, IxEnviIndex idx) {
		return db == null || idx == null || idx.isEmpty()
				? Optional.empty()
				: new IxEnviSync(db, idx.items()).sync();
	}

	private Optional<EnviIndex> sync() {

		// index flows and possible locations of the database
		var flowIds = new HashSet<String>();
		var locIds = new HashSet<String>();
		for (var item : items) {
			flowIds.add(item.flow().id());
			if (item.location() != null
					&& Strings.isNotBlank(item.location().id())) {
				locIds.add(item.location().id());
			}
		}

		var flows = new HashMap<String, FlowDescriptor>();
		var fxs = FlowDescriptors.of(db);
		fxs.getAll(row -> flowIds.contains(fxs.getRefIf(row)))
				.forEach(d -> flows.put(d.refId, d));
		var locs = new HashMap<String, LocationDescriptor>();
		if (!locIds.isEmpty()) {
			var lxs = LocationDescriptors.of(db);
			lxs.getAll(row -> locIds.contains(lxs.getRefIf(row)))
					.forEach(d -> locs.put(d.refId, d));
		}

		// construct the index
		var enviIndex = locIds.isEmpty()
				? EnviIndex.create()
				: EnviIndex.createRegionalized();
		for (var item : items) {
			var flow = flows.get(item.flow().id());
			if (flow == null)
				return Optional.empty();
			if (item.location() != null
					&& Strings.isNotBlank(item.location().id())) {
				// with location
				var loc = locs.get(item.location().id());
				if (loc == null)
					return Optional.empty();
				var enviFlow = item.isInput()
						? EnviFlow.inputOf(flow, loc)
						: EnviFlow.outputOf(flow, loc);
				enviIndex.add(enviFlow);
			} else {
				// without location
				var enviFlow = item.isInput()
						? EnviFlow.inputOf(flow)
						: EnviFlow.outputOf(flow);
				enviIndex.add(enviFlow);
			}
		}

		return enviIndex.isEmpty()
				? Optional.empty()
				: Optional.of(enviIndex);
	}
}
