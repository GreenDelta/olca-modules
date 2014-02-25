package org.openlca.core.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.Mapping;
import org.openlca.core.model.ModelType;

public class MappingDao extends BaseDao<Mapping> {

	public MappingDao(IDatabase database) {
		super(Mapping.class, database);
	}

	public List<Mapping> getAllForImport(ModelType type, String format) {
		return getAll(type, format, true);
	}

	public List<Mapping> getAllForExport(ModelType type, String format) {
		return getAll(type, format, false);
	}

	private List<Mapping> getAll(ModelType type, String format,
			boolean forImport) {
		String jpql = "select m from Mapping m where m.forImport = :forImport and "
				+ "m.format = :format and m.modelType = :modelType";
		Map<String, Object> args = new HashMap<>();
		args.put("forImport", forImport);
		args.put("format", format);
		args.put("modelType", type);
		return getAll(jpql, args);
	}

}
