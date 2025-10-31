package org.openlca.util;

import java.util.HashSet;
import java.util.Locale;
import java.util.function.Function;

/// Utility class for common string operations.
public class Strings {

	private Strings() {
	}

	/// Truncates a string to the specified length, appending "..." at the end if
  /// truncation occurs. The string is trimmed before truncation.
	public static String cutEnd(String string, int length) {

		if (string == null || length <= 0)
			return "";

		String str = string.trim();
		if (str.length() <= length)
			return str;

		return switch (length) {
			case 1 -> ".";
			case 2 -> "..";
			default -> str.substring(0, length - 3).concat("...");
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
		var trimmed = s.strip();
		if (trimmed.length() <= len)
			return trimmed;
		if (len <= 3)
			return "...".substring(0, len);

		double half = (len - 3.0) / 2.0;
		int prefixLen = (int) Math.ceil(half);
		var prefix = trimmed.substring(0, prefixLen) + "...";

		int suffixLen = len - prefixLen - 3;
		return suffixLen > 0
			? prefix + trimmed.substring(trimmed.length() - suffixLen)
			: prefix;
	}

	/**
	 * Returns true if both of the given strings are null or if both of the
	 * given strings are equal.
	 */
	public static boolean nullOrEqual(String string1, String string2) {
		return (string1 == null && string2 == null)
				|| (string1 != null && string1.equals(string2));
	}

	/**
	 * Returns true if the given string value is {@code null} or empty. It also
	 * returns true if the given string contains only whitespaces.
	 */
	public static boolean nullOrEmpty(String val) {
		return val == null || val.isBlank();
	}

	/**
	 * Returns true if the string is not null or empty, means that it contains
	 * other characters than white-spaces.
	 */
	public static boolean notEmpty(String val) {
		return val != null && !val.isBlank();
	}

	/**
	 * A null-save method for comparing two strings ignoring the case.
	 */
	public static int compare(String str1, String str2) {
		if (str1 == null && str2 == null)
			return 0;
		if (str1 != null && str2 == null)
			return 1;
		if (str1 == null)
			return -1;
		return str1.compareToIgnoreCase(str2);
	}

	/**
	 * Returns the empty string if the given string is null. Otherwise the
	 * given string is returned.
	 */
	public static String orEmpty(String s) {
		return s == null ? "" : s;
	}

	/**
	 * Returns null if the given string is empty or contains only whitespaces,
	 * otherwise it returns the unchanged string.
	 */
	public static String nullIfEmpty(String s) {
		return s == null
				? null
				: s.isBlank()
				? null
				: s;
	}

	public static <T> String uniqueNameOf(
			String base, Iterable<T> existing, Function<T, String> fn
	) {
		var raw = base == null ? "" : base.trim();
		if (existing == null)
			return raw;
		var s = new HashSet<String>();
		for (var e : existing) {
			var en = fn.apply(e);
			if (en != null) {
				s.add(en.trim().toLowerCase(Locale.US));
			}
		}

		var norm = raw.trim().toLowerCase(Locale.US);
		if (!s.contains(norm))
			return raw;

		int i = 1;
		String nextNorm;
		do {
			i++;
			nextNorm = norm + " (" + i + ")";
		} while (s.contains(nextNorm));
		return raw.trim() + " (" + i + ")";
	}
}
