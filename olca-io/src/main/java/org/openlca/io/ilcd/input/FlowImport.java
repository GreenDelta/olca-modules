package org.openlca.io.ilcd.input;

import java.util.Date;
import java.util.List;

import org.openlca.core.database.FlowDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.util.FlowBag;
import org.openlca.ilcd.util.Flows;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final ImportConfig config;
	private FlowBag ilcdFlow;
	private Flow flow;

	public FlowImport(ImportConfig config) {
		this.config = config;
	}

	public Flow run(org.openlca.ilcd.flows.Flow flow) throws ImportException {
		this.ilcdFlow = new FlowBag(flow, config.langs);
		Flow oFlow = findExisting(ilcdFlow.getId());
		if (oFlow != null)
			return oFlow;
		return createNew();
	}

	public Flow run(String flowId) throws ImportException {
		Flow flow = findExisting(flowId);
		if (flow != null)
			return flow;
		org.openlca.ilcd.flows.Flow iFlow = tryGetFlow(flowId);
		ilcdFlow = new FlowBag(iFlow, config.langs);
		return createNew();
	}

	private Flow findExisting(String flowId) throws ImportException {
		try {
			FlowDao dao = new FlowDao(config.db);
			return dao.getForRefId(flowId);
		} catch (Exception e) {
			String message = String
					.format("Search for flow %s failed.", flowId);
			throw new ImportException(message, e);
		}
	}

	private Flow createNew() throws ImportException {
		flow = new Flow();
		importCategory();
		createAndMapContent();
		saveInDatabase(flow);
		return flow;
	}

	private org.openlca.ilcd.flows.Flow tryGetFlow(String flowId)
			throws ImportException {
		try {
			org.openlca.ilcd.flows.Flow iFlow = config.store.get(
					org.openlca.ilcd.flows.Flow.class, flowId);
			if (iFlow == null) {
				throw new ImportException("No ILCD flow for ID " + flowId
						+ " found");
			}
			return iFlow;
		} catch (Exception e) {
			throw new ImportException(e.getMessage(), e);
		}
	}

	private void importCategory() throws ImportException {
		org.openlca.ilcd.commons.FlowType t = Flows.getType(ilcdFlow.flow);
		if (t == org.openlca.ilcd.commons.FlowType.ELEMENTARY_FLOW) {
			CompartmentImport imp = new CompartmentImport(config);
			Category category = imp.run(ilcdFlow.getSortedCompartments());
			flow.category = category;
		}
		if (flow.category == null) {
			CategoryImport imp = new CategoryImport(config, ModelType.FLOW);
			Category category = imp.run(ilcdFlow.getSortedClasses());
			flow.category = category;
		}
	}

	private void createAndMapContent() throws ImportException {
		validateInput();
		setFlowType();
		flow.refId = ilcdFlow.getId();
		flow.name = Strings.cut(
				Flows.getFullName(ilcdFlow.flow, ilcdFlow.langs), 2048);
		flow.description = ilcdFlow.getComment();
		flow.casNumber = ilcdFlow.getCasNumber();
		flow.synonyms = ilcdFlow.getSynonyms();
		flow.formula = ilcdFlow.getSumFormula();
		flow.version = Version.fromString(
				ilcdFlow.flow.version).getValue();
		Date time = ilcdFlow.getTimeStamp();
		if (time != null)
			flow.lastChange = time.getTime();
		addFlowProperties();
		if (flow.referenceFlowProperty == null)
			throw new ImportException("Could not import flow "
					+ flow.refId + " because the "
					+ "reference flow property of this flow "
					+ "could not be imported.");
		mapLocation();
	}

	private void mapLocation() {
		if (ilcdFlow == null || flow == null)
			return;
		String code = LangString.getFirst(ilcdFlow.getLocation(), config.langs);
		Location location = Locations.get(code, config);
		flow.location = location;
	}

	private void addFlowProperties() {
		Integer refID = Flows.getReferenceFlowPropertyID(ilcdFlow.flow);
		List<FlowPropertyRef> refs = Flows
				.getFlowProperties(ilcdFlow.flow);
		for (FlowPropertyRef ref : refs) {
			FlowProperty property = importProperty(ref);
			if (property == null)
				continue;
			FlowPropertyFactor factor = new FlowPropertyFactor();
			factor.flowProperty = property;
			factor.conversionFactor = ref.meanValue;
			flow.flowPropertyFactors.add(factor);
			Integer propID = ref.dataSetInternalID;
			if (refID == null || propID == null)
				continue;
			if (refID.intValue() == propID.intValue())
				flow.referenceFlowProperty = property;
		}
	}

	private FlowProperty importProperty(FlowPropertyRef ref) {
		if (ref == null)
			return null;
		try {
			FlowPropertyImport propImport = new FlowPropertyImport(config);
			return propImport.run(ref.flowProperty.uuid);
		} catch (Exception e) {
			log.warn("failed to get flow property " + ref.flowProperty, e);
			return null;
		}
	}

	private void setFlowType() {
		org.openlca.ilcd.commons.FlowType t = Flows.getType(ilcdFlow.flow);
		if (t == null) {
			flow.flowType = FlowType.ELEMENTARY_FLOW;
			return;
		}
		switch (t) {
		case ELEMENTARY_FLOW:
			flow.flowType = FlowType.ELEMENTARY_FLOW;
			break;
		case PRODUCT_FLOW:
			flow.flowType = FlowType.PRODUCT_FLOW;
			break;
		case WASTE_FLOW:
			flow.flowType = FlowType.WASTE_FLOW;
			break;
		default:
			flow.flowType = FlowType.PRODUCT_FLOW;
			break;
		}
	}

	private void validateInput() throws ImportException {
		FlowPropertyRef refProp = Flows.getReferenceFlowProperty(ilcdFlow.flow);
		if (refProp == null || refProp.flowProperty == null) {
			String message = "Invalid flow data set: no ref. flow property, flow "
					+ ilcdFlow.getId();
			throw new ImportException(message);
		}
		Ref propRef = refProp.flowProperty;
		if (propRef.uri != null) {
			if (!propRef.uri.contains(propRef.uuid)) {
				String message = "Flow data set {} -> reference to flow"
						+ " property {}: the UUID is not contained in the URI";
				log.warn(message, ilcdFlow.getId(), propRef.uuid);
			}
		}
	}

	private void saveInDatabase(Flow obj) throws ImportException {
		try {
			new FlowDao(config.db).insert(obj);
		} catch (Exception e) {
			String message = String.format("Save operation failed in flow %s.",
					flow.refId);
			throw new ImportException(message, e);
		}
	}

}
