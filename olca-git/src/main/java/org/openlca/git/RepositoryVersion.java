package org.openlca.git;

public record RepositoryVersion(int value) {

	static final int FALLBACK = 1;
	static final int CURRENT = 2;

	/**
	 * Get the current schema version that is supported by this API.
	 */
	public static RepositoryVersion current() {
		return new RepositoryVersion(CURRENT);
	}

	/**
	 * Get the fallback version of the schema.
	 */
	public static RepositoryVersion fallback() {
		return new RepositoryVersion(FALLBACK);
	}

	public boolean isCurrent() {
		return value == CURRENT;
	}

	public boolean isOlder() {
		return value < CURRENT;
	}

	public boolean isNewer() {
		return value > CURRENT;
	}
}
