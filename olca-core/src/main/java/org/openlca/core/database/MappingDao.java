package org.openlca.core.database;

import org.openlca.core.model.mapping.Mapping;

public class MappingDao extends BaseDao<Mapping> {

	public MappingDao(IDatabase database) {
		super(Mapping.class, database);
	}

//	public List<Mapping> getAll(boolean input, MapFormat format, ModelType type)
//			throws Exception {
//		String jpql = "select m from Mapping m where m.input = :input and "
//				+ "m.format = :format and m.mapType = :type";
//		Map<String, Object> args = new HashMap<>();
//		args.put("input", input);
//		args.put("format", format);
//		args.put("type", type);
//		return getAll(jpql, args);
//	}

}
