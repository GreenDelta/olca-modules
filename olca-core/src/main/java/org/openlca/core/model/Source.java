package org.openlca.core.model;

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

	@Column(name = "external_file")
	private String externalFile;

	@Override
	public Source clone() {
		Source clone = new Source();
		Util.cloneRootFields(this, clone);
		clone.setCategory(getCategory());
		clone.setDoi(getDoi());
		clone.setTextReference(getTextReference());
		clone.setYear(getYear());
		clone.setExternalFile(getExternalFile());
		return clone;
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

	public String getExternalFile() {
		return externalFile;
	}

	public void setExternalFile(String externalFile) {
		this.externalFile = externalFile;
	}

}
