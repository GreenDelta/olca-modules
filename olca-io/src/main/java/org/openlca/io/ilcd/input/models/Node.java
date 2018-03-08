package org.openlca.io.ilcd.input.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.ilcd.models.Parameter;
import org.openlca.ilcd.models.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Node {

	/** This is the same ID as in the eILCD data set. */
	int modelID;
	Process process;
	Double scalingFactor;
	final Map<String, Double> params = new HashMap<>();

	static Node init(ProcessInstance pi, Process process) {
		Node n = new Node();
		n.modelID = pi.id;
		n.process = process;
		n.scalingFactor = pi.scalingFactor;
		for (Parameter param : pi.parameters) {
			n.params.put(param.name, param.value);
		}
		return n;
	}

	Exchange findInput(String flowID) {
		return findExchange(flowID, true);
	}

	Exchange findOutput(String flowID) {
		return findExchange(flowID, false);
	}

	private Exchange findExchange(String flowID, boolean isInput) {
		if (process == null || flowID == null)
			return null;
		ArrayList<Exchange> matches = new ArrayList<>(1);
		for (Exchange e : process.getExchanges()) {
			if (e.isInput != isInput || e.flow == null)
				continue;
			if (Objects.equals(flowID, e.flow.getRefId())) {
				matches.add(e);
			}
		}
		if (matches.size() == 1)
			return matches.get(0);
		Logger log = LoggerFactory.getLogger(getClass());
		if (matches.size() > 1) {
			log.warn("There are multiple exchanges with flowID={} isInput={} "
					+ "in process={}; -> we take the first for linking",
					flowID, isInput, process.getRefId());
			return matches.get(0);
		}
		log.warn("Could not find exchange with flowID={} isInput={} "
				+ "in process={}", flowID, isInput, process.getRefId());
		return null;
	}

	@Override
	protected Node clone() {
		Node clone = new Node();
		clone.modelID = modelID;
		if (process != null) {
			clone.process = process.clone();
		}
		clone.scalingFactor = scalingFactor;
		clone.params.putAll(params);
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Node))
			return false;
		Node other = (Node) obj;
		return other.modelID == this.modelID;
	}
}
