package org.openlca.core.matrix.uncertainties;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.CalcImpactFactor;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.expressions.FormulaInterpreter;

import java.util.ArrayList;

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

	public void add(int row, int col, CalcImpactFactor i) {
		if (i == null)
			return;

		// clear the formula when there is an uncertainty distribution
		// assigned -> we cannot have both
		if (i.hasUncertainty() && i.formula != null) {
			i.formula = null;
		}

		TIntObjectHashMap<UCell> rowm = getRow(row);
		rowm.put(col, new UImpactCell(i));
	}

	/**
	 * Add the given exchange to the uncertainty matrix. This function should
	 * be called for every exchange of the corresponding inventory. Note that
	 * adding multiple exchanges to the same matrix cell is absolutely valid
	 * here (also with different uncertainties, allocation factors, etc.).
	 */
	public void add(int row, int col, CalcExchange e, double allocationFactor) {
		if (e == null)
			return;

		/*
		 * boolean considerCosts = withCosts && e.costFormula != null; if
		 * (!hasUncertainty && !considerCosts && e.amountFormula == null)
		 * return;
		 */

		// clear the formula when there is an uncertainty distribution
		// assigned -> we cannot have both
		if (e.hasUncertainty() && e.formula != null) {
			e.formula = null;
		}

		// select the cell
		TIntObjectHashMap<UCell> rowm = getRow(row);
		UCell cell = rowm.get(col);

		// no exchange at the given cell yet
		if (!(cell instanceof UExchangeCell)) {
			cell = new UExchangeCell(e, allocationFactor);
			rowm.put(col, cell);
			return;
		}

		// at least 2 exchanges that are mapped to the same cell
		UExchangeCell ecell = (UExchangeCell) cell;
		if (ecell.overlay == null) {
			ecell.overlay = new ArrayList<>(1);
		}
		ecell.overlay.add(new UExchangeCell(e, allocationFactor));
	}

	private TIntObjectHashMap<UCell> getRow(int row) {
		TIntObjectHashMap<UCell> rowm = data.get(row);
		if (rowm == null) {
			rowm = new TIntObjectHashMap<>(
					Constants.DEFAULT_CAPACITY,
					Constants.DEFAULT_LOAD_FACTOR,
					-1);
			data.put(row, rowm);
		}
		return rowm;
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

}
