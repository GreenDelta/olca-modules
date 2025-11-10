package org.openlca.io.xls.process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openlca.commons.Strings;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;

class InFlowSync {

	private final InConfig config;

	private InFlowSync(InConfig config) {
		this.config = config;
	}

	static void sync(InConfig config) {
		new InFlowSync(config).sync();
	}

	private void sync() {
		var factors = collectFactors();
		var sheet = config.getSheet(Tab.FLOWS);
		if (sheet == null)
			return;
		sheet.eachRow(row -> {
			var refId = row.str(Field.UUID);
			config.index().sync(Flow.class, refId, () -> create(row, factors));
		});

	}

	private Flow create(
		RowReader row, Map<String, List<FlowPropertyFactor>> factors) {
		var flow = new Flow();
		In.mapBase(row, flow);
		flow.category = row.syncCategory(config.db(), ModelType.FLOW);

		// reference flow property
		var prop = config.index().get(
			FlowProperty.class, row.str(Field.REFERENCE_FLOW_PROPERTY));
		if (prop == null) {
			config.log().error("invalid flow property in flow " + (row.rowNum() + 1));
		} else {
			var factor = FlowPropertyFactor.of(prop, 1);
			flow.flowPropertyFactors.add(factor);
			flow.referenceFlowProperty = prop;
		}

		// other flow properties
		var facs = factors.get(EntityIndex.flowKeyOf(flow));
		if (facs != null) {
			for (var fac : facs) {
				if (!Objects.equals(prop, fac.flowProperty)) {
					flow.flowPropertyFactors.add(fac);
				}
			}
		}

		// other fields
		flow.flowType = flowTypeOf(row);
		flow.casNumber = row.str(Field.CAS);
		flow.formula = row.str(Field.FORMULA);
		var locRef = row.str(Field.LOCATION);
		if (Strings.isNotBlank(locRef)) {
			flow.location = config.index().get(Location.class, locRef);
		}

		return flow;
	}

	private FlowType flowTypeOf(RowReader row) {
		var str = row.str(Field.TYPE);
		if (Strings.isBlank(str))
			return FlowType.ELEMENTARY_FLOW;
		char c = str.charAt(0);
		return switch (c) {
			case 'p', 'P' -> FlowType.PRODUCT_FLOW;
			case 'w', 'W' -> FlowType.WASTE_FLOW;
			default -> FlowType.ELEMENTARY_FLOW;
		};
	}

	private Map<String, List<FlowPropertyFactor>> collectFactors() {
		var sheet = config.getSheet(Tab.FLOW_PROPERTY_FACTORS);
		if (sheet == null)
			return Collections.emptyMap();

		var map = new HashMap<String, List<FlowPropertyFactor>>();
		sheet.eachRow(row -> {
			var propRef = row.str(Field.FLOW_PROPERTY);
			var prop = config.index().get(FlowProperty.class, propRef);
			if (prop == null) {
				config.log().error("invalid flow property name '"
					+ propRef + "' in flow property factors, row "
					+ (row.rowNum() + 1));
				return;
			}

			var factor = FlowPropertyFactor.of(
				prop, row.num(Field.CONVERSION_FACTOR));
			var flow = row.str(Field.FLOW);
			var category = row.str(Field.CATEGORY);
			var key = EntityIndex.flowKeyOf(flow, category);
			map.computeIfAbsent(key, $ -> new ArrayList<>()).add(factor);
		});

		return map;
	}


}
