package com.greendelta.cloud.util;

import java.util.Collection;

public class Valid {

	public static void checkNotEmpty(String value, String name) {
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException(Strings.concat("No ", name, " set"));
	}

	public static void checkNotEmpty(Collection<?> value, String name) {
		if (value == null || value.isEmpty())
			throw new IllegalArgumentException(Strings.concat("No ", name, " set"));
	}

	public static void checkNotEmpty(Object value, String name) {
		if (value == null)
			throw new IllegalArgumentException(Strings.concat("No ", name, " set"));
	}

}
