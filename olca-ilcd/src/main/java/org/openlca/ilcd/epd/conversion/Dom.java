package org.openlca.ilcd.epd.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilderFactory;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.epd.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility methods for reading and writing data using the W3C DOM API.
 */
public final class Dom {

	private Dom() {
	}

	public static Document createDocument() {
		try {
			var dbf = DocumentBuilderFactory.newInstance();
			var db = dbf.newDocumentBuilder();
			return db.newDocument();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Dom.class);
			log.error("failed to init DOM doc", e);
			return null;
		}
	}

	static Element createElement(String ns, String tag) {
		if (tag == null)
			return null;
		var doc = createDocument();
		if (doc == null)
			return null;
		if (ns == null)
			return doc.createElement(tag);
		if (tag.contains(":"))
			return doc.createElementNS(ns, tag);
		var prefix = Vocab.prefixOf(ns);
		return prefix.isEmpty()
				? doc.createElementNS(ns, tag)
				: doc.createElementNS(ns, prefix.get() + ":" + tag);
	}

	/**
	 * Get all child elements with the given name and namespace from the parent
	 * element.
	 */
	public static List<Element> getChilds(Element parent, String name,
			String ns) {
		if (parent == null || name == null)
			return Collections.emptyList();
		List<Element> elems = new ArrayList<>();
		eachChild(parent, e -> {
			if (!Objects.equals(ns, e.getNamespaceURI()))
				return;
			if (Objects.equals(name, e.getLocalName())) {
				elems.add(e);
			}
		});
		return elems;
	}

	public static String getAttribute(Element elem, String name) {
		if (elem == null)
			return null;
		return elem.getAttribute(name);
	}

	public static LangString getLangString(Element elem) {
		String text = getText(elem);
		if (text == null)
			return null;
		String lang = getAttribute(elem, "lang");
		if (Strings.nullOrEmpty(lang)) {
			lang = "en";
		}
		return LangString.of(text, lang);
	}

	public static void setLangString(Element elem, LangString s) {
		if (elem == null || s == null)
			return;
		elem.setTextContent(s.value);
		if (!Strings.nullOrEmpty(s.lang)) {
			elem.setAttributeNS(Vocab.NS_XML, "lang", s.lang);
		}
	}

	/**
	 * Get the first child element with the given name and namespace from the
	 * parent element.
	 */
	public static Element getChild(Element parent, String name, String ns) {
		if (parent == null || name == null)
			return null;
		var ar = new AtomicReference<Element>();
		eachChild(parent, e -> {
			if (ar.get() != null)
				return;
			if (!Objects.equals(ns, e.getNamespaceURI()))
				return;
			if (Objects.equals(name, e.getLocalName())) {
				ar.set(e);
			}
		});
		return ar.get();
	}

	/**
	 * Returns the first element with the given name and namespace from the
	 * given extension element.
	 */
	public static Element getChild(Other other, String name, String ns) {
		if (other == null || name == null)
			return null;
		for (Object any : other.any) {
			if (!(any instanceof Element e))
				continue;
			if (matches(e, name, ns))
				return e;
		}
		return null;
	}

	/**
	 * Returns true if the given element has the given (local) name and
	 * namespace.
	 */
	public static boolean matches(Element elem, String name, String ns) {
		if (elem == null || name == null || ns == null)
			return false;
		return Objects.equals(name, elem.getLocalName())
				&& Objects.equals(ns, elem.getNamespaceURI());
	}

	/**
	 * Creates a new element with the given name and namespace and appends it to
	 * the given parent element.
	 */
	public static Element addChild(Element parent, String name, String ns) {
		if (parent == null || name == null)
			return null;
		Element elem = parent.getOwnerDocument().createElementNS(ns, name);
		parent.appendChild(elem);
		return elem;
	}

	static Double getDouble(Element e) {
		String text = getText(e);
		if (text == null)
			return null;
		try {
			return Double.parseDouble(text);
		} catch (Exception _e) {
			Logger log = LoggerFactory.getLogger(Dom.class);
			log.error("content of {} is not numeric", e);
			return null;
		}
	}

	public static String getText(Element e) {
		if (e == null)
			return null;
		NodeList nl = e.getChildNodes();
		if (nl.getLength() == 0)
			return null;
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n == null)
				continue;
			s.append(n.getTextContent());
		}
		return s.toString();
	}

	static Element getElement(Other extension, String tagName) {
		if (extension == null || tagName == null)
			return null;
		for (var any : extension.any) {
			if (!(any instanceof Element e))
				continue;
			if (Objects.equals(tagName, e.getLocalName()))
				return e;
		}
		return null;
	}

	/**
	 * Removes all elements with the given tag-name from the extensions.
	 */
	public static void clear(Other extension, String tagName) {
		if (extension == null || tagName == null)
			return;
		List<Element> matches = new ArrayList<>();
		for (Object any : extension.any) {
			if (!(any instanceof Element e))
				continue;
			if (Objects.equals(tagName, e.getLocalName()))
				matches.add(e);
		}
		extension.any.removeAll(matches);
	}

	/**
	 * Returns true if the given extension element is null or empty.
	 */
	static boolean isEmpty(Other ext) {
		if (ext == null)
			return true;
		if (ext.any.isEmpty())
			return true;
		for (Object o : ext.any) {
			if (o != null)
				return false;
		}
		return true;
	}

	static Element findChild(Element root, String... path) {
		if (root == null || path.length == 0)
			return null;
		Element element = root;
		for (String tagName : path) {
			if (element == null)
				return null;
			NodeList list = element.getChildNodes();
			if (list.getLength() == 0)
				return null;
			element = null;
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (!(node instanceof Element child))
					continue;
				if (Objects.equals(child.getLocalName(), tagName)) {
					element = child;
					break;
				}
			}
		}
		return element;
	}

	public static void eachChild(Element parent, Consumer<Element> fn) {
		if (parent == null || fn == null)
			return;
		NodeList childs = parent.getChildNodes();
		if (childs.getLength() == 0)
			return;
		for (int i = 0; i < childs.getLength(); i++) {
			Node node = childs.item(i);
			if (!(node instanceof Element))
				continue;
			fn.accept((Element) node);
		}
	}
}
