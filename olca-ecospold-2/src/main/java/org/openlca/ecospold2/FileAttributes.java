package org.openlca.ecospold2;

import java.util.Date;

import org.jdom2.Element;

public class FileAttributes {

	public int majorRelease;
	public int minorRelease;
	public int majorRevision;
	public int minorRevision;
	public String defaultLanguage;
	public Date creationTimestamp;
	public Date lastEditTimestamp;
	public String internalSchemaVersion;
	public String fileGenerator;
	public Date fileTimestamp;
	public String contextId;
	public String contextName;
	public String requiredContext;

	static FileAttributes fromXml(Element e) {
		if (e == null)
			return null;
		FileAttributes atts = new FileAttributes();

		atts.majorRelease = In.integer(e.getAttributeValue("majorRelease"));
		atts.minorRelease = In.integer(e.getAttributeValue("minorRelease"));
		atts.majorRevision = In.integer(e.getAttributeValue("majorRevision"));
		atts.minorRevision = In.integer(e.getAttributeValue("minorRevision"));

		atts.defaultLanguage = e.getAttributeValue("defaultLanguage");
		atts.creationTimestamp = In.date(
				e.getAttributeValue("creationTimestamp"), IO.XML_DATE_TIME);
		atts.lastEditTimestamp = In.date(
				e.getAttributeValue("lastEditTimestamp"), IO.XML_DATE_TIME);
		atts.internalSchemaVersion = e
				.getAttributeValue("internalSchemaVersion");
		atts.fileGenerator = e.getAttributeValue("fileGenerator");
		atts.fileTimestamp = In.date(e.getAttributeValue("fileTimestamp"),
				IO.XML_DATE_TIME);
		atts.contextId = e.getAttributeValue("contextId");
		atts.contextName = In.childText(e, "contextName");
		atts.requiredContext = In.childText(e, "requiredContext");
		return atts;
	}

	Element toXml() {

		Element element = new Element("fileAttributes", IO.NS);
		element.setAttribute("majorRelease", Out.integer(majorRelease));
		element.setAttribute("minorRelease", Out.integer(minorRelease));
		element.setAttribute("majorRevision", Out.integer(majorRevision));
		element.setAttribute("minorRevision", Out.integer(minorRevision));

		if (defaultLanguage != null)
			element.setAttribute("defaultLanguage", defaultLanguage);

		if (creationTimestamp != null)
			element.setAttribute("creationTimestamp",
					Out.date(creationTimestamp, IO.XML_DATE_TIME));

		if (lastEditTimestamp != null)
			element.setAttribute("lastEditTimestamp",
					Out.date(lastEditTimestamp, IO.XML_DATE_TIME));

		if (internalSchemaVersion != null)
			element.setAttribute("internalSchemaVersion", internalSchemaVersion);

		if (fileGenerator != null)
			element.setAttribute("fileGenerator", fileGenerator);

		if (fileTimestamp != null)
			element.setAttribute("fileTimestamp",
					Out.date(fileTimestamp, IO.XML_DATE_TIME));

		if (contextId != null)
			element.setAttribute("contextId", contextId);

		if (contextName != null)
			Out.addChild(element, "contextName", contextName);

		if (requiredContext != null)
			Out.addChild(element, "requiredContext", requiredContext);

		return element;
	}

}
