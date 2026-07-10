package org.openlca.io.olca.migration.results;

import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.Result;

@NullMarked
record QueueItem(
	@Nullable Result result,
	@Nullable List<String> category
) {

	static QueueItem stop() {
		return new QueueItem(null, null);
	}

	boolean isStop() {
		return result == null;
	}

	void add(ImpactResult value) {
		if (result == null)
			return;
		result.impactResults.add(value);
	}
}
