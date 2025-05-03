package org.openlca.io.olca;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.slf4j.LoggerFactory;

import gnu.trove.map.hash.TLongLongHashMap;
import jakarta.persistence.Table;

/// A map between the database internal IDs of the entities the source and
/// target database. When we clone an entity of the source database, it has
/// then references to entities of that source database which we need to replace
/// with corresponding entities of the target database. Thus, we need to store
/// the mapping between the source and target IDs.
class SeqMap {

	private final Map<ModelType, TLongLongHashMap> sqs;

	private SeqMap() {
		sqs = new EnumMap<>(ModelType.class);
	}

	static SeqMap create(IDatabase source, IDatabase target) {

		var log = LoggerFactory.getLogger(SeqMap.class);
		log.info("initialize the sequence map");
		var seqMap = new SeqMap();

		var sSql = NativeSql.on(source);
		var tSql = NativeSql.on(target);

		for (var type : ModelType.values()) {
			var clazz = type.getModelClass();
			if (clazz == null)
				continue;
			var table = clazz.getAnnotation(Table.class);
			if (table == null)
				continue;

			log.info("scan table {}", table.name());
			var existing = new HashMap<String, Long>();
			var q = "select id, ref_id from " + table.name();
			tSql.query(q, r -> {
				var refId = r.getString(2);
				if (refId == null)
					return true;
				existing.put(refId, r.getLong(1));
				return true;
			});

			log.info("found {} entities in target", existing.size());
			if (existing.isEmpty())
				continue;

			log.info("scan source table");
			var map = seqMap.sqs.computeIfAbsent(type, $ -> new TLongLongHashMap());
			sSql.query(q, r -> {
				var refId = r.getString(2);
				if (refId == null)
					return true;
				var targetId = existing.get(refId);
				if (targetId != null) {
					map.put(r.getLong(1), targetId);
				}
				return true;
			});

			log.info("mapped {} existing entities", map.size());
		}

		return seqMap;
	}


	public void put(ModelType type, long sourceId, long targetId) {
		sqs.computeIfAbsent(type, $ -> new TLongLongHashMap())
				.put(sourceId, targetId);
	}

	public long get(ModelType type, long sourceId) {
		var map = sqs.get(type);
		return map != null
				? map.get(sourceId)
				: 0;
	}

	/// Returns true if there is a mapping for the given source ID.
	public boolean isMapped(ModelType type, long sourceId) {
		return get(type, sourceId) != 0;
	}
}
