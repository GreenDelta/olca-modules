package org.openlca.io.ilcd.input.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.model.Copyable;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.ilcd.models.Group;
import org.openlca.ilcd.models.Parameter;
import org.openlca.ilcd.models.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Node implements Copyable<Node> {

	/** This is the same ID as in the eILCD data set. */
	final int id;

	/** The corresponding openLCA process. */
	Process process;

	/**
	 * An optional scaling factor (for the reference amount of the product
	 * system.)
	 */
	Double scalingFactor;

	/**
	 * The parameter redefinitions of the process instances in the model.
	 */
	final Map<String, Double> params = new HashMap<>();

	/**
	 * The eILCD life cycle group (stage) of the corresponding process instance
	 * in the life cycle model.
	 */
	Group group;

	private Node(int id) {
		this.id = id;
	}

	static Node init(ProcessInstance pi, Process process) {
		Node n = new Node(pi.id);
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
		var matches = new ArrayList<Exchange>(1);
		for (var e : process.exchanges) {
			if (e.isInput != isInput || e.flow == null)
				continue;
			if (Objects.equals(flowID, e.flow.refId)) {
				matches.add(e);
			}
		}
		if (matches.size() == 1)
			return matches.get(0);
		Logger log = LoggerFactory.getLogger(getClass());
		if (matches.size() > 1) {
			log.warn("There are multiple exchanges with flowID={} isInput={} "
					+ "in process={}; -> we take the first for linking",
					flowID, isInput, process.refId);
			return matches.get(0);
		}
		log.warn("Could not find exchange with flowID={} isInput={} "
				+ "in process={}", flowID, isInput, process.refId);
		return null;
	}

	@Override
	public Node copy() {
		var clone = new Node(id);
		if (process != null) {
			clone.process = process.copy();
		}
		clone.scalingFactor = scalingFactor;
		clone.params.putAll(params);
		clone.group = group;
		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Node other))
			return false;
		return other.id == this.id;
	}
}
