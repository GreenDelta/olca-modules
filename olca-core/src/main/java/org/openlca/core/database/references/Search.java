package org.openlca.core.database.references;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.model.AbstractEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Search {

	private final static Logger log = LoggerFactory.getLogger(Search.class);
	private final IDatabase database;
	private final Class<? extends AbstractEntity> type;

	static Search on(IDatabase database, Class<? extends AbstractEntity> type) {
		return new Search(database, type);
	}

	private Search(IDatabase database, Class<? extends AbstractEntity> type) {
		this.database = database;
		this.type = type;
	}

	List<Reference> findReferences(String table, String idField, Set<Long> ids,
			Ref[] refs, boolean includeOptional) {
		return findReferences(table, idField, ids, null, refs, includeOptional);
	}

	List<Reference> findReferences(String table, String idField, Set<Long> ids,
			Map<Long, Long> idToOwnerId, Ref[] refs, boolean includeOptional) {
		if (ids.isEmpty())
			return Collections.emptyList();
		List<Reference> references = new ArrayList<Reference>();
		String query = createQuery(table, idField, ids, refs);
		query(query, (result) -> {
			long ownerId = result.getLong(1);
			long nestedOwnerId = 0;
			if (idToOwnerId != null) {
				nestedOwnerId = ownerId;
				ownerId = idToOwnerId.get(ownerId);
			}
			for (int i = 0; i < refs.length; i++) {
				if (refs[i].optional && !includeOptional)
					continue;
				long id = result.getLong(i + 2);
				references.add(createReference(refs[i], id, ownerId,
						nestedOwnerId));
			}
		});
		return references;
	}

	private Reference createReference(Ref ref, long id, long ownerId,
			long nestedOwnerId) {
		return new Reference(ref.property, ref.type, id, type, ownerId,
				ref.nestedProperty, ref.nestedType, nestedOwnerId, ref.optional);
	}

	void query(String query, Consumer<ResultSetWrapper> handler) {
		try {
			NativeSql.on(database).query(query, (resultSet) -> {
				handler.accept(new ResultSetWrapper(resultSet));
				return true;
			});
		} catch (SQLException e) {
			log.error("Error executing native query '" + query + "'");
		}
	}

	private String createQuery(String table, String idField, Set<Long> ids,
			Ref[] references) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT DISTINCT " + idField);
		for (int i = 0; i < references.length; i++) {
			query.append(", ");
			query.append(references[i].field);
		}
		query.append(" FROM " + table);
		query.append(" WHERE " + idField + " IN (" + asSqlList(ids.toArray())
				+ ")");
		return query.toString();
	}

	static String asSqlList(Object[] values) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (i != 0)
				builder.append(',');
			builder.append(values[i].toString());
		}
		return builder.toString();
	}

	static List<Reference> applyOwnerMaps(
			List<Reference> references,
			Map<Long, Class<? extends AbstractEntity>> ownerTypes,
			Map<Long, Long> ownerIds,
			Map<Class<? extends AbstractEntity>, Map<Class<? extends AbstractEntity>, String>> nestedProperties) {
		List<Reference> results = new ArrayList<>();
		for (Reference r : references) {
			Class<? extends AbstractEntity> ownerType = ownerTypes
					.get(r.ownerId);
			long ownerId = ownerIds.get(r.ownerId);
			String nestedProperty = getNestedProperty(r.getType(), ownerType,
					nestedProperties);
			results.add(new Reference(r.property, r.getType(), r.id, ownerType,
					ownerId, nestedProperty, r.getOwnerType(), r.ownerId,
					r.optional));
		}
		return results;
	}

	private static String getNestedProperty(
			Class<? extends AbstractEntity> type,
			Class<? extends AbstractEntity> ownerType,
			Map<Class<? extends AbstractEntity>, Map<Class<? extends AbstractEntity>, String>> nestedProperties) {
		String defaultValue = "unknown";
		Map<Class<? extends AbstractEntity>, String> map = nestedProperties
				.get(ownerType);
		if (map == null)
			return defaultValue;
		String value = map.get(type);
		if (value == null)
			return defaultValue;
		return value;
	}

	final static class Ref {

		final String property;
		final Class<? extends AbstractEntity> type;
		final String nestedProperty;
		final Class<? extends AbstractEntity> nestedType;
		final String field;
		final boolean optional;

		Ref(Class<? extends AbstractEntity> type, String property, String field) {
			this(type, property, null, null, field, false);
		}

		Ref(Class<? extends AbstractEntity> type, String property,
				String field, boolean optional) {
			this(type, property, null, null, field, optional);
		}

		Ref(Class<? extends AbstractEntity> type, String property,
				Class<? extends AbstractEntity> nestedType,
				String nestedProperty, String field) {
			this(type, property, nestedType, nestedProperty, field, false);
		}

		Ref(Class<? extends AbstractEntity> type, String property,
				Class<? extends AbstractEntity> nestedType,
				String nestedProperty, String field, boolean optional) {
			this.type = type;
			this.property = property;
			this.nestedType = nestedType;
			this.nestedProperty = nestedProperty;
			this.field = field;
			this.optional = optional;
		}

	}

	final static class ResultSetWrapper {

		private ResultSet set;

		private ResultSetWrapper(ResultSet set) {
			this.set = set;
		}

		long getLong(int column) {
			try {
				return set.getLong(column);
			} catch (SQLException e) {
				log.error("Error receiving a long from native sql result set",
						e);
				return 0l;
			}
		}

		String getString(int column) {
			try {
				String value = set.getString(column);
				if (value == null)
					return "";
				return value;
			} catch (SQLException e) {
				log.error(
						"Error receiving a string from native sql result set",
						e);
				return "";
			}
		}

		boolean getBoolean(int column) {
			try {
				return set.getBoolean(column);
			} catch (SQLException e) {
				log.error(
						"Error receiving a boolean from native sql result set",
						e);
				return false;
			}
		}

	}

}
