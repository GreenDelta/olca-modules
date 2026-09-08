package org.openlca.validation;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;

class ImpactCategoryCheck implements Runnable {

	private final Validation v;
	private boolean foundErrors;

	ImpactCategoryCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			checkFactors();
			checkMetaData();
			if (!foundErrors && !v.wasCanceled()) {
				v.ok("checked impact categories");
			}
		} catch (Exception e) {
			v.error("error in impact category validation", e);
		} finally {
			v.workerFinished();
		}
	}

	private void checkFactors() {
		if (v.wasCanceled())
			return;

		var visited = new TLongObjectHashMap<TLongObjectHashMap<TLongHashSet>>();

		var sql = "select " +
				/* 1 */ "f_impact_category, " +
				/* 2 */ "f_flow, " +
				/* 3 */ "f_flow_property_factor, " +
				/* 4 */ "f_unit, " +
				/* 5 */ "f_location from tbl_impact_factors";
		NativeSql.on(v.db).query(sql, r -> {

			var impactId = r.getLong(1);
			if (!v.ids.contains(ModelType.IMPACT_CATEGORY, impactId)) {
				v.warning("impact factor with invalid impact category ID @" + impactId);
				foundErrors = true;
				return !v.wasCanceled();
			}

			var flowId = r.getLong(2);
			if (!v.ids.contains(ModelType.FLOW, flowId)) {
				v.error(impactId, ModelType.IMPACT_CATEGORY,
						"impact factor with invalid flow ID @" + flowId);
				foundErrors = true;
			}

			var propId = r.getLong(3);
			var unitId = r.getLong(4);
			if (!v.ids.units().isFlowUnit(flowId, propId, unitId)) {
				v.error(impactId, ModelType.IMPACT_CATEGORY,
						"impact factor with invalid flow property or unit; "
								+ "flow=" + flowId + " property=" + propId + " unit=" + unitId);
				foundErrors = true;
			}

			var locId = r.getLong(5);
			if (locId != 0 && !v.ids.contains(ModelType.LOCATION, locId)) {
				v.error(impactId, ModelType.IMPACT_CATEGORY,
						"impact factor with invalid location ID @" + locId);
				foundErrors = true;
			}

			if (isDuplicate(impactId, flowId, locId, visited)) {
				var loc = locId == 0
						? " without a location"
						: " for location @" + locId;
				v.error(flowId, ModelType.FLOW,
						"has multiple impact factors" + loc
								+ " in impact category @" + impactId);
				foundErrors = true;
			}

			return !v.wasCanceled();
		});
	}

	private void checkMetaData() {
		if (v.wasCanceled())
			return;
		var sql = "select " +
				/* 1 */ "id, " +
				/* 2 */ "f_source from tbl_impact_categories";
		NativeSql.on(v.db).query(sql, r -> {
			var id = r.getLong(1);

			// source
			var source = r.getLong(2);
			if (source != 0 && !v.ids.contains(ModelType.SOURCE, source)) {
				v.warning(id, ModelType.IMPACT_CATEGORY,
						"invalid reference to source @" + source);
				foundErrors = true;
			}
			return !v.wasCanceled();
		});
	}

	private boolean isDuplicate(
			long impactId, long flowId, long locId,
			TLongObjectHashMap<TLongObjectHashMap<TLongHashSet>> visited
	) {
		var flowLocs = visited.get(impactId);
		if (flowLocs == null) {
			flowLocs = new TLongObjectHashMap<>();
			visited.put(impactId, flowLocs);
		}
		var locs = flowLocs.get(flowId);
		if (locs == null) {
			locs = new TLongHashSet();
			flowLocs.put(flowId, locs);
		}
		if (locs.contains(locId))
			return true;
		locs.add(locId);
		return false;
	}
}
