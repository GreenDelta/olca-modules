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
import org.openlca.io.KeyGen;
import org.openlca.io.UnitMapping;
import org.openlca.io.UnitMappingEntry;
import org.openlca.simapro.csv.model.AbstractExchangeRow;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.ProductType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.model.process.ProductExchangeRow;
import org.openlca.simapro.csv.model.process.RefProductRow;
import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowHandler {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase database;
	private final UnitMapping unitMapping;
	private final FlowDao dao;

	public FlowHandler(IDatabase database) {
		this.database = database;
		this.dao = new FlowDao(database);
		this.unitMapping = UnitMapping.createDefault(database);
	}

	public Flow getProductFlow(RefProductRow row) {
		String refId = getProductRefId(row);
		if (refId == null)
			return null;
		Flow flow = dao.getForRefId(refId);
		if (flow != null)
			return flow;
		flow = createProductFlow(refId, row);
		flow.setCategory(getProductCategory(row));
		dao.insert(flow);
		return flow;
	}

	/**
	 * You should always first check if the product can be found in the output
	 * products of another process because via this method you do not get the
	 * category path, waste type, and other information that are only available
	 * in the reference product rows (see {@link #getProductFlow(RefProductRow)}
	 * ).
	 */
	public Flow getProductFlow(ProductExchangeRow row, ProductType type) {
		String refId = getProductRefId(row);
		if (refId == null)
			return null;
		Flow flow = dao.getForRefId(refId);
		if (flow != null)
			return flow;
		flow = createProductFlow(refId, row);
		flow.setCategory(getProductCategory(type));
		dao.insert(flow);
		return flow;
	}

	/** Returns null if no unit / property pair could be found. */
	private String getProductRefId(AbstractExchangeRow row) {
		UnitMappingEntry unitEntry = unitMapping.getEntry(row.getUnit());
		if (unitEntry == null) {
			log.error("could not find unit {} in database", row.getUnit());
			return null;
		}
		// we take the olca-flow property, because the unit name may changes
		// in different data sets
		return KeyGen
				.get(row.getName(), unitEntry.getFlowProperty().getRefId());
	}

	private Flow createProductFlow(String refId, AbstractExchangeRow row) {
		UnitMappingEntry unitEntry = unitMapping.getEntry(row.getUnit());
		Flow flow;
		flow = new Flow();
		flow.setRefId(refId);
		flow.setName(row.getName());
		flow.setDescription(getProductDescription(row));
		flow.setFlowType(FlowType.PRODUCT_FLOW);
		flow.setLocation(getProductLocation(row));
		setFlowProperty(unitEntry, flow);
		return flow;
	}

	private String getProductDescription(AbstractExchangeRow row) {
		if (row == null)
			return null;
		String description = "Imported from SimaPro";
		if (row.getComment() != null)
			description += "\n" + row.getComment();
		if (!(row instanceof RefProductRow))
			return description;
		RefProductRow refRow = (RefProductRow) row;
		if (refRow.getWasteType() != null)
			description += "\nWaste type: " + refRow.getWasteType();
		return description;
	}

	private Category getProductCategory(ProductType type) {
		if (type == null)
			return null;
		String[] path = new String[] { type.getHeader() };
		return Categories.findOrAdd(database, ModelType.FLOW, path);
	}

	private Category getProductCategory(RefProductRow row) {
		if (row.getCategory() == null)
			return null;
		String[] path = row.getCategory().split("\\\\");
		return Categories.findOrAdd(database, ModelType.FLOW, path);
	}

	private Location getProductLocation(AbstractExchangeRow row) {
		if (row.getName() == null)
			return null;
		// get a 2 letter or 3 letter location code from the product name
		String codePattern = "\\{(([A-Z]{2})|([A-Z]{3}))\\}";
		Matcher matcher = Pattern.compile(codePattern).matcher(row.getName());
		if (!matcher.find())
			return null;
		String code = matcher.group();
		code = code.substring(1, code.length() - 1);
		String refId = KeyGen.get(code);
		LocationDao dao = new LocationDao(database);
		return dao.getForRefId(refId);
	}

	public Flow getElementaryFlow(ElementaryExchangeRow exchangeRow,
			ElementaryFlowType type, ElementaryFlowRow flowRow) {
		String unit = exchangeRow.getUnit();
		UnitMappingEntry unitEntry = unitMapping.getEntry(unit);
		if (unitEntry == null) {
			log.error("could not find unit {} in database", unit);
			return null;
		}
		String refId = KeyGen
				.get(exchangeRow.getName(), type.getExchangeHeader(),
						exchangeRow.getSubCompartment(), unit);
		Flow flow = dao.getForRefId(refId);
		if (flow != null)
			return flow;
		flow = new Flow();
		flow.setRefId(refId);
		flow.setName(exchangeRow.getName());
		flow.setCategory(getElementaryFlowCategory(exchangeRow, type));
		flow.setFlowType(FlowType.ELEMENTARY_FLOW);
		setFlowProperty(unitEntry, flow);
		setFlowData(flow, flowRow);
		dao.insert(flow);
		return flow;
	}

	private void setFlowData(Flow flow, ElementaryFlowRow flowRow) {
		if (flow == null || flowRow == null)
			return;
		flow.setCasNumber(flowRow.getCASNumber());
		flow.setDescription(flowRow.getComment());
		// TODO: we could parse the chemical formula, synonyms, and
		// location from the comment string
	}

	private Category getElementaryFlowCategory(
			ElementaryExchangeRow exchangeRow, ElementaryFlowType type) {
		if (exchangeRow == null || type == null)
			return null;
		String[] path = null;
		if (exchangeRow.getSubCompartment() != null)
			path = new String[] { type.getExchangeHeader(),
					exchangeRow.getSubCompartment() };
		else
			path = new String[] { type.getExchangeHeader(), "Unspecified" };
		return Categories.findOrAdd(database, ModelType.FLOW, path);
	}

	private void setFlowProperty(UnitMappingEntry unitEntry, Flow flow) {
		flow.setReferenceFlowProperty(unitEntry.getFlowProperty());
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setConversionFactor(1);
		factor.setFlowProperty(unitEntry.getFlowProperty());
		flow.getFlowPropertyFactors().add(factor);
	}

}
