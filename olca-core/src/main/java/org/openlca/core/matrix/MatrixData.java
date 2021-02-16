package org.openlca.core.matrix;

import java.util.Collections;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.uncertainties.UMatrix;
import org.openlca.core.results.SimpleResult;
import org.openlca.expressions.FormulaInterpreter;

/**
 * Contains the matrices of that are input of a calculation.
 */
public class MatrixData {

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
	public FlowIndex flowIndex;

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
	public MatrixReader flowMatrix;

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

	/**
	 * Create the matrix data for the given calculation setup.
	 */
	public static MatrixData of(IDatabase db, CalculationSetup setup) {
		return of(db, setup, Collections.emptyMap());
	}

	/**
	 * Create the matrix data for the given calculation setup and sub-results. The
	 * sub-results will be integrated into the resulting matrices.
	 */
	public static MatrixData of(
		IDatabase db, CalculationSetup setup,
		Map<ProcessProduct, SimpleResult> subResults) {

		// create the tech-index
		var system = setup.productSystem;
		var techIndex = system.withoutNetwork
			? TechIndex.unlinkedOf(system, db)
			: TechIndex.linkedOf(system, db);
		techIndex.setDemand(setup.getDemandValue());

		return MatrixConfig.of(db, techIndex)
			.withSetup(setup)
			.withSubResults(subResults)
			.build();
	}

	public static MatrixConfig.Builder of(IDatabase db, TechIndex techIndex) {
		return MatrixConfig.of(db, techIndex);
	}

	/**
	 * Generates new random values and modifies the respective matrices. Note that
	 * the matrix instances may change so you need to be carefully with aliases.
	 */
	public void simulate(FormulaInterpreter interpreter) {
		if (techMatrix != null && techUncertainties != null) {
			var t = techMatrix.asMutable();
			techUncertainties.generate(t, interpreter);
			techMatrix = t;
		}
		if (flowMatrix != null && enviUncertainties != null) {
			var f = flowMatrix.asMutable();
			enviUncertainties.generate(f, interpreter);
			flowMatrix = f;
		}
		if (impactMatrix != null && impactUncertainties != null) {
			var c = impactMatrix.asMutable();
			impactUncertainties.generate(c, interpreter);
			impactMatrix = c;
		}
	}

	public boolean isSparse() {
		return techMatrix instanceof HashPointMatrix
					 || techMatrix instanceof CSCMatrix;
	}

	public boolean hasLibraryLinks() {
		if (_hasLibraryLinks != null)
			return _hasLibraryLinks;
		if (techIndex != null) {
			for (int i = 0; i < techIndex.size(); i++) {
				var product = techIndex.getProviderAt(i);
				if (product.isFromLibrary()) {
					_hasLibraryLinks = true;
					return true;
				}
			}
		}
		if (impactIndex != null) {
			for (int i = 0; i < impactIndex.size(); i++) {
				var impact = impactIndex.at(i);
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
		if (flowMatrix instanceof HashPointMatrix) {
			flowMatrix = CSCMatrix.of(flowMatrix);
		}
		if (impactMatrix instanceof HashPointMatrix) {
			impactMatrix = CSCMatrix.of(impactMatrix);
		}
	}
}
