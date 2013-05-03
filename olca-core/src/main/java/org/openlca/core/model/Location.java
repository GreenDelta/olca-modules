package org.openlca.core.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "tbl_locations")
public class Location extends AbstractEntity {

	@Column(name = "code")
	private String code;

	@Lob
	@Column(name = "description")
	private String description;

	@Column(name = "latitude")
	private double latitude;

	@Column(name = "longitude")
	private double longitude;

	@Column(name = "name")
	private String name;

	@Transient
	private transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getName() {
		return name;
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	public void setCode(String code) {
		support.firePropertyChange("code", this.code, this.code = code);
	}

	public void setDescription(String description) {
		support.firePropertyChange("description", this.description,
				this.description = description);
	}

	public void setLatitude(double latitude) {
		support.firePropertyChange("latitude", this.latitude,
				this.latitude = latitude);
	}

	public void setLongitude(double longitude) {
		support.firePropertyChange("longitude", this.longitude,
				this.longitude = longitude);
	}

	public void setName(String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

}
