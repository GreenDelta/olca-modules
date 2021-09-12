package org.openlca.util;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Location;
import org.openlca.core.model.ResultFlow;
import org.openlca.core.model.ResultImpact;
import org.openlca.core.model.ResultModel;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.results.SimpleResult;

public class ResultModels {

	private ResultModels() {
	}

	public static ResultModel createFrom(IDatabase db,
		CalculationSetup setup, SimpleResult result) {
		return new Generator(db, setup, result).generate();
	}

	private static class Generator {

		private final EntityManager em;
		private final CalculationSetup setup;
		private final SimpleResult result;

		Generator(IDatabase db, CalculationSetup setup, SimpleResult result) {
			this.em	 = db.newEntityManager();
			this.setup = Objects.requireNonNull(setup);
			this.result = Objects.requireNonNull(result);
		}

		ResultModel generate() {
			var m = new ResultModel();
			m.refId = UUID.randomUUID().toString();
			m.lastChange = new Date().getTime();
			var calcRef = setup.hasProductSystem()
				? setup.productSystem()
				: setup.process();
			m.name = calcRef != null
				? calcRef.name
				: "-unknown-";

			try {

				// add inventory
				if (result.hasEnviFlows()) {
					result.enviIndex().each((i, enviFlow) -> {
						var flow = flowOf(enviFlow);
						if (flow != null) {
							m.inventory.add(flow);
						}
					});
				}

				// add impacts
				if (result.hasImpacts()) {
					result.impactIndex().each((i, indicator) -> {
						var impact = impactOf(indicator);
						if (impact != null) {
							m.impacts.add(impact);
						}
					});
				}
			} finally {
				em.close();
			}
			return m;
		}

		private ResultFlow flowOf(EnviFlow enviFlow) {
			var flow = em.find(Flow.class, enviFlow.flow().id);
			if (flow == null)
				return null;
			var r = new ResultFlow();
			r.flow = flow;
			r.flowPropertyFactor = flow.getReferenceFactor();
			r.unit = flow.getReferenceUnit();
			r.amount = result.getTotalFlowResult(enviFlow);
			r.isInput = enviFlow.isInput();
			var loc = enviFlow.location();
			if (loc != null) {
				r.location = em.find(Location.class, loc.id);
			}
			return r;
		}

		private ResultImpact impactOf(ImpactDescriptor d) {
			var impact = em.find(ImpactCategory.class, d.id);
			if (impact == null)
				return null;
			var r = new ResultImpact();
			r.indicator = impact;
			r.amount = result.getTotalImpactResult(d);
			return r;
		}
	}
}
