package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.Table;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

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

	List<CategorizedDescriptor> queryFor(ModelType type, Set<Long> toFind,
			String... inFields) {
		return queryFor(type, "id", tableNames.get(type), toFind, inFields);
	}

	List<CategorizedDescriptor> queryFor(ModelType type, String idField,
			String table, Set<Long> toFind, String... inFields) {
		Set<Long> ids = queryForIds(idField, table, toFind, inFields);
		return loadDescriptors(type, ids);
	}

	List<CategorizedDescriptor> queryFor(ModelType type, String query) {
		Set<Long> ids = queryForIds(query);
		return loadDescriptors(type, ids);
	}

	List<CategorizedDescriptor> loadDescriptors(ModelType type, Set<Long> ids) {
		if (ids.isEmpty())
			return Collections.emptyList();
		return new ArrayList<>(Daos.categorized(database, type).getDescriptors(ids));
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

	protected Set<Long> getIds(String table) {
		return queryForIds("SELECT id FROM " + table);
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

	static String asSqlList(Object[] values) {
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		for (int i = 0; i < values.length; i++) {
			if (i != 0)
				builder.append(",");
			String next = values[i].toString();
			builder.append("'" + next + "'");
		}
		builder.append(')');
		return builder.toString();
	}

}
