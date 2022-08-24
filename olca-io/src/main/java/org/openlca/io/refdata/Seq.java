package org.openlca.io.refdata;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RefEntity;
import org.openlca.util.Strings;

import jakarta.persistence.Table;

class Seq {

	private final IDatabase db;
	private final HashMap<Class<? extends RefEntity>, HashMap<String, Long>> ids;

	private long seqCount = 0;

	Seq(IDatabase db) {
		this.db = db;
		ids = new HashMap<>();
		initSeqCount();
	}

	private HashMap<String, Long> idsOf(Class<? extends RefEntity> type) {
		var existing = ids.get(type);
		if (existing != null)
			return existing;

		var table = type.getAnnotation(Table.class);
		var query = "select id, ref_id from " + table.name();
		var map = new HashMap<String, Long>();
		NativeSql.on(db).query(query, result -> {
			var refId = result.getString(2);
			map.put(refId, result.getLong(1));
			return true;
		});
		ids.put(type, map);
		return map;
	}

	private void initSeqCount() {
		var seq = new AtomicLong(0L);
		var query = "select seq_count from sequence";
		NativeSql.on(db).query(query, result -> {
			seq.set(result.getLong(1));
			return true;
		});
		this.seqCount = seq.get() + 500L;
	}

	boolean contains(ModelType type, String refId) {
		return contains(type.getModelClass(), refId);
	}

	boolean contains(Class<? extends RefEntity> type, String id) {
		var map = idsOf(type);
		return map.containsKey(id);
	}

	/**
	 * Get the allocated integer id for the given reference ID. If there is no
	 * such ID a new one is allocated.
	 */
	long get(ModelType type, String refId) {
		return get(type.getModelClass(), refId);
	}

	long get(Class<? extends RefEntity> type, String refId) {
		if (Strings.nullOrEmpty(refId))
			return 0;
		var map = idsOf(type);
		var i = map.get(refId);
		if (i != null)
			return i;
		seqCount++;
		map.put(refId, seqCount);
		return seqCount;
	}


	long next() {
		return ++seqCount;
	}

	void write() throws Exception {
		String sql = "UPDATE sequence SET SEQ_COUNT = " + next();
		NativeSql.on(db).batchUpdate(Collections.singletonList(sql));
	}

}
