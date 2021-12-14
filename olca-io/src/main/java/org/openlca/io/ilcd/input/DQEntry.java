package org.openlca.io.ilcd.input;

import org.openlca.ilcd.commons.DataQualityIndicator;
import org.openlca.ilcd.commons.Quality;
import org.openlca.ilcd.commons.QualityIndicator;
import org.openlca.ilcd.processes.Review;

class DQEntry {

	static String get(Review review) {
		if (review == null || review.indicators == null)
			return null;
		String[] entry = new String[7];
		for (DataQualityIndicator dqi : review.indicators) {
			if (dqi == null)
				continue;
			int pos = pos(dqi.name) - 1;
			if (pos < 0 || pos >= entry.length)
				continue;
			entry[pos] = val(dqi.value);
		}
		StringBuilder s = new StringBuilder("(");
		for (int i = 0; i < entry.length; i++) {
			String e = entry[i];
			if (e == null) {
				s.append("n.a.");
			} else {
				s.append(e);
			}
			if (i < (entry.length - 1)) {
				s.append(";");
			}
		}
		return s + ")";
	}

	private static int pos(QualityIndicator indicator) {
		if (indicator == null)
			return -1;
		return switch (indicator) {
			case TECHNOLOGICAL_REPRESENTATIVENESS -> 1;
			case TIME_REPRESENTATIVENESS -> 2;
			case GEOGRAPHICAL_REPRESENTATIVENESS -> 3;
			case COMPLETENESS -> 4;
			case PRECISION -> 5;
			case METHODOLOGICAL_APPROPRIATENESS_AND_CONSISTENCY -> 6;
			case OVERALL_QUALITY -> 7;
		};
	}

	private static String val(Quality v) {
		if (v == null)
			return "n.a.";
		return switch (v) {
			case VERY_GOOD -> "1";
			case GOOD -> "2";
			case FAIR -> "3";
			case POOR -> "4";
			case VERY_POOR -> "5";
			default -> "n.a.";
		};
	}
}
