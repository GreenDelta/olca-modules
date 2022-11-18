package org.openlca.io.refdata;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.core.model.Flow;

class FlowExport implements Runnable {

	private final ExportConfig config;

	FlowExport(ExportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {
		var flows = config.db().getAll(Flow.class);
		if (flows.isEmpty())
			return;
		config.sort(flows);
		var buffer = new ArrayList<>(8);

		// write the flow data and check if we need to create
		// a flow property factor file
		var multiProps = new AtomicBoolean(false);
		config.writeTo("flows.csv", csv -> {

			// write column headers
			csv.printRecord(
					"ID",
					"Name",
					"Description",
					"Category",
					"Flow type",
					"CAS number",
					"Chem. formula",
					"Reference flow property");

			for (var flow : flows) {
				buffer.add(flow.refId);
				buffer.add(flow.name);
				buffer.add(flow.description);
				buffer.add(config.toPath(flow.category));
				buffer.add(typeOf(flow));
				buffer.add(flow.casNumber);
				buffer.add(flow.formula);

				var refProp = flow.referenceFlowProperty != null
						? flow.referenceFlowProperty.name
						: "";
				buffer.add(refProp);
				if (flow.flowPropertyFactors.size() > 1) {
					multiProps.set(true);
				}

				csv.printRecord(buffer);
				buffer.clear();
			}
		});

		// write flow property factors, if needed
		if (!multiProps.get())
			return;
		config.writeTo("flow_property_factors.csv", csv -> {

			// write column headers
			csv.printRecord(
					"Flow",
					"Flow property",
					"Conversion factor");

			for (var flow : flows) {
				if (flow.flowPropertyFactors.size() <= 1)
					continue;
				for (var f : flow.flowPropertyFactors) {
					if (f.flowProperty == null
							|| f.flowProperty.equals(flow.referenceFlowProperty))
						continue;
					var prop = f.flowProperty.name;
					buffer.add(flow.refId);
					buffer.add(prop);
					buffer.add(f.conversionFactor);
					csv.printRecord(buffer);
					buffer.clear();
				}
			}
		});
	}

	private String typeOf(Flow flow) {
		if (flow.flowType == null)
			return "";
		return switch (flow.flowType) {
			case ELEMENTARY_FLOW -> "elementary";
			case PRODUCT_FLOW -> "product";
			case WASTE_FLOW -> "waste";
		};
	}

}
