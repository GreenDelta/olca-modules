package org.openlca.ecospold2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Element;

/** Some helper methods for XML parsing with Jdom */
class In {

	private In() {
	}

	static Element child(Element parent, String name) {
		if (parent == null)
			return null;
		return parent.getChild(name, parent.getNamespace());
	}

	static String childText(Element parent, String name) {
		if (parent == null)
			return null;
		String text = parent.getChildText(name, parent.getNamespace());
		TextVariables.apply(parent, text);
		return text;
	}

	static List<String> childTexts(Element parent, String name) {
		List<Element> elements = childs(parent, name);
		if (elements.isEmpty())
			return Collections.emptyList();
		List<String> texts = new ArrayList<>();
		for (Element element : elements) {
			String text = element.getText();
			text = TextVariables.apply(parent, text);
			texts.add(text);
		}
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
		String text = builder.toString();
		text = TextVariables.apply(elements.get(0).getParentElement(), text);
		return text;
	}

	private static List<Element> sort(List<Element> elements) {
		if (elements.size() < 2)
			return elements;
		List<Element> seq = new ArrayList<>(elements);
		Collections.sort(seq, new ElementComparator());
		return seq;
	}

	public static double decimal(String val) {
		if (val == null)
			return 0;
		try {
			return Double.parseDouble(val.trim());
		} catch (Exception e) {
			Logger log = Logger.getGlobal();
			log.log(Level.WARNING, "Failed to parse double " + val, e);
			return 0;
		}
	}

	public static boolean bool(String val) {
		if (val == null)
			return false;
		try {
			return Boolean.parseBoolean(val.trim());
		} catch (Exception e) {
			Logger log = Logger.getGlobal();
			log.log(Level.WARNING, "Failed to parse boolean " + val, e);
			return false;
		}
	}

	public static Boolean optionalBool(String val) {
		if (val == null)
			return null;
		return bool(val);
	}

	public static Date date(String val, String pattern) {
		if (val == null)
			return null;
		try {
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			return format.parse(val.trim());
		} catch (Exception e) {
			Logger log = Logger.getGlobal();
			log.log(Level.WARNING, "Failed to parse date " + val + " format "
					+ pattern, e);
			return null;
		}
	}

	public static int integer(String val) {
		if (val == null)
			return -1;
		try {
			return Integer.parseInt(val.trim());
		} catch (Exception e) {
			Logger log = Logger.getGlobal();
			log.log(Level.WARNING, "Failed to parse integer " + val, e);
			return -1;
		}
	}

	public static Integer optionalInteger(String val) {
		if (val == null)
			return null;
		return integer(val);
	}

	public static Double optionalDecimal(String val) {
		if (val == null)
			return null;
		return decimal(val);
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
