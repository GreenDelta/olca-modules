package org.openlca.validation;

import gnu.trove.map.hash.TLongByteHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.FlowDescriptor;

class FlowDirectionCheck implements Runnable {

	private final Validation v;
	private boolean foundErrors = false;

	FlowDirectionCheck(Validation v) {
		this.v = v;
	}

	@Override
	public void run() {
		try {
			var flows = new FlowDao(v.db).descriptorMap();

			// collect the IDs of quantitative references
			var qrefs = new TLongHashSet();
			var qrefQuery = "select f_quantitative_reference from tbl_processes";
			NativeSql.on(v.db).query(qrefQuery, r -> {
				qrefs.add(r.getLong(1));
				return true;
			});

			checkDirections(flows, qrefs);
			if (!foundErrors && !v.wasCanceled()) {
				v.ok("checked flow directions");
			}
		} catch (Exception e) {
			v.error("error in flow direction check", e);
		} finally {
			v.workerFinished();
		}
	}

	private void checkDirections(
		TLongObjectHashMap<FlowDescriptor> flows, TLongHashSet qrefs) {
		if (v.wasCanceled())
			return;

		var sql = "select " +
			/* 1 */ "id, " +
			/* 2 */ "f_owner, " +
			/* 3 */ "f_flow, " +
			/* 4 */ "is_input from tbl_exchanges";

		// the found directions of the elementary flows
		var directions = new TLongByteHashMap();
		byte input = (byte) 1;
		byte output = (byte) 2;
		byte error = (byte) 4;

		NativeSql.on(v.db).query(sql, r -> {
			var exchangeId = r.getLong(1);
			var processId = r.getLong(2);
			var flowId = r.getLong(3);
			var isInput = r.getBoolean(4);

			var flow = flows.get(flowId);
			// missing flows are reported in the ref-checks
			if (flow == null || flow.flowType == null)
				return !v.wasCanceled();
			var flowType = flow.flowType;

			if (qrefs.contains(exchangeId)) {
				checkRef(processId, flowType, isInput);

			} else if (flowType == FlowType.ELEMENTARY_FLOW) {
				byte direction = directions.get(flowId);
				if (direction == 0) {
					directions.put(flowId, isInput ? input : output);
				} else if ((direction == input && !isInput)
					|| (direction == output && isInput)) {
					directions.put(flowId, error);
					v.error(flowId, ModelType.FLOW,
						"elementary flow is used as input and output of processes");
					foundErrors = true;
				}
			}

			return !v.wasCanceled();
		});
	}

	private void checkRef(long processId, FlowType type, boolean isInput) {
		if ((type == FlowType.PRODUCT_FLOW && !isInput)
			|| (type == FlowType.WASTE_FLOW && isInput))
			return;
		v.warning(processId, ModelType.PROCESS,
			"the quantitative reference is not a product output or waste input");
		foundErrors = true;
	}

}
