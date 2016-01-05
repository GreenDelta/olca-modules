package org.openlca.io.refdata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.Table;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Seq {

	private final ModelType[] TYPES = {
			ModelType.LOCATION,
			ModelType.CATEGORY,
			ModelType.UNIT,
			ModelType.UNIT_GROUP,
			ModelType.FLOW_PROPERTY,
			ModelType.FLOW,
			ModelType.CURRENCY,
			ModelType.IMPACT_CATEGORY,
			ModelType.IMPACT_METHOD,
			ModelType.NW_SET
	};

	private Logger log = LoggerFactory.getLogger(getClass());
	private IDatabase db;
	private final HashMap<String, Long>[] sequences;
	private final HashSet<String>[] inDatabase;
	private long seqCount = 0;

	@SuppressWarnings("unchecked")
	public Seq(IDatabase db) {
		this.db = db;
		sequences = new HashMap[ModelType.values().length];
		inDatabase = new HashSet[ModelType.values().length];
		for (ModelType type : TYPES)
			initType(type);
		initSeqCount();
	}

	private void initType(ModelType type) {
		Table table = type.getModelClass().getAnnotation(Table.class);
		String query = "select id, ref_id from " + table.name();
		HashMap<String, Long> seq = new HashMap<>();
		HashSet<String> inDb = new HashSet<>();
		try {
			NativeSql.on(db).query(query, result -> {
				String refId = result.getString(2);
				seq.put(refId, result.getLong(1));
				inDb.add(refId);
				return true;
			});
		} catch (Exception e) {
			log.error("failed to initialize sequence map for " + type, e);
		}
		sequences[type.ordinal()] = seq;
		inDatabase[type.ordinal()] = inDb;
	}

	private void initSeqCount() {
		AtomicLong seq = new AtomicLong(0L);
		String query = "select seq_count from sequence";
		try {
			NativeSql.on(db).query(query, result -> {
				seq.set(result.getLong(1));
				return true;
			});
		} catch (Exception e) {
			log.error("failed to get sequence count", e);
		}
		this.seqCount = seq.get() + 500L;
	}

	public boolean isInDatabase(ModelType type, String refId) {
		if (refId == null)
			return false;
		return inDatabase[type.ordinal()].contains(refId);
	}

	/**
	 * Get the allocated integer id for the given reference ID. If there is no
	 * such ID a new one is allocated.
	 */
	public long get(ModelType type, String refId) {
		if (refId == null)
			return next();
		HashMap<String, Long> map = sequences[type.ordinal()];
		Long i = map.get(refId);
		if (i != null)
			return i;
		seqCount++;
		map.put(refId, seqCount);
		return seqCount;
	}

	public long next() {
		return ++seqCount;
	}

	public void write() throws Exception {
		String sql = "UPDATE sequence SET SEQ_COUNT = " + next();
		NativeSql.on(db).batchUpdate(Arrays.asList(sql));
	}

}
