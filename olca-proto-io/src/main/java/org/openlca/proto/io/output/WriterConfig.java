package org.openlca.proto.io.output;

import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.store.EntityStore;
import org.openlca.proto.ProtoRef;
import org.openlca.util.TLongSets;

import java.util.EnumMap;
import java.util.function.Consumer;

public class WriterConfig {

	private final EntityStore store;
	private final DependencyHandler deps;
	private final EnumMap<ModelType, TLongObjectHashMap<RootDescriptor>> descriptors;

	public WriterConfig(EntityStore store, DependencyHandler deps) {
		this.store = store;
		this.deps = deps;
		descriptors = new EnumMap<>(ModelType.class);
	}

	public static WriterConfig of(EntityStore store) {
		return new WriterConfig(store, null);
	}

	void dep(RefEntity e, Consumer<ProtoRef> ref) {
		if (e == null)
			return;
		if (deps != null && e instanceof RootEntity re) {
			deps.push(re);
		}
		ref.accept(Refs.refOf(e).build());
	}

	void dep(RootDescriptor d, Consumer<ProtoRef> ref) {
		if (d == null)
			return;
		if (deps != null) {
			deps.push(d);
		}
		ref.accept(Refs.refOf(d).build());
	}

	RootDescriptor getDescriptor(ModelType type, long id) {
		if (type == null)
			return null;
		var map = descriptors.computeIfAbsent(type, t -> {
			var descriptors = store.getDescriptors(type.getModelClass());
			var m = new TLongObjectHashMap<RootDescriptor>(descriptors.size());
			for (var d : descriptors) {
				m.put(d.id, d);
			}
			return m;
		});
		return map.get(id);
	}

	/**
	 * Maps the given exchange IDs to the respective process internal IDs.
	 */
	TLongIntHashMap mapExchangeIdsOf(TLongHashSet ids) {
		var map = new TLongIntHashMap(ids.size());
		if (store instanceof IDatabase db) {
			var sql = "select id, internal_id from tbl_exchanges";
			if (ids.size() < 500) {
				sql += " where id in (" + TLongSets.join(",", ids) + ")";
			}
			NativeSql.on(db).query(sql, r -> {
				long id = r.getLong(1);
				if (ids.contains(id)) {
					map.put(id, r.getInt(2));
				}
				return true;
			});
		} else {
			for (var process: store.getAll(Process.class)) {
				for (var exchange : process.exchanges) {
					if (ids.contains(exchange.id)) {
						map.put(exchange.id, exchange.internalId);
					}
				}
			}
		}
		return map;
	}

}
