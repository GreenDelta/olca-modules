package org.openlca.core.matrix.uncertainties;

import org.openlca.core.model.Copyable;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;

/**
 * Describes a matrix cell with a value that is possibly generated from one or
 * more uncertainty distributions.
 */
public interface UCell extends Copyable<UCell> {

	/**
	 * Generates the next value of the underlying distribution of this matrix cell.
	 * If formulas are linked to this cell the injected interpreter should be used
	 * to evaluate them as possibly values of dependent parameters with uncertainty
	 * distributions may changed.
	 */
	double next(FormulaInterpreter interpreter);

	/**
	 * Get the uncertainty distribution type of the cell;
	 */
	UncertaintyType type();

	/**
	 * Get the parameter values of the uncertainty distribution of this cell.
	 */
	double[] values();
}
