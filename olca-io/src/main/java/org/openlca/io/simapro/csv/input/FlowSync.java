package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.maps.FlowMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowSync {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase db;
	private final RefData refData;
	private final FlowMap flowMap;

	private final HashMap<String, SyncFlow> techFlows = new HashMap<>();
	private final HashMap<String, SyncFlow> elemFlows = new HashMap<>();
	private final HashMap<String, SyncFlow> mappedFlows = new HashMap<>();

	private final EnumMap<ElementaryFlowType, HashMap<String, ElementaryFlowRow>> flowInfos;

	FlowSync(IDatabase db, RefData refData, FlowMap flowMap) {
		this.db = db;
		this.refData = refData;
		this.flowMap = flowMap;
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
			log.error("failed to detect compartment: '{}'", row.compartment());
		}
		var subComp = SubCompartment.of(row.subCompartment());
		return elemFlow(Compartment.of(type, subComp), row.flow(), row.unit());
	}

	SyncFlow elemFlow(ElementaryFlowType type, ElementaryExchangeRow row) {
		var subComp = SubCompartment.of(row.subCompartment());
		return elemFlow(Compartment.of(type, subComp), row.name(), row.unit());
	}

	private SyncFlow elemFlow(Compartment comp, String name, String unit) {

		var mappingKey = SyncFlow.mappingKeyOf(comp, name, unit);
		var mappedFlow = getMappedFlow(mappingKey);
		if (mappedFlow != null)
			return mappedFlow;

		var quantity = refData.quantityOf(unit);
		if (quantity == null) {
			log.error("unknown unit {} in flow {}", unit, name);
			return SyncFlow.empty();
		}

		var refId = SyncFlow.refIdOf(comp, name, quantity);
		var syncFlow = elemFlows.get(refId);
		if (syncFlow != null)
			return syncFlow;

		var flow = db.get(Flow.class, refId);
		if (flow != null) {
			syncFlow = SyncFlow.of(flow);
			elemFlows.put(refId, syncFlow);
			return syncFlow;
		}

		flow = new Flow();
		flow.flowType = FlowType.ELEMENTARY_FLOW;
		flow.name = name;
		flow.category = categoryOf(comp);
		setFlowProperty(quantity, flow);

		var infos = flowInfos.get(comp.type());
		if (infos != null) {
			var key = Strings.orEmpty(name)
				.trim()
				.toLowerCase();
			var info = infos.get(key);
			if (info != null) {
				flow.casNumber = info.cas();
				// TODO: we could parse the chemical formula, synonyms, and
				// location from the comment string
				flow.description = info.comment();
			}
		}

		flow = db.insert(flow);
		syncFlow = SyncFlow.of(flow);
		elemFlows.put(refId, syncFlow);
		return syncFlow;
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

		var mappingKey = KeyGen.toPath(row.name(), row.unit());
		var mappedFlow = getMappedFlow(mappingKey);
		if (mappedFlow != null)
			return mappedFlow;

		var quantity = refData.quantityOf(row.unit());
		if (quantity == null) {
			log.error("unknown unit {} in flow {}", row.unit(), row.name());
			return SyncFlow.empty();
		}
		var refId = KeyGen.get(row.name(), quantity.unitGroup.refId);
		var syncFlow = techFlows.get(refId);
		if (syncFlow != null)
			return syncFlow;

		var flow = db.get(Flow.class, refId);
		if(flow != null) {
			syncFlow = SyncFlow.of(flow);
			techFlows.put(refId, syncFlow);
			return syncFlow;
		}

		flow = new Flow();
		flow.flowType = isWaste
			? FlowType.WASTE_FLOW
			: FlowType.PRODUCT_FLOW;
		flow.refId = refId;
		flow.name = row.name();
		flow.location = getProductLocation(row);
		setFlowProperty(quantity, flow);

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

		flow = db.insert(flow);
		syncFlow = SyncFlow.of(flow);
		techFlows.put(refId, syncFlow);
		return syncFlow;
	}

	private SyncFlow getMappedFlow(String mappingKey) {
		var mappedFlow = mappedFlows.get(mappingKey);
		if (mappedFlow != null)
			return mappedFlow;
		var mapEntry = flowMap.getEntry(mappingKey);
		if (mapEntry == null)
			return null;
		var flow = mapEntry.targetFlow().getMatchingFlow(db);
		if (flow == null)
			return null;
		var syncFlow = SyncFlow.ofMapped(flow, mapEntry.factor());
		mappedFlows.put(mappingKey, syncFlow);
		return syncFlow;
	}

	private Location getProductLocation(ExchangeRow row) {
		if (row.name() == null)
			return null;
		// get a 2 letter or 3 letter location code from the product name
		String codePattern = "\\{(([A-Z]{2})|([A-Z]{3}))\\}";
		Matcher matcher = Pattern.compile(codePattern).matcher(row.name());
		if (!matcher.find())
			return null;
		String code = matcher.group();
		code = code.substring(1, code.length() - 1);
		String refId = KeyGen.get(code);
		LocationDao dao = new LocationDao(db);
		return dao.getForRefId(refId);
	}

	private void setFlowProperty(UnitMappingEntry unitEntry, Flow flow) {
		flow.referenceFlowProperty = unitEntry.flowProperty;
		var factor = new FlowPropertyFactor();
		factor.conversionFactor = 1;
		factor.flowProperty = unitEntry.flowProperty;
		flow.flowPropertyFactors.add(factor);
	}
}
