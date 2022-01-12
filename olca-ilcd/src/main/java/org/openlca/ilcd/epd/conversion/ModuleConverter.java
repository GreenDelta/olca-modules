package org.openlca.ilcd.epd.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.epd.model.EpdDataSet;
import org.openlca.ilcd.epd.model.EpdProfile;
import org.openlca.ilcd.epd.model.ModuleEntry;
import org.openlca.ilcd.epd.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class ModuleConverter {

	static List<ModuleEntry> readModules(Other other, EpdProfile profile) {
		if (other == null)
			return Collections.emptyList();
		for (Object any : other.any) {
			if (!(any instanceof Element element))
				continue;
			if (!isValid(element))
				continue;
			return fromElement(element, profile);
		}
		return Collections.emptyList();
	}

	private static boolean isValid(Element element) {
		if (element == null)
			return false;
		String nsUri = element.getNamespaceURI();
		return Objects.equals(nsUri, Vocab.NS_OLCA)
				&& Objects.equals(element.getLocalName(), "modules");
	}

	private static List<ModuleEntry> fromElement(Element element,
			EpdProfile profile) {
		List<ModuleEntry> modules = new ArrayList<>();
		NodeList moduleList = element.getElementsByTagNameNS(
				Vocab.NS_OLCA, "module");
		for (int i = 0; i < moduleList.getLength(); i++) {
			Node node = moduleList.item(i);
			NamedNodeMap attributes = node.getAttributes();
			ModuleEntry module = new ModuleEntry();
			modules.add(module);
			module.description = node.getTextContent();
			for (int m = 0; m < attributes.getLength(); m++) {
				String attribute = attributes.item(m).getLocalName();
				String value = attributes.item(m).getNodeValue();
				setAttributeValue(module, attribute, value, profile);
			}
		}
		return modules;
	}

	private static void setAttributeValue(ModuleEntry e,
			String attribute, String value, EpdProfile profile) {
		switch (attribute) {
		case "name":
			e.module = profile.module(value);
			break;
		case "scenario":
			e.scenario = value;
			break;
		}
	}

	static void writeModules(EpdDataSet dataSet, Other other, Document doc) {
		if (other == null || doc == null || !shouldWriteEntries(dataSet))
			return;
		Element root = doc.createElementNS(Vocab.NS_OLCA,
				"olca:modules");
		for (ModuleEntry module : dataSet.moduleEntries) {
			Element element = toElement(module, doc);
			if (element != null)
				root.appendChild(element);
		}
		other.any.add(root);
	}

	private static boolean shouldWriteEntries(EpdDataSet dataSet) {
		if (dataSet == null)
			return false;
		for (ModuleEntry entry : dataSet.moduleEntries) {
			if (Strings.notEmpty(entry.description))
				return true;
		}
		return false;
	}

	private static Element toElement(ModuleEntry module, Document document) {
		if (document == null)
			return null;
		try {
			String nsUri = Vocab.NS_OLCA;
			Element element = document.createElementNS(nsUri, "olca:module");
			if (module.module != null)
				element.setAttribute("olca:name", module.module.name);
			if (module.scenario != null)
				element.setAttribute("olca:scenario", module.scenario);
			if (module.description != null)
				element.setTextContent(module.description);
			return element;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ModuleConverter.class);
			log.error("failed to convert module to DOM element", e);
			return null;
		}
	}

}
