package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.SectionValue;

@BlockModel("Literature reference")
public class LiteratureReferenceBlock {

	@SectionValue("Name")
	private String name;

	@SectionValue("Documentation link")
	private String documentationLink;

	@SectionValue("Category")
	private String category;

	@SectionValue("Description")
	private String description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDocumentationLink() {
		return documentationLink;
	}

	public void setDocumentationLink(String documentationLink) {
		this.documentationLink = documentationLink;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "SPLiteratureReference [name=" + name + "]";
	}

}
