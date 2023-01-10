package org.openlca.io.xls.process.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Row;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.io.xls.process.Field;
import org.openlca.util.Strings;

class Flows {

	private final ProcessWorkbook wb;

	private Flows(ProcessWorkbook wb) {
		this.wb = wb;
	}

	static void sync(ProcessWorkbook wb) {
		new Flows(wb).sync();
	}

	private void sync() {
		var factors = collectFactors();
		var sheet = wb.getSheet("Flows");
		if (sheet == null)
			return;
		var fields = FieldMap.parse(sheet.getRow(0));
		if (fields.isEmpty())
			return;

		sheet.rowIterator().forEachRemaining(row -> {
			if (row.getRowNum() == 0)
				return;
			var refId = fields.str(row, Field.UUID);
			wb.index.sync(Flow.class, refId, () -> create(row, fields, factors));
		});
	}

	private Flow create(
			Row row, FieldMap fields, Map<String, List<FlowPropertyFactor>> factors) {
		var flow = new Flow();
		Util.mapBase(row, fields, flow);
		flow.category = fields.category(row, ModelType.FLOW, wb.db);

		// reference flow property
		var prop = wb.index.get(FlowProperty.class,
				fields.str(row, Field.FLOW_PROPERTY));
		if (prop == null) {
			wb.log.error("invalid flow property in flow " + (row.getRowNum() + 1));
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
		flow.flowType = flowTypeOf(row, fields);
		flow.casNumber = fields.str(row, Field.CAS);
		flow.formula = fields.str(row, Field.FORMULA);
		var locRef = fields.str(row, Field.LOCATION);
		if (Strings.notEmpty(locRef)) {
			flow.location = wb.index.get(Location.class, locRef);
		}

		return flow;
	}

	private FlowType flowTypeOf(Row row, FieldMap fields) {
		var str = fields.str(row, Field.TYPE);
		if (Strings.nullOrEmpty(str))
			return FlowType.ELEMENTARY_FLOW;
		char c = str.charAt(0);
		return switch (c) {
			case 'p', 'P' -> FlowType.PRODUCT_FLOW;
			case 'w', 'W' -> FlowType.WASTE_FLOW;
			default -> FlowType.ELEMENTARY_FLOW;
		};
	}

	private Map<String, List<FlowPropertyFactor>> collectFactors() {
		var sheet = wb.getSheet("Flow property factors");
		if (sheet == null)
			return Collections.emptyMap();
		var fields = FieldMap.parse(sheet.getRow(0));
		if (fields.isEmpty())
			return Collections.emptyMap();

		var map = new HashMap<String, List<FlowPropertyFactor>>();
		sheet.rowIterator().forEachRemaining(row -> {
			if (row.getRowNum() == 0)
				return;
			var propRef = fields.str(row, Field.FLOW_PROPERTY);
			var prop = wb.index.get(FlowProperty.class, propRef);
			if (prop == null) {
				wb.log.error("invalid flow property name '"
						+ propRef + "' in flow property factors, row "
						+ (row.getRowNum() + 1));
				return;
			}

			var factor = FlowPropertyFactor.of(
					prop, fields.num(row, Field.CONVERSION_FACTOR));
			var flow = fields.str(row, Field.FLOW);
			var category = fields.str(row, Field.CATEGORY);
			var key = EntityIndex.flowKeyOf(flow, category);
			map.computeIfAbsent(key, $ -> new ArrayList<FlowPropertyFactor>())
					.add(factor);
		});

		return map;
	}


}
