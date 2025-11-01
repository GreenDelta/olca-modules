package org.openlca.util;

/// Utility class for common string operations.
public class Strings {

	private Strings() {
	}

	/// Truncates a string to the specified length, appending "..." at the end if
	/// truncation occurs. The string is trimmed before truncation.
	public static String cutEnd(String s, int len) {

		if (s == null || len <= 0)
			return "";

		String str = s.trim();
		if (str.length() <= len)
			return str;

		return switch (len) {
			case 1 -> ".";
			case 2 -> "..";
			default -> str.substring(0, len - 3).concat("...");
		};
	}

	/// Truncates a string to the specified length, prepending "..." at the
	/// beginning if truncation occurs. The string is trimmed before truncation.
	public static String cutStart(String s, int len) {
		if (s == null || len <= 0)
			return "";

		String str = s.trim();
		if (str.length() <= len)
			return str;

		return switch (len) {
			case 1 -> ".";
			case 2 -> "..";
			default -> "...".concat(str.substring(str.length() - len + 3));
		};
	}

	/// Truncates a string in the middle to fit within the specified maximum
	/// length, replacing the removed portion with "...". The prefix and suffix
	/// portions are preserved to give context from both ends of the string.
	/// The string is trimmed before truncation.
	public static String cutMid(String s, int len) {
		if (s == null || len <= 0)
			return "";
		var str = s.trim();
		if (str.length() <= len)
			return str;
		if (len <= 3)
			return "...".substring(0, len);

		double half = (len - 3.0) / 2.0;
		int prefixLen = (int) Math.ceil(half);
		var prefix = str.substring(0, prefixLen) + "...";

		int suffixLen = len - prefixLen - 3;
		return suffixLen > 0
			? prefix + str.substring(str.length() - suffixLen)
			: prefix;
	}

	/// Returns `true` if the given string is `null`, empty, or contains only
	/// whitespace characters.
	public static boolean isBlank(String s) {
		return s == null || s.isBlank();
	}

	/// Returns `true` if the given string is not `null`, not empty, and contains
	/// at least one non-whitespace character.
	public static boolean isNotBlank(String s) {
		return s != null && !s.isBlank();
	}

	/// Compares two strings lexicographically, ignoring case differences. Returns
	/// `0` if both strings are equal (ignoring case) or both are `null`, a
	/// negative value if the first string is less than the second, or a positive
	/// value if the first string is greater than the second.
	public static int compareIgnoreCase(String a, String b) {
		if (a == null && b == null)
			return 0;
		if (a == null)
			return -1;
		if (b == null)
			return 1;
		return a.compareToIgnoreCase(b);
	}

	/// Compares two strings for equality, ignoring case differences. Returns
	/// `true` if both strings are equal (ignoring case), or if both are `null`.
	public static boolean equalsIgnoreCase(String a, String b) {
		return a == b
			? true
			: (a != null && a.equalsIgnoreCase(b));
	}

	/// Returns an empty string if the given string is `null`, otherwise returns
	/// the original string unchanged. This method provides a null-safe way to
	/// ensure you always get a non-null string value.
	public static String notNull(String s) {
		return s == null ? "" : s;
	}

	/// Returns `null` if the given string is `null`, empty, or contains only
	/// whitespace characters, otherwise returns the original string unchanged.
	/// This method provides a way to convert blank strings to `null` values.
	public static String nullIfBlank(String s) {
		return isBlank(s) ? null : s;
	}
}
