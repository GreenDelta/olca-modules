package org.openlca.io.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

/** Creates a lean data set with the data of an EcoSpold 02 data set. */
class LeanProcess {

	private String id;
	private String name;
	private String comment;
	private String category;
	private String locationCode;
	private String locationComment;
	private String technologyComment;
	private String modelName;
	private String samplingProcedure;
	private String extrapolations;
	private List<LeanExchange> exchanges = new ArrayList<>();

	private LeanProcess() {
	}

	public static LeanProcess create(Element dataSet) {
		LeanProcess process = new LeanProcess();
		if (dataSet == null)
			return process;
		Element description = Jdom.child(dataSet, "activityDescription");
		process.mapDescription(description);
		process.mapFlows(Jdom.child(dataSet, "flowData"));
		return process;
	}

	private void mapDescription(Element description) {
		if (description == null)
			return;
		activity(Jdom.child(description, "activity"));
	}

	private void activity(Element activity) {
		if (activity == null)
			return;
		id = activity.getAttributeValue("id");
		name = Jdom.childText(activity, "activityName");
		// TODO: comments from text list
	}

	private void mapFlows(Element flowData) {
		if (flowData == null)
			return;
		addExchanges(flowData, LeanExchange.ELEMENTARY_FLOW);
		addExchanges(flowData, LeanExchange.PRODUCT_FLOW);
	}

	private void addExchanges(Element flowData, int type) {
		String tag = type == LeanExchange.ELEMENTARY_FLOW ? "elementaryExchange"
				: "intermediateExchange";
		for (Element e : Jdom.childs(flowData, tag)) {
			LeanExchange exchange = LeanExchange.create(e, type);
			exchanges.add(exchange);
		}
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getComment() {
		return comment;
	}

	public String getCategory() {
		return category;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public String getLocationComment() {
		return locationComment;
	}

	public String getTechnologyComment() {
		return technologyComment;
	}

	public String getModelName() {
		return modelName;
	}

	public String getSamplingProcedure() {
		return samplingProcedure;
	}

	public String getExtrapolations() {
		return extrapolations;
	}

	public List<LeanExchange> getExchanges() {
		return exchanges;
	}

}
