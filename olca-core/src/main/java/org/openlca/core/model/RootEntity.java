package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;

/**
 * A categorized entity is a root entity with a category.
 */
@MappedSuperclass
public abstract class RootEntity extends RefEntity {

	// @Version
	@Column(name = "version")
	public long version;

	@Column(name = "last_change")
	public long lastChange;

	@OneToOne
	@JoinColumn(name = "f_category")
	public Category category;

	/**
	 * Tags are stored in a single string separated by commas `,`.
	 */
	@Column(name = "tags")
	public String tags;

	/**
	 * If a data set belongs to a library, this field must contain
	 * the identifier (which is typically a combination of library
	 * name and version, e.g. ecoinvent_apos_3.6).
	 */
	@Column(name = "library")
	public String library;

	/**
	 * Returns true if this data set is from a library.
	 */
	public boolean isFromLibrary() {
		return library != null;
	}
}
