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
			if (!foundErrors && !v.hasStopped()) {
				v.ok("checked impact methods");
			}
		} catch (Exception e) {
			v.error("error in impact method validation", e);
		} finally {
			v.workerFinished();
		}
	}

	private void checkImpactRefs() {
		if (v.hasStopped())
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
			return !v.hasStopped();
		});
	}

	private void checkNwSets() {
		if (v.hasStopped())
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
			return !v.hasStopped();
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
			return !v.hasStopped();
		});
	}


	private void checkDocRefs() {
		if (v.hasStopped())
			return;

		var sql = "select " +
			/* 1 */ "id, " +
			/* 2 */ "f_author, " +
			/* 3 */ "f_generator from tbl_impact_methods";
		NativeSql.on(v.db).query(sql, r -> {
			var id = r.getLong(1);

			var author = r.getLong(2);
			if (author != 0 && !v.ids.contains(ModelType.ACTOR, author)) {
				v.warning(id, ModelType.IMPACT_METHOD,
					"invalid reference to author @" + author);
				foundErrors = true;
			}

			var generator = r.getLong(3);
			if (generator != 0 && !v.ids.contains(ModelType.ACTOR, generator)) {
				v.warning(id, ModelType.ACTOR,
					"invalid reference to generator @" + generator);
				foundErrors = true;
			}

			return !v.hasStopped();
		});
	}
}
