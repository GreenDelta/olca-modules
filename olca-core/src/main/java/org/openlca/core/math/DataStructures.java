package org.openlca.core.math;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ImpactBuilder;
import org.openlca.core.matrix.ImpactIndex;
import org.openlca.core.matrix.InventoryBuilder;
import org.openlca.core.matrix.InventoryConfig;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ParameterTable;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.results.SimpleResult;
import org.openlca.expressions.FormulaInterpreter;

/**
 * Provides helper methods for creating matrix-like data structures that can be
 * used in calculations (but also exports, validations, etc.).
 */
public class DataStructures {

	private DataStructures() {
	}

	/**
	 * Create the matrix data for the given calculation setup.
	 */
	public static MatrixData matrixData(IDatabase db, CalculationSetup setup) {
		return matrixData(db, setup, Collections.emptyMap());
	}

	/**
	 * Create the matrix data for the given calculation setup and sub-results.
	 * The sub-results will be integrated into the resulting matrices.
	 */
	public static MatrixData matrixData(
			IDatabase db,
			CalculationSetup setup,
			Map<ProcessProduct, SimpleResult> subResults) {

		var techIndex = TechIndex.linkedOf(setup.productSystem, db);
		techIndex.setDemand(setup.getDemandValue());
		var interpreter = interpreter(db, setup, techIndex);

		var conf = InventoryConfig.of(db, techIndex)
				.withSetup(setup)
				.withInterpreter(interpreter)
				.withSubResults(subResults)
				.create();
		var builder = new InventoryBuilder(conf);
		var data = builder.build();

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

			var impactBuilder = new ImpactBuilder(db);
			impactBuilder.withUncertainties(conf.withUncertainties);
			var impactData = impactBuilder.build(
					data.flowIndex, impactIdx, interpreter);
			data.impactMatrix = impactData.impactMatrix;
			data.impactIndex = impactIdx;
			data.impactUncertainties = impactData.impactUncertainties;
		}

		return data;
	}

	public static FormulaInterpreter interpreter(IDatabase db,
												 CalculationSetup setup, TechIndex techIndex) {
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
}
