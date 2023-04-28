package org.openlca.io.simapro.csv.output;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.io.maps.FlowMap;
import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.io.simapro.csv.Compartment;
import org.openlca.simapro.csv.enums.ElementaryFlowType;

class FlowClassifier {

	private final Map<Category, Compartment> compartments = new HashMap<>();
	private final Map<Flow, Compartment> flowCompartments = new HashMap<>();
	private final Map<String, FlowMapEntry> flowMap;

	private FlowClassifier(FlowMap flowMap) {
		this.flowMap = flowMap != null
				? flowMap.index()
				: null;
	}

	static FlowClassifier withMapping(FlowMap flowMap) {
		return new FlowClassifier(flowMap);
	}

	static FlowClassifier withoutMapping() {
		return new FlowClassifier(null);
	}

	/**
	 * Returns a flow mapping for the given flow or {@code null}
	 * if no such mapping is available.
	 */
	FlowMapEntry mappingOf(Flow flow) {
		if (flowMap == null || flow == null)
			return null;
		var mapping = flowMap.get(flow.refId);
		return mapping == null || mapping.targetFlow() == null
			? null
			: mapping;
	}

	/**
	 * Returns the mapped compartment for the given flow or {@code null}
	 * if no compartment could be inferred.
	 */
	Compartment compartmentOf(Flow flow) {
		if (flow == null)
			return null;
		var c = flowCompartments.get(flow);
		if (c != null)
			return c;

		var mapping = mappingOf(flow);
		if (mapping != null && mapping.targetFlow() != null) {
			c = Compartment.fromPath(mapping.targetFlow().flowCategory);
			if (c != null) {
				flowCompartments.put(flow, c);
				return c;
			}
		}

		c = compartments.computeIfAbsent(flow.category, Compartment::of);
		if (c != null) {
			flowCompartments.put(flow, c);
		}
		return c;
	}

	EnumMap<ElementaryFlowType, List<Flow>> groupFlows() {
		var map = new EnumMap<ElementaryFlowType, List<Flow>>(
				ElementaryFlowType.class);
		for (var e : flowCompartments.entrySet()) {
			var type = e.getValue().type();
			var list = map.computeIfAbsent(
					type, t -> new ArrayList<Flow>());
			list.add(e.getKey());
		}
		return map;
	}
}
