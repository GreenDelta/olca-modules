package org.openlca.core.matrix;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.expressions.FormulaInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ImpactTable is the corresponding data type to the Inventory type but
 * wraps a matrix of impact assessment factors.
 */
public class ImpactTable {

	public DIndex<ImpactCategoryDescriptor> impactIndex;
	public FlowIndex flowIndex;
	public ImpactFactorMatrix factorMatrix;

	public static ImpactTable build(MatrixCache cache, long impactMethodId,
			FlowIndex flowIndex) {
		return new ImpactTableBuilder(cache, impactMethodId, flowIndex).build();
	}

	public boolean isEmpty() {
		return impactIndex == null || impactIndex.isEmpty()
				|| flowIndex == null || flowIndex.isEmpty()
				|| factorMatrix == null || factorMatrix.isEmpty();
	}

	public IMatrix createMatrix(IMatrixSolver solver) {
		return createMatrix(solver, null);
	}

	public IMatrix createMatrix(IMatrixSolver solver,
			FormulaInterpreter interpreter) {
		if (factorMatrix == null)
			return null;
		evalFormulas(interpreter);
		return (IMatrix) factorMatrix.createRealMatrix(solver);
	}

	/**
	 * Re-evaluates the parameters and formulas in the impact factor table
	 * (because they may changed), generates new values for the entries that
	 * have an uncertainty distribution and set these values to the entries of
	 * the given matrix. The given matrix and this impact table have to match
	 * exactly in size (so normally you first call createMatrix and than
	 * simulate).
	 */
	public void simulate(IMatrix matrix, FormulaInterpreter interpreter) {
		if (matrix == null)
			return;
		evalFormulas(interpreter);
		if (factorMatrix != null) {
			factorMatrix.simulate(matrix);
		}
	}

	private void evalFormulas(FormulaInterpreter interpreter) {
		if (interpreter == null || factorMatrix == null)
			return;
		factorMatrix.eval(interpreter);
	}

	private static class ImpactTableBuilder {

		private Logger log = LoggerFactory.getLogger(getClass());

		private final MatrixCache cache;
		private final long methodId;
		private final FlowIndex flowIndex;

		ImpactTableBuilder(MatrixCache cache, long impactMethodId,
				FlowIndex flowIndex) {
			this.cache = cache;
			this.methodId = impactMethodId;
			this.flowIndex = flowIndex;
		}

		ImpactTable build() {
			log.trace("Build impact factor matrix for method {}", methodId);
			DIndex<ImpactCategoryDescriptor> impacts = new DIndex<>();
			ImpactCategoryDao dao = new ImpactCategoryDao(cache.getDatabase());
			impacts.putAll(dao.getMethodImpacts(methodId));
			if (impacts.isEmpty() || flowIndex.isEmpty())
				return null;
			ImpactTable table = new ImpactTable();
			table.impactIndex = impacts;
			table.flowIndex = flowIndex;
			ImpactFactorMatrix matrix = new ImpactFactorMatrix(
					impacts.size(), flowIndex.size());
			table.factorMatrix = matrix;
			fill(matrix, impacts);
			log.trace("Impact factor matrix ready");
			return table;
		}

		private void fill(
				ImpactFactorMatrix matrix,
				DIndex<ImpactCategoryDescriptor> impacts) {
			Map<Long, List<CalcImpactFactor>> factorMap = loadFactors(impacts);
			for (int row = 0; row < impacts.size(); row++) {
				long impactID = impacts.idAt(row);
				List<CalcImpactFactor> factors = factorMap.get(impactID);
				if (factors == null)
					continue;
				for (CalcImpactFactor factor : factors) {
					long flowId = factor.flowId;
					int col = flowIndex.of(flowId);
					if (col < 0)
						continue;
					boolean input = flowIndex.isInput(flowId);
					ImpactFactorCell cell = new ImpactFactorCell(factor,
							methodId,
							input);
					matrix.setEntry(row, col, cell);
				}
			}
		}

		private Map<Long, List<CalcImpactFactor>> loadFactors(
				DIndex<ImpactCategoryDescriptor> impacts) {
			try {
				Set<Long> keys = LongStream.of(impacts.ids())
						.boxed()
						.collect(Collectors.toSet());
				return cache.getImpactCache().getAll(keys);
			} catch (Exception e) {
				log.error("failed to load impact factors");
				return Collections.emptyMap();
			}
		}
	}
}
