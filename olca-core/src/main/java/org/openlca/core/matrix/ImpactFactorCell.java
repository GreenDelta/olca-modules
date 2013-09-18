package org.openlca.core.matrix;

import org.openlca.core.math.NumberGenerator;
import org.openlca.core.model.UncertaintyType;

/**
 * The cell value is negative if the factor is related to an input flow.
 */
public class ImpactFactorCell {

	private final boolean inputFlow;
	private final CalcImpactFactor factor;
	private NumberGenerator generator;

	ImpactFactorCell(CalcImpactFactor factor, boolean inputFlow) {
		this.factor = factor;
		this.inputFlow = inputFlow;
	}

	public double getMatrixValue() {
		if (factor == null)
			return 0;
		double amount = factor.getAmount() * factor.getConversionFactor();
		return inputFlow ? -amount : amount;
	}

	public double getNextSimulationValue() {
		if (generator == null)
			generator = createGenerator();
		double amount = generator.next() * factor.getConversionFactor();
		return inputFlow ? -amount : amount;
	}

	private NumberGenerator createGenerator() {
		UncertaintyType type = factor.getUncertaintyType();
		if (type == null && type == UncertaintyType.NONE)
			return NumberGenerator.discrete(factor.getAmount()
					* factor.getConversionFactor());
		final CalcImpactFactor f = factor;
		switch (f.getUncertaintyType()) {
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
