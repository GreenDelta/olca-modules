package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_mappings")
public class Mapping extends AbstractEntity {

	@Column(name = "for_import")
	private boolean forImport;

	@Column(name = "format")
	private String format;

	@Column(name = "model_type")
	@Enumerated(EnumType.STRING)
	private ModelType modelType;

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

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}

	public ModelType getModelType() {
		return modelType;
	}

}
