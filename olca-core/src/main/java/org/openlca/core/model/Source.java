package org.openlca.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_sources")
public class Source extends CategorizedEntity {

	@Column(name = "doi")
	private String doi;

	@Lob
	@Column(name = "text_reference")
	private String textReference;

	@Column(name = "source_year")
	private Short year;

	@Override
	public Source clone() {
		Source source = new Source();
		source.setRefId(UUID.randomUUID().toString());
		source.setName(getName());
		source.setCategory(getCategory());
		source.setDescription(getDescription());
		source.setDoi(getDoi());
		source.setTextReference(getTextReference());
		source.setYear(getYear());
		return source;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public String getTextReference() {
		return textReference;
	}

	public void setTextReference(String textReference) {
		this.textReference = textReference;
	}

	public Short getYear() {
		return year;
	}

	public void setYear(Short year) {
		this.year = year;
	}

}
