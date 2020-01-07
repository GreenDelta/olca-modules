package org.openlca.core.matrix.uncertainties;

import org.openlca.core.math.NumberGenerator;
import org.openlca.core.matrix.CalcImpactFactor;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;

/*
 * TODO: this cannot be used yet because we need an
 *  implementation of `matrixValue` for CalcImpactFactor
 * but for this it would be good to first make LCIA categories
 * stand-alone entities.
 */
class UImpactCell implements UCell {

	private final CalcImpactFactor factor;
	private final NumberGenerator gen;

	UImpactCell(CalcImpactFactor factor) {
		this.factor = factor;
		gen = factor.hasUncertainty()
				? generator(factor)
				: null;
	}

	@Override
	public double next(FormulaInterpreter interpreter) {
		if (gen != null) {
			factor.amount = gen.next();
		}
		return 0;
	}

	private static NumberGenerator generator(CalcImpactFactor e) {
		UncertaintyType t = e.uncertaintyType;
		if (t == null) {
			return NumberGenerator.discrete(e.amount);
		}
		switch (t) {
			case LOG_NORMAL:
				return NumberGenerator.logNormal(
						e.parameter1, e.parameter2);
			case NORMAL:
				return NumberGenerator.normal(
						e.parameter1, e.parameter2);
			case TRIANGLE:
				return NumberGenerator.triangular(
						e.parameter1, e.parameter2, e.parameter3);
			case UNIFORM:
				return NumberGenerator.uniform(
						e.parameter1, e.parameter2);
			default:
				return NumberGenerator.discrete(e.amount);
		}
	}
}
