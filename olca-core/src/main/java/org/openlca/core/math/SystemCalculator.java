package org.openlca.core.math;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.SimpleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the results of a calculation setup or project. The same calculator
 * can be used for different setups. The product systems of the setups may
 * contain sub-systems. The calculator does not check if there are obvious
 * errors like sub-system cycles etc.
 */
public class SystemCalculator {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase db;

	public SystemCalculator(IDatabase db) {
		this.db = db;
	}

	public SimpleResult calculateSimple(CalculationSetup setup) {
		var subs = calculateSubSystems(setup);
		var data = MatrixData.of(db, setup, subs);
		return SimpleResult.of(db, data);
	}

	public ContributionResult calculateContributions(CalculationSetup setup) {
		var subs = calculateSubSystems(setup);
		var data = MatrixData.of(db, setup, subs);
		return ContributionResult.of(db, data);
	}

	public FullResult calculateFull(CalculationSetup setup) {
		var subs = calculateSubSystems(setup);
		var data = MatrixData.of(db, setup, subs);
		return FullResult.of(db, data);
	}

	/**
	 * Calculates (recursively) the sub-systems of the product system of the
	 * given setup. It returns an empty map when there are no subsystems.
	 */
	private Map<TechFlow, SimpleResult> calculateSubSystems(
		CalculationSetup setup) {
		if (setup == null || !setup.hasProductSystem())
			return Collections.emptyMap();

		// collect the sub-systems
		var subSystems = new HashSet<TechFlow>();
		var sysDao = new ProductSystemDao(db);
		var flowDao = new FlowDao(db);
		for (var link : setup.productSystem().processLinks) {
			if (!link.isSystemLink)
				continue;
			var sys = sysDao.getDescriptor(link.providerId);
			var flow = flowDao.getDescriptor(link.flowId);
			if (sys == null || flow == null) {
				log.error("could not load descriptors of system link {}", link);
				continue;
			}
			subSystems.add(TechFlow.of(sys, flow));
		}
		if (subSystems.isEmpty())
			return Collections.emptyMap();

		// calculate the LCI results of the sub-systems
		var subResults = new HashMap<TechFlow, SimpleResult>();
		for (var pp : subSystems) {
			var subSystem = sysDao.getForId(pp.processId());
			if (subSystem == null)
				continue;
			var subSetup = CalculationSetup.simple(subSystem)
				.withParameters(setup.parameters())
				.withCosts(setup.hasCosts())
				.withUncertainties(setup.hasUncertainties())
				.withRegionalization(setup.hasRegionalization())
				.withAllocation(setup.allocation());
			ParameterRedefs.addTo(subSetup, subSystem);
			var subResult = calculateSimple(subSetup);
			subResults.put(pp, subResult);
		}
		return subResults;
	}
}
