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
		importAndSetCompartment();
		if (flow.getCategory() == null)
			importAndSetCategory();
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

	private void importAndSetCategory() throws ImportException {
		CategoryImport categoryImport = new CategoryImport(config,
				ModelType.FLOW);
		Category category = categoryImport.run(ilcdFlow.getSortedClasses());
		flow.setCategory(category);
	}

	private void importAndSetCompartment() throws ImportException {
		if (ilcdFlow.getFlowType() == org.openlca.ilcd.commons.FlowType.ELEMENTARY_FLOW) {
			CompartmentImport compartmentImport = new CompartmentImport(config);
			Category category = compartmentImport.run(ilcdFlow
					.getSortedCompartments());
			flow.setCategory(category);
		}
	}

	private void createAndMapContent() throws ImportException {
		validateInput();
		setFlowType();
		flow.setRefId(ilcdFlow.getId());
		flow.setName(Strings.cut(ilcdFlow.getName(), 254));
		flow.setDescription(ilcdFlow.getComment());
		flow.setCasNumber(ilcdFlow.getCasNumber());
		flow.synonyms = ilcdFlow.getSynonyms();
		flow.setFormula(ilcdFlow.getSumFormula());
		String v = ilcdFlow.getVersion();
		flow.setVersion(Version.fromString(v).getValue());
		Date time = ilcdFlow.getTimeStamp();
		if (time != null)
			flow.setLastChange(time.getTime());
		addFlowProperties();
		if (flow.getReferenceFlowProperty() == null)
			throw new ImportException("Could not import flow "
					+ flow.getRefId() + " because the "
					+ "reference flow property of this flow "
					+ "could not be imported.");
		mapLocation();
	}

	private void mapLocation() {
		if (ilcdFlow == null || flow == null)
			return;
		String code = LangString.getFirst(ilcdFlow.getLocation(), config.langs);
		Location location = Locations.get(code, config);
		flow.setLocation(location);
	}

	private void addFlowProperties() {
		Integer refPropertyId = ilcdFlow.getReferenceFlowPropertyId();
		List<FlowPropertyRef> refs = Flows.getFlowProperties(ilcdFlow.getValue());
		for (FlowPropertyRef ref : refs) {
			FlowProperty property = importProperty(ref);
			if (property == null)
				continue;
			FlowPropertyFactor factor = new FlowPropertyFactor();
			factor.setFlowProperty(property);
			factor.setConversionFactor(ref.meanValue);
			flow.getFlowPropertyFactors().add(factor);
			Integer propId = ref.dataSetInternalID;
			if (refPropertyId == null || propId == null)
				continue;
			if (refPropertyId.intValue() == propId.intValue())
				flow.setReferenceFlowProperty(property);
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
		if (ilcdFlow.getFlowType() == null) {
			flow.setFlowType(FlowType.ELEMENTARY_FLOW);
			return;
		}
		switch (ilcdFlow.getFlowType()) {
		case ELEMENTARY_FLOW:
			flow.setFlowType(FlowType.ELEMENTARY_FLOW);
			break;
		case PRODUCT_FLOW:
			flow.setFlowType(FlowType.PRODUCT_FLOW);
			break;
		case WASTE_FLOW:
			flow.setFlowType(FlowType.WASTE_FLOW);
			break;
		default:
			flow.setFlowType(FlowType.PRODUCT_FLOW);
			break;
		}
	}

	private void validateInput() throws ImportException {
		Integer internalId = ilcdFlow.getReferenceFlowPropertyId();
		Ref propRef = null;
		for (FlowPropertyRef prop : Flows.getFlowProperties(ilcdFlow.getValue())) {
			Integer propId = prop.dataSetInternalID;
			if (propId == null || internalId == null)
				continue;
			if (propId.intValue() == internalId.intValue()) {
				propRef = prop.flowProperty;
				break;
			}
		}
		if (internalId == null || propRef == null) {
			String message = "Invalid flow data set: no ref. flow property, flow "
					+ ilcdFlow.getId();
			throw new ImportException(message);
		}
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
					flow.getRefId());
			throw new ImportException(message, e);
		}
	}

}
