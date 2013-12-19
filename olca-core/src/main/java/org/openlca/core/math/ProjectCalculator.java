package org.openlca.core.math;

import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.results.InventoryResult;
import org.openlca.core.results.ProjectResult;

public class ProjectCalculator {

	private final IMatrixFactory factory;
	private final MatrixCache matrixCache;

	public ProjectCalculator(MatrixCache matrixCache, IMatrixFactory factory) {
		this.matrixCache = matrixCache;
		this.factory = factory;
	}

	public ProjectResult solve(Project project) {
		ProjectResult result = new ProjectResult();
		SystemCalculator calculator = new SystemCalculator(matrixCache, factory);
		ImpactMethodDescriptor method = getImpactMethod(project);
		NormalizationWeightingSet nwSet = getNwSet(project);
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

	private NormalizationWeightingSet getNwSet(Project project) {
		if (project.getNwSetId() == null)
			return null;
		return matrixCache.getDatabase()
				.createDao(NormalizationWeightingSet.class)
				.getForId(project.getNwSetId());
	}
}
