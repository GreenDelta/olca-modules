package org.openlca.io.openepd.io;

import org.openlca.core.model.ImpactCategory;

import java.util.HashMap;
import java.util.Map;

/**
 * A mapping between an openEPD and openLCA indicator. It also contains values
 * of the respective EPD scopes / modules for that indicator and conversion
 * factor that needs to be applied in an import or export.
 */
public class IndicatorMapping {

	private final Map<String, Double> values = new HashMap<>();

	private ImpactCategory indicator;
	private Vocab.Indicator epdIndicator;
	private Vocab.UnitMatch unit;
	private double factor = 1.0;

	public ImpactCategory indicator() {
		return indicator;
	}

	public IndicatorMapping indicator(ImpactCategory indicator) {
		this.indicator = indicator;
		return this;
	}

	public Vocab.Indicator epdIndicator() {
		return epdIndicator;
	}

	public IndicatorMapping epdIndicator(Vocab.Indicator epdIndicator) {
		this.epdIndicator = epdIndicator;
		return this;
	}

	public Vocab.UnitMatch unit() {
		return unit;
	}

	public IndicatorMapping unit(Vocab.UnitMatch unitMatch) {
		this.unit = unitMatch;
		return this;
	}

	public Map<String, Double> values() {
		return values;
	}

	public double factor() {
		return factor;
	}

	public IndicatorMapping factor(double factor) {
		this.factor = factor;
		return this;
	}
}
