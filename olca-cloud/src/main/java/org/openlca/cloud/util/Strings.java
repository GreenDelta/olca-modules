package org.openlca.cloud.util;

import java.util.ArrayList;
import java.util.List;

public class Strings {

	public static String concat(Object... values) {
		if (values == null)
			return null;
		int totalLength = 0;
		List<String> strings = new ArrayList<>();
		for (Object value : values)
			strings.add(value.toString());
		for (String string : strings)
			totalLength += string.length();
		StringBuilder builder = new StringBuilder(totalLength);
		for (String string : strings)
			builder.append(string);
		return builder.toString();
	}

}
