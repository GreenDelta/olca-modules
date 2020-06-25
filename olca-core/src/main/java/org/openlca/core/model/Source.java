package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_sources")
public class Source extends CategorizedEntity {

	@Column(name = "url")
	public String url;

	@Lob
	@Column(name = "text_reference")
	public String textReference;

	@Column(name = "source_year")
	public Short year;

	@Column(name = "external_file")
	public String externalFile;

	@Override
	public Source clone() {
		var clone = new Source();
		Util.copyFields(this, clone);
		clone.url = url;
		clone.textReference = textReference;
		clone.year = year;
		clone.externalFile = externalFile;
		return clone;
	}

}
