package org.openlca.io.refdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

class FlowImport implements Runnable {

	private final ImportConfig config;

	FlowImport(ImportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {

		var flows = new HashMap<String, Flow>();
		config.eachRowOf("flows.csv", row -> {
			var flow = new Flow();
			flow.refId = row.get(0);
			flow.name = row.get(1);
			flow.description = row.get(2);
			flow.category = config.category(ModelType.FLOW, row.get(3));
			flow.flowType = typeOf(row.get(4));
			flow.casNumber = row.get(5);
			flow.formula = row.get(6);

			var prop = config.get(FlowProperty.class, row.get(7));
			if (prop == null) {
				config.log().error("unknown flow property: " + row.get(7));
			} else {
				var factor = FlowPropertyFactor.of(prop, 1);
				flow.flowPropertyFactors.add(factor);
				flow.referenceFlowProperty = prop;
			}

			flows.put(flow.refId, flow);
		});


		// add more flow properties if present
		config.eachRowOf("flow_property_factors.csv", row -> {
			var flow = flows.get(row.get(0));
			if (flow == null) {
				config.log().error("unknown flow: " + row.get(0));
				return;
			}
			var prop = config.get(FlowProperty.class, row.get(1));
			if (prop == null) {
				config.log().error("unknown flow property: " + row.get(1));
				return;
			}
			if (Objects.equals(prop, flow.referenceFlowProperty))
				return;
			var factor = FlowPropertyFactor.of(prop, row.getDouble(2));
			flow.flowPropertyFactors.add(factor);
		});

		config.insert(new ArrayList<>(flows.values()));
	}

	private FlowType typeOf(String s) {
		if (Strings.nullOrEmpty(s))
			return FlowType.ELEMENTARY_FLOW;
		var c = s.trim().charAt(0);
		return switch (c) {
			case 'p', 'P' -> FlowType.PRODUCT_FLOW;
			case 'w', 'W' -> FlowType.WASTE_FLOW;
			default -> FlowType.ELEMENTARY_FLOW;
		};
	}
}
