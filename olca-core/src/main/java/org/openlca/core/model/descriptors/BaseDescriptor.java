package org.openlca.core.model.descriptors;

import java.io.Serializable;

import org.openlca.core.model.Indexable;
import org.openlca.core.model.ModelType;

/**
 * Descriptors are lightweight models containing only descriptive information of
 * the corresponding model.The intention of descriptors is to get these
 * information fast from the database without loading the complete model.
 * Therefore, the respective DAO classes should provide these.
 */
public class BaseDescriptor implements Indexable, Comparable<BaseDescriptor>,
		Serializable {

	private static final long serialVersionUID = -8609519818770549160L;

	private String id;
	private String name;
	private String description;
	private ModelType type = ModelType.UNKNOWN;

	@Override
	public int compareTo(BaseDescriptor o) {
		if (name == null)
			return 0;
		return name.toLowerCase().compareTo(
				o.name != null ? o.name.toLowerCase() : null);
	}

	@Override
	public String getRefId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/** Returns a name to be used in label-providers etc. */
	public String getDisplayName() {
		if (name == null)
			return "";
		return name;
	}

	/** Returns an info-text to be used in tool-tips etc. */
	public String getDisplayInfoText() {
		if (description == null)
			return "";
		return description;
	}

	public void setType(ModelType type) {
		this.type = type;
	}

	public ModelType getModelType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(this.getClass().isInstance(obj)))
			return false;
		BaseDescriptor other = (BaseDescriptor) obj;
		return this.id != null && other.id != null && this.id.equals(other.id)
				&& this.type == other.type;
	}

	@Override
	public int hashCode() {
		if (id != null)
			return id.hashCode();
		return super.hashCode();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id=" + id + ", name=" + name
				+ "type=" + type + "]";
	}

}
