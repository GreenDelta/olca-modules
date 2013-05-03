package org.openlca.ecospold2.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

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

	static List<String> childTexts(Element parent, String name) {
		List<Element> elements = childs(parent, name);
		if (elements.isEmpty())
			return Collections.emptyList();
		List<String> texts = new ArrayList<>();
		for (Element element : elements)
			texts.add(element.getText());
		return texts;
	}

	static List<Element> childs(Element parent, String name) {
		if (parent == null)
			return Collections.emptyList();
		return parent.getChildren(name, parent.getNamespace());
	}

	static Element child(Element parent, String... path) {
		Element current = parent;
		for (String childName : path) {
			if (current == null)
				break;
			current = child(current, childName);
		}
		return current;
	}

	static List<Element> childs(Element parent, String... path) {
		Element current = parent;
		int i = 0;
		for (; i < (path.length - 1); i++) {
			if (current == null)
				break;
			String next = path[i];
			current = child(current, next);
		}
		if (current == null)
			return Collections.emptyList();
		return childs(current, path[i]);
	}

	static String joinText(List<Element> elements) {
		if (elements == null || elements.size() == 0)
			return null;
		List<Element> sorted = sort(elements);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < sorted.size(); i++) {
			Element next = sorted.get(i);
			builder.append(next.getText());
			if (i < (sorted.size() - 1))
				builder.append(";");
		}
		return builder.toString();
	}

	private static List<Element> sort(List<Element> elements) {
		if (elements.size() < 2)
			return elements;
		List<Element> seq = new ArrayList<>(elements);
		Collections.sort(seq, new ElementComparator());
		return seq;
	}

	private static class ElementComparator implements Comparator<Element> {

		@Override
		public int compare(Element e1, Element e2) {
			if (e1 == null || e2 == null)
				return 0;
			String index1 = e1.getAttributeValue("index");
			String index2 = e2.getAttributeValue("index");
			if (index1 == null || index2 == null)
				return 0;
			try {
				int i1 = Integer.parseInt(index1);
				int i2 = Integer.parseInt(index2);
				return i1 - i2;
			} catch (Exception e) {
				Logger.getLogger("ElementComparator").severe(
						"failed to compare elements " + e.getMessage());
				return 0;
			}
		}
	}

}
