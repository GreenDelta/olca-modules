package org.openlca.io.olca.migration.results;

import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Result;
import org.openlca.io.olca.TransferContext;

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

	void transfer(TransferContext ctx) {
		if (result == null)
			return;
		var r = ctx.resolve(result);

		// set the category of the result & update
		if (r == null || category == null || category.isEmpty())
			return;
		r.category = CategoryDao.sync(
			ctx.target(), ModelType.RESULT, category.toArray(String[]::new));
		if (r.category != null) {
			ctx.target().update(r);
		}
	}
}
