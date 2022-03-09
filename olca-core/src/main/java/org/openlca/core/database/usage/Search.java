package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gnu.trove.set.TLongSet;
import jakarta.persistence.Table;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.util.TLongSets;

class Search {

	private final static Map<ModelType, String> tableNames = new HashMap<>();
	private final IDatabase database;

	static {
		for (ModelType type : ModelType.values()) {
			if (type == ModelType.UNKNOWN)
				continue;
			Class<?> mClass = type.getModelClass();
			Table table = mClass.getDeclaredAnnotation(Table.class);
			tableNames.put(type, table.name());
		}
	}

	static Search on(IDatabase database) {
		return new Search(database);
	}

	private Search(IDatabase database) {
		this.database = database;
	}

	List<RootDescriptor> queryFor(ModelType type, String query) {
		Set<Long> ids = queryForIds(query);
		return loadDescriptors(type, ids);
	}

	List<RootDescriptor> loadDescriptors(ModelType type, Set<Long> ids) {
		if (ids.isEmpty())
			return Collections.emptyList();
		return new ArrayList<>(Daos.root(database, type).getDescriptors(ids));
	}

	Set<Long> queryForIds(ModelType type, Set<Long> toFind, String... inFields) {
		return queryForIds("id", tableNames.get(type), toFind, inFields);
	}

	Set<Long> queryForIds(String idField, String table, Set<Long> toFind,
			String... inFields) {
		if (toFind.isEmpty())
			return Collections.emptySet();
		String query = createQuery(idField, table, toFind, inFields);
		return queryForIds(query);
	}

	Set<Long> queryForIds(String query) {
		Set<Long> ids = new HashSet<>();
		NativeSql.on(database).query(query, (result) -> {
			ids.add(result.getLong(1));
			return result.next();
		});
		return ids;
	}

	private String createQuery(String idField, String table, Set<Long> toFind,
			String... fields) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT ");
		query.append(idField);
		query.append(" FROM ");
		query.append(table);
		query.append(" WHERE ");
		String idList = asSqlList(toFind);
		for (int i = 0; i < fields.length; i++) {
			if (i != 0)
				query.append(" OR ");
			query.append(fields[i]);
			query.append(" IN ");
			query.append(idList);
		}
		return query.toString();
	}

	static String asSqlList(Set<Long> ids) {
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		Iterator<Long> it = ids.iterator();
		while (it.hasNext()) {
			long next = it.next();
			builder.append(next);
			if (it.hasNext())
				builder.append(',');
		}
		builder.append(')');
		return builder.toString();
	}

	/**
	 * Executes the given query, collects the IDs from the first field of the
	 * cursor, and returns the descriptors of the given type for these IDs.
	 */
	static <T extends RootEntity> Set<? extends RootDescriptor> collect(
		IDatabase db, String query, Class<T> type) {
		var ids = new HashSet<Long>();
		NativeSql.on(db).query(query, r -> {
			ids.add(r.getLong(1));
			return true;
		});
		return new HashSet<>(db.getDescriptors(type, ids));
	}

	static String eqIn(TLongSet ids) {
		return ids.size() == 1
			? " = " + TLongSets.first(ids)
			: " in (" + TLongSets.join(", ", ids) + ")";
	}
}
