package org.openlca.proto.io.input;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Unit;
import org.openlca.proto.ProtoRef;
import org.openlca.commons.Strings;

record Quantity(Flow flow, FlowPropertyFactor factor, Unit unit) {

	public FlowProperty property() {
		return factor != null ? factor.flowProperty : null;
	}

	static Builder of(Flow flow) {
		return new Builder(flow);
	}

	static class Builder {

		private final Flow flow;
		private String unitId;
		private String propertyId;

		Builder(Flow flow) {
			this.flow = flow;
		}

		Builder withUnit(ProtoRef ref) {
			unitId = ref.getId();
			return this;
		}

		Builder withProperty(ProtoRef ref) {
			propertyId = ref.getId();
			return this;
		}

		Quantity get() {
			if (flow == null)
				return new Quantity(null, null, null);

			FlowPropertyFactor factor = null;
			if (Strings.isBlank(propertyId)) {
				factor = flow.getReferenceFactor();
			} else {
				for (var f : flow.flowPropertyFactors) {
					if (f.flowProperty == null)
						continue;
					if (propertyId.equals(f.flowProperty.refId)) {
						factor = f;
						break;
					}
				}
			}
			if (factor == null || factor.flowProperty == null)
				return new Quantity(flow, null, null);

			var group = factor.flowProperty.unitGroup;
			if (group == null)
				return new Quantity(flow, factor, null);

			if (Strings.isBlank(unitId))
				return new Quantity(flow, factor, group.referenceUnit);
			var unit = group.units.stream()
				.filter(u -> unitId.equals(u.refId))
				.findAny()
				.orElse(null);
			return new Quantity(flow, factor, unit);
		}
	}
}
