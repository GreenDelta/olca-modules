package org.openlca.ilcd.epd.model.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.epd.conversion.Dom;
import org.openlca.ilcd.epd.conversion.Vocab;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Content declaration according to EN 15804/ISO 219301. The content declaration
 * may contain component, material and/or substance elements, which may (but do
 * not have to) be nested.
 */
public class ContentDeclaration {

	/**
	 * A content declaration can contain components, materials, and substances.
	 * Components can in turn contain materials and substances; materials can
	 * contain substances.
	 */
	public final List<ContentElement> content = new ArrayList<>();

	/**
	 * Read a content declaration from the given extension element. May return
	 * null when the extension is null or when it has no content declaration.
	 */
	public static ContentDeclaration read(Other other) {
		if (other == null)
			return null;

		// find the root element
		Element root = null;
		for (Object any : other.any) {
			if (!(any instanceof Element e))
				continue;
			if (Objects.equals(Vocab.NS_EPDv2, e.getNamespaceURI())
					&& Objects.equals("contentDeclaration", e.getLocalName())) {
				root = e;
				break;
			}
		}
		if (root == null)
			return null;

		// add the content elements
		ContentDeclaration decl = new ContentDeclaration();
		Dom.eachChild(root, e -> {
			ContentElement ce = readElement(e);
			if (ce == null)
				return;
			decl.content.add(ce);
		});
		return decl;
	}

	/**
	 * Write this content declaration to the given extension element deleting an
	 * old declaration if it already exists.
	 */
	public void write(Other other, Document doc) {
		if (other == null || doc == null)
			return;
		Dom.clear(other, "contentDeclaration");
		if (content.isEmpty())
			return;
		Element root = doc.createElementNS(
				Vocab.NS_EPDv2, "epd2:contentDeclaration");
		other.any.add(root);
		for (ContentElement e : content) {
			writeElement(root, e);
		}
	}

	static ContentElement readElement(Element elem) {
		if (elem == null)
			return null;
		if (!Objects.equals(Vocab.NS_EPDv2, elem.getNamespaceURI()))
			return null;
		return switch (elem.getLocalName()) {
			case "component" -> new Component().read(elem);
			case "material" -> new Material().read(elem);
			case "substance" -> new Substance().read(elem);
			default -> null;
		};
	}

	static void writeElement(Element parent, ContentElement celem) {
		if (celem == null)
			return;
		String tag = null;
		if (celem instanceof Component) {
			tag = "component";
		} else if (celem instanceof Material) {
			tag = "material";
		} else if (celem instanceof Substance) {
			tag = "substance";
		}
		if (tag == null)
			return;
		Element elem = Dom.addChild(parent, "epd2:" + tag, Vocab.NS_EPDv2);
		celem.write(elem);
	}

	@Override
	public ContentDeclaration clone() {
		ContentDeclaration clone = new ContentDeclaration();
		for (ContentElement elem : content) {
			if (elem != null) {
				clone.content.add(elem.clone());
			}
		}
		return clone;
	}
}
