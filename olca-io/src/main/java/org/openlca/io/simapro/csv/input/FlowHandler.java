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
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.model.process.ProductOutputRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FlowHandler {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase database;
	private final UnitMapping unitMapping;

	public FlowHandler(IDatabase database) {
		this.database = database;
		this.unitMapping = UnitMapping.createDefault(database);
	}

	public Flow getProductFlow(ProductOutputRow row) {
		UnitMappingEntry unitEntry = unitMapping.getEntry(row.getUnit());
		if (unitEntry == null) {
			log.error("could not find unit {} in database", row.getUnit());
			return null;
		}
		// we take the olca-flow property, because the unit name may changes
		// in different data sets
		String refId = KeyGen.get(row.getName(), unitEntry.getFlowProperty()
				.getRefId(), row.getCategory());
		FlowDao flowDao = new FlowDao(database);
		Flow flow = flowDao.getForRefId(refId);
		if (flow != null)
			return flow;
		flow = createProductFlow(refId, row, unitEntry);
		flowDao.insert(flow);
		return flow;
	}

	private Flow createProductFlow(String refId, ProductOutputRow row,
			UnitMappingEntry unitEntry) {
		Flow flow;
		flow = new Flow();
		flow.setRefId(refId);
		flow.setName(row.getName());
		flow.setDescription(getProductDescription(row));
		flow.setCategory(getProductCategory(row));
		flow.setFlowType(FlowType.PRODUCT_FLOW);
		flow.setLocation(getProductLocation(row));
		flow.setReferenceFlowProperty(unitEntry.getFlowProperty());
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setConversionFactor(1);
		factor.setFlowProperty(unitEntry.getFlowProperty());
		flow.getFlowPropertyFactors().add(factor);
		return flow;
	}

	private String getProductDescription(ProductOutputRow row) {
		if (row == null)
			return null;
		String description = "Imported from SimaPro";
		if (row.getWasteType() != null)
			description += "\nWaste type: " + row.getWasteType();
		if (row.getComment() != null)
			description += "\n" + row.getComment();
		return description;
	}

	private Category getProductCategory(ProductOutputRow row) {
		if (row.getCategory() == null)
			return null;
		String[] path = row.getCategory().split("\\\\");
		return Categories.findOrAdd(database, ModelType.FLOW, path);
	}

	private Location getProductLocation(ProductOutputRow row) {
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

	public Flow getElementaryFlow(ElementaryExchangeRow row) {

	}

}
