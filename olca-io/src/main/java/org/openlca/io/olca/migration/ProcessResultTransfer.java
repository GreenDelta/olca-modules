package org.openlca.io.olca.migration;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.openlca.commons.Res;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Result;
import org.openlca.core.results.providers.FactorizationSolver;
import org.openlca.core.results.providers.SolverContext;
import org.openlca.io.olca.EntityTransfer;
import org.openlca.io.olca.TransferContext;
import org.openlca.util.Categories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Calculates the results of all processes in a database for a given impact
/// assessment method and transfers them to a given target database.
public class ProcessResultTransfer {

	private static final Logger log = LoggerFactory.getLogger(ProcessResultTransfer.class);

	private final IDatabase source;
	private final ImpactMethod method;
	private final TransferContext ctx;

	private MatrixSolver solver;

	public ProcessResultTransfer(
		ImpactMethod method, IDatabase source, IDatabase target
	) {
		this.source = source;
		this.method = method;
		this.ctx = TransferContext.create(source, target);
	}

	public ProcessResultTransfer withSolver(MatrixSolver solver) {
		this.solver = solver;
		return this;
	}

	public Res<Void> run() {
		try {
			var solver = this.solver != null
				? this.solver
				: MatrixSolver.get();

			log.info("Build matrix data");
			var techIdx = TechIndex.of(source);
			if (techIdx.isEmpty()) {
				return Res.ok();
			}

			var matrices = MatrixData.of(source, techIdx)
					.withImpacts(ImpactIndex.of(method))
					.build();
			var first = techIdx.at(0);
			matrices.demand = Demand.of(first, first.isWaste() ? -1 : 1);

			var indicators = new HashMap<String, ImpactCategory>();
			for (var i : method.impactCategories) {
				indicators.put(i.refId, i);
			}
			var locationCodes = new LocationDao(source).getCodes();

			@SuppressWarnings("unchecked")
			var entityTransfer = (EntityTransfer<Result>) ctx.getTransfer(ModelType.RESULT);
			var transferTask = new TransferTask(entityTransfer);
			var transferThread = new Thread(transferTask, "ProcessResultTransfer-thread");
			transferThread.start();

			var solverCtx = SolverContext.of(source, matrices).withSolver(solver);
			log.info("Calculate results with solver {}", solverCtx.solver());

			var results = FactorizationSolver.solve(solverCtx);
			for (int i = 0; i < techIdx.size(); i++) {
				if (transferTask.error != null) {
					break;
				}

				var techFlow = techIdx.at(i);
				var flow = source.get(Flow.class, techFlow.flowId());

				var name = techFlow.provider().name.split("\\|")[0].strip()
					+  " | " + flow.name.strip();
				var loc = locationCodes.get(techFlow.locationId());
				if (loc != null) {
					name += " - " + loc;
				}

				var r = Result.of(name, flow);
				r.impactMethod = method;
				r.category = resultCategoryOf(flow.category);

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
					r.impactResults.add(ImpactResult.of(indicator, value));
				}

				if (i > 0 && i % 1000 == 0) {
					log.info("Created {} results", i);
				}

				transferTask.put(r);
			}

			transferTask.stop();
			try {
				transferThread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return Res.error("Transfer interrupted", e);
			}

			if (transferTask.error != null) {
				return Res.error(transferTask.error);
			}

			return Res.ok();
		} catch (Exception e) {
			return Res.error("Failed to calculate and transfer database results", e);
		}
	}

	// TODO: this will create a category but we want to have it in-memory
	private Category resultCategoryOf(Category category) {
		if (category == null)
			return null;
		var path = Categories.path(category);
		return CategoryDao.sync(
			source, ModelType.RESULT, path.toArray(new String[0]));
	}

	private static class TransferTask implements Runnable {
		private final EntityTransfer<Result> transfer;
		private final BlockingQueue<Item> queue;
		private final CountDownLatch latch;
		private volatile String error;

		TransferTask(EntityTransfer<Result> transfer) {
			this.transfer = transfer;
			this.queue = new ArrayBlockingQueue<>(100);
			this.latch = new CountDownLatch(1);
		}

		void put(Result result) {
			try {
				queue.put(new Item(result));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				error = "Failed to add Result to transfer queue: " + e.getMessage();
			}
		}

		void stop() {
			try {
				queue.put(Item.stop());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		@Override
		public void run() {
			try {
				while (true) {
					if (error != null) {
						break;
					}
					var item = queue.take();
					if (item.isStop()) {
						break;
					}
					transfer.sync(item.result);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				error = "Database transfer failed: " + e.getMessage();
			} finally {
				latch.countDown();
			}
		}
	}

	private record Item(Result result) {
		static Item stop() {
			return new Item(null);
		}

		boolean isStop() {
			return result == null;
		}
	}
}
