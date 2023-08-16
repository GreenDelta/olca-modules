package org.openlca.core.matrix;

import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

public class CalcImpactFactor {

	public long imactCategoryId;
	public long flowId;

	/**
	 * Indicates whether the impact direction of the flow of this factor is
	 * `input` or `output`; e.g. for an emission the direction is `output`
	 * while for a resource it is `input`. If it is `input`, the matrix value
	 * is set to a negative value (as in the corresponding intervention matrix).
	 */
	public boolean isInput;

	/**
	 * Indicates whether the absolute value of the factor should be taken before
	 * applying the impact direction sign. This is the case when the impact
	 * direction is defined on the category level.
	 */
	public boolean withAbs;

	public double conversionFactor;
	public double amount;
	public String formula;

	public UncertaintyType uncertaintyType;
	public double parameter1;
	public double parameter2;
	public double parameter3;

	/**
	 * Returns true when this LCIA factor has an uncertainty distribution
	 * assigned.
	 */
	public boolean hasUncertainty() {
		return uncertaintyType != null
				&& uncertaintyType != UncertaintyType.NONE;
	}

	public double matrixValue(FormulaInterpreter interpreter) {

		double a = amount;
		if (Strings.notEmpty(formula) && interpreter != null) {
			try {
				var scope = interpreter.getScopeOrGlobal(imactCategoryId);
				a = scope.eval(formula);
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(getClass());
				log.error("Formula evaluation failed for" +
						" LCIA factor with formula: " + formula, e);
			}
		}

		a *= conversionFactor;
		return applyDirectionSign(a);
	}

	public double applyDirectionSign(double value) {
		if (withAbs) {
			return isInput ? -Math.abs(value) : Math.abs(value);
		} else {
			return isInput ? -value : value;
		}
	}
}
