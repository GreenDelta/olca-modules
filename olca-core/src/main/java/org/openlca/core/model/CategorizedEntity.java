package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

/**
 * A categorised entity is a root entity with a category.
 */
@MappedSuperclass
public abstract class CategorizedEntity extends RootEntity {

	@OneToOne
	@JoinColumn(name = "f_category")
	private Category category;

	// @Version
	@Column(name = "version")
	private long version;

	@Column(name = "last_change")
	private long lastChange;

	@PreUpdate
	@PrePersist
	private void updateDates() {
		version++;
		lastChange = System.currentTimeMillis();
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
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
}
