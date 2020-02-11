package org.openlca.core.matrix.uncertainties;

import java.util.List;

import org.openlca.core.math.NumberGenerator;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;

class UExchangeCell implements UCell {
	
	private final CalcExchange exchange;
	private final double allocationFactor;

	// possible other distributions that are mapped to the same matrix
	// cell.
	List<UCell> overlay;

	/*
	 * TODO: we have to think about stateless functions here; also with a
	 * shared instance of Random; see also this issue:
	 * https://github.com/GreenDelta/olca-app/issues/62
	 */
	private final NumberGenerator gen;

	UExchangeCell(CalcExchange e, double allocationFactor) {
		this.exchange = e;
		this.allocationFactor = allocationFactor;
		gen = e.hasUncertainty()
				? generator(e)
				: null;
	}

	@Override
	public double next(FormulaInterpreter interpreter) {
		if (gen != null) {
			exchange.amount = gen.next();
		}
		double a = exchange.matrixValue(
				interpreter, allocationFactor);
		if (overlay != null) {
			for (UCell u : overlay) {
				a += u.next(interpreter);
			}
		}
		return a;
	}

	private static NumberGenerator generator(CalcExchange e) {
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
