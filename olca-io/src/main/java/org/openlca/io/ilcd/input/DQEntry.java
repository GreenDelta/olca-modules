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
		String s = "(";
		for (int i = 0; i < entry.length; i++) {
			String e = entry[i];
			if (e == null) {
				s += "n.a.";
			} else {
				s += e;
			}
			if (i < (entry.length - 1)) {
				s += ";";
			}
		}
		return s + ")";
	}

	private static int pos(QualityIndicator indicator) {
		if (indicator == null)
			return -1;
		switch (indicator) {
		case TECHNOLOGICAL_REPRESENTATIVENESS:
			return 1;
		case TIME_REPRESENTATIVENESS:
			return 2;
		case GEOGRAPHICAL_REPRESENTATIVENESS:
			return 3;
		case COMPLETENESS:
			return 4;
		case PRECISION:
			return 5;
		case METHODOLOGICAL_APPROPRIATENESS_AND_CONSISTENCY:
			return 6;
		case OVERALL_QUALITY:
			return 7;
		default:
			return -1;
		}
	}

	private static String val(Quality v) {
		if (v == null)
			return "n.a.";
		switch (v) {
		case VERY_GOOD:
			return "1";
		case GOOD:
			return "2";
		case FAIR:
			return "3";
		case POOR:
			return "4";
		case VERY_POOR:
			return "5";
		default:
			return "n.a.";
		}
	}
}
