package org.openlca.core.database.references;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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

	final static Logger log = LoggerFactory.getLogger(Search.class);
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
			return new ArrayList<>();
		List<Reference> references = new ArrayList<Reference>();
		List<String> queries = createQueries(table, idField, ids, refs);
		for (String query : queries) {
			query(query, result -> {
				long ownerId = result.getLong(1);
				long nestedOwnerId = 0;
				if (idToOwnerId != null) {
					nestedOwnerId = ownerId;
					ownerId = idToOwnerId.get(ownerId);
				}
				for (int i = 0; i < refs.length; i++) {
					Ref ref = refs[i];
					if (ref.optional && !includeOptional)
						continue;
					long id = result.getLong(i + 2);
					references.add(createReference(ref, id, ownerId, nestedOwnerId));
				}
			});
		}
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
			log.error("Error executing native query '" + query + "'", e);
		}
	}

	private List<String> createQueries(String table, String idField, Set<Long> ids,
			Ref[] references) {
		List<String> queries = new ArrayList<>();
		StringBuilder subquery = new StringBuilder();
		subquery.append("SELECT DISTINCT " + idField);
		for (int i = 0; i < references.length; i++) {
			subquery.append(", ");
			subquery.append(references[i].field);
		}
		subquery.append(" FROM " + table);
		if (ids.isEmpty())
			return Collections.singletonList(subquery.toString());
		subquery.append(" WHERE " + idField + " IN ");
		List<String> idLists = asSqlLists(ids.toArray());
		for (String idList : idLists)
			queries.add(subquery + "(" + idList + ")");
		return queries;
	}

	static List<String> createQueries(String base, String where, Collection<Long> ids) {
		if (ids.isEmpty())
			return Collections.singletonList(base);
		base += " " + where + " ";
		List<String> idLists = Search.asSqlLists(ids.toArray());
		List<String> queries = new ArrayList<>();
		for (String idList : idLists) {
			queries.add(base + "(" + idList + ")");
		}
		return queries;
	}

	/**
	 * Creates comma separated lists, each containing a thousand ids
	 */
	static List<String> asSqlLists(Object[] values) {
		List<String> idLists = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (i % 1000 != 0)
				builder.append(',');
			builder.append(values[i].toString());
			if ((i + 1) % 1000 == 0 || (i + 1) == values.length) {
				idLists.add(builder.toString());
				builder = new StringBuilder();
			}
		}
		return idLists;
	}

}
