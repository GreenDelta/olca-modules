package org.openlca.git.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class GitUtil {

	private static final Map<String, String> encodings = new HashMap<>();

	static {
		encodings.put("<", "%3C");
		encodings.put(">", "%3E");
		encodings.put(":", "%3A");
		encodings.put("\"", "%22");
		encodings.put("\\", "%5C");
		encodings.put("|", "%7C");
		encodings.put("?", "%3F");
		encodings.put("*", "%2A");
	}

	public static String encode(String name) {
		if (name == null)
			return null;
		for (Entry<String, String> entry : encodings.entrySet()) {
			name = name.replace(entry.getKey(), entry.getValue());
		}
		return name;
	}

	public static String decode(String name) {
		if (name == null)
			return null;
		for (Entry<String, String> entry : encodings.entrySet()) {
			name = name.replace(entry.getValue(), entry.getKey());
		}
		return name;
	}

}
