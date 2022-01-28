package org.openlca.validation;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

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
			checkDocRefs();
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

		var propIDs = v.ids.flowPropertyFactors();
		var sql = "select " +
			/* 1 */ "f_impact_category, " +
			/* 2 */ "f_flow, " +
			/* 3 */ "f_flow_property_factor, " +
			/* 4 */ "f_unit, " +
			/* 5 */ "f_location from tbl_impact_factors";
		NativeSql.on(v.db).query(sql, r -> {
			var id = r.getLong(1);
			if (!v.ids.contains(ModelType.IMPACT_CATEGORY, id)) {
				v.warning("impact factor with invalid impact category ID @" + id);
				foundErrors = true;
				return !v.wasCanceled();
			}

			var flowID = r.getLong(2);
			if (!v.ids.contains(ModelType.FLOW, flowID)) {
				v.error(id, ModelType.IMPACT_CATEGORY,
					"impact factor with invalid flow ID @" + flowID);
				foundErrors = true;
			}

			var propID = r.getLong(3);
			if (!propIDs.contains(propID)) {
				v.error(id, ModelType.IMPACT_CATEGORY,
					"impact factor with invalid flow property ID @" + propID);
				foundErrors = true;
			}

			var unitID = r.getLong(4);
			if (!v.ids.contains(ModelType.UNIT, unitID)) {
				v.error(id, ModelType.IMPACT_CATEGORY,
					"impact factor with invalid unit ID @" + unitID);
				foundErrors = true;
			}

			var locID = r.getLong(5);
			if (locID != 0 && !v.ids.contains(ModelType.LOCATION, locID)) {
				v.error(id, ModelType.IMPACT_CATEGORY,
					"impact factor with invalid location ID @" + locID);
				foundErrors = true;
			}

			return !v.wasCanceled();
		});
	}

	private void checkDocRefs() {
		if (v.wasCanceled())
			return;
		var sql = "select " +
			/* 1 */ "id, " +
			/* 2 */ "f_source from tbl_impact_categories";
		NativeSql.on(v.db).query(sql, r -> {
			var id = r.getLong(1);
			var source = r.getLong(2);
			if (source != 0 && !v.ids.contains(ModelType.SOURCE, source)) {
				v.warning(id, ModelType.IMPACT_CATEGORY,
					"invalid reference to source @" + source);
				foundErrors = true;
			}
			return !v.wasCanceled();
		});
	}
}
