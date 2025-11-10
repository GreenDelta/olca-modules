package org.openlca.core.matrix.io.index;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.descriptors.FlowDescriptors;
import org.openlca.core.database.descriptors.ProcessDescriptors;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

class IxTechSync {

	private final IDatabase db;
	private final List<IxTechItem> items;

	private IxTechSync(IDatabase db, List<IxTechItem> items) {
		this.db = db;
		this.items = items;
	}

	static Optional<TechIndex> sync(IDatabase db, IxTechIndex idx) {
		return db == null || idx == null || idx.isEmpty()
				? Optional.empty()
				: new IxTechSync(db, idx.items()).sync();
	}

	private Optional<TechIndex> sync() {

		// index the descriptors of the database
		var processIds = new HashSet<String>();
		var flowIds = new HashSet<String>();
		for (var item : items) {
			processIds.add(item.provider().id());
			flowIds.add(item.flow().id());
		}
		var processes = new HashMap<String, ProcessDescriptor>();
		var pxs = ProcessDescriptors.of(db);
		pxs.getAll(row -> processIds.contains(pxs.getRefIf(row)))
				.forEach(d -> processes.put(d.refId, d));
		var flows = new HashMap<String, FlowDescriptor>();
		var fxs = FlowDescriptors.of(db);
		fxs.getAll(row -> flowIds.contains(fxs.getRefIf(row)))
				.forEach(d -> flows.put(d.refId, d));

		// construct the index
		var techIdx = new TechIndex();
		for (var item : items) {
			var process = processes.get(item.provider().id());
			var flow = flows.get(item.flow().id());
			if (process == null || flow == null)
				return Optional.empty();
			techIdx.add(TechFlow.of(process, flow));
		}

		return techIdx.isEmpty()
				? Optional.empty()
				: Optional.of(techIdx);
	}
}
