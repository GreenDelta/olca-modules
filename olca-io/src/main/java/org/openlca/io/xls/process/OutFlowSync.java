package org.openlca.io.xls.process;

import java.util.HashSet;
import java.util.Set;

import org.openlca.core.model.Flow;
import org.openlca.core.model.RootEntity;

class OutFlowSync implements OutEntitySync {

	private final OutConfig config;
	private final Set<Flow> flows = new HashSet<>();

	OutFlowSync(OutConfig wb) {
		this.config = wb;
	}

	@Override
	public void visit(RootEntity entity) {
		if (entity instanceof Flow flow) {
			flows.add(flow);
		}
	}

	@Override
	public void flush() {
		var sheet = config.createSheet(Tab.FLOWS)
			.withColumnWidths(10, 25)
			.header(
				Field.UUID,
				Field.NAME,
				Field.DESCRIPTION,
				Field.CATEGORY,
				Field.VERSION,
				Field.LAST_CHANGE,
				Field.TYPE,
				Field.CAS,
				Field.FORMULA,
				Field.LOCATION,
				Field.REFERENCE_FLOW_PROPERTY);

		for (var flow : Out.sort(flows)) {
			sheet.next(row ->
				row.next(flow.refId)
					.next(flow.name)
					.next(flow.description)
					.next(Out.pathOf(flow))
					.nextAsVersion(flow.version)
					.nextAsDate(flow.lastChange)
					.next(getType(flow))
					.next(flow.casNumber)
					.next(flow.formula)
					.next(flow.location != null
						? flow.location.name
						: null)
					.next(flow.referenceFlowProperty != null
						? flow.referenceFlowProperty.name
						: null));
		}
	}


	private String getType(Flow flow) {
		if (flow.flowType == null)
			return "Elementary flow";
		return switch (flow.flowType) {
			case PRODUCT_FLOW -> "Product flow";
			case WASTE_FLOW -> "Waste flow";
			default -> "Elementary flow";
		};
	}
}
