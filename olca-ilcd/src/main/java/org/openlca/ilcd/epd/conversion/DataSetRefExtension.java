package org.openlca.ilcd.epd.conversion;

import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a data set reference element in an extension point (see the generic
 * product or vendor information in the extended product data sets).
 */
class DataSetRefExtension {

	private String type;
	private String path;
	private String tagName;

	public static Ref readFlow(String tagName, Other extension) {
		DataSetRefExtension ext = new DataSetRefExtension(tagName,
				DataSetType.FLOW);
		return ext.read(DataSetType.FLOW, extension);
	}

	public static Ref readActor(String tagName, Other extension) {
		DataSetRefExtension ext = new DataSetRefExtension(tagName,
				DataSetType.CONTACT);
		return ext.read(DataSetType.CONTACT, extension);
	}

	public static Ref readSource(String tagName, Other extension) {
		DataSetRefExtension ext = new DataSetRefExtension(tagName,
				DataSetType.SOURCE);
		return ext.read(DataSetType.SOURCE, extension);
	}

	/**
	 * Stores the given data set reference under the given tag name in the
	 * extension point. If the data set reference is null, it just drops a
	 * possible old data set reference from that extension.
	 */
	public static void write(Ref ref, String tag, Other extension) {
		if (tag == null || extension == null)
			return;
		Element old = Dom.getElement(extension, tag);
		if (old != null) {
			extension.any.remove(old);
		}
		if (ref == null)
			return;
		DataSetRefExtension ext = new DataSetRefExtension(tag, ref.type);
		ext.write(ref, extension);
	}

	private DataSetRefExtension(String tagName, DataSetType modelType) {
		this.tagName = tagName;
		initTypeAndPath(modelType);
	}

	private void initTypeAndPath(DataSetType modelType) {
		if (modelType == null) {
			type = "other external file";
			path = "unknown";
		}
		switch (modelType) {
		case CONTACT:
			type = "contact data set";
			path = "contacts";
			break;
		case SOURCE:
			type = "source data set";
			path = "sources";
			break;
		case FLOW:
			type = "flow data set";
			path = "flows";
			break;
		case LCIA_METHOD:
			type = "LCIA method data set";
			path = "lciamethods";
			break;
		case PROCESS:
			type = "process data set";
			path = "processes";
			break;
		case FLOW_PROPERTY:
			type = "flow property data set";
			path = "flowproperties";
			break;
		case UNIT_GROUP:
			type = "unit group data set";
			path = "unitgroups";
			break;
		default:
			type = "other external file";
			path = "unknown";
			break;
		}
	}

	private Ref read(DataSetType type, Other other) {
		Element element = Dom.getElement(other, tagName);
		if (element == null)
			return null;
		try {
			Ref ref = new Ref();
			ref.type = type;
			ref.uuid = element.getAttribute("refObjectId");
			Element nameElement = Dom.findChild(element,
					"shortDescription");
			if (nameElement != null) {
				String lang = nameElement.getAttribute("xml:lang");
				String val = nameElement.getTextContent();
				LangString.set(ref.name, val, lang);
			}
			return ref;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to create descriptor instance for " + type, e);
			return null;
		}
	}

	private void write(Ref d, Other other) {
		if (other == null)
			return;
		Element element = Dom.getElement(other, tagName);
		if (element != null)
			other.any.remove(element);
		element = createElement(d);
		if (element == null)
			return;
		other.any.add(element);
	}

	private Element createElement(Ref d) {
		Document doc = Dom.createDocument();
		if (doc == null || d == null)
			return null;
		Element e = doc.createElementNS(Vocab.NS_EPD, "epd:" + tagName);
		e.setAttribute("type", type);
		e.setAttribute("refObjectId", d.uuid);
		e.setAttribute("uri", "../" + path + "/" + d.uuid);
		for (LangString name : d.name) {
			Element nameElem = doc.createElementNS(
					"http://lca.jrc.it/ILCD/Common",
					"common:shortDescription");
			e.appendChild(nameElem);
			e.setAttribute("xml:lang", name.lang);
			nameElem.setTextContent(name.value);
		}
		return e;
	}
}
