package org.openlca.util;

import java.util.Objects;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;

import gnu.trove.set.TLongSet;
import jakarta.persistence.Table;

public class VersionUpdate {

	private final IDatabase db;
	private final String table;

	private VersionUpdate(IDatabase db, String table) {
		this.db = Objects.requireNonNull(db);
		this.table = Objects.requireNonNull(table);
	}

	static public VersionUpdate of(IDatabase db, String table) {
		return new VersionUpdate(db, table);
	}

	static public VersionUpdate of(IDatabase db, Class<? extends RootEntity> type) {
		var table = type.getAnnotation(Table.class);
		return new VersionUpdate(db, table.name());
	}

	public void run(long id) {
		exec(IdContainer.of(id));
	}

	public void run(Set<Long> ids) {
		exec(IdContainer.of(ids));
	}

	public void run(TLongSet ids) {
		exec(IdContainer.of(ids));
	}

	private void exec(IdContainer ids) {
		if (ids.isEmpty())
			return;
		var q = "select id, version, last_change from " + table;
		if (ids.value != null) {
			q += " where id = " + ids.value;
		}
		long date = System.currentTimeMillis();
		NativeSql.on(db).updateRows(q, r -> {
			long id = r.getLong(1);
			if (!ids.contains(id))
				return true;
			var v = new Version(r.getLong(2));
			v.incUpdate();
			r.updateLong(2, v.getValue());
			r.updateLong(3, date);
			r.updateRow();
			return true;
		});
		db.clearCache();
	}

	private record IdContainer(
			Long value, Set<Long> boxed, TLongSet unboxed
	) {

		static IdContainer of(long value) {
			return new IdContainer(value, null, null);
		}

		static IdContainer of(Set<Long> boxed) {
			return new IdContainer(null, boxed, null);
		}

		static IdContainer of(TLongSet unboxed) {
			return new IdContainer(null, null, unboxed);
		}

		boolean isEmpty() {
			return size() == 0;
		}

		int size() {
			if (value != null)
				return 1;
			if (boxed != null)
				return boxed.size();
			return unboxed != null ? unboxed.size() : 0;
		}

		boolean contains(long v) {
			if (value != null)
				return value == v;
			if (boxed != null)
				return boxed.contains(v);
			return unboxed != null && unboxed.contains(v);
		}

	}
}
