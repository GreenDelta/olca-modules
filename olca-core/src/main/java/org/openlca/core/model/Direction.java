package org.openlca.core.model;

public enum Direction {

	INPUT,

	OUTPUT;

	/**
	 * Tries to determine the direction from the given string. The rule is very
	 * simple: if the given string starts with {@code i} or {@code I} it returns
	 * {@code INPUT}, if it starts with {@code o} or {@code O} it returns
	 * {@code OUTPUT}, and in all other cases it returns {@code null}.
	 */
	public static Direction from(String s) {
		if (s == null || s.isEmpty())
			return null;
		char first = s.charAt(0);
		return switch (first) {
			case 'i', 'I' -> INPUT;
			case 'o', 'O' -> OUTPUT;
			default -> null;
		};
	}

}
