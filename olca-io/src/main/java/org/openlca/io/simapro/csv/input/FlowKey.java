package org.openlca.io.simapro.csv.input;

import java.util.function.Supplier;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.maps.FlowSync;
import org.openlca.io.maps.SyncFlow;
import org.openlca.io.simapro.csv.Compartment;
import org.openlca.simapro.csv.enums.SubCompartment;
import org.openlca.util.KeyGen;

/**
 * Defines how we identify SimaPro CSV flows.
 */
record FlowKey (String path, String refId, FlowType type) {

	static FlowKey elementary(
		Compartment compartment, String name, UnitMappingEntry quantity) {
		var top = compartment.type() != null
			? compartment.type().exchangeHeader()
			: "";
		var sub = compartment.sub() != null
			? compartment.sub().toString()
			: SubCompartment.UNSPECIFIED.toString();
		var path = "elementary::" + String.join(
			"/", top, sub, norm(name), norm(quantity));
		return new FlowKey(path, KeyGen.get(path), FlowType.ELEMENTARY_FLOW);
	}

	static FlowKey product(String name, UnitMappingEntry quantity) {
		var path = "product::" + String.join("/", norm(name), norm(quantity));
		return new FlowKey(path, KeyGen.get(path), FlowType.PRODUCT_FLOW);
	}

	static FlowKey waste(String name, UnitMappingEntry quantity) {
		var path = "waste::" + String.join("/", norm(name), norm(quantity));
		return new FlowKey(path, KeyGen.toPath(), FlowType.WASTE_FLOW);
	}

	private static String norm(UnitMappingEntry q) {
		return q == null || q.flowProperty == null
			? ""
			: norm(q.flowProperty.name);
	}

	private static String norm(String s) {
		return s == null
			? ""
			: s.trim().toLowerCase();
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
