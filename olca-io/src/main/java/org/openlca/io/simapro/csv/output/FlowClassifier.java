package org.openlca.io.simapro.csv.output;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.openlca.core.io.maps.FlowMap;
import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.io.simapro.csv.Compartment;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.refdata.ElementaryFlowRow;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

class FlowClassifier {

	private final Map<Category, Compartment> compartments = new HashMap<>();
	private final Map<Flow, Compartment> flowCompartments = new HashMap<>();
	private final HashSet<Category> unmappedCompartments = new HashSet<>();
	private final UnitMap units;
	private final Map<String, FlowMapEntry> flowMap;

	private FlowClassifier(UnitMap units, FlowMap flowMap) {
		this.units = Objects.requireNonNull(units);
		this.flowMap = flowMap != null
				? flowMap.index()
				: null;
	}

	static FlowClassifier of(UnitMap units, FlowMap flowMap) {
		return new FlowClassifier(units, flowMap);
	}

	static FlowClassifier of(UnitMap units) {
		return new FlowClassifier(units, null);
	}

	/**
	 * Returns a flow mapping for the given flow or {@code null}.
	 */
	Mapping mappingOf(Flow flow) {
		if (flowMap == null || flow == null)
			return null;
		var mapping = flowMap.get(flow.refId);
		if (mapping == null)
			return null;
		return Mapping.of(mapping, units).orElse(null);
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
		if (mapping != null) {
			flowCompartments.put(flow, mapping.compartment());
			return mapping.compartment;
		}

		if (flow.category == null) {
			LoggerFactory.getLogger(getClass())
					.error("elementary flow {} has no category " +
							"and cannot be mapped to SimaPro", flow.refId);
			return null;
		}
		if (unmappedCompartments.contains(flow.category)) {
			return null;
		}

		c = compartments.computeIfAbsent(flow.category, Compartment::of);
		if (c == null) {
			LoggerFactory.getLogger(getClass()).error(
					"category {} cannot mapped to SimaPro; all flows in this "
							+ "category will be skipped", flow.category.toPath());
			unmappedCompartments.add(flow.category);
			return null;
		}
		flowCompartments.put(flow, c);
		return c;
	}

	EnumMap<ElementaryFlowType, List<Flow>> groupFlows() {
		var map = new EnumMap<ElementaryFlowType, List<Flow>>(
				ElementaryFlowType.class);
		for (var e : flowCompartments.entrySet()) {
			var type = e.getValue().type();
			var list = map.computeIfAbsent(type, t -> new ArrayList<>());
			list.add(e.getKey());
		}
		return map;
	}

	void writeGroupsTo(CsvDataSet ds) {
		if (ds == null)
			return;

		var groups = groupFlows();
		for (var type : ElementaryFlowType.values()) {
			var group = groups.get(type);
			if (group == null || group.isEmpty())
				return;
			var rows = ds.getElementaryFlows(type);

			// duplicate names are not allowed here
			var handledNames = new HashSet<String>();
			for (var flow : group) {

				// select name & unit
				var mapping = mappingOf(flow);
				var name = mapping == null
						? flow.name
						: mapping.flow();
				var unit = mapping == null
						? units.get(flow.getReferenceUnit())
						: units.get(mapping.unit());
				if (name == null || unit == null)
					continue;

				// skip duplicate names
				String id = name.trim().toLowerCase();
				if (handledNames.contains(id))
					continue;
				handledNames.add(id);

				// add row
				var row = new ElementaryFlowRow()
						.name(name)
						.unit(unit);
				if (mapping == null) {
					row.cas(flow.casNumber)
							.comment(flow.description);
				}
				rows.add(row);
			}
		}
	}

	/**
	 * Wraps a flow mapping entry. These mappings should be
	 * only created when they are fully qualified with all
	 * information available.
	 */
	record Mapping(
			String flow,
			String unit,
			Compartment compartment,
			double factor) {

		static Optional<Mapping> of(FlowMapEntry e, UnitMap units) {
			var target = e.targetFlow();
			if (target == null
					|| target.flow == null
					|| Strings.nullOrEmpty(target.flow.name)
					|| target.unit == null
					|| Strings.nullOrEmpty(target.unit.name))
				return Optional.empty();
			var unit = units.get(target.unit.name);
			var comp = Compartment.fromPath(target.flowCategory);
			if (comp == null)
				return Optional.empty();
			var mapping = new Mapping(
					target.flow.name, unit, comp, e.factor());
			return Optional.of(mapping);
		}
	}
}
