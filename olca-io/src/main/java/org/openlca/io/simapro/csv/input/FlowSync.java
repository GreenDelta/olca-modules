package org.openlca.io.simapro.csv.input;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.io.Categories;
import org.openlca.io.UnitMapping;
import org.openlca.io.UnitMappingEntry;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.MapFactor;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProductType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.model.process.ExchangeRow;
import org.openlca.simapro.csv.model.process.ProductExchangeRow;
import org.openlca.simapro.csv.model.process.RefProductRow;
import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowSync {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final SpRefDataIndex index;
	private final FlowDao dao;
	private final IDatabase db;
	private final UnitMapping unitMapping;
	private FlowMap flowMap;

	public FlowSync(
			SpRefDataIndex index,
			UnitMapping unitMapping,
			IDatabase database) {
		this.index = index;
		this.unitMapping = unitMapping;
		this.db = database;
		this.dao = new FlowDao(database);
	}

	public void run(RefData refData, FlowMap flowMap) {
		this.flowMap = flowMap;
		log.trace("synchronize flows with database");
		try {
			for (var row : index.getProducts()) {
				syncProduct(row, refData);
			}
			for (var type : ElementaryFlowType.values()) {
				for (var row : index.getElementaryFlows(type)) {
					syncElemFlow(row, type, refData);
				}
			}
		} catch (Exception e) {
			log.error("failed to synchronize flows with database", e);
		}
	}

	private void syncElemFlow(ElementaryExchangeRow row,
			ElementaryFlowType type, RefData refData) {
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
		if (row instanceof RefProductRow) {
			Flow flow = getProductFlow((RefProductRow) row);
			refData.putProduct(row.name, flow);
		} else if (row instanceof ProductExchangeRow) {
			ProductExchangeRow pRow = (ProductExchangeRow) row;
			ProductType type = index.getProductType(pRow);
			Flow flow = getProductFlow(pRow, type);
			refData.putProduct(row.name, flow);
		}
	}

	private Flow getProductFlow(RefProductRow row) {
		String refId = getProductRefId(row);
		if (refId == null)
			return null;
		Flow flow = dao.getForRefId(refId);
		if (flow != null)
			return flow;
		flow = createProductFlow(refId, row);
		flow.category = getProductCategory(row);
		dao.insert(flow);
		return flow;
	}

	private Flow getProductFlow(ProductExchangeRow row, ProductType type) {
		String refId = getProductRefId(row);
		if (refId == null)
			return null;
		Flow flow = dao.getForRefId(refId);
		if (flow != null)
			return flow;
		flow = createProductFlow(refId, row);
		flow.category = getProductCategory(type);
		dao.insert(flow);
		return flow;
	}

	/**
	 * Returns null if no unit / property pair could be found.
	 */
	private String getProductRefId(ExchangeRow row) {
		UnitMappingEntry unitEntry = unitMapping.getEntry(row.unit);
		if (unitEntry == null) {
			log.error("could not find unit {} in database", row.unit);
			return null;
		}
		// we take the olca-flow property, because the unit name may changes
		// in different data sets
		return KeyGen
				.get(row.name, unitEntry.flowProperty.refId);
	}

	private Flow createProductFlow(String refId, ExchangeRow row) {
		UnitMappingEntry unitEntry = unitMapping.getEntry(row.unit);
		Flow flow;
		flow = new Flow();
		flow.refId = refId;
		flow.name = Strings.cut(row.name, 250);
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
		if (row.comment != null)
			description += "\n" + row.comment;
		if (!(row instanceof RefProductRow))
			return description;
		RefProductRow refRow = (RefProductRow) row;
		if (refRow.wasteType != null)
			description += "\nWaste type: " + refRow.wasteType;
		return description;
	}

	private Category getProductCategory(ProductType type) {
		if (type == null)
			return null;
		String[] path = new String[] { type.getHeader() };
		return Categories.findOrAdd(db, ModelType.FLOW, path);
	}

	private Category getProductCategory(RefProductRow row) {
		if (row.category == null)
			return null;
		String[] path = row.category.split("\\\\");
		return Categories.findOrAdd(db, ModelType.FLOW, path);
	}

	private Location getProductLocation(ExchangeRow row) {
		if (row.name == null)
			return null;
		// get a 2 letter or 3 letter location code from the product name
		String codePattern = "\\{(([A-Z]{2})|([A-Z]{3}))\\}";
		Matcher matcher = Pattern.compile(codePattern).matcher(row.name);
		if (!matcher.find())
			return null;
		String code = matcher.group();
		code = code.substring(1, code.length() - 1);
		String refId = KeyGen.get(code);
		LocationDao dao = new LocationDao(db);
		return dao.getForRefId(refId);
	}

	private Flow getElementaryFlow(ElementaryExchangeRow row,
			ElementaryFlowType type, String refId) {
		String unit = row.unit;
		UnitMappingEntry unitEntry = unitMapping.getEntry(unit);
		if (unitEntry == null) {
			log.error("could not find unit {} in database", unit);
			return null;
		}
		Flow flow = dao.getForRefId(refId);
		if (flow != null)
			return flow;
		flow = new Flow();
		flow.refId = refId;
		flow.name = row.name;
		flow.category = getElementaryFlowCategory(row, type);
		flow.flowType = FlowType.ELEMENTARY_FLOW;
		setFlowProperty(unitEntry, flow);
		ElementaryFlowRow flowInfo = index.getFlowInfo(row.name, type);
		setFlowData(flow, flowInfo);
		dao.insert(flow);
		return flow;
	}

	private void setFlowData(Flow flow, ElementaryFlowRow flowRow) {
		if (flow == null || flowRow == null)
			return;
		flow.casNumber = flowRow.casNumber;
		flow.description = flowRow.comment;
		// TODO: we could parse the chemical formula, synonyms, and
		// location from the comment string
	}

	private Category getElementaryFlowCategory(
			ElementaryExchangeRow exchangeRow, ElementaryFlowType type) {
		if (exchangeRow == null || type == null)
			return null;
		String[] path;
		String subCompartment = exchangeRow.subCompartment;
		if (subCompartment != null && !subCompartment.isEmpty())
			path = new String[] { type.getExchangeHeader(), subCompartment };
		else
			path = new String[] { type.getExchangeHeader(), "Unspecified" };
		return Categories.findOrAdd(db, ModelType.FLOW, path);
	}

	private void setFlowProperty(UnitMappingEntry unitEntry, Flow flow) {
		flow.referenceFlowProperty = unitEntry.flowProperty;
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.conversionFactor = 1;
		factor.flowProperty = unitEntry.flowProperty;
		flow.flowPropertyFactors.add(factor);
	}
}
