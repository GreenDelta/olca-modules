package org.openlca.core.matrix.index;

import gnu.trove.map.hash.TLongObjectHashMap;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;

public abstract class ProviderIndex {

	private final IDatabase db;
	private final TLongObjectHashMap<ProcessDescriptor> processes;
	private final TLongObjectHashMap<ProductSystemDescriptor> systems;
	private final TLongObjectHashMap<FlowDescriptor> flows;

	private ProviderIndex(IDatabase db) {
		this.db = db;
		processes = new ProcessDao(db).descriptorMap();
		systems = new ProductSystemDao(db).descriptorMap();
		var flowDescriptors = new FlowDao(db).getDescriptors(
			FlowType.PRODUCT_FLOW, FlowType.WASTE_FLOW);
		flows = new TLongObjectHashMap<>(flowDescriptors.size());
		for (var f : flowDescriptors) {
			flows.put(f.id, f);
		}
	}

	public static ProviderIndex eager(IDatabase db) {
		return new EagerIndex(db);
	}


	public static ProviderIndex lazy(IDatabase db) {
		return new LazyIndex(db);
	}

	private static class LazyIndex extends ProviderIndex {

		LazyIndex(IDatabase db) {
			super(db);
		}

	}

	private static class EagerIndex extends ProviderIndex {

		EagerIndex(IDatabase db) {
			super(db);
		}

	}


}
