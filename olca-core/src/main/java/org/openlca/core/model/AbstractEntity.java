package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * This is an abstract class for everything that is identified by an ID and can
 * be stored in a database via JPA. It provides implementations for
 * <code>hashCode</code> and <code>equals</code> that are based on the ID field.
 */
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
		return this.getId() != null && other.getId() != null
				&& this.getId().equals(other.getId());
	}

	@Override
	public int hashCode() {
		if (getId() != null)
			return getId().hashCode();
		return super.hashCode();
	}

	@Override
	public String toString() {
		return "Entity [type=" + getClass().getSimpleName() + ", id=" + getId()
				+ "]";
	}

}
