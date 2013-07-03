package org.openlca.io.ilcd.input;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.flows.FlowPropertyReference;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.util.FlowBag;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase database;
	private DataStore dataStore;
	private FlowBag ilcdFlow;
	private Flow flow;

	public FlowImport(DataStore dataStore, IDatabase database) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public Flow run(org.openlca.ilcd.flows.Flow flow) throws ImportException {
		this.ilcdFlow = new FlowBag(flow);
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
		ilcdFlow = new FlowBag(iFlow);
		return createNew();
	}

	private Flow findExisting(String flowId) throws ImportException {
		try {
			return database.createDao(Flow.class).getForId(flowId);
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
			org.openlca.ilcd.flows.Flow iFlow = dataStore.get(
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
		CategoryImport categoryImport = new CategoryImport(database,
				ModelType.FLOW);
		Category category = categoryImport.run(ilcdFlow.getSortedClasses());
		flow.setCategory(category);
	}

	private void importAndSetCompartment() throws ImportException {
		if (ilcdFlow.getFlowType() == org.openlca.ilcd.commons.FlowType.ELEMENTARY_FLOW) {
			CompartmentImport compartmentImport = new CompartmentImport(
					database);
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
		flow.setFormula(ilcdFlow.getSumFormula());
		try {
			addFlowProperties();
		} catch (Exception e) {
			log.error("Failed to add flow property factors to " + flow, e);
			throw new ImportException(e);
		}
	}

	private void addFlowProperties() throws Exception {
		Integer refPropertyId = ilcdFlow.getReferenceFlowPropertyId();
		List<FlowPropertyReference> refs = ilcdFlow.getFlowPropertyReferences();
		for (FlowPropertyReference prop : refs) {
			FlowPropertyImport flowPropertyImport = new FlowPropertyImport(
					dataStore, database);
			FlowProperty flowProperty = flowPropertyImport.run(prop
					.getFlowProperty().getUuid());
			FlowPropertyFactor factor = new FlowPropertyFactor();
			factor.setRefId(UUID.randomUUID().toString());
			factor.setFlowProperty(flowProperty);
			factor.setConversionFactor(prop.getMeanValue());
			flow.getFlowPropertyFactors().add(factor);
			BigInteger propId = prop.getDataSetInternalID();
			if (refPropertyId == null || propId == null)
				continue;
			if (refPropertyId.intValue() == propId.intValue())
				flow.setReferenceFlowProperty(flowProperty);
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
			flow.setFlowType(FlowType.ELEMENTARY_FLOW);
			break;
		}
	}

	private void validateInput() throws ImportException {
		Integer internalId = ilcdFlow.getReferenceFlowPropertyId();
		DataSetReference propRef = null;
		for (FlowPropertyReference prop : ilcdFlow.getFlowPropertyReferences()) {
			BigInteger propId = prop.getDataSetInternalID();
			if (propId == null || internalId == null)
				continue;
			if (propId.intValue() == internalId.intValue()) {
				propRef = prop.getFlowProperty();
				break;
			}
		}
		if (internalId == null || propRef == null) {
			String message = "Invalid flow data set: no ref. flow property, flow "
					+ flow.getRefId();
			throw new ImportException(message);
		}
		if (propRef.getUri() != null) {
			if (!propRef.getUri().contains(propRef.getUuid())) {
				String message = "Flow data set {} -> reference to flow"
						+ " property {}: the UUID is not contained in the URI";
				log.warn(message, ilcdFlow.getId(), propRef.getUuid());
			}
		}
	}

	private void saveInDatabase(Flow obj) throws ImportException {
		try {
			database.createDao(Flow.class).insert(obj);
		} catch (Exception e) {
			String message = String.format("Save operation failed in flow %s.",
					flow.getRefId());
			throw new ImportException(message, e);
		}
	}

}
