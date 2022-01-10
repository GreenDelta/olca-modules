package org.openlca.util;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Location;
import org.openlca.core.model.ResultFlow;
import org.openlca.core.model.ResultImpact;
import org.openlca.core.model.ResultModel;
import org.openlca.core.model.ResultOrigin;
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
			this.setup = Objects.requireNonNull(setup);
			this.result = Objects.requireNonNull(result);
			// we use a shared entity manager for fetching flows and impacts
			this.em	 = db.newEntityManager();
		}

		ResultModel generate() {
			var m = new ResultModel();
			m.setup = setup;
			m.refId = UUID.randomUUID().toString();
			m.lastChange = System.currentTimeMillis();
			m.calculationTime = m.lastChange;

			// name and URN
			if (setup.hasProductSystem()) {
				m.name = setup.productSystem().name;
				m.urn = "openLCA:model:" + setup.productSystem().refId;
			} else {
				var process = setup.process();
				if (process != null) {
					m.name = process.name;
					m.urn = "openLCA:process:" +process.refId;
				} else {
					m.name = "-unknown-";
				}
			}

			var refFlow = referenceFlowOf(setup);
			if (refFlow != null) {
				m.inventory.add(refFlow);
				m.referenceFlow = refFlow;
			}

			try {

				// add inventory
				if (result.hasEnviFlows()) {
					result.enviIndex().each((_i, enviFlow) -> {
						var flow = flowOf(enviFlow);
						if (flow != null) {
							m.inventory.add(flow);
						}
					});
				}

				// add impacts
				if (result.hasImpacts()) {
					result.impactIndex().each((_i, indicator) -> {
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
			r.origin = ResultOrigin.CALCULATED;
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
			r.origin = ResultOrigin.CALCULATED;
			r.indicator = impact;
			r.amount = result.getTotalImpactResult(d);
			return r;
		}

		private ResultFlow referenceFlowOf(CalculationSetup setup) {
			if (setup == null)
				return null;
			var flow = setup.flow();
			var factor = setup.flowPropertyFactor();
			var unit = setup.unit();
			if (flow == null || factor == null || unit == null)
				return null;
			var r = new ResultFlow();
			r.amount = setup.amount();
			r.flow = flow;
			r.flowPropertyFactor = factor;
			r.unit = unit;
			r.origin = ResultOrigin.CALCULATED;
			r.isInput = flow.flowType == FlowType.WASTE_FLOW;
			var refProcess = setup.process();
			if (refProcess != null) {
				r.location = refProcess.location;
			}
			return r;
		}

	}
}
