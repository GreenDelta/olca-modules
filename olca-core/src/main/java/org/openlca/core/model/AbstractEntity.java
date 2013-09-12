package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;

import com.google.common.primitives.Longs;

/**
 * This is an abstract class for everything that is identified by an ID and can
 * be stored in a database via JPA. Generally, the generation of the ID should
 * be managed by JPA.
 * 
 * This class provides implementations for <code>hashCode</code> and
 * <code>equals</code> that are based on the ID field.
 */
@MappedSuperclass
public abstract class AbstractEntity {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "entity_seq")
	@TableGenerator(name = "entity_seq", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "entity_seq", allocationSize = 150, table = "SEQUENCE")
	private long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
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
		if (this.getId() == 0L && other.getId() == 0L)
			return false; // we already checked this == other above
		return this.getId() == other.getId();
	}

	@Override
	public int hashCode() {
		return Longs.hashCode(getId());
	}

	@Override
	public String toString() {
		return "Entity [type=" + getClass().getSimpleName() + ", id=" + getId()
				+ "]";
	}

}
