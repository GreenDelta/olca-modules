package org.openlca.ecospold2;

import java.util.Date;

import org.jdom2.Element;

public class FileAttributes {

	private int majorRelease;
	private int minorRelease;
	private int majorRevision;
	private int minorRevision;
	private String defaultLanguage;
	private Date creationTimestamp;
	private Date lastEditTimestamp;
	private String internalSchemaVersion;
	private String fileGenerator;
	private Date fileTimestamp;
	private String contextId;
	private String contextName;
	private String requiredContext;

	public int getMajorRelease() {
		return majorRelease;
	}

	public void setMajorRelease(int majorRelease) {
		this.majorRelease = majorRelease;
	}

	public int getMinorRelease() {
		return minorRelease;
	}

	public void setMinorRelease(int minorRelease) {
		this.minorRelease = minorRelease;
	}

	public int getMajorRevision() {
		return majorRevision;
	}

	public void setMajorRevision(int majorRevision) {
		this.majorRevision = majorRevision;
	}

	public int getMinorRevision() {
		return minorRevision;
	}

	public void setMinorRevision(int minorRevision) {
		this.minorRevision = minorRevision;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public Date getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(Date creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public Date getLastEditTimestamp() {
		return lastEditTimestamp;
	}

	public void setLastEditTimestamp(Date lastEditTimestamp) {
		this.lastEditTimestamp = lastEditTimestamp;
	}

	public String getInternalSchemaVersion() {
		return internalSchemaVersion;
	}

	public void setInternalSchemaVersion(String internalSchemaVersion) {
		this.internalSchemaVersion = internalSchemaVersion;
	}

	public String getFileGenerator() {
		return fileGenerator;
	}

	public void setFileGenerator(String fileGenerator) {
		this.fileGenerator = fileGenerator;
	}

	public Date getFileTimestamp() {
		return fileTimestamp;
	}

	public void setFileTimestamp(Date fileTimestamp) {
		this.fileTimestamp = fileTimestamp;
	}

	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	public String getContextName() {
		return contextName;
	}

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	public String getRequiredContext() {
		return requiredContext;
	}

	public void setRequiredContext(String requiredContext) {
		this.requiredContext = requiredContext;
	}

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
