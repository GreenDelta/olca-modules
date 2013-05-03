package org.openlca.core.model;

import java.util.HashMap;
import java.util.Map;

public class PedigreeMatrix {

	private PedigreeMatrix() {
	}

	public static String toString(Map<PedigreeMatrixRow, Integer> map) {
		if (map == null || map.isEmpty())
			return "(n.a.;n.a.;n.a.;n.a.;n.a.)";
		StringBuilder builder = new StringBuilder("(");
		PedigreeMatrixRow[] keys = { PedigreeMatrixRow.RELIABILITY,
				PedigreeMatrixRow.COMPLETENESS, PedigreeMatrixRow.TIME,
				PedigreeMatrixRow.GEOGRAPHY, PedigreeMatrixRow.TECHNOLOGY };
		for (int i = 0; i < keys.length; i++) {
			Integer val = map.get(keys[i]);
			if (val == null)
				builder.append("n.a.");
			else
				builder.append(val);
			if (i < (keys.length - 1))
				builder.append(";");
		}
		builder.append(")");
		return builder.toString();
	}

	public static Map<PedigreeMatrixRow, Integer> fromString(String s) {
		if (s == null || s.isEmpty() || !s.startsWith("(") || !s.endsWith(")"))
			throw new IllegalArgumentException(
					"String must not be empty and start and end with brackets");
		String[] vals = s.substring(1, s.length() - 1).split(";");
		if (vals.length != 5)
			throw new IllegalArgumentException(
					"String must contain 5 elements separated by semicolon");
		return parseValues(vals);
	}

	private static Map<PedigreeMatrixRow, Integer> parseValues(String[] vals) {
		Map<PedigreeMatrixRow, Integer> map = new HashMap<>();
		PedigreeMatrixRow[] keys = { PedigreeMatrixRow.RELIABILITY,
				PedigreeMatrixRow.COMPLETENESS, PedigreeMatrixRow.TIME,
				PedigreeMatrixRow.GEOGRAPHY, PedigreeMatrixRow.TECHNOLOGY };
		for (int i = 0; i < keys.length; i++) {
			Integer intVal = parseValue(vals[i]);
			if (intVal != null) {
				map.put(keys[i], intVal);
			}
		}
		return map;
	}

	private static Integer parseValue(String string) {
		String val = string.trim();
		if (val.equals("n.a."))
			return null;
		boolean found = false;
		for (String allowed : new String[] { "1", "2", "3", "4", "5" }) {
			if (val.equals(allowed)) {
				found = true;
				break;
			}
		}
		if (!found)
			throw new IllegalArgumentException(string + " is not a valid entry");
		return Integer.parseInt(val);
	}

}
