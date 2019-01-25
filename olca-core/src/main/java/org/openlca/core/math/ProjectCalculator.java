package org.openlca.core.math;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NwSetDao;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.NwSetDescriptor;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.ProjectResult;

public class ProjectCalculator {

	private final IMatrixSolver solver;
	private final MatrixCache matrixCache;

	public ProjectCalculator(MatrixCache matrixCache, IMatrixSolver solver) {
		this.matrixCache = matrixCache;
		this.solver = solver;
	}

	public ProjectResult solve(Project project, EntityCache cache) {
		ProjectResult result = new ProjectResult(cache);
		SystemCalculator calculator = new SystemCalculator(matrixCache, solver);
		ImpactMethodDescriptor method = getImpactMethod(project);
		NwSetDescriptor nwSet = getNwSet(project);
		for (ProjectVariant v : project.variants) {
			CalculationSetup setup = new CalculationSetup(
					CalculationType.CONTRIBUTION_ANALYSIS,
					v.productSystem);
			setup.setUnit(v.unit);
			setup.setFlowPropertyFactor(v.flowPropertyFactor);
			setup.setAmount(v.amount);
			setup.allocationMethod = v.allocationMethod;
			setup.impactMethod = method;
			setup.nwSet = nwSet;
			setup.parameterRedefs.addAll(v.parameterRedefs);
			setup.withCosts = true;
			ContributionResult cr = calculator.calculateContributions(setup);
			result.addResult(v, cr);
		}
		return result;
	}

	private ImpactMethodDescriptor getImpactMethod(Project project) {
		if (project.impactMethodId == null)
			return null;
		ImpactMethodDao dao = new ImpactMethodDao(matrixCache.getDatabase());
		return dao.getDescriptor(project.impactMethodId);
	}

	private NwSetDescriptor getNwSet(Project project) {
		if (project.nwSetId == null)
			return null;
		NwSetDao dao = new NwSetDao(matrixCache.getDatabase());
		return dao.getDescriptor(project.nwSetId);
	}
}
