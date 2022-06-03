package org.openlca.core.library;

/**
 * Describes the state of a library that is not mounted to a database yet.
 */
public enum PreMountState {

	/**
	 * No data of the library are present in the database.
	 */
	NEW,

	/**
	 * All data of the library are present in the database and tagged with that
	 * library.
	 */
	PRESENT,

	/**
	 * All data of the library are present in the database but tagged with another
	 * or no library.
	 */
	TAG_CONFLICT,

	/**
	 * All other conflicting states.
	 */
	CONFLICT;

	/**
	 * Returns the recommended action when a library with the given state should
	 * be added to a database.
	 */
	public MountAction defaultAction() {
		return switch (this) {
			case NEW -> MountAction.ADD;
			case PRESENT -> MountAction.SKIP;
			default -> MountAction.UPDATE;
		};
	}

	/**
	 * Returns all possible actions that can be performed when a library of this
	 * state is mounted to a database.
	 */
	public MountAction[] actions() {
		return switch (this) {
			case NEW -> new MountAction[]{
				MountAction.ADD
			};
			case PRESENT -> new MountAction[]{
				MountAction.SKIP, MountAction.UPDATE
			};
			case TAG_CONFLICT -> new MountAction[]{
				MountAction.UPDATE, MountAction.RETAG
			};
			case CONFLICT -> new MountAction[]{
				MountAction.UPDATE
			};
		};
	}

	PreMountState join(PreMountState other) {
		if (other == null || this == other)
			return this;
		if (this == PreMountState.PRESENT && other == PreMountState.TAG_CONFLICT)
			return PreMountState.TAG_CONFLICT;
		if (this == PreMountState.TAG_CONFLICT && other == PreMountState.PRESENT)
			return PreMountState.TAG_CONFLICT;
		return PreMountState.CONFLICT;
	}
}
