package org.openlca.core.model.descriptors;

import java.util.Objects;

import org.openlca.core.model.ModelType;

/**
 * Descriptors are lightweight models containing only descriptive information of
 * a corresponding entity.The intention of descriptors is to get these
 * information fast from the database without loading the complete model.
 * Therefore, the respective DAO classes should provide these.
 */
public class BaseDescriptor {

	public String refId;
	public long id;
	public String name;
	public String description;
	public long version;
	public long lastChange;
	
	/**
	 * Tags are stored in a single string separated by commas `,`.
	 */
	public String tags;

	public ModelType type = ModelType.UNKNOWN;

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(this.getClass().isInstance(obj)))
			return false;
		BaseDescriptor other = (BaseDescriptor) obj;
		if (this.type != other.type)
			return false;
		if (this.id != 0 || other.id != 0)
			return this.id == other.id;
		else
			return Objects.equals(this.refId, other.refId)
					&& Objects.equals(this.name, other.name)
					&& Objects.equals(this.description, other.description);
	}

	@Override
	public int hashCode() {
		return Long.hashCode(id);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id=" + id + ", name=" + name
				+ ", type=" + type + "]";
	}

}
