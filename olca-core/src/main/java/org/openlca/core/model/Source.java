package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_sources")
public class Source extends RootEntity {

	@Column(name = "url")
	public String url;

	@Lob
	@Column(name = "text_reference")
	public String textReference;

	@Column(name = "source_year")
	public Short year;

	@Column(name = "external_file")
	public String externalFile;

	public static Source of(String name) {
		var source = new Source();
		Entities.init(source, name);
		return source;
	}

	@Override
	public Source copy() {
		var clone = new Source();
		Entities.copyFields(this, clone);
		clone.url = url;
		clone.textReference = textReference;
		clone.year = year;
		clone.externalFile = externalFile;
		return clone;
	}

}
