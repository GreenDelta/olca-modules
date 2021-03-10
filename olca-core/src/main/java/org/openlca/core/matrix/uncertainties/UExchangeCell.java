package org.openlca.core.matrix.uncertainties;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.math.NumberGenerator;
import org.openlca.core.matrix.CalcAllocationFactor;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;

class UExchangeCell implements UCell {

	private final CalcExchange exchange;
	private final CalcAllocationFactor allocationFactor;

	// possible other distributions that are mapped to the same matrix
	// cell.
	List<UCell> overlay;

	/*
	 * TODO: we have to think about stateless functions here; also with a shared
	 * instance of Random; see also this issue:
	 * https://github.com/GreenDelta/olca-app/issues/62
	 */
	private final NumberGenerator gen;

	UExchangeCell(CalcExchange e, CalcAllocationFactor f) {
		this.exchange = e;
		this.allocationFactor = f;
		gen = e.hasUncertainty()
			? generator(e)
			: null;
	}

	@Override
	public double next(FormulaInterpreter interpreter) {
		if (gen != null) {
			exchange.amount = gen.next();
		}
		double af = allocationFactor != null
			? allocationFactor.force(interpreter)
			: 1;
		double amount = exchange.matrixValue(interpreter, af);
		if (overlay != null) {
			for (UCell u : overlay) {
				amount += u.next(interpreter);
			}
		}
		return amount;
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

	@Override
	public UncertaintyType type() {
		return exchange.uncertaintyType == null
			? UncertaintyType.NONE
			: exchange.uncertaintyType;
	}

	@Override
	public double[] values() {
		if (exchange.uncertaintyType == null
				|| exchange.uncertaintyType == UncertaintyType.NONE)
			return new double[0];
		if (exchange.uncertaintyType == UncertaintyType.TRIANGLE)
			return new double[]{
				exchange.parameter1,
				exchange.parameter2,
				exchange.parameter3};
		return new double[] {
			exchange.parameter1,
			exchange.parameter2,
		};
	}

	@Override
	public UExchangeCell copy() {
		var copy = new UExchangeCell(exchange, allocationFactor);
		if (overlay != null) {
			copy.overlay = new ArrayList<>(overlay.size());
			for (var o : overlay) {
				copy.overlay.add(o.copy());
			}
		}
		return copy;
	}
}
