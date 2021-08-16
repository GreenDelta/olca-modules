package org.openlca.validation;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class FlowPropertyCheck implements Runnable {

	private final Validation v;
	private boolean foundErrors = false;

	FlowPropertyCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			checkReferences();
			if (!foundErrors && !v.wasCanceled()) {
				v.ok("checked flow properties");
			}
		} catch (Exception e) {
			v.error("error in flow property validation", e);
		} finally {
			v.workerFinished();
		}
	}

	private void checkReferences() {
		if (v.wasCanceled())
			return;
		var sql = "select id, f_unit_group from tbl_flow_properties";
		NativeSql.on(v.db).query(sql, r -> {
			long id = r.getLong(1);
			long groupID = r.getLong(2);
			if (!v.ids.contains(ModelType.UNIT_GROUP, groupID)) {
				v.error(id, ModelType.FLOW_PROPERTY,
					"invalid link to unit group @" + groupID);
				foundErrors = true;
			}
			return !v.wasCanceled();
		});
	}
}
