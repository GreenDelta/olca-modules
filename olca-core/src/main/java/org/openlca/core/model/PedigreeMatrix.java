package org.openlca.core.model;

import java.util.HashMap;
import java.util.Map;

public class PedigreeMatrix {

	private PedigreeMatrix() {
	}

	/**
	 * Calculates the geometric standard deviation from the given pedigree
	 * matrix entries as described in the methodology report of ecoinvent 2
	 * (http://www.ecoinvent.org/fileadmin/documents/en/
	 * 01_OverviewAndMethodology.pdf, pp. 44).
	 */
	public static double getGeometricSD(
			Map<PedigreeMatrixRow, Integer> selection, double baseUncertainty) {
		double varSum = 0;
		for (PedigreeMatrixRow row : PedigreeMatrixRow.values()) {
			Integer selectedScore = selection.get(row);
			int score = selectedScore == null ? 5 : selectedScore.intValue();
			double factor = getFactor(row, score);
			varSum += Math.pow(Math.log(factor), 2);
		}
		varSum += Math.pow(Math.log(baseUncertainty), 2);
		return Math.sqrt(Math.exp(Math.sqrt(varSum)));
	}

	/**
	 * Returns the uncertainty factor of the given pedigree matrix cell. We take
	 * the same factors as in the methodology report of ecoinvent 2
	 * (http://www.ecoinvent.org/fileadmin/documents/en/
	 * 01_OverviewAndMethodology.pdf, pp. 46). For the cells that have no value
	 * we recalculated the value from the methodology report of ecoinvent 3
	 * (http://www.ecoinvent.org/fileadmin/documents/en/Data_Quality_Guidelines/
	 * 01_DataQualityGuideline_v3_Final.pdf, pp. 77):<br>
	 * 
	 * <code> f' = log(sqrt(f))^2 </code><br>
	 * 
	 * Where f is the uncertainty factor in ecoinvent 3 and f the uncertainty
	 * factor in ecoinvent 2.
	 * 
	 */
	public static double getFactor(PedigreeMatrixRow row, int score) {
		switch (row) {
		case RELIABILITY:
			switch (score) {
			case 1:
				return 1;
			case 2:
				return 1.05;
			case 3:
				return 1.1;
			case 4:
				return 1.2;
			case 5:
				return 1.5;
			}
		case COMPLETENESS:
			switch (score) {
			case 1:
				return 1;
			case 2:
				return 1.02;
			case 3:
				return 1.05;
			case 4:
				return 1.1;
			case 5:
				return 1.2;
			}
		case TIME:
			switch (score) {
			case 1:
				return 1;
			case 2:
				return 1.03;
			case 3:
				return 1.1;
			case 4:
				return 1.2;
			case 5:
				return 1.5;
			}
		case GEOGRAPHY:
			switch (score) {
			case 1:
				return 1d;
			case 2:
				return 1.01;
			case 3:
				return 1.02;
			case 4:
				return 1.05;
			case 5:
				return 1.1;
			}
		case TECHNOLOGY:
			switch (score) {
			case 1:
				return 1d;
			case 2:
				return 1.05;
			case 3:
				return 1.2;
			case 4:
				return 1.5;
			case 5:
				return 2.0;
			}
		}
		throw new IllegalArgumentException("Row = " + row + " and score = "
				+ score + " is not a valid input");
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
