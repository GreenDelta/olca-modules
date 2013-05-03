package org.openlca.core.model.iomaps;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.openlca.core.model.AbstractEntity;

@Entity
@Table(name = "tbl_mappings")
public class Mapping extends AbstractEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "map_type")
	private MapType mapType;

	@Enumerated(EnumType.STRING)
	@Column(name = "format")
	private IOFormat format;

	@Column(name = "external_key")
	private String externalKey;

	@Column(name = "external_name")
	private String externalName;

	@Column(name = "olca_id")
	private String olcaId;

	@Column(name = "factor")
	private double factor;

	public MapType getMapType() {
		return mapType;
	}

	public void setMapType(MapType mapType) {
		this.mapType = mapType;
	}

	public IOFormat getFormat() {
		return format;
	}

	public void setFormat(IOFormat format) {
		this.format = format;
	}

	public String getExternalKey() {
		return externalKey;
	}

	public void setExternalKey(String externalKey) {
		this.externalKey = externalKey;
	}

	public String getExternalName() {
		return externalName;
	}

	public void setExternalName(String externalName) {
		this.externalName = externalName;
	}

	public String getOlcaId() {
		return olcaId;
	}

	public void setOlcaId(String olcaId) {
		this.olcaId = olcaId;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

}
