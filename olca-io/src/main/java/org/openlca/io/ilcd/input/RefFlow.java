package org.openlca.io.ilcd.input;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.ilcd.util.Processes;

import java.util.List;
import java.util.Map;

/**
 * Maps the reference flow of an ILCD process to an openLCA process. An ILCD
 * process can have a single, multiple, or even no reference flow (= the
 * quantitative reference of the process). In the two former cases, this class
 * tries to find the best match.
 */
class RefFlow {

	private final org.openlca.ilcd.processes.Process ds;
	private final Process oProcess;
	private final Map<Integer, Exchange> map;

	/**
	 * Map the reference flows. The given map (int -> Exchange) maps the ILCD
	 * exchange IDs to the respective openLCA exchanges.
	 */
	public static void map(
			org.openlca.ilcd.processes.Process ds,
			Process oProcess,
			Map<Integer, Exchange> map) {
		if (ds == null || oProcess == null
				|| map == null || map.isEmpty()) {
			return;
		}
		new RefFlow(ds, oProcess, map).map();
	}

	private RefFlow(
			org.openlca.ilcd.processes.Process ds,
			Process oProcess,
			Map<Integer, Exchange> map) {
		this.ds = ds;
		this.oProcess = oProcess;
		this.map = map;
	}

	private void map() {
		List<Integer> refFlowIds = Processes.getReferenceFlows(ds);
		Exchange e = null;
		if (refFlowIds != null) {
			e = find(refFlowIds);
		}
		if (e == null) {
			e = find(map.keySet());
		}
		oProcess.quantitativeReference = e;
	}

	private Exchange find(Iterable<Integer> ids) {
		Exchange candidate = null;
		for (Integer id : ids) {
			Exchange e = map.get(id);
			if (betterMatch(e, candidate))
				candidate = e;
		}
		return candidate;
	}

	private boolean betterMatch(Exchange newCandidate, Exchange oldCandidate) {
		if (newCandidate == null)
			return false;
		if (oldCandidate == null)
			return true;
		if (isProvider(newCandidate) && !isProvider(oldCandidate))
			return true;
		if (!isProvider(newCandidate) && isProvider(oldCandidate))
			return false;
		return newCandidate.amount > oldCandidate.amount;
	}

	private boolean isProvider(Exchange e) {
		if (e == null || e.flow == null)
			return false;
		FlowType type = e.flow.flowType;
		if (type == FlowType.ELEMENTARY_FLOW)
			return false;
		return (e.isInput && type == FlowType.WASTE_FLOW)
				|| (!e.isInput && type == FlowType.PRODUCT_FLOW);
	}
}
