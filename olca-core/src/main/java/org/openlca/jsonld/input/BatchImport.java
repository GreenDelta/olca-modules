package org.openlca.jsonld.input;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;

class BatchImport<T extends RootEntity> {

	private final JsonImport imp;
	private final Class<T> clazz;
	private final ModelType type;
	private final int batchSize;
	private final Writer writer = new Writer();

	private final ArrayList<RootEntity> inserts = new ArrayList<>();
	private final ArrayList<RootEntity> updates = new ArrayList<>();

	BatchImport(JsonImport imp, Class<T> type, int batchSize) {
		this.imp = imp;
		this.type = imp.types.get(type);
		this.clazz = type;
		this.batchSize = batchSize;
	}

	static int batchSizeOf(ModelType type) {
		return switch (type) {
			case IMPACT_CATEGORY, PRODUCT_SYSTEM -> 1;
			case LOCATION, PROCESS, RESULT -> 100;
			default -> 1000;
		};
	}

	@SuppressWarnings("unchecked")
	void run() {
		for (var refId : imp.reader.getRefIds(type)) {
			var item = imp.fetch(clazz, refId);
			if (item.isVisited() || item.isError())
				continue;
			var reader = (EntityReader<T>)imp.readerFor(type);
			if (item.isNew()) {
				insert(reader.read(item.json()));
			} else {
				T model = item.entity();
				reader.update(model, item.json());
				update(model);
			}
		}
		if (inserts.size() > 0) {
			flushInserts();
		}
		if (updates.size() > 0) {
			flushUpdates();
		}
		writer.close();
	}

	private void insert(RootEntity entity) {
		inserts.add(entity);
		if (inserts.size() >= batchSize) {
			flushInserts();
		}
	}

	private void update(RootEntity entity) {
		updates.add(entity);
		if (updates.size() >= batchSize) {
			flushUpdates();
		}
	}

	private void flushInserts() {
		writer.insert(inserts);
		inserts.clear();
	}

	private void flushUpdates() {
		writer.update(updates);
		updates.clear();
	}

	private class Writer {

		private final ExecutorService exec = Executors.newFixedThreadPool(1);
		private final ArrayList<RootEntity> buffer = new ArrayList<>();
		private volatile Future<?> task;

		void insert(List<RootEntity> batch) {
			next(batch);
			task = exec.submit(() -> {
				imp.db().transaction(em -> buffer.forEach(em::persist));
				buffer.forEach(imp::visited);
				buffer.clear();
			});
		}

		void update(List<RootEntity> batch) {
			next(batch);
			task = exec.submit(() -> {
				imp.db().transaction(em -> buffer.replaceAll(em::merge));
				buffer.forEach(imp::visited);
				buffer.clear();
			});
		}

		private void next(List<RootEntity> batch) {
			flush();
			buffer.clear();
			buffer.addAll(batch);
		}

		private void flush() {
			if (task != null) {
				try {
					task.get();
					task = null;
				} catch (Exception e) {
					throw new RuntimeException("failed to wait for worker", e);
				}
			}
		}

		private void close() {
			flush();
			exec.shutdown();
		}
	}

}
