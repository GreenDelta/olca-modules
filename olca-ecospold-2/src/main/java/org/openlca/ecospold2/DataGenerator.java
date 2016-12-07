package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataGenerator {

	@XmlAttribute
	public String personId;

	@XmlAttribute
	public String personName;

	@XmlAttribute
	public String personEmail;

	@XmlAttribute
	public String publishedSourceId;

	@XmlAttribute
	public Integer publishedSourceYear;

	@XmlAttribute
	public String publishedSourceFirstAuthor;

	@XmlAttribute
	public boolean isCopyrightProtected;

	@XmlAttribute
	public String pageNumbers;

	@XmlAttribute
	public Integer accessRestrictedTo;

	@XmlAttribute
	public String companyId;

	@XmlAttribute
	public String companyCode;

	static DataGenerator fromXml(Element e) {
		if (e == null)
			return null;
		DataGenerator datagenerator = new DataGenerator();
		datagenerator.personId = e.getAttributeValue("personId");
		datagenerator.personName = e.getAttributeValue("personName");
		datagenerator.personEmail = e.getAttributeValue("personEmail");
		datagenerator.publishedSourceId = e
				.getAttributeValue("publishedSourceId");
		String yearStr = e.getAttributeValue("publishedSourceYear");
		if (yearStr != null)
			datagenerator.publishedSourceYear = In.integer(yearStr);
		datagenerator.publishedSourceFirstAuthor = e
				.getAttributeValue("publishedSourceFirstAuthor");
		datagenerator.isCopyrightProtected = In.bool(e
				.getAttributeValue("isCopyrightProtected"));
		datagenerator.pageNumbers = e.getAttributeValue("pageNumbers");
		String accessRestrictedToStr = e
				.getAttributeValue("accessRestrictedTo");
		if (accessRestrictedToStr != null)
			datagenerator.accessRestrictedTo = In
					.integer(accessRestrictedToStr);
		datagenerator.companyId = e.getAttributeValue("companyId");
		datagenerator.companyCode = e.getAttributeValue("companyCode");
		return datagenerator;
	}

	Element toXml() {
		Element element = new Element("dataGeneratorAndPublication", IO.NS);

		if (personId != null)
			element.setAttribute("personId", personId);

		if (personName != null)
			element.setAttribute("personName", personName);

		if (personEmail != null)
			element.setAttribute("personEmail", personEmail);

		if (publishedSourceId != null)
			element.setAttribute("publishedSourceId", publishedSourceId);

		if (publishedSourceYear != null)
			element.setAttribute("publishedSourceYear",
					publishedSourceYear.toString());

		if (publishedSourceFirstAuthor != null)
			element.setAttribute("publishedSourceFirstAuthor",
					publishedSourceFirstAuthor);

		element.setAttribute("isCopyrightProtected",
				Boolean.toString(isCopyrightProtected));

		if (pageNumbers != null)
			element.setAttribute("pageNumbers", pageNumbers);

		if (accessRestrictedTo != null)
			element.setAttribute("accessRestrictedTo",
					accessRestrictedTo.toString());

		if (companyId != null)
			element.setAttribute("companyId", companyId);

		if (companyCode != null)
			element.setAttribute("companyCode", companyCode);

		return element;
	}

}
