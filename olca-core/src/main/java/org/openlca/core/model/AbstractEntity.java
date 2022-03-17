package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.TableGenerator;

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
	@TableGenerator(
			name = "entity_seq",
			pkColumnName = "SEQ_NAME",
			valueColumnName = "SEQ_COUNT",
			pkColumnValue = "entity_seq",
			allocationSize = 150,
			table = "SEQUENCE")
	public long id;

	@Override
	public final boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(this.getClass().isInstance(obj)))
			return false;
		var other = (AbstractEntity) obj;
		if (this.id == 0L && other.id == 0L) {
			if (this instanceof RefEntity) {
				var thisRE = (RefEntity) this;
				var otherRE = (RefEntity) other;
				if (thisRE.refId != null)
					return thisRE.refId.equals(otherRE.refId);
			}
			return false;
		}
		return this.id == other.id;
	}

	@Override
	public final int hashCode() {
		if (id != 0)
			return Long.hashCode(id);
		if (this instanceof RefEntity) {
			var re = (RefEntity) this;
			if (re.refId != null) {
				return re.refId.hashCode();
			}
		}
		return super.hashCode();
	}

	@Override
	public String toString() {
		return "Entity [type="
				+ getClass().getSimpleName()
				+ ", id=" + id + "]";
	}

}
