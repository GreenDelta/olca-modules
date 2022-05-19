package org.openlca.jsonld;

public record SchemaVersion(int value) {

	static final int FALLBACK = 1;
	static final int CURRENT = 2;

	/**
	 * Get the current schema version that is supported by this API.
	 */
	public static SchemaVersion current() {
		return new SchemaVersion(CURRENT);
	}

	/**
	 * Get the fallback version of the schema.
	 */
	public static SchemaVersion fallback() {
		return new SchemaVersion(FALLBACK);
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
