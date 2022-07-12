package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowSync;
import org.openlca.io.maps.SyncFlow;
import org.openlca.io.simapro.csv.Compartment;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.SubCompartment;
import org.openlca.simapro.csv.method.ImpactFactorRow;
import org.openlca.simapro.csv.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.process.ExchangeRow;
import org.openlca.simapro.csv.process.ProductOutputRow;
import org.openlca.simapro.csv.process.RefExchangeRow;
import org.openlca.simapro.csv.refdata.ElementaryFlowRow;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;

class CsvFlowSync {

	private final IDatabase db;
	private final RefData refData;
	private final FlowSync flowSync;
	private final ImportLog log;
	private final EnumMap<ElementaryFlowType, HashMap<String, ElementaryFlowRow>> flowInfos;

	CsvFlowSync(IDatabase db, RefData refData, FlowMap flowMap, ImportLog log) {
		this.db = db;
		this.refData = refData;
		this.flowSync = FlowSync.of(db, flowMap);
		this.log = log;
		flowInfos = new EnumMap<>(ElementaryFlowType.class);
	}

	void sync(CsvDataSet dataSet) {
		try {

			// collect elem. flow infos
			for (var type : ElementaryFlowType.values()) {
				for (var row : dataSet.getElementaryFlows(type)) {
					var name = Strings.orEmpty(row.name())
						.trim()
						.toLowerCase();
					flowInfos.computeIfAbsent(type, t -> new HashMap<>())
						.put(name, row);
				}
			}

			// sync reference flows; other flows are
			// synced on demand
			for (var process : dataSet.processes()) {
				var topCategory = process.category() != null
					? process.category().toString()
					: null;
				for (var product : process.products()) {
					techFlow(product, topCategory, false);
				}
				if (process.wasteTreatment() != null) {
					techFlow(process.wasteTreatment(), topCategory, true);
				}
				if (process.wasteScenario() != null) {
					techFlow(process.wasteScenario(), topCategory, true);
				}
			}

			for (var stage : dataSet.productStages()) {
				var topCategory = stage.category() != null
					? stage.category().toString()
					: null;
				for (var product : stage.products()) {
					techFlow(product, topCategory, false);
				}
			}

		} catch (Exception e) {
			log.error("failed to synchronize flows with database", e);
		}
	}

	SyncFlow elemFlow(ImpactFactorRow row) {
		var type = ElementaryFlowType.of(row.compartment());
		if (type == null) {
			log.error("failed to detect compartment: " + row.compartment());
		}
		var subComp = SubCompartment.of(row.subCompartment());
		return elemFlow(Compartment.of(type, subComp), row.flow(), row.unit());
	}

	SyncFlow elemFlow(ElementaryFlowType type, ElementaryExchangeRow row) {
		var subComp = SubCompartment.of(row.subCompartment());
		return elemFlow(Compartment.of(type, subComp), row.name(), row.unit());
	}

	private SyncFlow elemFlow(Compartment comp, String name, String unit) {

		// calculate the key
		var quantity = refData.quantityOf(unit);
		if (quantity == null) {
			log.error("unknown unit '" + unit + "' in flow: " +  name);
			return SyncFlow.empty();
		}
		var key = FlowKey.elementary(comp, name, unit);

		// get or create the flow
		return key.getOrCreate(flowSync, () -> {
			var flow = initFlow(key, name, quantity);
			flow.category = categoryOf(comp);

			var infos = flowInfos.get(comp.type());
			if (infos != null) {
				var infoKey = Strings.orEmpty(name)
					.trim()
					.toLowerCase();
				var info = infos.get(infoKey);
				if (info != null) {
					flow.casNumber = info.cas();
					// TODO: we could parse the chemical formula, synonyms, and
					// location from the comment string
					flow.description = info.comment();
				}
			}
			flow =  db.insert(flow);
			log.imported(flow);
			return flow;
		});
	}

	private Category categoryOf(Compartment comp) {
		if (comp == null || comp.type() == null)
			return null;
		var sub = comp.sub() == null
			? SubCompartment.UNSPECIFIED.toString()
			: comp.sub().toString();
		return CategoryDao.sync(db, ModelType.FLOW,
			comp.type().exchangeHeader(), sub);
	}

	SyncFlow product(ExchangeRow row) {
		return techFlow(row, null, false);
	}

	SyncFlow waste(ExchangeRow row) {
		return techFlow(row, null, true);
	}

	private SyncFlow techFlow(
		ExchangeRow row, String topCategory, boolean isWaste) {

		// calculate the key
		var quantity = refData.quantityOf(row.unit());
		if (quantity == null) {
			log.error("unknown unit '"+ row.unit() + "' in flow: " + row.name());
			return SyncFlow.empty();
		}
		var key = isWaste
			? FlowKey.waste(row.name(), row.unit())
			:FlowKey.product(row.name(), row.unit());

		// get or create the flow
		return key.getOrCreate(flowSync, () -> {

			var flow = initFlow(key, row.name(), quantity);
			flow.location = getProductLocation(row);

			// create the flow category
			var categoryPath = new ArrayList<String>();
			if (Strings.notEmpty(topCategory)) {
				categoryPath.add(topCategory);
			}
			if (row instanceof RefExchangeRow refRow) {
				if (Strings.notEmpty(refRow.category())) {
					var segments = refRow.category().split("\\\\");
					categoryPath.addAll(Arrays.asList(segments));
				}
			}
			if (!categoryPath.isEmpty()) {
				flow.category = CategoryDao.sync(
					db, ModelType.FLOW, categoryPath.toArray(String[]::new));
			}

			// description and tags
			if (row instanceof RefExchangeRow) {
				flow.description = row.comment();
				if (row instanceof ProductOutputRow productRow) {
					if (Strings.notEmpty(productRow.wasteType())) {
						flow.tags = productRow.wasteType();
					}
				}
			}

			flow =  db.insert(flow);
			log.imported(flow);
			return flow;
		});
	}

	private Location getProductLocation(ExchangeRow row) {
		if (row.name() == null)
			return null;
		var matcher = Pattern.compile("\\{([A-Za-z]+)}").matcher(row.name());
		if (!matcher.find())
			return null;
		var code = matcher.group();
		code = code.substring(1, code.length() - 1);
		var refId = KeyGen.get(code);
		return db.get(Location.class, refId);
	}

	private Flow initFlow(FlowKey key, String name, UnitMappingEntry q) {
		var flow = new Flow();
		flow.flowType = key.type();
		flow.refId = key.refId();
		flow.name = name;
		flow.referenceFlowProperty = q.flowProperty;
		var factor = new FlowPropertyFactor();
		factor.conversionFactor = 1;
		factor.flowProperty = q.flowProperty;
		flow.flowPropertyFactors.add(factor);
		return flow;
	}

}
