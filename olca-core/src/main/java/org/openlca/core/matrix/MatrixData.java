package org.openlca.core.matrix;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
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

		var system = setup.productSystem;
		var techIndex = system.withoutNetwork
				? TechIndex.unlinkedOf(system, db)
				: TechIndex.linkedOf(system, db);
		techIndex.setDemand(setup.getDemandValue());
		var interpreter = interpreter(db, setup, techIndex);

		var conf = MatrixConfig.of(db, techIndex)
				.withSetup(setup)
				.withInterpreter(interpreter)
				.withSubResults(subResults)
				.create();
		var data = new InventoryBuilder(conf).build();

		// add the LCIA matrix structures; note that in case
		// of a library system we may not have elementary
		// flows in the foreground system but still want to
		// attach an impact index to the matrix data.
		if (setup.impactMethod != null) {

			var impactIdx = new ImpactIndex();
			new ImpactMethodDao(db)
					.getCategoryDescriptors(setup.impactMethod.id)
					.forEach(impactIdx::put);

			if (impactIdx.isEmpty())
				return data;

			if (FlowIndex.isEmpty(data.flowIndex)) {
				data.impactIndex = impactIdx;
				return data;
			}

			new ImpactBuilder(db)
					.withUncertainties(conf.withUncertainties)
					.build(data.flowIndex, impactIdx, interpreter)
					.addTo(data);
		}

		return data;
	}

	// TODO: hide this
	@Deprecated
	public static FormulaInterpreter interpreter(
			IDatabase db, CalculationSetup setup, TechIndex techIndex) {
		// collect the process and LCIA category IDs; these
		// are the possible contexts of local parameters
		HashSet<Long> contexts = new HashSet<>();
		if (techIndex != null) {
			contexts.addAll(techIndex.getProcessIds());
		}
		if (setup.impactMethod != null) {
			ImpactMethodDao dao = new ImpactMethodDao(db);
			dao.getCategoryDescriptors(setup.impactMethod.id).forEach(
					d -> contexts.add(d.id));
		}
		return ParameterTable.interpreter(
				db, contexts, setup.parameterRedefs);
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
