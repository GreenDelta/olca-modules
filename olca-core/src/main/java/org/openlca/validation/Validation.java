package org.openlca.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;

public class Validation implements Runnable {

	final IDatabase db;
	final IdSet ids;

	private final List<Item> items = new ArrayList<>();
	private int maxIssues = -1;
	private boolean skipWarnings = false;
	private boolean skipInfos = false;

	private final BlockingQueue<Item> queue = new ArrayBlockingQueue<>(100);
	private final Item FINISH = Item.ok("_finished");
	private volatile boolean _canceled = false;
	private volatile boolean _finished = false;
	private final AtomicInteger _totalWorkers = new AtomicInteger(0);
	private final AtomicInteger _finishedWorkers = new AtomicInteger(0);

	private Validation(IDatabase db) {
		this.db = db;
		this.ids = IdSet.of(db);
	}

	public static Validation on(IDatabase db) {
		return new Validation(db);
	}

	/**
	 * Set the maximum number of validation items that should be recorded. Setting
	 * a value {@code <= 0} means that there is no limit for the number of items
	 * (the default case).
	 *
	 * @param c the maximum number of validation items that should be recorded
	 * @return this validation object
	 */
	public Validation maxItems(int c) {
		this.maxIssues = c;
		return this;
	}

	public Validation skipWarnings(boolean b) {
		this.skipWarnings = b;
		return this;
	}

	public Validation skipInfos(boolean b) {
		this.skipInfos = b;
		return this;
	}

	/**
	 * Cancels a running validation. It can take a bit until the separate
	 * validation workers stop after the cancel signal was sent.
	 */
	public void cancel() {
		put(Item.ok("validation cancelled"));
		_canceled = true;
	}

	/**
	 * Returns {@code true} when the validation was canceled, externally or
	 * internally (e.g. when the maximum number of validation items was reached).
	 */
	public boolean wasCanceled() {
		return _canceled;
	}

	public boolean hasFinished() {
		return _finished;
	}

	/**
	 * Get the total number of workers of this validation where each worker checks
	 * one or more validation aspects.
	 */
	public int workerCount() {
		return _totalWorkers.get();
	}

	/**
	 * Get the number of workers that finished their checks.
	 */
	public int finishedWorkerCount() {
		return _finishedWorkers.get();
	}

	public List<Item> items() {
		return Collections.unmodifiableList(items);
	}

	@Override
	public void run() {

		// reset the validation state
		_finished = false;
		_canceled = false;
		_finishedWorkers.set(0);
		long start = System.currentTimeMillis();

		// create and start the worker threads
		var workers = new Runnable[]{
			new RootFieldCheck(this),
			new UnitCheck(this),
			new FlowPropertyCheck(this),
			new FlowCheck(this),
			new CurrencyCheck(this),
			new SocialIndicatorCheck(this),
			new DQSystemCheck(this),
			new ProcessCheck(this),
			new ImpactCategoryCheck(this),
			new ImpactMethodCheck(this),
			new ProjectCheck(this),
			new FormulaCheck(this),
			new FlowDirectionCheck(this),
			new AllocationCheck(this),
		};
		_totalWorkers.set(workers.length);
		int activeWorkers = 0;
		var threads = Executors.newFixedThreadPool(8);
		for (var worker : workers) {
			activeWorkers++;
			threads.execute(worker);
		}

		// process the validation items. each worker must send a finish marker
		// otherwise this loop blocks forever.
		while (activeWorkers > 0) {
			try {
				var item = queue.take();
				if (item == FINISH) {
					activeWorkers--;
					_finishedWorkers.incrementAndGet();
					continue;
				}
				if (_canceled
					|| (skipWarnings && item.isWarning())
					|| (skipInfos && item.isOk())) {
					continue;
				}
				items.add(item);

				// auto-cancel the validation when it exceeds the max. number of max.
				// issues
				if (maxIssues > 0 && items.size() >= maxIssues) {
					_canceled = true;
				}

			} catch (Exception e) {
				throw new RuntimeException("failed to get item from validation queue", e);
			}
		}
		threads.shutdown();

		// add the validation time
		if (!skipInfos && !wasCanceled()) {
			var time = (System.currentTimeMillis() - start) / 1000.0;
			var unit = "seconds";
			if (time > 60) {
				time /= 60.0;
				unit = "minutes";
			}
			items.add(Item.ok(
				String.format("Validated database in %.2f %s", time, unit)));
		}

		_finished = true;
	}


	/**
	 * This method must be called by each validation check, otherwise the
	 * validation will wait forever for the check to finish.
	 */
	void workerFinished() {
		put(FINISH);
	}

	void ok(String message) {
		put(Item.ok(message));
	}

	void error(String message, Throwable e) {
		put(Item.error(message, e));
	}

	void error(long id, ModelType type, String message) {
		try {
			var dao = Daos.refDao(db, type);
			var d = dao.getDescriptor(id);
			put(Item.error(d, message));
		} catch (Exception e) {
			error("failed to get descriptor " + type + "@" + id, e);
		}
	}

	void warning(long id, ModelType type, String message) {
		try {
			var dao = Daos.refDao(db, type);
			var d = dao.getDescriptor(id);
			put(Item.warning(d, message));
		} catch (Exception e) {
			error("failed to get descriptor " + type + "@" + id, e);
		}
	}

	void warning(String message) {
		put(Item.warning(message));
	}

	private void put(Item item) {
		if (item == null)
			return;
		try {
			queue.put(item);
		} catch (InterruptedException e) {
			throw new RuntimeException("failed to add item to validation queue", e);
		}
	}
}
