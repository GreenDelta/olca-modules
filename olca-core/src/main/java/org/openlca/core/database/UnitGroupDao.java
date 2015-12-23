package org.openlca.core.database;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;

public class UnitGroupDao extends
		CategorizedEntityDao<UnitGroup, UnitGroupDescriptor> {

	public UnitGroupDao(IDatabase database) {
		super(UnitGroup.class, UnitGroupDescriptor.class, database);
	}

	public boolean hasReferenceUnit(long id) {
		return hasReferenceUnit(Collections.singleton(id)).get(id);
	}

	public Map<Long, Boolean> hasReferenceUnit(Set<Long> ids) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT id, f_reference_unit FROM tbl_unit_groups ");
		query.append("WHERE id IN " + asSqlList(ids));
		query.append(" AND f_reference_unit IN ");
		query.append("(SELECT id FROM tbl_units WHERE id = f_reference_unit)");
		Map<Long, Boolean> result = new HashMap<>();
		for (long id : ids)
			result.put(id, false);
		try {
			NativeSql.on(database).query(query.toString(), (res) -> {
				result.put(res.getLong(1), res.getLong(2) != 0l);
				return true;
			});
		} catch (SQLException e) {
			log.error("Error checking for reference unit existence", e);
		}
		return result;
	}

}
