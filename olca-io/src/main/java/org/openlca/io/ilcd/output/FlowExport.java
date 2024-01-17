package org.openlca.io.ilcd.output;

import java.util.List;

import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.commons.Publication;
import org.openlca.ilcd.flows.AdminInfo;
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

	private final Export exp;
	private org.openlca.core.model.Flow flow;
	private String baseUri;

	public FlowExport(Export exp) {
		this.exp = exp;
	}

	public void write(org.openlca.core.model.Flow flow) {
		if (flow == null || exp.store.contains(Flow.class, flow.refId))
			return;
		this.flow = flow;
		var iFlow = new Flow();
		iFlow.version = "1.1";
		FlowInfo info = new FlowInfo();
		iFlow.flowInfo = info;
		info.dataSetInfo = makeDataSetInfo();
		var qRef = new QuantitativeReference();
		info.quantitativeReference = qRef;
		qRef.referenceFlowProperty = 0;
		iFlow.adminInfo = makeAdminInfo();
		iFlow.modelling = makeModellingInfo();
		makeFlowProperties(Flows.forceFlowProperties(iFlow));
		addLocation(iFlow);
		exp.store.put(iFlow);
		this.flow = null;
	}

	private DataSetInfo makeDataSetInfo() {
		var info = new DataSetInfo();
		info.uuid = flow.refId;
		var flowName = new FlowName();
		info.name = flowName;
		exp.add(flowName.baseName, flow.name);
		exp.add(info.generalComment, flow.description);
		info.casNumber = flow.casNumber;
		info.sumFormula = flow.formula;
		exp.add(info.synonyms, flow.synonyms);
		makeCategoryInfo(info);
		return info;
	}

	private void makeCategoryInfo(DataSetInfo dataSetInfo) {
		var info = new FlowCategoryInfo();
		dataSetInfo.classificationInformation = info;
		if (flow.flowType == org.openlca.core.model.FlowType.ELEMENTARY_FLOW) {
			Categories.toCompartments(flow.category)
					.ifPresent(info.compartmentLists::add);
		} else {
			Categories.toClassification(flow.category)
					.ifPresent(info.classifications::add);
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
			propRef.flowProperty = exp.writeRef(property);
			propRef.dataSetInternalID = property.equals(refProp)
					? 0
					: pos++;
			propRef.meanValue = factor.conversionFactor;
		}
	}

	private void addLocation(org.openlca.ilcd.flows.Flow iFlow) {
		if (flow != null && flow.location != null) {
			var geography = new Geography();
			exp.add(geography.location, flow.location.code);
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
		return switch (flow.flowType) {
			case ELEMENTARY_FLOW -> FlowType.ELEMENTARY_FLOW;
			case PRODUCT_FLOW -> FlowType.PRODUCT_FLOW;
			case WASTE_FLOW -> FlowType.WASTE_FLOW;
		};
	}
}
