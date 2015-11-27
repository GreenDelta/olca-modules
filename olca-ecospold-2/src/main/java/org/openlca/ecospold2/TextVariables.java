package org.openlca.ecospold2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

class TextVariables {

	static String apply(Element parent, String text) {
		Map<String, String> variables = getVariables(parent);
		for (String name : variables.keySet()) {
			String value = variables.get(name);
			text = text.replace("{{" + name + "}}", value);
		}
		return text;
	}

	private static Map<String, String> getVariables(Element element) {
		List<Element> variables = In.childs(element, "variable");
		Map<String, String> map = new HashMap<>();
		for (Element variable : variables) {
			String name = variable.getAttributeValue("name");
			String value = variable.getText();
			map.put(name, value);
		}
		return map;
	}

}
