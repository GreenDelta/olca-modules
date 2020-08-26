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

	@Lob
	@Column(name = "kmz")
	public byte[] kmz;

	@Override
	public Location clone() {
		Location clone = new Location();
		Util.copyRootFields(this, clone);
		clone.code = code;
		clone.latitude = latitude;
		clone.longitude = longitude;
		clone.kmz = kmz;
		return clone;
	}

}
