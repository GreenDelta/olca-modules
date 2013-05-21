package org.openlca.io.ecospold2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom2.Element;

/** Some helper methods for XML parsing with Jdom */
class Jdom {

	private Jdom() {
	}

	static Element child(Element parent, String name) {
		if (parent == null)
			return null;
		return parent.getChild(name, parent.getNamespace());
	}

	static String childText(Element parent, String name) {
		if (parent == null)
			return null;
		return parent.getChildText(name, parent.getNamespace());
	}

	static List<Element> childs(Element parent, String name) {
		if (parent == null)
			return Collections.emptyList();
		List<Element> list = new ArrayList<>();
		for (Object obj : parent.getChildren(name, parent.getNamespace())) {
			if (obj instanceof Element)
				list.add((Element) obj);
		}
		return list;
	}

}
