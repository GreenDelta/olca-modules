package org.openlca.core.math;

import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NwSetDao;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.NwSetDescriptor;
import org.openlca.core.results.InventoryResult;
import org.openlca.core.results.ProjectResult;

public class ProjectCalculator {

	private final IMatrixSolver solver;
	private final MatrixCache matrixCache;

	public ProjectCalculator(MatrixCache matrixCache, IMatrixSolver solver) {
		this.matrixCache = matrixCache;
		this.solver = solver;
	}

	public ProjectResult solve(Project project) {
		ProjectResult result = new ProjectResult();
		SystemCalculator calculator = new SystemCalculator(matrixCache, solver);
		ImpactMethodDescriptor method = getImpactMethod(project);
		NwSetDescriptor nwSet = getNwSet(project);
		for (ProjectVariant v : project.getVariants()) {
			CalculationSetup setup = new CalculationSetup(v.getProductSystem(),
					CalculationSetup.QUICK_RESULT);
			setup.setUnit(v.getUnit());
			setup.setFlowPropertyFactor(v.getFlowPropertyFactor());
			setup.setAmount(v.getAmount());
			setup.setAllocationMethod(v.getAllocationMethod());
			setup.setImpactMethod(method);
			setup.setNwSet(nwSet);
			setup.getParameterRedefs().addAll(v.getParameterRedefs());
			InventoryResult inventoryResult = calculator.solve(setup);
			result.addResult(v, inventoryResult);
		}
		return result;
	}

	private ImpactMethodDescriptor getImpactMethod(Project project) {
		if (project.getImpactMethodId() == null)
			return null;
		ImpactMethodDao dao = new ImpactMethodDao(matrixCache.getDatabase());
		return dao.getDescriptor(project.getImpactMethodId());
	}

	private NwSetDescriptor getNwSet(Project project) {
		if (project.getNwSetId() == null)
			return null;
		NwSetDao dao = new NwSetDao(matrixCache.getDatabase());
		return dao.getDescriptor(project.getNwSetId());
	}
}
