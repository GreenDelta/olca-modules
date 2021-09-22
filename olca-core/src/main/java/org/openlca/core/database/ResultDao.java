package org.openlca.core.database;

import java.util.HashSet;
import java.util.List;

import org.openlca.core.model.ResultModel;
import org.openlca.core.model.descriptors.ResultDescriptor;

public class ResultDao extends CategorizedEntityDao<ResultModel, ResultDescriptor> {

	public ResultDao(IDatabase db) {
		super(ResultModel.class, ResultDescriptor.class, db);
	}

	/**
	 * Get the descriptors of the results in the database that are not a
	 * sub-result of some other result.
	 */
	public List<ResultDescriptor> getTopResults() {
		var sql = "select id, f_parent_result from tbl_results";
		var ids = new HashSet<Long>();
		NativeSql.on(db).query(sql, r -> {
			var parentId = r.getLong(2);
			if (parentId == 0) {
				ids.add(r.getLong(1));
			}
			return true;
		});
		return getDescriptors(ids);
	}

}
