package org.openlca.core.math;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the results of a calculation setup. The same calculator can be
 * used for the calculation of different setups. The product system of the setup
 * may contain sub-systems. The calculator does not check if there are obvious
 * errors like sub-system cycles etc.
 */
public class SystemCalculator {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final MatrixCache mcache;
	private final IMatrixSolver solver;

	public SystemCalculator(MatrixCache cache, IMatrixSolver solver) {
		this.mcache = cache;
		this.solver = solver;
	}

	public SimpleResult calculateSimple(CalculationSetup setup) {
		log.trace("calculate product system - simple result");
		return calculator(setup).calculateSimple();
	}

	public ContributionResult calculateContributions(CalculationSetup setup) {
		log.trace("calculate product system - contribution result");
		return calculator(setup).calculateContributions();
	}

	public FullResult calculateFull(CalculationSetup setup) {
		log.trace("calculate product system - full result");
		return calculator(setup).calculateFull();
	}

	private LcaCalculator calculator(CalculationSetup setup) {
		Map<ProcessProduct, SimpleResult> subResults = calculateSubSystems(
				setup);
		MatrixData data = DataStructures.matrixData(
				setup, solver, mcache, subResults);
		return new LcaCalculator(solver, data);
	}

	/**
	 * Calculates (recursively) the sub-systems of the product system of the
	 * given setup. It returns an empty map when there are no subsystems.
	 */
	private Map<ProcessProduct, SimpleResult> calculateSubSystems(
			CalculationSetup setup) {
		if (setup == null || setup.productSystem == null)
			return Collections.emptyMap();

		// collect the sub-systems
		HashSet<ProcessProduct> subSystems = new HashSet<>();
		ProductSystemDao sysDao = new ProductSystemDao(mcache.getDatabase());
		FlowDao flowDao = new FlowDao(mcache.getDatabase());
		for (ProcessLink link : setup.productSystem.processLinks) {
			if (!link.isSystemLink)
				continue;
			ProductSystemDescriptor sys = sysDao.getDescriptor(link.providerId);
			FlowDescriptor flow = flowDao.getDescriptor(link.flowId);
			if (sys == null || flow == null) {
				log.error("could not load descriptors of system link {}", link);
				continue;
			}
			subSystems.add(ProcessProduct.of(sys, flow));
		}
		if (subSystems.isEmpty())
			return Collections.emptyMap();

		// calculate the LCI results of the sub-systems
		HashMap<ProcessProduct, SimpleResult> map = new HashMap<>();
		for (ProcessProduct pp : subSystems) {
			ProductSystem sys = sysDao.getForId(pp.id());
			if (sys == null)
				continue;
			CalculationSetup subSetup = new CalculationSetup(setup.type, sys);
			subSetup.parameterRedefs.addAll(sys.parameterRedefs);
			subSetup.withCosts = setup.withCosts;
			subSetup.allocationMethod = setup.allocationMethod;
			SimpleResult r = calculateSimple(subSetup);
			map.put(pp, r);
		}
		return map;
	}
}
