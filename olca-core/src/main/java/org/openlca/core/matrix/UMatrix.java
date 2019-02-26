package org.openlca.core.matrix;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.math.NumberGenerator;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * An UMatrix is a matrix with uncertainty distributions.
 */
public class UMatrix {

	private final TIntObjectHashMap<TIntObjectHashMap<UCell>> data;

	public UMatrix() {
		data = new TIntObjectHashMap<>(
				Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR,
				-1);
	}

	/**
	 * Add the given Exchange to the uncertainty matrix. It is only added when
	 * it has an uncertainty distribution or formulas assigned. Thus, you should
	 * call this function for every exchange when building the inventory
	 * matrices as this function will decide whether an exchange should be
	 * added.
	 */
	public void add(int row, int col, CalcExchange e, double allocationFactor) {
		if (e == null)
			return;

		// decide whether we should add the exchange
		// NO! -> we could have the case where multiple exchanges are mapped to
		// the same matrix cell and not all exchanges have an uncertainty
		// distribution (or formula assigned)
		/*
		 * boolean considerCosts = withCosts && e.costFormula != null; if
		 * (!hasUncertainty && !considerCosts && e.amountFormula == null)
		 * return;
		 */

		// clear the formula when there is an uncertainty distribution
		// assigned -> we cannot have both
		if (e.hasUncertainty() && e.amountFormula != null) {
			e.amountFormula = null;
		}

		// select the cell
		TIntObjectHashMap<UCell> rowm = data.get(row);
		if (rowm == null) {
			rowm = new TIntObjectHashMap<>(
					Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR,
					-1);
			data.put(row, rowm);
		}
		UCell cell = rowm.get(col);

		if (cell == null) {
			cell = new UCell(e, allocationFactor);
			rowm.put(col, cell);
		} else {
			if (cell.overlay == null) {
				cell.overlay = new ArrayList<>(1);
			}
			cell.overlay.add(
					new UCell(e, allocationFactor));
		}
	}

	/**
	 * Generates new values and sets them to the given matrix.
	 */
	public void generate(IMatrix m, FormulaInterpreter interpreter) {
		TIntObjectIterator<TIntObjectHashMap<UCell>> rows = data.iterator();
		while (rows.hasNext()) {
			rows.advance();
			int row = rows.key();
			TIntObjectIterator<UCell> cols = rows.value().iterator();
			while (cols.hasNext()) {
				cols.advance();
				int col = cols.key();
				UCell cell = cols.value();
				m.set(row, col, cell.next(interpreter));
			}
		}
	}

	// A single cell in the matrix
	private static class UCell {

		final CalcExchange exchange;
		final double allocationFactor;

		// possible other distritbutions that are mapped to the same matrix
		// cell.
		List<UCell> overlay;

		/*
		 * TODO: we have to think about stateless functions here; also with a
		 * shared instance of Random; see also this issue:
		 * https://github.com/GreenDelta/olca-app/issues/62
		 */
		private final NumberGenerator gen;

		UCell(CalcExchange e, double allocationFactor) {
			this.exchange = e;
			this.allocationFactor = allocationFactor;
			gen = e.hasUncertainty()
				? generator(e)
				: null;
		}

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
}
