package org.openlca.core.model;

/**
 * Parameters can be defined globally, in processes, or LCIA categories. They
 * can be redefined in calculation setups on the project and product system
 * level, but the initial definition is always only global, in processes, or
 * LCIA category.
 */
public enum ParameterScope {

	PROCESS,

	IMPACT,

	GLOBAL;

	/**
	 * Same as valueOf but it returns {@code null} instead of throwing an
	 * exception when the given string does not match a constant of this
	 * enumeration. Also, lower case constant names are allowed.
	 */
	public static ParameterScope fromString(String s) {
		if (s == null || s.isBlank())
			return null;
		for (var v : values()) {
			if (s.equalsIgnoreCase(v.name()))
				return v;
		}
		return null;
	}


}
