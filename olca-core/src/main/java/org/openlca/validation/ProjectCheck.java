package org.openlca.validation;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class ProjectCheck implements Runnable {

	private final Validation v;
	private boolean foundErrors = false;

	ProjectCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			checkProjectRefs();
			checkVariantRefs();
			if (!foundErrors && !v.wasCanceled()) {
				v.ok("checked projects");
			}
		} catch (Exception e) {
			v.error("error in project validation", e);
		} finally {
			v.workerFinished();
		}
	}

	private void checkProjectRefs() {
		if (v.wasCanceled())
			return;
		var sql = "select " +
			/* 1 */ "id, " +
			/* 2 */ "f_impact_method, " +
			/* 3 */ "f_nwset from tbl_projects";
		NativeSql.on(v.db).query(sql, r -> {
			var projectId = r.getLong(1);

			var methodId = r.getLong(2);
			if (!v.ids.containsOrZero(ModelType.IMPACT_METHOD, methodId)) {
				v.error(projectId, ModelType.PROJECT,
					"invalid reference to impact method @" + methodId);
				foundErrors = true;
			}

			var nwSetId = r.getLong(3);
			if (!v.ids.containsOrZero(ModelType.NW_SET, nwSetId)) {
				v.error(projectId, ModelType.PROJECT,
					"invalid reference to nw-set @" + nwSetId);
				foundErrors = true;
			}
			return !v.wasCanceled();
		});
	}

	private void checkVariantRefs() {
		if (v.wasCanceled())
			return;
		var sql = "select " +
			/* 1 */ "id, " +
			/* 2 */ "f_project, " +
			/* 3 */ "f_product_system, " +
			/* 4 */ "f_unit, " +
			/* 5 */ "f_flow_property_factor from tbl_project_variants";
		NativeSql.on(v.db).query(sql, r -> {
			long variantId = r.getLong(1);
			long projectId = r.getLong(2);

			if (!v.ids.contains(ModelType.PROJECT, projectId)) {
				v.warning("unlinked project variant @" + variantId);
				foundErrors = true;
				return !v.wasCanceled();
			}

			long systemId = r.getLong(3);
			if (!v.ids.contains(ModelType.PRODUCT_SYSTEM, systemId)) {
				v.error(projectId, ModelType.PROJECT,
					"invalid reference to product system @" + systemId
						+ " in variant @" + variantId);
				foundErrors = true;
				return !v.wasCanceled();
			}

			long unitId = r.getLong(4);
			if (!v.ids.contains(ModelType.UNIT, unitId)) {
				v.error(projectId, ModelType.PROJECT,
					"invalid reference to unit @" + unitId
						+ " in variant @" + variantId);
				foundErrors = true;
			}

			long propId = r.getLong(5);
			if (!v.ids.flowPropertyFactors().contains(propId)) {
				v.error(projectId, ModelType.PROJECT,
					"invalid reference to flow property fact. @" + propId
						+ " in variant @" + variantId);
				foundErrors = true;
			}

			return !v.wasCanceled();
		});
	}
}
