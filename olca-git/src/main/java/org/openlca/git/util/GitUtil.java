package org.openlca.git.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GitUtil {

	private static final List<Character> hexChars = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
			'b', 'c', 'd', 'e', 'f');
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

	public static boolean isBinDir(String path) {
		path = path.toLowerCase();
		if (!path.endsWith("_bin"))
			return false;
		if (path.contains("/")) {
			path = path.substring(path.lastIndexOf("/") + 1);
		}
		if (path.length() != 40)
			return false;
		for (int i = 0; i < path.length() - 4; i++) {
			var c = path.charAt(i);
			if (i == 8 || i == 13 || i == 18 || i == 23) {
				if (c != '-')
					return false;
				continue;
			}
			if (!hexChars.contains(c))
				return false;
		}
		return true;
	}

}
