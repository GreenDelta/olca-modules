package org.openlca.simapro.csv.model;

/**
 * This class represents a SimaPro literature reference
 */
public class SPLiteratureReference {

	private String category;
	private String content;
	private String name;
	private String documentLink;

	public SPLiteratureReference(String name, String content, String category) {
		this.name = name;
		this.content = content;
		this.category = category;
	}

	public String getContent() {
		return content;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDocumentLink(String documentLink) {
		this.documentLink = documentLink;
	}

	public String getDocumentLink() {
		return documentLink;
	}

}
