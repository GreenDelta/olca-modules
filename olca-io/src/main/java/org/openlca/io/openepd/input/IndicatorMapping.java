package org.openlca.io.openepd.input;

import org.openlca.core.model.ImpactCategory;

public record IndicatorMapping(IndicatorKey key, ImpactCategory indicator) {

	static IndicatorMapping emptyOf(IndicatorKey key) {
		return new IndicatorMapping(key, null);
	}

	boolean isEmpty() {
		return indicator == null;
	}

	String code() {
		return key.code();
	}

	String unit() {
		return key.unit();
	}
}
