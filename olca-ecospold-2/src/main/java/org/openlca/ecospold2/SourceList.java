package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Represents a list of sources in a master data file.
 */
public class SourceList {

	private List<Source> sources = new ArrayList<>();

	public List<Source> getSources() {
		return sources;
	}

	static SourceList fromXml(Document doc) {
		SourceList list = new SourceList();
		if (doc == null)
			return list;
		List<Element> elements = In.childs(doc.getRootElement(), "source");
		for (Element element : elements) {
			Source source = Source.fromXml(element);
			if (source != null)
				list.sources.add(source);
		}
		return list;
	}

	Document toXml() {
		Element root = new Element("validSources", IO.NS);
		Document doc = new Document(root);
		for (Source s : sources)
			root.addContent(s.toXml(IO.NS));
		return doc;
	}

}
