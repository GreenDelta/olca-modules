package org.openlca.validation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import gnu.trove.set.hash.TLongHashSet;
import jakarta.persistence.Table;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.util.Pair;

class IdSet {

	private final IDatabase db;
	private final EnumMap<ModelType, TLongHashSet> ids;
	private final TLongHashSet propertyFactors;

	private IdSet(IDatabase db) {
		this.db = db;
		ids = new EnumMap<>(ModelType.class);
		propertyFactors = new TLongHashSet();
	}

	public static IdSet of(IDatabase db) {
		var ids = new IdSet(db);
		ids.fill();
		return ids;
	}

	boolean containsOrZero(ModelType type, long id) {
		return id == 0 || contains(type, id);
	}

	boolean contains(ModelType type, long id) {
		if (type == null || id <= 0)
			return false;
		var modelIDs = ids.get(type);
		return modelIDs != null && modelIDs.contains(id);
	}

	public TLongHashSet flowPropertyFactors() {
		return propertyFactors;
	}

	TLongHashSet allOf(ModelType type) {
		if (type == null)
			return new TLongHashSet(0);
		var ids = this.ids.get(type);
		return ids == null
			? new TLongHashSet(0)
			: ids;
	}

	private void fill() {

		var service = Executors.newFixedThreadPool(8);

		// start the ID collectors
		var futures = new ArrayList<Future<Pair<ModelType, TLongHashSet>>>();
		for (var type : ModelType.values()) {
			if (type.getModelClass() == null)
				continue;
			futures.add(service.submit(() -> of(type)));
		}
		service.submit(() -> {
			var sql = "select id from tbl_flow_property_factors";
			NativeSql.on(db).query(sql, r -> {
				propertyFactors.add(r.getLong(1));
				return true;
			});
		});

		for (var future : futures) {
			try {
				var pair = future.get();
				this.ids.put(pair.first, pair.second);
			} catch (Exception e) {
				throw new RuntimeException("Failed to collect IDs", e);
			}
		}
		service.shutdown();
	}

	private Pair<ModelType, TLongHashSet> of(ModelType type) {
		var table = type.getModelClass().getAnnotation(Table.class);
		if (table == null)
			return Pair.of(type, null);
		var sql = "select id from " + table.name();
		var ids = new TLongHashSet();
		NativeSql.on(db).query(sql, r -> {
			ids.add(r.getLong(1));
			return true;
		});
		return Pair.of(type, ids);
	}

}
