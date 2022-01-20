package org.openlca.git.model;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;

public enum DiffType {

	ADDED,

	MODIFIED,

	DELETED;

	static DiffType forChangeType(ChangeType type) {
		switch (type) {
		case ADD:
			return ADDED;
		case MODIFY:
			return MODIFIED;
		case DELETE:
			return DELETED;
		default:
			throw new IllegalArgumentException("Unsupported change type: " + type);
		}
	}

}
