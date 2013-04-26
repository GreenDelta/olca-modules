package org.openlca.expressions;

import java.util.HashMap;

class Constants {

	private static HashMap<String, Object> values = new HashMap<>();

	static {
		values.put("pi", Math.PI);
		values.put("e", Math.E);
		values.put("true", Boolean.TRUE);
		values.put("false", Boolean.FALSE);
	}

	private Constants() {
	}

	public static Object get(String name) {
		return values.get(name);
	}

}
