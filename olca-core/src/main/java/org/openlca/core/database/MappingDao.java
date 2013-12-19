package org.openlca.core.database;

import org.openlca.core.model.Mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingDao extends BaseDao<Mapping> {

	public MappingDao(IDatabase database) {
		super(Mapping.class, database);
	}

	public List<Mapping> getAllForImport(String mappingType) {
		return getAll(mappingType, true);
	}

	public List<Mapping> getAllForExport(String mappingType) {
		return getAll(mappingType, false);
	}

	private List<Mapping> getAll(String mappingType, boolean forImport) {
		String jpql = "select m from Mapping m where m.forImport = :forImport and "
				+ "m.mappingType = :mappingType";
		Map<String, Object> args = new HashMap<>();
		args.put("forImport", forImport);
		args.put("mappingType", mappingType);
		return getAll(jpql, args);
	}

}
