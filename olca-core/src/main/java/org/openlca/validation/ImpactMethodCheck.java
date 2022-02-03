package org.openlca.validation;

import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class ImpactMethodCheck implements Runnable {

	private final Validation v;
	private boolean foundErrors;

	ImpactMethodCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			checkImpactRefs();
			checkNwSets();
			checkDocRefs();
			if (!foundErrors && !v.wasCanceled()) {
				v.ok("checked impact methods");
			}
		} catch (Exception e) {
			v.error("error in impact method validation", e);
		} finally {
			v.workerFinished();
		}
	}

	private void checkImpactRefs() {
		if (v.wasCanceled())
			return;
		var sql = "select " +
			/* 1 */ "f_impact_method, " +
			/* 2 */ "f_impact_category from tbl_impact_links";
		NativeSql.on(v.db).query(sql, r -> {
			var id = r.getLong(1);
			var impact = r.getLong(2);
			if (!v.ids.contains(ModelType.IMPACT_CATEGORY, impact)) {
				v.error(id, ModelType.IMPACT_METHOD,
					"invalid reference to impact category @" + impact);
				foundErrors = true;
			}
			return !v.wasCanceled();
		});
	}

	private void checkNwSets() {
		if (v.wasCanceled())
			return;

		// collect and check nw-sets
		var nwsets = new TLongHashSet();
		var sql = "select " +
			/* 1 */ "id, " +
			/* 2 */ "f_impact_method from tbl_nw_sets";
		NativeSql.on(v.db).query(sql, r -> {
			var nwset = r.getLong(1);
			nwsets.add(nwset);
			var method = r.getLong(2);
			if (!v.ids.contains(ModelType.IMPACT_METHOD, method)) {
				v.warning("unlinked nw-set @" + nwset);
				foundErrors = true;
			}
			return !v.wasCanceled();
		});

		// check nw-factors
		sql = "select " +
			/* 1 */ "f_impact_category, " +
			/* 2 */ "f_nw_set from tbl_nw_factors";
		NativeSql.on(v.db).query(sql, r -> {
			var impact = r.getLong(1);
			var nwset = r.getLong(2);
			if (!nwsets.contains(nwset)) {
				v.warning("unlinked nw-factor for nw-set @" + nwset);
				foundErrors = true;
			}
			if (!v.ids.contains(ModelType.IMPACT_CATEGORY, impact)) {
				v.warning("nw-factor with unlinked impact category @" + impact);
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
			/* 2 */ "f_source from tbl_impact_methods";
		NativeSql.on(v.db).query(sql, r -> {
			var id = r.getLong(1);
			var source = r.getLong(2);
			if (source != 0 && !v.ids.contains(ModelType.SOURCE, source)) {
				v.warning(id, ModelType.IMPACT_METHOD,
					"invalid reference to source @" + source);
				foundErrors = true;
			}
			return !v.wasCanceled();
		});
	}
}
