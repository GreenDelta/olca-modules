package org.openlca.ecospold2;

import org.jdom2.Element;

public class DataGenerator {

	private String personId;
	private String personName;
	private String personEmail;
	private String publishedSourceId;
	private Integer publishedSourceYear;
	private String publishedSourceFirstAuthor;
	private boolean isCopyrightProtected;
	private String pageNumbers;
	private Integer accessRestrictedTo;
	private String companyId;
	private String companyCode;

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public String getPersonEmail() {
		return personEmail;
	}

	public void setPersonEmail(String personEmail) {
		this.personEmail = personEmail;
	}

	public String getPublishedSourceId() {
		return publishedSourceId;
	}

	public void setPublishedSourceId(String publishedSourceId) {
		this.publishedSourceId = publishedSourceId;
	}

	public Integer getPublishedSourceYear() {
		return publishedSourceYear;
	}

	public void setPublishedSourceYear(Integer publishedSourceYear) {
		this.publishedSourceYear = publishedSourceYear;
	}

	public String getPublishedSourceFirstAuthor() {
		return publishedSourceFirstAuthor;
	}

	public void setPublishedSourceFirstAuthor(String publishedSourceFirstAuthor) {
		this.publishedSourceFirstAuthor = publishedSourceFirstAuthor;
	}

	public boolean isCopyrightProtected() {
		return isCopyrightProtected;
	}

	public void setCopyrightProtected(boolean isCopyrightProtected) {
		this.isCopyrightProtected = isCopyrightProtected;
	}

	public String getPageNumbers() {
		return pageNumbers;
	}

	public void setPageNumbers(String pageNumbers) {
		this.pageNumbers = pageNumbers;
	}

	public Integer getAccessRestrictedTo() {
		return accessRestrictedTo;
	}

	public void setAccessRestrictedTo(Integer accessRestrictedTo) {
		this.accessRestrictedTo = accessRestrictedTo;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

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
		Element element = new Element("dataGeneratorAndPublication", Out.NS);

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
