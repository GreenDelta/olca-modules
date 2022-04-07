package org.openlca.git.model;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;

public enum DiffType {

	ADDED,

	MODIFIED,

	DELETED;

	public static DiffType forChangeType(ChangeType type) {
		return switch (type) {
			case ADD -> ADDED;
			case MODIFY -> MODIFIED;
			case DELETE -> DELETED;
			default -> throw new IllegalArgumentException("Unsupported change type: " + type);
		};
	}

	public ChangeType toChangeType() {
		return switch (this) {
			case ADDED -> ChangeType.ADD;
			case MODIFIED -> ChangeType.MODIFY;
			case DELETED -> ChangeType.DELETE;
		};
	}

}
