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
record FlowKey(String mappingId, String refId, FlowType type) {

	static FlowKey elementary(
		Compartment compartment, String name, String unit, UnitMappingEntry quan) {
		var top = compartment.type() != null
			? compartment.type().exchangeHeader()
			: "";
		var sub = compartment.sub() != null
			? compartment.sub().toString()
			: SubCompartment.UNSPECIFIED.toString();
		var mappingId = KeyGen.toPath("elementary flow", top, sub, name, unit);
		var refId = KeyGen.get("elementary flow", top, sub, name, propIdOf(quan));
		return new FlowKey(mappingId, refId, FlowType.ELEMENTARY_FLOW);
	}

	static FlowKey product(String name, String unit, UnitMappingEntry quan) {
		var mappingId = KeyGen.toPath("product", name, unit);
		var refId = KeyGen.get("product", name,  propIdOf(quan));
		return new FlowKey(mappingId, refId, FlowType.PRODUCT_FLOW);
	}

	static FlowKey waste(String name, String unit, UnitMappingEntry quan) {
		var mappingId = KeyGen.toPath("waste", name, unit);
		var refId = KeyGen.get("waste", name,  propIdOf(quan));
		return new FlowKey(mappingId, refId, FlowType.WASTE_FLOW);
	}

	private static String propIdOf(UnitMappingEntry quan) {
		return quan != null && quan.flowProperty != null
				? quan.flowProperty.refId
				: "";
	}

	SyncFlow getOrCreate(FlowSync sync, Supplier<Flow> fn) {
		var syncFlow = sync.get(mappingId);
		if (!syncFlow.isEmpty())
			return syncFlow;
		syncFlow = sync.get(refId);
		if (!syncFlow.isEmpty())
			return syncFlow;
		var flow = fn.get();
		syncFlow = SyncFlow.of(flow);
		sync.put(refId, syncFlow);
		return syncFlow;
	}
}
