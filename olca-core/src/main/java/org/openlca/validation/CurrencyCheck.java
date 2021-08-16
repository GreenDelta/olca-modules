package org.openlca.validation;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class CurrencyCheck implements Runnable {

	private final Validation v;
	private boolean foundErrors = false;

	CurrencyCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			checkRefFactors();
			if (!foundErrors && !v.wasCanceled()) {
				v.ok("checked currencies");
			}
		} catch (Exception e) {
			v.error("error in currency validation", e);
		} finally {
			v.workerFinished();
		}
	}

	private void checkRefFactors() {
		if (v.wasCanceled())
			return;
		var sql = "select " +
			/* 1 */ "id, " +
			/* 2 */ "f_reference_currency, " +
			/* 3 */ "conversion_factor from tbl_currencies";
		NativeSql.on(v.db).query(sql, r -> {
			long id = r.getLong(1);

			long refID = r.getLong(2);
			if (!v.ids.contains(ModelType.CURRENCY, refID)) {
				v.error(id, ModelType.CURRENCY,
					"invalid link to reference currency @" + refID);
				foundErrors = true;
			}

			double factor = r.getDouble(3);
			if (Double.compare(factor, 0) == 0) {
				v.error(id, ModelType.CURRENCY,
					"invalid conversion factor of 0 to ref. currency @" + refID);
				foundErrors = true;
			}

			return !v.wasCanceled();
		});
	}
}
