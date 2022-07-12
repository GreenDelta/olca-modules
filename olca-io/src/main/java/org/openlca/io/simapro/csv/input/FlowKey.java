package org.openlca.io.simapro.csv.input;

import java.util.function.Supplier;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.io.maps.FlowSync;
import org.openlca.io.maps.SyncFlow;
import org.openlca.io.simapro.csv.Compartment;
import org.openlca.simapro.csv.enums.SubCompartment;
import org.openlca.util.KeyGen;

/**
 * Defines how we identify SimaPro CSV flows.
 */
record FlowKey(String path, String refId, FlowType type) {

	static FlowKey elementary(
		Compartment compartment, String name, String unit) {
		var top = compartment.type() != null
			? compartment.type().exchangeHeader()
			: "";
		var sub = compartment.sub() != null
			? compartment.sub().toString()
			: SubCompartment.UNSPECIFIED.toString();
		var path = KeyGen.toPath("elementary flow", top, sub, name, unit);
		return new FlowKey(path, KeyGen.get(path), FlowType.ELEMENTARY_FLOW);
	}

	static FlowKey product(String name, String unit) {
		var path = KeyGen.toPath("product", name, unit);
		return new FlowKey(path, KeyGen.get(path), FlowType.PRODUCT_FLOW);
	}

	static FlowKey waste(String name, String unit) {
		var path = KeyGen.toPath("waste", name, unit);
		return new FlowKey(path, KeyGen.get(path), FlowType.WASTE_FLOW);
	}

	SyncFlow getOrCreate(FlowSync sync, Supplier<Flow> fn) {
		var syncFlow = sync.get(path);
		if (!syncFlow.isEmpty())
			return syncFlow;
		syncFlow = sync.get(refId);
		if (!syncFlow.isEmpty())
			return syncFlow;
		var flow = fn.get();
		syncFlow = SyncFlow.of(flow);
		sync.put(path, syncFlow);
		sync.put(refId, syncFlow);
		return syncFlow;
	}
}
