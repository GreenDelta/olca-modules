package org.openlca.ilcd.epd.model;

import java.util.Objects;

public final class MaterialProperty implements Cloneable {

	public String id;
	public String name;
	public String unit;
	public String unitDescription;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof MaterialProperty other))
			return false;
		return Objects.equals(this.id, other.id);
	}

	@Override
	public int hashCode() {
		return id == null
			? super.hashCode()
			: id.hashCode();
	}

	@Override
	public MaterialProperty clone() {
		try {
			return (MaterialProperty) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
