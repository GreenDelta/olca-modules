package org.openlca.io.xls.process;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.openlca.core.model.Flow;
import org.openlca.core.model.RootEntity;

/**
 * Flow property factors are only written when required, which is the case
 * when a flow has more properties than its reference flow property.
 */
class OutFlowPropertyFactorSync implements OutEntitySync {

	private final OutConfig config;
	private final Set<Flow> flows = new HashSet<>();

	OutFlowPropertyFactorSync(OutConfig config) {
		this.config = config;
	}

	@Override
	public void visit(RootEntity entity) {
		if (!(entity instanceof Flow flow))
			return;
		if (flow.flowPropertyFactors.size() < 2)
			return;
		flows.add(flow);
	}

	@Override
	public void flush() {
		if (flows.isEmpty())
			return;
		var sheet = config.createSheet(Tab.FLOW_PROPERTY_FACTORS)
			.withColumnWidths(5, 25)
			.header(
				Field.FLOW,
				Field.CATEGORY,
				Field.FLOW_PROPERTY,
				Field.CONVERSION_FACTOR,
				Field.REFERENCE_UNIT);

		for (var flow : Out.sort(flows)) {
			for (var factor : flow.flowPropertyFactors) {
				var prop = factor.flowProperty;
				if (prop == null
					|| prop.unitGroup == null
					|| Objects.equals(prop, flow.referenceFlowProperty))
					continue;
				var refUnit = prop.unitGroup.referenceUnit;
				sheet.next(row ->
					row.next(flow.name)
						.next(Out.pathOf(flow))
						.next(prop.name)
						.next(factor.conversionFactor)
						.next(refUnit != null
							? refUnit.name
							: null));
			}
		}
	}
}
