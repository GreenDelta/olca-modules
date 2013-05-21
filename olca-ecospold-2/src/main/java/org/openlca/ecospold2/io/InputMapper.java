package org.openlca.ecospold2.io;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.Classification;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.ElementaryExchange;
import org.openlca.ecospold2.Exchange;
import org.openlca.ecospold2.IntermediateExchange;

/**
 * Maps an XML document to the EcoSpold model.
 */
class InputMapper {

	private Document doc;
	private DataSet dataSet;

	public InputMapper(Document doc) {
		this.doc = doc;
	}

	public DataSet map() {
		if (doc == null)
			return null;
		dataSet = new DataSet();
		Element dataSetElement = getDataSetElement(doc);
		if (dataSetElement != null)
			mapContent(dataSetElement);
		DataSet val = dataSet;
		dataSet = null;
		return val;
	}

	private Element getDataSetElement(Document doc) {
		if (doc == null)
			return null;
		Element root = doc.getRootElement();
		if (!"ecoSpold".equals(root.getName()))
			return null;
		Element e = root.getChild("activityDataset", root.getNamespace());
		if (e == null)
			e = root.getChild("childActivityDataset", root.getNamespace());
		return e;
	}

	private void mapContent(Element dataSet) {
		Element description = In.child(dataSet, "activityDescription");
		Element activity = In.child(description, "activity");
		mapActivity(activity);
		List<Element> classifications = In
				.childs(description, "classification");
		mapClassifications(classifications);
		List<Element> elementaryExchanges = In.childs(dataSet, "flowData",
				"elementaryExchange");
		mapElementaryExchanges(elementaryExchanges);
		List<Element> intermediateExchanges = In.childs(dataSet, "flowData",
				"intermediateExchange");
		mapIntermediateExchanges(intermediateExchanges);
	}

	private void mapActivity(Element e) {
		if (e == null)
			return;
		Activity activity = new Activity();
		dataSet.setActivity(activity);
		activity.setId(e.getAttributeValue("id"));
		activity.setName(In.childText(e, "activityName"));
		List<Element> elems = In.childs(e, "generalComment", "text");
		activity.setGeneralComment(In.joinText(elems));
		activity.setIncludedActivitiesEnd(In.childText(e,
				"includedActivitiesEnd"));
		activity.setIncludedActivitiesStart(In.childText(e,
				"includedActivitiesStart"));
		List<String> syns = In.childTexts(e, "synonym");
		activity.getSynonyms().addAll(syns);
		List<String> tags = In.childTexts(e, "tag");
		activity.getTags().addAll(tags);
	}

	private void mapClassifications(List<Element> elems) {
		for (Element e : elems) {
			Classification classification = new Classification();
			dataSet.getClassifications().add(classification);
			classification.setClassificationId(e
					.getAttributeValue("classificationId"));
			classification.setClassificationSystem(In.childText(e,
					"classificationSystem"));
			classification.setClassificationValue(In.childText(e,
					"classificationValue"));
		}
	}

	private void mapElementaryExchanges(List<Element> elems) {
		for (Element e : elems) {
			ElementaryExchange exchange = new ElementaryExchange();
			fillExchange(exchange, e);
			exchange.setElementaryExchangeId(e
					.getAttributeValue("elementaryExchangeId"));
			dataSet.getElementaryExchanges().add(exchange);
		}
	}

	private void mapIntermediateExchanges(List<Element> elems) {
		for (Element e : elems) {
			IntermediateExchange exchange = new IntermediateExchange();
			fillExchange(exchange, e);
			exchange.setActivityLinkId(e.getAttributeValue("activityLinkId"));
			exchange.setIntermediateExchangeId(e
					.getAttributeValue("intermediateExchangeId"));
			dataSet.getIntermediateExchanges().add(exchange);
		}
	}

	private void fillExchange(Exchange exchange, Element element) {
		String amount = element.getAttributeValue("amount");
		exchange.setAmount(In.decimal(amount));
		exchange.setId(element.getAttributeValue("id"));
		exchange.setMathematicalRelation(element
				.getAttributeValue("mathematicalRelation"));
		exchange.setName(In.childText(element, "name"));
		exchange.setUnitName(In.childText(element, "unitName"));
		exchange.setComment(In.childText(element, "comment"));
		exchange.setUnitId(element.getAttributeValue("unitId"));
		String inGroup = In.childText(element, "inputGroup");
		if (inGroup != null)
			exchange.setInputGroup(In.integer(inGroup));
		else {
			String outGroup = In.childText(element, "outputGroup");
			exchange.setOutputGroup(In.integer(outGroup));
		}
	}
}
