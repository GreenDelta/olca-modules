package org.openlca.io.simapro.csv.output;

import java.util.Optional;

import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.io.simapro.csv.Compartment;
import org.openlca.util.Strings;

/**
 * Wraps a flow mapping entry. These mappings should be
 * only created when they are fully qualified with all
 * information available.
 */
record FlowMapping(
		String flow,
		String unit,
		Compartment compartment,
		double factor) {

	static Optional<FlowMapping> of(FlowMapEntry e, UnitMap units) {
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
		var mapping = new FlowMapping(
				target.flow.name, unit, comp, e.factor());
		return Optional.of(mapping);
	}
}
