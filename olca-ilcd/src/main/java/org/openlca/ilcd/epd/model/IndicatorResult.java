package org.openlca.ilcd.epd.model;

import java.util.ArrayList;
import java.util.List;

public class IndicatorResult {

	public Indicator indicator;
	public final List<Amount> amounts = new ArrayList<>();

	@Override
	public IndicatorResult clone() {
		IndicatorResult clone = new IndicatorResult();
		clone.indicator = indicator;
		for (Amount a : amounts) {
			clone.amounts.add(a.clone());
		}
		return clone;
	}
}
