package org.openlca.io.openepd.input;

import org.openlca.util.Strings;

public record IndicatorKey(String code, String unit)
	implements Comparable<IndicatorKey> {

	@Override
	public int compareTo(IndicatorKey other) {
		return other != null
			? Strings.compare(this.code, other.code)
			: 1;
	}
}
