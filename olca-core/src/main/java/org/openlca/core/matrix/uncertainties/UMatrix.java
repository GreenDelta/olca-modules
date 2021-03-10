package org.openlca.core.matrix.uncertainties;

import java.util.ArrayList;

import org.openlca.core.matrix.CalcAllocationFactor;
import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.CalcImpactFactor;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.model.Copyable;
import org.openlca.expressions.FormulaInterpreter;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * An UMatrix is a matrix with uncertainty distributions.
 */
public class UMatrix implements Copyable<UMatrix> {

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

		var rowm = getRow(row);
		rowm.put(col, new UImpactCell(i));
	}

	public void add(int row, int col, CalcExchange e) {
		add(row, col, e, null);
	}

	/**
	 * Add the given exchange to the uncertainty matrix. This function should be
	 * called for every exchange of the corresponding inventory. Note that adding
	 * multiple exchanges to the same matrix cell is absolutely valid here (also
	 * with different uncertainties, allocation factors, etc.).
	 */
	public void add(int row, int col, CalcExchange e, CalcAllocationFactor af) {
		if (e == null)
			return;

		/*
		 * boolean considerCosts = withCosts && e.costFormula != null; if
		 * (!hasUncertainty && !considerCosts && e.amountFormula == null) return;
		 */

		// clear the formula when there is an uncertainty distribution
		// assigned -> we cannot have both
		if (e.hasUncertainty() && e.formula != null) {
			e.formula = null;
		}

		// select the cell
		var rowm = getRow(row);
		var cell = rowm.get(col);

		// no exchange at the given cell yet
		if (!(cell instanceof UExchangeCell)) {
			cell = new UExchangeCell(e, af);
			rowm.put(col, cell);
			return;
		}

		// at least 2 exchanges that are mapped to the same cell
		var ecell = (UExchangeCell) cell;
		if (ecell.overlay == null) {
			ecell.overlay = new ArrayList<>(1);
		}
		ecell.overlay.add(new UExchangeCell(e, af));
	}

	private TIntObjectHashMap<UCell> getRow(int row) {
		var rowm = data.get(row);
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
	 * Generates new values and sets them to the given matrix, thus, it modifies
	 * the given matrix.
	 */
	public void generate(Matrix m, FormulaInterpreter interpreter) {
		each((row, col, cell) -> m.set(row, col, cell.next(interpreter)));
	}

	/**
	 * Iterate over the cells of this matrix.
	 */
	public void each(EntryFunction fn) {
		var rows = data.iterator();
		while (rows.hasNext()) {
			rows.advance();
			int row = rows.key();
			var cols = rows.value().iterator();
			while (cols.hasNext()) {
				cols.advance();
				int col = cols.key();
				fn.accept(row, col, cols.value());
			}
		}
	}

	@Override
	public UMatrix copy() {
		var copy = new UMatrix();
		for(var rowIt = data.iterator(); rowIt.hasNext();) {
			rowIt.advance();
			var row = rowIt.key();
			var cells = rowIt.value();
			if (cells == null || cells.isEmpty())
				continue;
			var copyCells = new TIntObjectHashMap<UCell>(
				Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR,
				-1);
			copy.data.put(row, copyCells);
			for (var cellIt = cells.iterator(); cellIt.hasNext();) {
				cellIt.advance();
				var col = cellIt.key();
				var cell = cellIt.value();
				if (cell == null)
					continue;
				copyCells.put(col, cell.copy());
			}
		}
		return copy;
	}
}
