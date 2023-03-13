package org.openlca.validation;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class ProductSystemCheck implements Runnable {

	private final Validation v;
	private boolean foundIssues = false;

	ProductSystemCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			var processIds = checkProcessSets();
			checkLinks(processIds);
			if (!foundIssues && !v.wasCanceled()) {
				v.ok("checked product systems");
			}
		} catch (Exception e) {
			v.error("error in product system check", e);
		} finally {
			v.workerFinished();
		}
	}

	private ProcessIdSet checkProcessSets() {
		var ids = new ProcessIdSet(v);
		if (v.wasCanceled())
			return ids;
		var q = "select f_product_system, f_process from " +
				"tbl_product_system_processes";
		NativeSql.on(v.db).query(q, r -> {
			var systemId = r.getLong(1);
			var processId = r.getLong(2);
			if (!ids.add(systemId, processId)) {
				v.error(systemId, ModelType.PRODUCT_SYSTEM,
						"invalid process/sub-system/result @" + processId);
				foundIssues = true;
			}
			return !v.wasCanceled();
		});
		return ids;
	}

	private void checkLinks(ProcessIdSet processIds) {
		if (v.wasCanceled() || foundIssues)
			return;
		var q = "select " +
				/* 1 */  "f_product_system, " +
				/* 2 */  "f_provider, " +
				/* 3 */  "f_flow, " +
				/* 4 */  "f_process, " +
				/* 5 */  "f_exchange from tbl_process_links";
		NativeSql.on(v.db).query(q, r -> {
			var systemId = r.getLong(1);

			// provider
			var providerId = r.getLong(2);
			if (!processIds.contains(systemId, providerId)) {
				v.error(systemId, ModelType.PRODUCT_SYSTEM,
						"invalid link; provider @" + providerId);
				foundIssues = true;
				return !v.wasCanceled();
			}

			// flow
			var flowId = r.getLong(3);
			if (!v.ids.contains(ModelType.FLOW, flowId)) {
				v.error(systemId, ModelType.PRODUCT_SYSTEM,
						"invalid link; flow @" + flowId);
				foundIssues = true;
				return !v.wasCanceled();
			}

			// process
			var processId = r.getLong(4);
			if (!processIds.contains(systemId, processId)) {
				v.error(systemId, ModelType.PRODUCT_SYSTEM,
						"invalid link; process @" + processId);
				foundIssues = true;
				return !v.wasCanceled();
			}

			// exchange
			var exchangeId = r.getLong(5);
			if (exchangeId == 0) {
				v.error(systemId, ModelType.PRODUCT_SYSTEM,
						"invalid linkg; no exchange");
				foundIssues = true;
				return !v.wasCanceled();
			}

			return !v.wasCanceled();
		});
	}

	private static class ProcessIdSet {
		private final Validation v;
		final TLongObjectHashMap<TLongHashSet> ids = new TLongObjectHashMap<>();

		ProcessIdSet(Validation v) {
			this.v = v;
		}

		boolean add(long systemId, long processId) {
			if (!v.ids.contains(ModelType.PROCESS, processId)
					&& !v.ids.contains(ModelType.PRODUCT_SYSTEM, processId)
					&& !v.ids.contains(ModelType.RESULT, processId)) {
				return false;
			}
			var sysIds = ids.get(systemId);
			if (sysIds == null) {
				sysIds = new TLongHashSet();
				ids.put(systemId, sysIds);
			}
			sysIds.add(processId);
			return true;
		}

		boolean contains(long systemId, long processId) {
			var sysIds = ids.get(systemId);
			return sysIds != null && sysIds.contains(processId);
		}
	}

}
