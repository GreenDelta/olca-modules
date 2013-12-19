package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_mappings")
public class Mapping extends AbstractEntity {

	@Column(name = "for_import")
	private boolean forImport;

	@Column(name = "mapping_type")
	private String mappingType;

	@Column(name = "olca_content")
	private String olcaContent;

	@Column(name = "external_content")
	private String externalContent;

	public boolean isForImport() {
		return forImport;
	}

	public void setForImport(boolean forImport) {
		this.forImport = forImport;
	}

	public String getMappingType() {
		return mappingType;
	}

	public void setMappingType(String mappingType) {
		this.mappingType = mappingType;
	}

	public String getOlcaContent() {
		return olcaContent;
	}

	public void setOlcaContent(String olcaContent) {
		this.olcaContent = olcaContent;
	}

	public String getExternalContent() {
		return externalContent;
	}

	public void setExternalContent(String externalContent) {
		this.externalContent = externalContent;
	}

}
