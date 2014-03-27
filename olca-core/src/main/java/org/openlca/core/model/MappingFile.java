package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A mapping file is used to store mappings of entities from other data formats
 * (like ILCD, EcoSpold etc.) to openLCA entities or the other way around. See
 * the olca-io module for the use of such mapping files in imports and exports.
 * We just store the name of the file as string and its content as BLOB (which
 * normally contains the compressed content).
 */
@Entity
@Table(name = "tbl_mapping_files")
public class MappingFile extends AbstractEntity {

	@Column(name = "file_name")
	private String fileName;

	@Column(name = "content")
	private byte[] content;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

}
