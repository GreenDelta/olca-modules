package org.openlca.core.database.references;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

	static Search on(IDatabase database) {
		return new Search(database);
	}

	private Search(IDatabase database) {
		this.database = database;
	}

	List<Reference> findReferences(String table, String idField, Set<Long> ids,
			Ref[] refs, boolean includeOptional) {
		if (ids.isEmpty())
			return Collections.emptyList();
		List<Reference> references = new ArrayList<Reference>();
		String query = createQuery(table, idField, ids, refs);
		query(query, (result) -> {
			for (int i = 0; i < refs.length; i++) {
				if (refs[i].optional && !includeOptional)
					continue;
				long id = result.getLong(i + 1);
				if (id == 0l)
					continue;
				references.add(createReference(refs[i], id));
			}
		});
		return references;
	}

	private Reference createReference(Ref ref, long id) {
		return new Reference(ref.type, id, ref.optional);
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
		query.append("SELECT DISTINCT ");
		for (int i = 0; i < references.length; i++) {
			if (i != 0)
				query.append(",");
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

	final static class Ref {

		private Class<? extends AbstractEntity> type;
		private String field;
		private boolean optional;

		Ref(Class<? extends AbstractEntity> type, String field) {
			this(type, field, false);
		}

		Ref(Class<? extends AbstractEntity> type, String field, boolean optional) {
			this.type = type;
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
