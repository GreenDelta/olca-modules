package org.openlca.core.matrix;

import org.openlca.core.math.NumberGenerator;
import org.openlca.core.model.UncertaintyType;

/**
 * The cell value is negative if the factor is related to an input flow.
 */
class ImpactFactorCell {

	private final boolean inputFlow;
	private final CalcImpactFactor factor;
	private NumberGenerator generator;

	ImpactFactorCell(CalcImpactFactor factor, boolean inputFlow) {
		this.factor = factor;
		this.inputFlow = inputFlow;
	}

	double getMatrixValue() {
		if (factor == null)
			return 0;
		double amount = factor.getAmount() * factor.getConversionFactor();
		return inputFlow ? -amount : amount;
	}

	double getNextSimulationValue() {
		UncertaintyType type = factor.getUncertaintyType();
		if (type == null || type == UncertaintyType.NONE)
			return getMatrixValue();
		if (generator == null)
			generator = createGenerator(type);
		double amount = generator.next() * factor.getConversionFactor();
		return inputFlow ? -amount : amount;
	}

	private NumberGenerator createGenerator(UncertaintyType type) {
		final CalcImpactFactor f = factor;
		switch (type) {
		case LOG_NORMAL:
			return NumberGenerator.logNormal(f.getParameter1(),
					f.getParameter2());
		case NORMAL:
			return NumberGenerator.normal(f.getParameter1(), f.getParameter2());
		case TRIANGLE:
			return NumberGenerator.triangular(f.getParameter1(),
					f.getParameter2(), f.getParameter3());
		case UNIFORM:
			return NumberGenerator
					.uniform(f.getParameter1(), f.getParameter2());
		default:
			return NumberGenerator.discrete(f.getAmount());
		}
	}

}
