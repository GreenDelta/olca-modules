package org.openlca.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contains helper methods for string operations.
 */
public class Strings {

	private Strings() {
	}

	/**
	 * Cut a string to the given length. Appends "..." if the string was
	 * truncated.
	 */
	public static String cut(String string, int length) {

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

	public static String cutLeft(String s, int len) {
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

	public static String cutMid(String s, int maxLen) {
		if (s == null || maxLen <= 0)
			return "";
		if (s.length() < maxLen)
			return s;
		if (maxLen < 4)
			return "...";

		double half = (maxLen - 3.0) / 2.0;
		int prefixLen = (int) Math.ceil(half);
		int suffixLen = maxLen - prefixLen - 3;

		var r = prefixLen > 0
			? s.substring(0, prefixLen) + "..."
			: "...";
		return suffixLen > 0 && suffixLen < s.length()
			? r + s.substring(s.length() - suffixLen)
			: r;
	}

	public static String[] readLines(InputStream is) throws IOException {
		if (is == null)
			return new String[0];

		List<String> list = new ArrayList<>();
		InputStreamReader reader = new InputStreamReader(is);
		try (BufferedReader buffer = new BufferedReader(reader)) {
			String line;
			while ((line = buffer.readLine()) != null) {
				list.add(line);
			}
		}
		return list.toArray(new String[0]);
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
		if (val == null)
			return false;
		String str = val.trim();
		return !str.isEmpty();
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

	public static <T> String join(Collection<T> values, char delimiter) {
		String[] stringValues = new String[values.size()];
		int i = 0;
		for (T value : values)
			if (value != null)
				stringValues[i++] = value.toString();
		return join(stringValues, delimiter);
	}

	public static String join(String[] values, char delimiter) {
		int length = 0;
		for (String v : values)
			if (v != null)
				length += v.length();
		StringBuilder b = new StringBuilder(length + values.length - 1);
		for (int i = 0; i < values.length; i++) {
			if (i != 0)
				b.append(delimiter);
			if (values[i] != null)
				b.append(values[i]);
		}
		return b.toString();
	}

	public static String[] append(String[] array, String value) {
		return put(array, value, array == null ? 0 : array.length);
	}

	public static String[] prepend(String[] array, String value) {
		return put(array, value, 0);
	}

	private static String[] put(String[] array, String value, int index) {
		if (array == null || array.length == 0)
			return new String[]{value};
		String[] copy = new String[array.length + 1];
		for (int i = 0; i < copy.length; i++) {
			if (i == index) {
				copy[i] = value;
			} else if (i < index) {
				copy[i] = array[i];
			} else {
				copy[i] = array[i - 1];
			}
		}
		return copy;
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
}
