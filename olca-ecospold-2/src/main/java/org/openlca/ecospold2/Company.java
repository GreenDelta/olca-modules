package org.openlca.ecospold2;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class Company {

	private String id;
	private String code;
	private String website;
	private String name;
	private String comment;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	Element toXml(Namespace ns) {
		Element element = new Element("company", ns);
		if (id != null)
			element.setAttribute("id", id);
		if (code != null)
			element.setAttribute("code", code);
		if (website != null)
			element.setAttribute("website", website);
		if (name != null)
			Out.addChild(element, "name", name);
		if (comment != null)
			Out.addChild(element, "comment", comment);
		return element;
	}

}
