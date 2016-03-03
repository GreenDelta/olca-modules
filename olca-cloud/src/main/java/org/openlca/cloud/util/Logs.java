package org.openlca.cloud.util;

public final class Logs {

	public static String collectClasses(Object[] objects) {
		if (objects == null)
			return null;
		if (objects.length == 0)
			return null;
		StringBuilder classes = new StringBuilder();
		for (int i = 0; i < objects.length; i++) {
			if (i != 0)
				classes.append(", ");
			classes.append(objects[i].getClass().getCanonicalName());
		}
		return classes.toString();
	}

	public static String simpleClassName(Object object) {
		if (object == null)
			return null;
		return object.getClass().getSimpleName();
	}

	public static String className(Object object) {
		if (object == null)
			return null;
		return object.getClass().getSimpleName();
	}

}
