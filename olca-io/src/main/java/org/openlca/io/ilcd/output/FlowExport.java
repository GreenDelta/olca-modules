package org.openlca.io.ilcd.output;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.commons.FlowCategorization;
import org.openlca.ilcd.commons.FlowCategoryInfo;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.DataEntry;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.flows.FlowPropertyList;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.flows.Geography;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.ModellingAndValidation;
import org.openlca.ilcd.flows.Publication;
import org.openlca.ilcd.flows.QuantitativeReference;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.util.Reference;

public class FlowExport {

	private final ExportConfig config;
	private org.openlca.core.model.Flow flow;
	private String baseUri;

	public FlowExport(ExportConfig config) {
		this.config = config;
	}

	public Flow run(org.openlca.core.model.Flow flow) throws DataStoreException {
		if (config.store.contains(Flow.class, flow.getRefId()))
			return config.store.get(Flow.class, flow.getRefId());
		this.flow = flow;
		Flow iFlow = new Flow();
		iFlow.version = "1.1";
		FlowInfo info = new FlowInfo();
		iFlow.flowInformation = info;
		info.dataSetInfo = makeDataSetInfo();
		QuantitativeReference qRef = new QuantitativeReference();
		info.quantitativeReference = qRef;
		qRef.referenceFlowProperty = 0;
		iFlow.administrativeInformation = makeAdminInfo();
		iFlow.modellingAndValidation = makeModellingInfo();
		iFlow.flowProperties = makeFlowProperties();
		addLocation(iFlow);
		config.store.put(iFlow, flow.getRefId());
		this.flow = null;
		return iFlow;
	}

	private DataSetInfo makeDataSetInfo() {
		DataSetInfo info = new DataSetInfo();
		info.uuid = flow.getRefId();
		FlowName flowName = new FlowName();
		info.name = flowName;
		LangString.set(flowName.baseName, flow.getName(),
				config.lang);
		if (flow.getDescription() != null)
			LangString.set(info.generalComment,
					flow.getDescription(), config.lang);
		info.casNumber = flow.getCasNumber();
		info.sumFormula = flow.getFormula();
		if (flow.synonyms != null)
			LangString.set(info.synonyms, flow.synonyms,
					config.lang);
		makeCategoryInfo(info);
		return info;
	}

	private void makeCategoryInfo(DataSetInfo dataSetInfo) {
		CategoryConverter converter = new CategoryConverter();
		FlowCategoryInfo info = new FlowCategoryInfo();
		dataSetInfo.classificationInformation = info;
		if (flow.getFlowType() == org.openlca.core.model.FlowType.ELEMENTARY_FLOW) {
			FlowCategorization categorization = converter
					.getElementaryFlowCategory(flow.getCategory());
			info.elementaryFlowCategorizations.add(categorization);
		} else {
			Classification classification = converter.getClassification(flow
					.getCategory());
			info.classifications.add(classification);
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
			FlowPropertyRef propRef = new FlowPropertyRef();
			list.flowProperty.add(propRef);
			FlowProperty property = factor.getFlowProperty();
			DataSetReference ref = ExportDispatch.forwardExportCheck(property,
					config);
			propRef.flowProperty = ref;
			if (property.equals(referenceProperty))
				propRef.dataSetInternalID = 0;
			else
				propRef.dataSetInternalID = pos++;
			propRef.meanValue = factor.getConversionFactor();
		}
		return list;
	}

	private void addLocation(org.openlca.ilcd.flows.Flow iFlow) {
		if (flow != null && flow.getLocation() != null) {
			Geography geography = new Geography();
			LangString.set(geography.location, flow.getLocation()
					.getCode(), config.lang);
			iFlow.flowInformation.geography = geography;
		}
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		DataEntry entry = new DataEntry();
		info.dataEntry = entry;
		entry.timeStamp = Out.getTimestamp(flow);
		entry.referenceToDataSetFormat.add(
				Reference.forIlcdFormat());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		Publication pub = new Publication();
		info.publication = pub;
		pub.dataSetVersion = Version.asString(flow.getVersion());
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.permanentDataSetURI = baseUri + "flows/" + flow.getRefId();
	}

	private ModellingAndValidation makeModellingInfo() {
		ModellingAndValidation mav = new ModellingAndValidation();
		LCIMethod method = new LCIMethod();
		mav.lciMethod = method;
		method.flowType = getFlowType();
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
