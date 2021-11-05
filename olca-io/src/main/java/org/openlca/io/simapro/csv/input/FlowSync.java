package org.openlca.io.simapro.csv.input;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
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
import org.openlca.io.maps.MapFactor;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.ProductType;
import org.openlca.simapro.csv.enums.SubCompartment;
import org.openlca.simapro.csv.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.process.ExchangeRow;
import org.openlca.simapro.csv.process.ProductOutputRow;
import org.openlca.simapro.csv.process.TechExchangeRow;
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

	private final HashMap<String, SyncFlow> products = new HashMap<>();
	private final HashMap<String, SyncFlow> elemFlows = new HashMap<>();
	private final HashMap<String, SyncFlow> mappedFlows = new HashMap<>();

	private final EnumMap<ElementaryFlowType, HashMap<String, ElementaryFlowRow>> flowInfos;

	FlowSync(IDatabase db,RefData refData, FlowMap flowMap) {
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

			for (var row : index.getProducts()) {
				syncProduct(row, refData);
			}

		} catch (Exception e) {
			log.error("failed to synchronize flows with database", e);
		}
	}

	SyncFlow get(ElementaryFlowType type, ElementaryExchangeRow row) {

		//
		var mappingKey = SyncFlow.mappingKeyOf(type, row);
		var mappedFlow = mappedFlows.get(mappingKey);
		if (mappedFlow != null)
			return mappedFlow;
		flowMap.getEntry(mappingKey);

		var unit = refData.getUnit(row.unit());
		if (unit == null) {
			log.error("unknown unit {} in flow {}", row.unit(), row.name());
			return SyncFlow.empty();
		}
		var subCompartment = SubCompartment.of(row.subCompartment());
		if (subCompartment == null) {
			subCompartment = SubCompartment.UNSPECIFIED;
		}

		var refId = KeyGen.get(
			type.exchangeHeader(),
			subCompartment.toString(),
			row.name(),
			unit.unitGroup.refId);

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
		flow.name = row.name();
		flow.category = CategoryDao.sync(
			db, ModelType.FLOW, "Elementary flows",
			type.exchangeHeader(), subCompartment.toString());
		setFlowProperty(unit, flow);

		var infos = flowInfos.get(type);
		if (infos != null) {
			var key = Strings.orEmpty(row.name())
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



	private Category getCategory(ElementaryExchangeRow row, ElementaryFlowType type) {
		if (row == null || type == null)
			return null;
		var sub = Strings.notEmpty(row.subCompartment())
			? row.subCompartment()
			: "unspecified";
		return CategoryDao.sync(db, ModelType.FLOW, type.exchangeHeader(), sub);
	}


	private void syncElemFlow(ElementaryExchangeRow row,
			ElementaryFlowType type) {
		var key = Flows.getMappingID(type, row);
		var mapEntry = flowMap.getEntry(key);
		MapFactor<Flow> mappedFlow = null;
		if (mapEntry != null && mapEntry.targetFlow != null) {
			var flow = mapEntry.targetFlow.getMatchingFlow(db);
			if (flow != null) {
				mappedFlow = new MapFactor<>(flow, mapEntry.factor);
			}
		}
		if (mappedFlow != null)
			refData.putMappedFlow(key, mappedFlow);
		else {
			Flow elemFlow = getElementaryFlow(row, type, key);
			refData.putElemFlow(key, elemFlow);
		}
	}

	private void syncProduct(ExchangeRow row, RefData refData) {
		if (row instanceof ProductOutputRow) {
			Flow flow = getProductFlow((ProductOutputRow) row);
			refData.putProduct(row.name(), flow);
		} else if (row instanceof TechExchangeRow pRow) {
			ProductType type = index.getProductType(pRow);
			Flow flow = getProductFlow(pRow, type);
			refData.putProduct(row.name, flow);
		}
	}

	private Flow getProductFlow(ProductOutputRow row) {
		String refId = getProductRefId(row);
		if (refId == null)
			return null;
		Flow flow = dao.getForRefId(refId);
		if (flow != null)
			return flow;
		flow = createProductFlow(refId, row);
		flow.category = Strings.nullOrEmpty(row.category())
			?  null
			: CategoryDao.sync(db, ModelType.FLOW, row.category().split("\\\\"));
		dao.insert(flow);
		return flow;
	}

	private Flow getProductFlow(TechExchangeRow row, ProductType type) {
		var refId = getProductRefId(row);
		if (refId == null)
			return null;
		var flow = dao.getForRefId(refId);
		if (flow != null)
			return flow;
		flow = createProductFlow(refId, row);
		flow.category = type != null
			? CategoryDao.sync(db, ModelType.FLOW, type.toString())
			: null;
		dao.insert(flow);
		return flow;
	}

	/**
	 * Returns null if no unit / property pair could be found.
	 */
	private String getProductRefId(ExchangeRow row) {
		UnitMappingEntry unitEntry = unitMapping.getEntry(row.unit());
		if (unitEntry == null) {
			log.error("could not find unit {} in database", row.unit());
			return null;
		}
		// we take the olca-flow property, because the unit name may changes
		// in different data sets
		return KeyGen.get(row.name(), unitEntry.flowProperty.refId);
	}

	private Flow createProductFlow(String refId, ExchangeRow row) {
		UnitMappingEntry unitEntry = unitMapping.getEntry(row.unit());
		Flow flow;
		flow = new Flow();
		flow.refId = refId;
		flow.name = Strings.cut(row.name(), 250);
		flow.description = getProductDescription(row);
		flow.flowType = FlowType.PRODUCT_FLOW;
		flow.location = getProductLocation(row);
		setFlowProperty(unitEntry, flow);
		return flow;
	}

	private String getProductDescription(ExchangeRow row) {
		if (row == null)
			return null;
		String description = "Imported from SimaPro";
		if (row.comment() != null)
			description += "\n" + row.comment();

		if (!(row instanceof RefProductRow))
			return description;
		RefProductRow refRow = (RefProductRow) row;
		if (refRow.wasteType != null)
			description += "\nWaste type: " + refRow.wasteType;
		return description;
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
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.conversionFactor = 1;
		factor.flowProperty = unitEntry.flowProperty;
		flow.flowPropertyFactors.add(factor);
	}






}
