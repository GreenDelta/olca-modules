package org.openlca.ilcd.epd.conversion;

import java.util.Objects;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.epd.model.EpdDataSet;
import org.openlca.ilcd.epd.model.SafetyMargins;
import org.openlca.ilcd.epd.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class SafetyMarginsConverter {

	static SafetyMargins read(Other other) {
		if (other == null)
			return null;
		for (Object any : other.any) {
			if (!(any instanceof Element element))
				continue;
			if (!isValid(element))
				continue;
			return fromElement(element);
		}
		return null;
	}

	private static boolean isValid(Element element) {
		if (element == null)
			return false;
		String nsUri = element.getNamespaceURI();
		return Objects.equals(nsUri, Vocab.NS_EPD)
				&& Objects.equals(element.getLocalName(), "safetyMargins");
	}

	private static SafetyMargins fromElement(Element e) {
		SafetyMargins margins = new SafetyMargins();
		margins.margins = Dom.getDouble(Dom.getChild(
				e, "margins", Vocab.NS_EPD));
		NodeList nodes = e.getElementsByTagNameNS(
				Vocab.NS_EPD, "description");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (!(n instanceof Element))
				continue;
			String text = n.getTextContent();
			if (Strings.nullOrEmpty(text))
				continue;
			String lang = ((Element) n).getAttributeNS(Vocab.NS_XML, "lang");
			if (Strings.nullOrEmpty(lang)) {
				lang = "en";
			}
			margins.description.add(LangString.of(text, lang));
		}
		return margins;
	}

	static void write(EpdDataSet ds, Other other, Document doc) {
		if (ds == null || other == null || doc == null)
			return;
		Dom.clear(other, "safetyMargins");
		SafetyMargins m = ds.safetyMargins;
		if (m == null || (m.margins == null && m.description.isEmpty()))
			return;
		Element element = toElement(m, doc);
		other.any.add(element);
	}

	private static Element toElement(SafetyMargins margins, Document doc) {
		try {
			String nsUri = Vocab.NS_EPD;
			Element root = doc.createElementNS(nsUri, "epd:safetyMargins");
			if (margins.margins != null) {
				Element e = doc.createElementNS(nsUri, "epd:margins");
				root.appendChild(e);
				e.setTextContent(margins.margins.toString());
			}
			for (LangString d : margins.description) {
				if (Strings.nullOrEmpty(d.value))
					continue;
				Element e = doc.createElementNS(nsUri, "epd:description");
				e.setTextContent(d.value);
				if (!Strings.nullOrEmpty(d.lang)) {
					e.setAttributeNS(Vocab.NS_XML, "xml:lang", d.lang);
				}
				root.appendChild(e);
			}
			return root;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(SafetyMarginsConverter.class);
			log.error("failed to convert safety margins to DOM element", e);
			return null;
		}
	}

}
