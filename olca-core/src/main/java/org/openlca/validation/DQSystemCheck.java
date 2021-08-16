package org.openlca.validation;

import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class DQSystemCheck implements Runnable {

	private final Validation v;
	boolean foundError = false;

	DQSystemCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			checkSourceLinks();
			checkIndicatorLinks();
			if (!foundError && !v.wasCanceled()) {
				v.ok("checked DQ systems");
			}
		} catch (Exception e) {
			v.error("error in DQ system validation", e);
		} finally {
			v.workerFinished();
		}
	}

	private void checkSourceLinks() {
		if (v.wasCanceled())
			return;
		var sql = "select id, f_source from tbl_dq_systems";
		NativeSql.on(v.db).query(sql, r -> {
			var id = r.getLong(1);
			var sourceID = r.getLong(2);
			if (sourceID != 0 && !v.ids.contains(ModelType.SOURCE, sourceID)) {
				v.warning(id, ModelType.DQ_SYSTEM,
					"invalid reference to source @" + sourceID);
				foundError = true;
			}
			return !v.wasCanceled();
		});
	}

	private void checkIndicatorLinks() {
		if (v.wasCanceled())
			return;

		// collect and check the indicators
		var indicatorIDs = new TLongHashSet();
		var sql = "select id, f_dq_system from tbl_dq_indicators";
		NativeSql.on(v.db).query(sql, r -> {
			var indicatorID = r.getLong(1);
			var systemID = r.getLong(2);
			if (!v.ids.contains(ModelType.DQ_SYSTEM, systemID)) {
				v.warning("unlinked DQ indicator @" + indicatorID);
				foundError = true;
			} else {
				indicatorIDs.add(indicatorID);
			}
			return !v.wasCanceled();
		});

		// check the scores
		if (v.wasCanceled())
			return;
		sql = "select id, f_dq_indicator from tbl_dq_scores";
		NativeSql.on(v.db).query(sql, r -> {
			var scoreID = r.getLong(1);
			var indicatorID = r.getLong(2);
			if (!indicatorIDs.contains(indicatorID)) {
				v.warning("unlinked DQ score @" + scoreID);
				foundError = true;
			}
			return !v.wasCanceled();
		});
	}

}
