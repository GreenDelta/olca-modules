package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;

/**
 * A RefEntity is an entity that can be referenced by a unique ID, the reference
 * ID or short `refId`.
 */
@MappedSuperclass
public abstract class RefEntity
	extends AbstractEntity implements Copyable<RefEntity> {

	@Column(name = "ref_id")
	public String refId;

	@Column(name = "name")
	public String name;

	@Lob
	@Column(name = "description")
	public String description;

	@Override
	public String toString() {
		return "RootEntity [type="
				+ getClass().getSimpleName()
				+ ", refId=" + refId
				+ ", name=" + name + "]";
	}

}
