package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractEntity implements Indexable {

	@Id
	@Column(name = "id")
	private String id;

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(this.getClass().isInstance(obj)))
			return false;
		AbstractEntity other = (AbstractEntity) obj;
		return this.id != null && other.id != null && this.id.equals(other.id);
	}

	@Override
	public int hashCode() {
		if (id != null)
			return id.hashCode();
		return super.hashCode();
	}

	@Override
	public String toString() {
		return "AbstractEntity [type=" + getClass().getSimpleName() + ", id="
				+ id + "]";
	}

}
