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

	@Column(name = "olca_ref_id")
	private String olcaRefId;

	@Column(name = "content")
	private String content;

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

	public String getOlcaRefId() {
		return olcaRefId;
	}

	public void setOlcaRefId(String olcaRefId) {
		this.olcaRefId = olcaRefId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String externalContent) {
		this.content = externalContent;
	}

}
