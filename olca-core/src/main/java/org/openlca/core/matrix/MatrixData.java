package org.openlca.core.matrix;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.Matrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.uncertainties.UMatrix;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.util.Copy;

/**
 * Contains the matrices of that are input of a calculation.
 */
public class MatrixData {

	/**
	 * A demand value. This field is required when results should be calculated
	 * from this data.
	 */
	public Demand demand;

	/**
	 * The matrix index of the product and waste flows of the technosphere (i.e. the
	 * row and column index of the technology matrix; the column index of the
	 * intervention matrix).
	 */
	public TechIndex techIndex;

	/**
	 * The matrix index of the environmental/elementary flows (i.e. the row index of
	 * the intervention matrix; the column index of the impact matrix).
	 */
	public EnviIndex enviIndex;

	/**
	 * The matrix index of the LCIA categories (i.e. the row index of the impact
	 * matrix).
	 */
	public ImpactIndex impactIndex;

	/**
	 * The technology matrix.
	 */
	public MatrixReader techMatrix;

	/**
	 * The intervention matrix.
	 */
	public MatrixReader enviMatrix;

	/**
	 * The matrix with the characterization factors: LCIA categories * elementary
	 * flows.
	 */
	public MatrixReader impactMatrix;

	/**
	 * A cost vector contains the unscaled net-costs for a set of process-products.
	 * Unscaled means that these net-costs are related to the (allocated) product
	 * amount in the respective process. The vector is then scaled with the
	 * respective scaling factors in the result calculation. This vector is only
	 * available when LCC calculation should be done.
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

	private Boolean _hasLibraryLinks;

	public static MatrixConfig.Builder of(IDatabase db, TechIndex techIndex) {
		return MatrixConfig.of(db, techIndex);
	}

	/**
	 * Generates new random values and modifies the respective matrices. Note that
	 * the matrix instances may change so you need to be carefully with aliases.
	 */
	public void simulate(FormulaInterpreter interpreter) {

		BiFunction<MatrixReader, UMatrix, Optional<Matrix>> next =
			(matrix, uncertainties) -> {
				if (matrix == null || uncertainties == null)
					return Optional.empty();
				var m = matrix.asMutable();
				uncertainties.generate(m, interpreter);
				return Optional.of(m);
			};

		next.apply(techMatrix, techUncertainties)
			.ifPresent(m -> techMatrix = m);
		next.apply(enviMatrix, enviUncertainties)
			.ifPresent(m -> enviMatrix = m);
		next.apply(impactMatrix, impactUncertainties)
			.ifPresent(m -> impactMatrix = m);
	}

	public boolean isSparse() {
		return techMatrix instanceof HashPointMatrix
			|| techMatrix instanceof CSCMatrix;
	}

	public boolean hasLibraryLinks() {
		if (_hasLibraryLinks != null)
			return _hasLibraryLinks;
		if (techIndex != null) {
			for (var product : techIndex) {
				if (product.isFromLibrary()) {
					_hasLibraryLinks = true;
					return true;
				}
			}
		}
		if (impactIndex != null) {
			for (var impact : impactIndex) {
				if (impact.isFromLibrary()) {
					_hasLibraryLinks = true;
					return true;
				}
			}
		}
		_hasLibraryLinks = false;
		return false;
	}

	public void compress() {
		if (techMatrix instanceof HashPointMatrix) {
			techMatrix = CSCMatrix.of(techMatrix);
		}
		if (enviMatrix instanceof HashPointMatrix) {
			enviMatrix = CSCMatrix.of(enviMatrix);
		}
		if (impactMatrix instanceof HashPointMatrix) {
			impactMatrix = CSCMatrix.of(impactMatrix);
		}
	}

	public MatrixData copy() {
		var copy = new MatrixData();
		copy.techIndex = Copy.of(techIndex);
		copy.enviIndex = Copy.of(enviIndex);
		copy.impactIndex = Copy.of(impactIndex);
		copy.techMatrix = Copy.of(techMatrix);
		copy.enviMatrix = Copy.of(enviMatrix);
		copy.impactMatrix = Copy.of(impactMatrix);
		if (costVector != null) {
			copy.costVector = Arrays.copyOf(costVector, costVector.length);
		}
		copy.techUncertainties = Copy.of(techUncertainties);
		copy.enviUncertainties = Copy.of(enviUncertainties);
		copy.impactUncertainties = Copy.of(impactUncertainties);
		copy._hasLibraryLinks = _hasLibraryLinks;
		return copy;
	}
}
