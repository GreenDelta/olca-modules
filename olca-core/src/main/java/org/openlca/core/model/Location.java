package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_locations")
public class Location extends CategorizedEntity {

	@Column(name = "code")
	public String code;

	@Column(name = "latitude")
	public double latitude;

	@Column(name = "longitude")
	public double longitude;

	/**
	 * Contains the geographic data of this location in a binary format.
	 * Currently we convert GeoJSON to MessagePack and then compress this via
	 * gzip when writing this information and the other way around when reading
	 * it.
	 */
	@Lob
	@Column(name = "geodata")
	public byte[] geodata;

	@Override
	public Location clone() {
		Location clone = new Location();
		Util.copyFields(this, clone);
		clone.code = code;
		clone.latitude = latitude;
		clone.longitude = longitude;
		clone.geodata = geodata;
		return clone;
	}
}
