package org.openlca.core.matrix;

import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.uncertainties.UMatrix;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.expressions.FormulaInterpreter;

/**
 * Contains the matrices of that are input of a calculation.
 */
public class MatrixData {

	/**
	 * The matrix index of the product and waste flows of the technosphere (i.e.
	 * the row and column index of the technology matrix; the column index of
	 * the intervention matrix).
	 */
	public TechIndex techIndex;

	/**
	 * The matrix index of the environmental/elementary flows (i.e. the row
	 * index of the intervention matrix; the column index of the impact matrix).
	 */
	public FlowIndex flowIndex;

	/**
	 * The matrix index of the LCIA categories (i.e. the row index of the impact
	 * matrix).
	 */
	public DIndex<ImpactCategoryDescriptor> impactIndex;

	/**
	 * The technology matrix.
	 */
	public IMatrix techMatrix;

	/**
	 * The intervention matrix.
	 */
	public IMatrix enviMatrix;

	/**
	 * The matrix with the characterization factors: LCIA categories *
	 * elementary flows.
	 */
	public IMatrix impactMatrix;

	/**
	 * A cost vector contains the unscaled net-costs for a set of
	 * process-products. Unscaled means that these net-costs are related to the
	 * (allocated) product amount in the respective process. The vector is then
	 * scaled with the respective scaling factors in the result calculation.
	 * This vector is only available when LCC calculation should be done.
	 */
	public double[] costVector;

	/**
	 * Contains the uncertainty distributions of the entries in the technology
	 * matrix. This field is only used (not null) for uncertainty calculations.
	 */
	public UMatrix techUncertainties;

	/**
	 * Contains the uncertainty distributions of the entries in the intervention
	 * matrix. This field is only used (not null) for uncertainty calculations.
	 */
	public UMatrix enviUncertainties;

	/**
	 * Contains the uncertainty distributions of the entries in the matrix with LCIA
	 * characterization factors. This field is only used (not null) for uncertainty
	 * calculations.
	 */
	public UMatrix impactUncertainties;

	public void simulate(FormulaInterpreter interpreter) {
		if (techMatrix != null && techUncertainties != null) {
			techUncertainties.generate(techMatrix, interpreter);
		}
		if (enviMatrix != null && enviUncertainties != null) {
			enviUncertainties.generate(enviMatrix, interpreter);
		}
		if (impactMatrix != null && impactUncertainties != null) {
			impactUncertainties.generate(impactMatrix, interpreter);
		}
	}

	public boolean isSparse() {
		return techMatrix instanceof HashPointMatrix
			|| techMatrix instanceof CSCMatrix;
	}
}
