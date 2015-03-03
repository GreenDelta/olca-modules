package org.openlca.io.ilcd.output;

import java.math.BigInteger;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FlowCategorization;
import org.openlca.ilcd.commons.FlowCategoryInformation;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.flows.AdministrativeInformation;
import org.openlca.ilcd.flows.DataEntry;
import org.openlca.ilcd.flows.DataSetInformation;
import org.openlca.ilcd.flows.FlowInformation;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.flows.FlowPropertyList;
import org.openlca.ilcd.flows.FlowPropertyReference;
import org.openlca.ilcd.flows.Geography;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.ModellingAndValidation;
import org.openlca.ilcd.flows.Publication;
import org.openlca.ilcd.flows.QuantitativeReference;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.util.LangString;
import org.openlca.ilcd.util.Reference;

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
		this.flow = flow;
		org.openlca.ilcd.flows.Flow iFlow = new org.openlca.ilcd.flows.Flow();
		iFlow.setVersion("1.1");
		FlowInformation info = new FlowInformation();
		iFlow.setFlowInformation(info);
		info.setDataSetInformation(makeDataSetInfo());
		QuantitativeReference qRef = new QuantitativeReference();
		info.setQuantitativeReference(qRef);
		qRef.setReferenceFlowProperty(BigInteger.ZERO);
		iFlow.setAdministrativeInformation(makeAdminInfo());
		iFlow.setModellingAndValidation(makeModellingInfo());
		iFlow.setFlowProperties(makeFlowProperties());
		addLocation(iFlow);
		dataStore.put(iFlow, flow.getRefId());
		this.flow = null;
		return iFlow;
	}

	private DataSetInformation makeDataSetInfo() {
		DataSetInformation info = new DataSetInformation();
		info.setUUID(flow.getRefId());
		FlowName flowName = new FlowName();
		info.setName(flowName);
		LangString.addLabel(flowName.getBaseName(), flow.getName());
		if (flow.getDescription() != null)
			LangString.addFreeText(info.getGeneralComment(),
					flow.getDescription());
		info.setCASNumber(flow.getCasNumber());
		info.setSumFormula(flow.getFormula());
		makeCategoryInfo(info);
		return info;
	}

	private void makeCategoryInfo(DataSetInformation dataSetInfo) {
		CategoryConverter converter = new CategoryConverter();
		FlowCategoryInformation info = new FlowCategoryInformation();
		dataSetInfo.setClassificationInformation(info);
		if (flow.getFlowType() == org.openlca.core.model.FlowType.ELEMENTARY_FLOW) {
			FlowCategorization categorization = converter
					.getElementaryFlowCategory(flow.getCategory());
			info.getElementaryFlowCategorizations().add(categorization);
		} else {
			Classification classification = converter.getClassification(flow
					.getCategory());
			info.getClassifications().add(classification);
		}
	}

	/**
	 * Exports the flow property factors. The reference flow property gets a
	 * data set internal ID of 0, the others 1++.
	 */
	private FlowPropertyList makeFlowProperties() {
		FlowPropertyList list = new FlowPropertyList();
		FlowProperty referenceProperty = flow.getReferenceFlowProperty();
		int pos = 1;
		for (FlowPropertyFactor factor : flow.getFlowPropertyFactors()) {
			FlowPropertyReference propRef = new FlowPropertyReference();
			list.getFlowProperty().add(propRef);
			FlowProperty property = factor.getFlowProperty();
			DataSetReference ref = ExportDispatch.forwardExportCheck(property,
					database, dataStore);
			propRef.setFlowProperty(ref);
			if (property.equals(referenceProperty))
				propRef.setDataSetInternalID(BigInteger.valueOf(0));
			else
				propRef.setDataSetInternalID(BigInteger.valueOf(pos++));
			propRef.setMeanValue(factor.getConversionFactor());
		}
		return list;
	}

	private void addLocation(org.openlca.ilcd.flows.Flow iFlow) {
		if (flow != null && flow.getLocation() != null) {
			Geography geography = new Geography();
			LangString.addLabel(geography.getLocation(), flow.getLocation()
					.getCode());
			iFlow.getFlowInformation().setGeography(geography);
		}
	}

	private AdministrativeInformation makeAdminInfo() {
		AdministrativeInformation info = new AdministrativeInformation();
		DataEntry entry = new DataEntry();
		info.setDataEntry(entry);
		entry.setTimeStamp(Out.getTimestamp(flow));
		entry.getReferenceToDataSetFormat().add(Reference.forIlcdFormat());
		addPublication(info);
		return info;
	}

	private void addPublication(AdministrativeInformation info) {
		Publication pub = new Publication();
		info.setPublication(pub);
		pub.setDataSetVersion(Version.asString(flow.getVersion()));
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.setPermanentDataSetURI(baseUri + "flows/" + flow.getRefId());
	}

	private ModellingAndValidation makeModellingInfo() {
		ModellingAndValidation mav = new ModellingAndValidation();
		LCIMethod method = new LCIMethod();
		mav.setLCIMethod(method);
		method.setFlowType(getFlowType());
		return mav;
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

}
