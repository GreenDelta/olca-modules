package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.Epd;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.Process;
import org.openlca.core.model.Project;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.RootDescriptor;

/**
 * Searches for data sets where a unit is used. This does not include the unit
 * group where the unit is defined.
 */
public record UnitUsageSearch(IDatabase db, Unit unit) {

	public List<RootDescriptor> run() {
		if (unit == null || db == null)
			return Collections.emptyList();
		try {
			var exec = Executors.newFixedThreadPool(5);
			var results = exec.invokeAll(List.of(
				q(Process.class,
					"tbl_exchanges", "f_owner", "f_unit"),
				q(ImpactCategory.class,
					"tbl_impact_factors", "f_impact_category", "f_unit"),
				q(Project.class,
					"tbl_project_variants", "f_project", "f_unit"),
				q(Result.class,
					"tbl_flow_results", "f_result", "f_unit"),
				q(SocialIndicator.class,
					"tbl_social_indicators", "id", "f_activity_unit"),
				q(Epd.class,
					"tbl_epds", "id", "f_unit")
			));
			exec.shutdown();
			var descriptors = new ArrayList<RootDescriptor>();
			for (var result : results) {
				descriptors.addAll(result.get());
			}
			return descriptors;
		} catch (Exception e) {
			throw new RuntimeException("failed to query for usages of " + unit, e);
		}
	}

	private <T extends RootEntity> Callable<List<? extends RootDescriptor>> q(
		Class<T> type, String table, String entityField, String unitField) {
		return () -> {
			var query = "select distinct " + entityField
				+ " from " + table + " where " + unitField + " = " + unit.id;
			var ids = new HashSet<Long>();
			NativeSql.on(db).query(query, r -> {
				ids.add(r.getLong(1));
				return true;
			});
			return ids.isEmpty()
				? Collections.emptyList()
				: db.getDescriptors(type, ids);
		};
	}
}
