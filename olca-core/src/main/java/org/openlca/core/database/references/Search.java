package org.openlca.core.database.references;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.database.CategorizedEntityDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.UnitDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;
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

	List<BaseDescriptor> findMixedReferences(String table, String idField,
			Set<Long> ids, Reference[] references, boolean includeOptional) {
		if (ids.isEmpty())
			return Collections.emptyList();
		Map<ModelType, Set<Long>> idsByType = createEmptyMapFrom(references);
		String query = createQuery(table, idField, ids, references);
		query(query, (result) -> {
			for (int i = 0; i < references.length; i++)
				if (!references[i].optional || includeOptional)
					idsByType.get(references[i].type)
							.add(result.getLong(i + 1));
		});
		return loadDescriptors(idsByType);
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
			Reference[] references) {
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

	private Map<ModelType, Set<Long>> createEmptyMapFrom(Reference[] references) {
		Map<ModelType, Set<Long>> map = new HashMap<>();
		for (int i = 0; i < references.length; i++)
			map.put(references[i].type, new HashSet<>());
		return map;
	}

	private List<BaseDescriptor> loadDescriptors(
			Map<ModelType, Set<Long>> idsByType) {
		List<BaseDescriptor> results = new ArrayList<>();
		for (ModelType type : idsByType.keySet()) {
			Set<Long> typeIds = idsByType.get(type);
			if (typeIds.isEmpty())
				continue;
			if (type.isCategorized())
				results.addAll(loadDescriptors(type, typeIds));
			else if (type == ModelType.UNIT)
				results.addAll(loadUnitDescriptors(typeIds));
			else if (type == ModelType.IMPACT_CATEGORY)
				results.addAll(loadImpactCategoryDescriptors(typeIds));
			else if (type == ModelType.UNKNOWN)
				results.addAll(createUnknownDescriptors(typeIds));
		}
		return results;
	}

	private List<CategorizedDescriptor> loadDescriptors(ModelType type,
			Set<Long> ids) {
		CategorizedEntityDao<?, ? extends CategorizedDescriptor> dao = Daos
				.createCategorizedDao(database, type);
		return new ArrayList<>(dao.getDescriptors(ids));
	}

	private List<UnitDescriptor> loadUnitDescriptors(Set<Long> ids) {
		UnitDao dao = new UnitDao(database);
		return new ArrayList<>(dao.getDescriptors(ids));
	}

	private List<ImpactCategoryDescriptor> loadImpactCategoryDescriptors(
			Set<Long> ids) {
		ImpactCategoryDao dao = new ImpactCategoryDao(database);
		return new ArrayList<>(dao.getDescriptors(ids));
	}

	private List<BaseDescriptor> createUnknownDescriptors(Set<Long> ids) {
		List<BaseDescriptor> descriptors = new ArrayList<>();
		for (long id : ids) {
			BaseDescriptor descriptor = new BaseDescriptor();
			descriptor.setId(id);
			descriptors.add(descriptor);
		}
		return descriptors;
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

	final static class Reference {

		private ModelType type;
		private String field;
		private boolean optional;

		Reference(ModelType type, String field) {
			this(type, field, false);
		}

		Reference(ModelType type, String field, boolean optional) {
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
