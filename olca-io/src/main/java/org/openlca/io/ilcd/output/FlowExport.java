package org.openlca.io.ilcd.output;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FlowCategorization;
import org.openlca.ilcd.commons.FlowCategoryInformation;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.flows.DataSetInformation;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.flows.FlowPropertyReference;
import org.openlca.ilcd.flows.Geography;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.util.FlowBuilder;
import org.openlca.ilcd.util.LangString;

/**
 * The export of an openLCA flow to an ILCD flow data set.
 */
public class FlowExport {

	private Flow flow;
	private IDatabase database;
	private DataStore dataStore;
	private String baseUri;

	public FlowExport(IDatabase database, DataStore dataStore) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public org.openlca.ilcd.flows.Flow run(Flow flow) throws DataStoreException {
		DataSetInformation dataSetInfo = makeDataSetInfo();
		List<FlowPropertyReference> flowPropRefs = makeFlowPropRefs();
		org.openlca.ilcd.flows.Flow iFlow = FlowBuilder.makeFlow()
				.withBaseUri(baseUri).withDataSetInfo(dataSetInfo)
				.withFlowProperties(flowPropRefs).withFlowType(getFlowType())
				.withReferenceFlowPropertyId(0).getFlow();
		addLocation(iFlow);
		dataStore.put(iFlow, flow.getRefId());
		return iFlow;
	}

	private DataSetInformation makeDataSetInfo() {
		DataSetInformation dataSetInfo = new DataSetInformation();
		dataSetInfo.setUUID(flow.getRefId());
		FlowName flowName = new FlowName();
		dataSetInfo.setName(flowName);
		LangString.addLabel(flowName.getBaseName(), flow.getName());
		if (flow.getDescription() != null)
			LangString.addFreeText(dataSetInfo.getGeneralComment(),
					flow.getDescription());

		dataSetInfo.setCASNumber(flow.getCasNumber());
		dataSetInfo.setSumFormula(flow.getFormula());
		makeCategoryInfo(dataSetInfo);
		return dataSetInfo;
	}

	private void makeCategoryInfo(DataSetInformation dataSetInfo) {
		CategoryConverter converter = new CategoryConverter();
		FlowCategoryInformation categoryInformation = new FlowCategoryInformation();
		dataSetInfo.setClassificationInformation(categoryInformation);
		if (flow.getFlowType() == org.openlca.core.model.FlowType.ELEMENTARY_FLOW) {
			FlowCategorization categorization = converter
					.getElementaryFlowCategory(flow.getCategory());
			categoryInformation.getElementaryFlowCategorizations().add(
					categorization);
		} else {
			Classification classification = converter.getClassification(flow
					.getCategory());
			categoryInformation.getClassifications().add(classification);
		}
	}

	private FlowType getFlowType() {
		if (flow.getFlowType() == null)
			return FlowType.OTHER_FLOW;
		switch (flow.getFlowType()) {
		case ELEMENTARY_FLOW:
			return FlowType.ELEMENTARY_FLOW;
		case PRODUCT_FLOW:
			return FlowType.PRODUCT_FLOW;
		case WASTE_FLOW:
			return FlowType.WASTE_FLOW;
		default:
			return FlowType.OTHER_FLOW;
		}
	}

	/**
	 * Exports the flow property factors. The reference flow property gets a
	 * data set internal ID of 0, the others 1++.
	 */
	private List<FlowPropertyReference> makeFlowPropRefs() {
		List<FlowPropertyReference> refs = new ArrayList<>();
		FlowProperty referenceProperty = flow.getReferenceFlowProperty();
		int pos = 1;
		for (FlowPropertyFactor factor : flow.getFlowPropertyFactors()) {
			FlowPropertyReference propRef = new FlowPropertyReference();
			refs.add(propRef);
			FlowProperty property = factor.getFlowProperty();
			DataSetReference ref = ExportDispatch.forwardExportCheck(property,
					database, dataStore);
			propRef.setFlowProperty(ref);
			if (property.equals(referenceProperty)) {
				propRef.setDataSetInternalID(BigInteger.valueOf(0));
			} else {
				propRef.setDataSetInternalID(BigInteger.valueOf(pos));
				pos++;
			}
			propRef.setMeanValue(factor.getConversionFactor());
		}
		return refs;
	}

	private void addLocation(org.openlca.ilcd.flows.Flow iFlow) {
		if (flow != null && flow.getLocation() != null) {
			Geography geography = new Geography();
			LangString.addLabel(geography.getLocation(), flow.getLocation()
					.getCode());
			iFlow.getFlowInformation().setGeography(geography);
		}
	}

}
