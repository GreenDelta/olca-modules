package org.openlca.io.openepd.input;

import org.openlca.core.model.ImpactCategory;

public record IndicatorMapping(IndicatorKey key, ImpactCategory indicator) {

	public static IndicatorMapping emptyOf(IndicatorKey key) {
		return new IndicatorMapping(key, null);
	}

	public boolean isEmpty() {
		return indicator == null;
	}

	public String code() {
		return key.code();
	}

	public String unit() {
		return key.unit();
	}
}
