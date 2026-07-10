package org.openlca.io.olca.migration.results;

import java.util.HashMap;
import java.util.Map;

import org.openlca.commons.Res;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.Result;
import org.openlca.core.results.providers.FactorizationSolver;
import org.openlca.core.results.providers.SolverContext;
import org.openlca.io.olca.TransferContext;
import org.openlca.util.Categories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Calculates the results of all processes in a database for a given impact
/// assessment method and transfers them to a given target database.
public class ProcessResultTransfer {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase source;
	private final ImpactMethod method;
	private final TransferContext ctx;

	private final Map<String, ImpactCategory> indicators;
	private final Map<Long, String> locationCodes;

	private ProcessResultTransfer(
		ImpactMethod method, IDatabase source, IDatabase target
	) {
		this.source = source;
		this.method = method;
		this.ctx = TransferContext.create(source, target);
		locationCodes = new LocationDao(source).getCodes();
		indicators = new HashMap<>();
		for (var i : method.impactCategories) {
			indicators.put(i.refId, i);
		}
	}

	public Res<Void> run() {
		try {

			log.info("build matrix data");
			var matrices = buildMatrices();
			if (matrices == null)
				return Res.error("Database contains no process data");
			var techIdx = matrices.techIndex;

			log.info("initialize transfer");
			var transfer = new TransferTask(ctx);
			var transferThread = new Thread(transfer, "transfer-thread");
			transferThread.start();

			log.info("calculate results");
			var solverCtx = SolverContext.of(source, matrices);
			var results = FactorizationSolver.solve(solverCtx);

			log.info("start result transfer");
			for (int i = 0; i < techIdx.size(); i++) {
				if (transfer.hasError())
					break;

				var techFlow = techIdx.at(i);
				var item = prepareItemOf(techFlow);
				if (item == null)
					continue;

				var values = results.totalImpactsOfOne(i);
				for (int k = 0; k < values.length; k++) {
					var d = matrices.impactIndex.at(k);
					var indicator = indicators.get(d.refId);
					if (indicator == null) {
						continue;
					}
					var value = techFlow.isWaste()
						? -values[k]
						: values[k];
					item.add(ImpactResult.of(indicator, value));
				}

				if (i > 0 && i % 1000 == 0) {
					log.info("Created {} results", i);
				}

				transfer.put(item);
			}

			log.info("wait for transfer thread");
			return finalizeIt(transfer, transferThread);

		} catch (Exception e) {
			return Res.error("Failed to calculate and transfer results", e);
		}
	}

	private MatrixData buildMatrices() {
		var techIdx = TechIndex.of(source);
		if (techIdx.isEmpty())
			return null;
		var matrices = MatrixData.of(source, techIdx)
				.withImpacts(ImpactIndex.of(method))
				.build();
		var first = techIdx.at(0);
		matrices.demand = Demand.of(first, first.isWaste() ? -1 : 1);
		return matrices;
	}

	private QueueItem prepareItemOf(TechFlow tf) {
		var flow = source.get(Flow.class, tf.flowId());
		if (flow == null)
			return null;
		var name = tf.provider().name.split("\\|")[0].strip()
			+  " | " + flow.name.strip();
		var loc = locationCodes.get(tf.locationId());
		if (loc != null) {
			name += " - " + loc;
		}
		var r = Result.of(name, flow);
		r.impactMethod = method;
		return new QueueItem(r, Categories.path(flow.category));
	}

	private Res<Void> finalizeIt(TransferTask transfer, Thread thread) {
		transfer.stop();
		try {
			thread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return Res.error("Transfer interrupted", e);
		}
		return transfer.hasError()
			? Res.error(transfer.error())
			: Res.ok();
	}
}
