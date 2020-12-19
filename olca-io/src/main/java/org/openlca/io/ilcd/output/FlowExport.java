package org.openlca.io.ilcd.output;

import java.util.List;

import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.Classification;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.CompartmentList;
import org.openlca.ilcd.flows.DataEntry;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowCategoryInfo;
import org.openlca.ilcd.flows.FlowInfo;
import org.openlca.ilcd.flows.FlowName;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.flows.Geography;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.Modelling;
import org.openlca.ilcd.flows.QuantitativeReference;
import org.openlca.ilcd.util.Flows;
import org.openlca.ilcd.util.Refs;
import org.openlca.io.Xml;

public class FlowExport {

	private final ExportConfig config;
	private org.openlca.core.model.Flow flow;
	private String baseUri;

	public FlowExport(ExportConfig config) {
		this.config = config;
	}

	public Flow run(org.openlca.core.model.Flow flow) {
		if (config.store.contains(Flow.class, flow.refId))
			return config.store.get(Flow.class, flow.refId);
		this.flow = flow;
		Flow iFlow = new Flow();
		iFlow.version = "1.1";
		FlowInfo info = new FlowInfo();
		iFlow.flowInfo = info;
		info.dataSetInfo = makeDataSetInfo();
		QuantitativeReference qRef = new QuantitativeReference();
		info.quantitativeReference = qRef;
		qRef.referenceFlowProperty = 0;
		iFlow.adminInfo = makeAdminInfo();
		iFlow.modelling = makeModellingInfo();
		makeFlowProperties(Flows.flowProperties(iFlow));
		addLocation(iFlow);
		config.store.put(iFlow);
		this.flow = null;
		return iFlow;
	}

	private DataSetInfo makeDataSetInfo() {
		DataSetInfo info = new DataSetInfo();
		info.uuid = flow.refId;
		FlowName flowName = new FlowName();
		info.name = flowName;
		LangString.set(flowName.baseName, flow.name,
			config.lang);
		if (flow.description != null)
			LangString.set(info.generalComment,
				flow.description, config.lang);
		info.casNumber = flow.casNumber;
		info.sumFormula = flow.formula;
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
		if (flow.flowType == org.openlca.core.model.FlowType.ELEMENTARY_FLOW) {
			CompartmentList categorization = converter
				.getElementaryFlowCategory(flow.category);
			info.compartmentLists.add(categorization);
		} else {
			Classification classification = converter
				.getClassification(flow.category);
			info.classifications.add(classification);
		}
	}

	/**
	 * Exports the flow property factors. The reference flow property gets a
	 * data set internal ID of 0, the others 1++.
	 */
	private void makeFlowProperties(List<FlowPropertyRef> refs) {
		var refProp = flow.referenceFlowProperty;
		int pos = 1;
		for (var factor : flow.flowPropertyFactors) {
			var propRef = new FlowPropertyRef();
			refs.add(propRef);
			var property = factor.flowProperty;
			propRef.flowProperty = Export.of(property, config);
			propRef.dataSetInternalID = property.equals(refProp)
				? 0
				: pos++;
			propRef.meanValue = factor.conversionFactor;
		}
	}

	private void addLocation(org.openlca.ilcd.flows.Flow iFlow) {
		if (flow != null && flow.location != null) {
			Geography geography = new Geography();
			LangString.set(geography.location, flow.location.code, config.lang);
			iFlow.flowInfo.geography = geography;
		}
	}

	private AdminInfo makeAdminInfo() {
		AdminInfo info = new AdminInfo();
		DataEntry entry = new DataEntry();
		info.dataEntry = entry;
		entry.timeStamp = Xml.calendar(flow.lastChange);
		entry.formats.add(Refs.ilcd());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		Publication pub = new Publication();
		info.publication = pub;
		pub.version = Version.asString(flow.version);
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		pub.uri = baseUri + "flows/" + flow.refId;
	}

	private Modelling makeModellingInfo() {
		Modelling mav = new Modelling();
		LCIMethod method = new LCIMethod();
		mav.lciMethod = method;
		method.flowType = getFlowType();
		return mav;
	}

	private FlowType getFlowType() {
		if (flow.flowType == null)
			return FlowType.OTHER_FLOW;
		switch (flow.flowType) {
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
