package org.openlca.util;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

/**
 * Generates a UUID version 3 for a given set of names.
 *
 * Note that the generated key is case insensitive and that leading and trailing
 * white-spaces are ignored.
 *
 */
public class KeyGen {

	public static final String NULL_UUID = "00000000-0000-0000-0000-000000000000";

	public static String get(String... names) {
		if (names == null || names.length == 0)
			return UUID.nameUUIDFromBytes(new byte[0]).toString();
		String path = toPath(names);
		try {
			var bytes = path.getBytes(StandardCharsets.UTF_8);
			return UUID.nameUUIDFromBytes(bytes).toString();
		} catch (Exception e) {
			throw new RuntimeException("Key generation failed", e);
		}
	}

	public static String toPath(String... names) {
		var builder = new StringBuilder();
		for (int i = 0; i < names.length; i++) {
			String val = names[i];
			val = val == null ? "" : val.trim();
			builder.append(val);
			if (i < (names.length - 1))
				builder.append('/');
		}
		return builder.toString().toLowerCase(Locale.US);
	}
}
