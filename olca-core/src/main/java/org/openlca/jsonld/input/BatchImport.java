package org.openlca.jsonld.input;

import java.util.ArrayList;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;

class BatchImport<T extends RootEntity> {

	private final JsonImport imp;
	private final Class<T> clazz;
	private final ModelType type;
	private final int batchSize;

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
		imp.db().transaction(em -> inserts.forEach(em::persist));
		inserts.forEach(imp::visited);
		inserts.clear();
	}

	private void flushUpdates() {
		imp.db().transaction(em -> updates.replaceAll(em::merge));
		updates.forEach(imp::visited);
		updates.clear();
	}

}
