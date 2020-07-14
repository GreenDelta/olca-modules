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

		switch (length) {
		case 1:
			return ".";
		case 2:
			return "..";
		default:
			return str.substring(0, length - 3).concat("...");
		}
	}

	public static String cutLeft(String s, int len) {
		if (s == null || len <= 0)
			return "";

		String str = s.trim();
		if (str.length() <= len)
			return str;

		switch (len) {
		case 1:
			return ".";
		case 2:
			return "..";
		default:
			return "...".concat(str.substring(str.length() - len + 3));
		}
	}

	public static String[] readLines(InputStream is) throws IOException {
		if (is == null)
			return new String[0];

		List<String> list = new ArrayList<>();
		InputStreamReader reader = new InputStreamReader(is);
		try (BufferedReader buffer = new BufferedReader(reader)) {
			String line = null;
			while ((line = buffer.readLine()) != null) {
				list.add(line);
			}
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Returns true if both of the given strings are null or if both of the
	 * given strings are equal.
	 */
	public static boolean nullOrEqual(String string1, String string2) {
		return (string1 == null && string2 == null)
				|| (string1 != null && string2 != null && string1
						.equals(string2));
	}

	/**
	 * Returns true if the given string value is null or empty.
	 */
	public static boolean nullOrEmpty(String val) {
		if (val == null)
			return true;
		return val.trim().length() == 0;
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
		if (str1 == null && str2 != null)
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
			return new String[] { value };
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

}
