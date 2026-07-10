package org.openlca.io.olca.migration.results;

import org.openlca.core.model.Result;

record QueueItem(Result result) {
	static QueueItem stop() {
		return new QueueItem(null);
	}

	boolean isStop() {
		return result == null;
	}
}
