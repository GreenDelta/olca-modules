package org.openlca.core.database.usage;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

record Query<T extends RootEntity>(
	IDatabase db, Class<T> type, String query)
	implements Callable<Set<? extends RootDescriptor>> {

	static <T extends RootEntity> Query<T> of(
		IDatabase db, Class<T> type, String query) {
		return new Query<>(db, type,query);
	}

	@Override
	public Set<? extends RootDescriptor> call() {
		var ids = new HashSet<Long>();
		NativeSql.on(db).query(query, r -> {
			ids.add(r.getLong(1));
			return true;
		});
		return new HashSet<>(db.getDescriptors(type, ids));
	}
}

record QueryPlan(IDatabase db, List<Query<?>> queries) {

	static QueryPlan of(IDatabase db) {
		return new QueryPlan(db, new ArrayList<>());
	}

	QueryPlan submit(Class<? extends RootEntity> type, String query) {
		queries.add(new Query<>(db, type, query));
		return this;
	}

	Set<? extends RootDescriptor> exec() {
		if (queries.isEmpty())
			return Collections.emptySet();
		if (queries.size() == 1)
			return queries.get(0).call();
		int threads = Math.min(queries.size(), 4);
		try {
			var pool = Executors.newFixedThreadPool(threads);
			var calls = new ArrayList<Future<Set<? extends RootDescriptor>>>();
			for (var q : queries) {
				calls.add(pool.submit(q));
			}
			pool.shutdown();
			var descriptors = new HashSet<RootDescriptor>();
			for (var call : calls) {
				descriptors.addAll(call.get());
			}
			return descriptors;
		}catch (Exception e) {
			throw new RuntimeException("failed to execute query plan", e);
		}
	}
}
