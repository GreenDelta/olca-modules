package org.openlca.ilcd.epd.model;

import java.util.Objects;

public class EpdDescriptor {

	public String name;
	public String refId;
	public String version;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof EpdDescriptor other))
			return false;
		return Objects.equals(this.refId, other.refId);
	}

	@Override
	public int hashCode() {
		return refId == null ? super.hashCode() : refId.hashCode();
	}

}
