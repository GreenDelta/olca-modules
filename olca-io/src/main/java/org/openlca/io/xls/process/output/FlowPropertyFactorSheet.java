package org.openlca.io.xls.process.output;

import org.openlca.core.model.Flow;
import org.openlca.core.model.RootEntity;
import org.openlca.io.CategoryPath;
import org.openlca.io.xls.Excel;
import org.openlca.io.xls.process.Field;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Flow property factors are only written when required, which is the case
 * when a flow has more properties than its reference flow property.
 */
class FlowPropertyFactorSheet implements EntitySheet {

	private final ProcessWorkbook config;
	private final Set<Flow> flows = new HashSet<>();

	FlowPropertyFactorSheet(ProcessWorkbook config) {
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
		var cursor = config.createCursor("Flow property factors");
		cursor.header(
				Field.FLOW,
				Field.CATEGORY,
				Field.FLOW_PROPERTY,
				Field.CONVERSION_FACTOR,
				Field.REFERENCE_UNIT);

		for (var flow : Util.sort(flows)) {
			for (var factor : flow.flowPropertyFactors) {
				var prop = factor.flowProperty;
				if (prop == null
						|| prop.unitGroup == null
						|| Objects.equals(prop, flow.referenceFlowProperty))
					continue;
				cursor.next(row -> {
					Excel.cell(row, 0, flow.name);
					Excel.cell(row, 1, CategoryPath.getFull(flow.category));
					Excel.cell(row, 2, prop.name);
					Excel.cell(row, 3, factor.conversionFactor);
					var refUnit = prop.unitGroup.referenceUnit;
					if (refUnit != null) {
						Excel.cell(row, 4, refUnit.name);
					}
				});
			}
		}
	}
}
