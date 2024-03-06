package org.openlca.io.ilcd.output;

import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.flows.AdminInfo;
import org.openlca.ilcd.flows.DataSetInfo;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowPropertyRef;
import org.openlca.ilcd.flows.Modelling;
import org.openlca.ilcd.util.Refs;
import org.openlca.io.Xml;

import java.util.List;

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
		var iFlow = new Flow()
				.withAdminInfo(makeAdminInfo())
				.withModelling(makeModellingInfo());
		iFlow.withFlowInfo()
				.withDataSetInfo(makeDataSetInfo())
				.withQuantitativeReference()
				.withReferenceFlowProperty(0);
		makeFlowProperties(iFlow.withFlowProperties());
		addLocation(iFlow);
		exp.store.put(iFlow);
		this.flow = null;
	}

	private DataSetInfo makeDataSetInfo() {
		var info = new DataSetInfo()
				.withUUID(flow.refId)
				.withCasNumber(flow.casNumber)
				.withSumFormula(flow.formula);
		var flowName = info.withFlowName();
		exp.add(flowName::withBaseName, flow.name);
		exp.add(info::withComment, flow.description);
		exp.add(info::withSynonyms, flow.synonyms);
		makeCategoryInfo(info);
		return info;
	}

	private void makeCategoryInfo(DataSetInfo info) {
		if (flow.flowType == org.openlca.core.model.FlowType.ELEMENTARY_FLOW) {
			Categories.toCompartments(
					flow.category,
					() -> info.withClassificationInformation().withCompartmentLists());
		} else {
			Categories.toClassification(
					flow.category,
					() -> info.withClassificationInformation().withClassifications());
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
			var property = factor.flowProperty;
			if (property == null)
				continue;
			var propRef = new FlowPropertyRef();
			propRef.withFlowProperty(exp.writeRef(property))
					.withDataSetInternalID(property.equals(refProp) ? 0 : pos++)
					.withMeanValue(factor.conversionFactor);
			refs.add(propRef);
		}
	}

	private void addLocation(org.openlca.ilcd.flows.Flow iFlow) {
		if (flow != null && flow.location != null) {
			var geo = iFlow.withFlowInfo().withGeography();
			exp.add(geo::withLocation, flow.location.code);
		}
	}

	private AdminInfo makeAdminInfo() {
		var info = new AdminInfo();
		info.withDataEntry()
				.withTimeStamp(Xml.calendar(flow.lastChange))
				.withFormats()
				.add(Refs.ilcd());
		addPublication(info);
		return info;
	}

	private void addPublication(AdminInfo info) {
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		info.withPublication()
				.withVersion(Version.asString(flow.version))
				.withUri(baseUri + "flows/" + flow.refId);
	}

	private Modelling makeModellingInfo() {
		var mav = new Modelling();
		mav.withInventoryMethod()
				.withFlowType(getFlowType());
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
