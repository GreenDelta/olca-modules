package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

/**
 * A root entity is a stand alone entity in the application. It should have a
 * name, description, and an UUID (reference ID). A root entity can contain
 * other root entities via aggregation but the life cycle of the contained
 * entities is then not coupled to the life cycle of the respective container
 * (no cascade delete etc.). On the other side, the life cycle of non-root
 * entities contained in root entities is coupled to the life cycle of the
 * container.
 * 
 * Root entities must provide an implementation of <code>clone</code> with flat
 * copies for contained root-entities and deep copies for contained non-root
 * entities.
 * 
 */
@MappedSuperclass
public abstract class RootEntity extends AbstractEntity implements Cloneable {

	@Column(name = "ref_id")
	private String refId;

	@Column(name = "name")
	private String name;

	@Lob
	@Column(name = "description")
	private String description;

	// @Version
	@Column(name = "version")
	private long version;

	@Column(name = "last_change")
	private long lastChange;

	public abstract Object clone();

	public String getRefId() {
		return refId;
	}

	public void setRefId(String id) {
		this.refId = id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public long getLastChange() {
		return lastChange;
	}

	public void setLastChange(long lastChange) {
		this.lastChange = lastChange;
	}

	@Override
	public String toString() {
		return "RootEntity [type=" + getClass().getSimpleName() + ", refId="
				+ getRefId() + ", name=" + getName() + "]";
	}

}
