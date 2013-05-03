package org.openlca.core.model.iomaps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.database.BaseDao;

public class MappingDao extends BaseDao<Mapping> {

	public MappingDao(EntityManagerFactory factory) {
		super(Mapping.class, factory);
	}

	public List<Mapping> getAll(MapType type, IOFormat format) throws Exception {
		String jpql = "select m from Mapping m where m.mapType = :type "
				+ "and m.format = :format";
		Map<String, Object> params = new HashMap<>();
		params.put("type", type);
		params.put("format", format);
		return getAll(jpql, params);
	}

}
