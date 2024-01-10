package org.openlca.validation;

import gnu.trove.map.hash.TLongLongHashMap;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class ProcessCheck implements Runnable {

	private final Validation v;
	private boolean foundErrors = false;

	ProcessCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			var lastInternalIds = checkProcessRefs();
			checkExchanges(lastInternalIds);
			checkQuantitativeRefs();
			checkProcessDocs();
			checkAllocationFactors();
			checkSocialAspects();
			if (!foundErrors && !v.wasCanceled()) {
				v.ok("checked processes");
			}
		} catch (Exception e) {
			v.error("error in process validation", e);
		} finally {
			v.workerFinished();
		}
	}

	private TLongLongHashMap checkProcessRefs() {
		var lastInternalIds = new TLongLongHashMap();
		if (v.wasCanceled())
			return lastInternalIds;

		var sql = "select " +
				/* 1 */ "id, " +
				/* 2 */ "f_location, " +
				/* 3 */ "f_dq_system, " +
				/* 4 */ "f_exchange_dq_system, " +
				/* 5 */ "f_social_dq_system, " +
				/* 6 */ "last_internal_id from tbl_processes";

		NativeSql.on(v.db).query(sql, r -> {
			long id = r.getLong(1);

			var locID = r.getLong(2);
			if (locID != 0 && !v.ids.contains(ModelType.LOCATION, locID)) {
				v.error(id, ModelType.PROCESS, "invalid location @" + locID);
				foundErrors = true;
			}

			for (int i = 3; i < 6; i++) {
				var dqID = r.getLong(i);
				if (dqID != 0 && !v.ids.contains(ModelType.DQ_SYSTEM, dqID)) {
					v.error(id, ModelType.PROCESS, "invalid DQ system @" + dqID);
					foundErrors = true;
				}
			}

			lastInternalIds.put(id, r.getLong(6));

			return !v.wasCanceled();
		});

		return lastInternalIds;
	}

	private void checkProcessDocs() {
		if (v.wasCanceled())
			return;
		var sql = "select " +
				/* 1 */ "p.id, " +
				/* 2 */ "doc.f_reviewer, " +
				/* 3 */ "doc.f_data_generator, " +
				/* 4 */ "doc.f_data_owner, " +
				/* 5 */ "doc.f_data_documentor, " +
				/* 6 */ "doc.f_publication, " +
				/* 7 */ "doc.f_review_report from tbl_processes p inner join " +
				"tbl_process_docs doc on p.f_process_doc = doc.id";
		var refs = new String[]{
				"reviewer",
				"data generator",
				"data set owner",
				"data documentor",
				"publication",
				"review report"
		};

		NativeSql.on(v.db).query(sql, r -> {
			var id = r.getLong(1);

			for (int i = 0; i < refs.length; i++) {
				var refID = r.getLong(i + 2);
				if (refID == 0)
					continue;
				var type = i >= 4 ? ModelType.SOURCE : ModelType.ACTOR;
				if (!v.ids.contains(type, refID)) {
					v.warning(id, ModelType.PROCESS,
							"invalid reference to " + refs[i] + " @" + refID);
					foundErrors = true;
				}
			}
			return !v.wasCanceled();
		});
	}

	private void checkQuantitativeRefs() {
		if (v.wasCanceled())
			return;
		var sql = "select p.id from tbl_processes p " +
				"left join tbl_exchanges e on " +
				"p.f_quantitative_reference = e.id " +
				"where e.id is null";
		NativeSql.on(v.db).query(sql, r -> {
			long id = r.getLong(1);
			v.warning(id, ModelType.PROCESS, "no quantitative reference");
			foundErrors = true;
			return !v.wasCanceled();
		});
	}

	private void checkExchanges(TLongLongHashMap lastInternalIds) {
		if (v.wasCanceled())
			return;

		var processIDs = v.ids.allOf(ModelType.PROCESS);
		var sql = "select " +
				/* 1 */ "f_owner, " +
				/* 2 */ "f_flow, " +
				/* 3 */ "f_unit, " +
				/* 4 */ "f_flow_property_factor, " +
				/* 5 */ "f_default_provider, " +
				/* 6 */ "f_location, " +
				/* 7 */ "f_currency, " +
				/* 8 */ "internal_id from tbl_exchanges";

		NativeSql.on(v.db).query(sql, r -> {
			var id = r.getLong(1);

			if (!processIDs.contains(id))
				return true;

			var lastInternalId = lastInternalIds.get(id);
			var internalId = r.getLong(8);
			if (internalId == 0) {
				v.error(id, ModelType.PROCESS, "no internal exchange ID");
				foundErrors = true;
			} else if (internalId > lastInternalId) {
				v.error(id, ModelType.PROCESS, "internal ID of exchange ("
						+ internalId + ") is larger than last-internal ID ("
						+ lastInternalId + ") of process");
				foundErrors = true;
			}

			var flowID = r.getLong(2);
			if (!v.ids.contains(ModelType.FLOW, flowID)) {
				v.error(id, ModelType.PROCESS,
						"invalid exchange flow @" + flowID);
				foundErrors = true;
			}

			var unitID = r.getLong(3);
			var factorID = r.getLong(4);
			if (!v.ids.units().isFlowUnit(flowID, factorID, unitID)) {
				v.error(id, ModelType.PROCESS,
						"invalid exchange unit; flow=" + flowID
								+ " property=" + factorID + " unit=" + unitID);
				foundErrors = true;
			}

			var providerID = r.getLong(5);
			if (providerID != 0 && !processIDs.contains(providerID)) {
				v.error(id, ModelType.PROCESS,
						"invalid exchange provider @" + providerID);
				foundErrors = true;
			}

			var locID = r.getLong(6);
			if (locID != 0 && !v.ids.contains(ModelType.LOCATION, locID)) {
				v.error(id, ModelType.PROCESS,
						"invalid exchange location @" + locID);
				foundErrors = true;
			}

			var currencyID = r.getLong(7);
			if (currencyID != 0 && !v.ids.contains(ModelType.CURRENCY, currencyID)) {
				v.error(id, ModelType.PROCESS,
						"invalid exchange currency @" + currencyID);
				foundErrors = true;
			}

			return !v.wasCanceled();
		});
	}

	private void checkAllocationFactors() {
		if (v.wasCanceled())
			return;
		var sql = "select " +
				/* 1 */ "a.f_process, " +
				/* 2 */ "a.f_product, " +
				/* 3 */ "a.f_exchange, " +
				/* 4 */ "e.id from tbl_allocation_factors a left " +
				"join tbl_exchanges e on a.f_exchange = e.id";
		NativeSql.on(v.db).query(sql, r -> {
			var id = r.getLong(1);

			if (!v.ids.contains(ModelType.PROCESS, id)) {
				v.warning("allocation factor with invalid process ID @" + id);
				foundErrors = true;
				return !v.wasCanceled();
			}

			var productID = r.getLong(2);
			if (!v.ids.contains(ModelType.FLOW, productID)) {
				v.error(id, ModelType.PROCESS,
						"allocation factor with invalid product ID @" + productID);
				foundErrors = true;
			}

			var exchangeID = r.getLong(3);
			if (exchangeID != 0) {
				var otherID = r.getLong(4);
				if (exchangeID != otherID) {
					v.error(id, ModelType.PROCESS,
							"allocation factor with invalid exchange ID @" + exchangeID);
					foundErrors = true;
				}
			}

			return !v.wasCanceled();
		});
	}

	private void checkSocialAspects() {
		if (v.wasCanceled())
			return;
		var sql = "select " +
				/* 1 */ "f_process, " +
				/* 2 */ "f_indicator, " +
				/* 3 */ "f_source from tbl_social_aspects";
		NativeSql.on(v.db).query(sql, r -> {

			var id = r.getLong(1);
			if (!v.ids.contains(ModelType.PROCESS, id)) {
				v.warning("social aspect with invalid process ID @" + id);
				foundErrors = true;
				return !v.wasCanceled();
			}

			var indicatorID = r.getLong(2);
			if (!v.ids.contains(ModelType.SOCIAL_INDICATOR, indicatorID)) {
				v.error(id, ModelType.PROCESS,
						"social aspect with invalid indicator @" + indicatorID);
				foundErrors = true;
			}

			var sourceID = r.getLong(3);
			if (sourceID != 0 && !v.ids.contains(ModelType.SOURCE, sourceID)) {
				v.warning(id, ModelType.PROCESS,
						"social aspect with invalid source @" + sourceID);
				foundErrors = true;
			}

			return !v.wasCanceled();
		});
	}

}
