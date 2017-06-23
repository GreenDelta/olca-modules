package org.openlca.io.ilcd.input;

import java.util.List;
import java.util.Map;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.ilcd.util.ProcessBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps the reference flow of an ILCD process to an openLCA process. There can
 * be multiple-issues with the ILCD process that may needs to be solved to
 * create a valid openLCA process:
 * <ul>
 * <li>no reference flow provided: try to find a product on the output site, if
 * there is none, try to find a negative input product, change the sign and
 * direction and set it as reference flow.
 * <li>the reference flow is on the input site: move it to the output site
 * <li>there are multiple-reference flows: try to find the product with the
 * highest amount on the output site and take this as reference flow.
 * </ul>
 */
public class ProcessRefFlowMapper {

	private Logger log = LoggerFactory.getLogger(getClass());
	private ProcessBag ilcdProcess;
	private Process olcaProcess;
	private Map<Integer, Exchange> map;

	/**
	 * Initialise the mapper: idMap is the mapping of ILCD data set internal IDs
	 * of exchanges to the respective openLCA exchanges in the process.
	 */
	public ProcessRefFlowMapper(ProcessBag ilcdProcess, Process olcaProcess,
			Map<Integer, Exchange> map) {
		this.ilcdProcess = ilcdProcess;
		this.olcaProcess = olcaProcess;
		this.map = map;
	}

	public void setReferenceFlow() {
		if (ilcdProcess == null || olcaProcess == null || map == null
				|| map.isEmpty()) {
			log.warn("Null arguments or empty exchanges for process {} "
					+ "in reference flow mapper, no reference flow set",
					olcaProcess);
			return;
		}
		List<Integer> refFlowIds = ilcdProcess.getReferenceFlowIds();
		if (refFlowIds == null || refFlowIds.isEmpty())
			handleNoReferenceFlow();
		else
			setReferenceFlow(refFlowIds);
	}

	private void setReferenceFlow(List<Integer> refFlowIds) {
		Exchange candidate = null;
		for (Integer flowId : refFlowIds) {
			Exchange refExchange = map.get(flowId);
			if (refExchange == null) {
				log.warn("Reference flow ID {} in ILCD process {} does "
						+ "not link to an exchange", candidate,
						ilcdProcess.getId());
				continue;
			}
			if (betterMatch(refExchange, candidate))
				candidate = refExchange;
		}
		handleCandidate(candidate);
	}

	private void handleCandidate(Exchange candidate) {
		if (candidate == null
				|| candidate.flow
						.getFlowType() == FlowType.ELEMENTARY_FLOW)
			handleNoReferenceFlow();
		else {
			if (candidate.isInput) {
				log.warn("Input found as reference flow in ILCD process {};"
						+ " changed it to output", ilcdProcess.getId());
				candidate.isInput = false;
				if (candidate.amountValue < 0)
					switchSign(candidate);
			}
			olcaProcess.setQuantitativeReference(candidate);
		}
	}

	private void handleNoReferenceFlow() {
		String processId = ilcdProcess.getId();
		log.warn("No reference flow found in ILCD process {}", processId);
		Exchange found = findBestOutput();
		if (found == null) {
			found = findNegativeInputProduct();
			if (found == null) {
				log.warn("No ref. flow found in ILCD process {}", processId);
			} else {
				switchSign(found);
				found.isInput = false;
				olcaProcess.setQuantitativeReference(found);
			}
		} else {
			log.info("Take best tech. output as ref. flow for ILCD process {}",
					processId);
			olcaProcess.setQuantitativeReference(found);
		}
	}

	private void switchSign(Exchange found) {
		log.info("Set a negative input product as quant. ref., "
				+ "change sign, in process {}", ilcdProcess.getId());
		double val = found.amountValue;
		found.amountValue = Math.abs(val);
		found.amountFormula = null;
	}

	private Exchange findBestOutput() {
		Exchange candidate = null;
		for (Exchange exchange : olcaProcess.getExchanges()) {
			if (exchange.isInput
					|| exchange.flow
							.getFlowType() == FlowType.ELEMENTARY_FLOW)
				continue;
			if (betterMatch(exchange, candidate))
				candidate = exchange;
		}
		return candidate;
	}

	private Exchange findNegativeInputProduct() {
		for (Exchange exchange : olcaProcess.getExchanges()) {
			if (exchange.isInput
					&& exchange.flow.getFlowType() == FlowType.PRODUCT_FLOW
					&& exchange.amountValue < 0)
				return exchange;
		}
		return null;
	}

	private boolean betterMatch(Exchange newCandidate, Exchange oldCandidate) {
		if (newCandidate == null)
			return false;
		if (oldCandidate == null)
			return true;
		if (!newCandidate.isInput && oldCandidate.isInput)
			return true;
		if (newCandidate.flow.getFlowType() == FlowType.PRODUCT_FLOW
				&& oldCandidate.flow
						.getFlowType() != FlowType.PRODUCT_FLOW)
			return true;
		if (newCandidate.flow.getFlowType() == FlowType.WASTE_FLOW
				&& oldCandidate.flow
						.getFlowType() != FlowType.ELEMENTARY_FLOW)
			return true;
		if (newCandidate.amountValue > oldCandidate.amountValue)
			return true;
		return false;
	}
}
