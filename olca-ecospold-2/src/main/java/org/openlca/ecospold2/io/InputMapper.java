package org.openlca.ecospold2.io;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.Classification;
import org.openlca.ecospold2.DataSet;

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
		Element description = Jdom.child(dataSet, "activityDescription");
		Element activity = Jdom.child(description, "activity");
		mapActivity(activity);
		List<Element> classifications = Jdom.childs(description,
				"classification");
		mapClassifications(classifications);
	}

	private void mapActivity(Element e) {
		if (e == null)
			return;
		Activity activity = new Activity();
		dataSet.setActivity(activity);
		activity.setId(e.getAttributeValue("id"));
		activity.setName(Jdom.childText(e, "activityName"));
		List<Element> elems = Jdom.childs(e, "generalComment", "text");
		activity.setGeneralComment(Jdom.joinText(elems));
		activity.setIncludedActivitiesEnd(Jdom.childText(e,
				"includedActivitiesEnd"));
		activity.setIncludedActivitiesStart(Jdom.childText(e,
				"includedActivitiesStart"));
		List<String> syns = Jdom.childTexts(e, "synonym");
		activity.getSynonyms().addAll(syns);
		List<String> tags = Jdom.childTexts(e, "tag");
		activity.getTags().addAll(tags);
	}

	private void mapClassifications(List<Element> elems) {
		for (Element e : elems) {
			Classification classification = new Classification();
			dataSet.getClassifications().add(classification);
			classification.setClassificationId(e
					.getAttributeValue("classificationId"));
			classification.setClassificationSystem(Jdom.childText(e,
					"classificationSystem"));
			classification.setClassificationValue(Jdom.childText(e,
					"classificationValue"));
		}
	}
}
