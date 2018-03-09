package org.openlca.io.ilcd.input.models;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Transformation {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Graph source;
	private final Graph target;

	private final Queue<Node> next = new ArrayDeque<>();
	private final Set<Integer> visitedNodes = new HashSet<>();
	private final Set<Integer> visitedLinks = new HashSet<>();

	private Transformation(Graph source) {
		this.source = source;
		this.target = new Graph();
	}

	static Graph on(Graph source) {
		Transformation t = new Transformation(source);
		t.doIt();
		return t.target;
	}

	private void doIt() {
		target.root = forTarget(source.root);
		next.add(target.root);
		while (!next.isEmpty()) {
			visit(next.poll());
		}
		mapRefFlow();
	}

	/** Visits the given node which is already a node from the target graph. */
	private void visit(Node n) {
		if (visitedNodes.contains(n.modelID))
			return;
		visitedNodes.add(n.modelID);
		for (Link inLink : source.getInputLinks(n.modelID)) {
			if (visitedLinks.contains(inLink.id))
				continue;
			visitedLinks.add(inLink.id);
			linkProduct(n, inLink);
		}
		for (Link outLink : source.getOutputLinks(n.modelID)) {
			if (visitedLinks.contains(outLink.id))
				continue;
			visitedLinks.add(outLink.id);
			linkWaste(n, outLink);
		}
	}

	private void linkProduct(Node recipient, Link inLink) {
		Node provider = forTarget(inLink.provider);
		Flow product = inLink.output.flow.clone();
		product.setFlowType(FlowType.PRODUCT_FLOW);
		Link link = link(provider, recipient, product, inLink);
		provider.process.setQuantitativeReference(link.output);
		next.add(provider);
	}

	private void linkWaste(Node provider, Link outLink) {
		Node recipient = forTarget(outLink.recipient);
		Flow waste = outLink.input.flow.clone();
		waste.setFlowType(FlowType.WASTE_FLOW);
		Link link = link(provider, recipient, waste, outLink);
		recipient.process.setQuantitativeReference(link.input);
		next.add(recipient);
	}

	private Link link(Node provider, Node recipient, Flow flow, Link sourceLink) {
		Exchange output = sourceLink.output.clone();
		Exchange input = sourceLink.input.clone();
		setFlow(flow, output);
		provider.process.getExchanges().add(output);
		setFlow(flow, input);
		recipient.process.getExchanges().add(input);
		Link link = new Link();
		link.input = input;
		link.output = output;
		link.provider = provider;
		link.recipient = recipient;
		target.putLink(link);
		return link;
	}

	private void setFlow(Flow flow, Exchange e) {
		e.flow = flow;
		e.unit = null;
		e.flowPropertyFactor = flow.getReferenceFactor();
		if (e.flowPropertyFactor == null) {
			log.warn("Could get a flow property factor for {}", flow);
			return;
		}
		FlowProperty prop = e.flowPropertyFactor.getFlowProperty();
		if (prop == null || prop.getUnitGroup() == null) {
			log.warn("Could not determine unit group for {}", flow);
			return;
		}
		e.unit = prop.getUnitGroup().getReferenceUnit();
		if (e.unit == null) {
			log.warn("Could not determine unit group for {}", flow);
		}
	}

	/**
	 * Initialize a node for the target graph from the given node in the source
	 * graph. This creates a copy of the process with all product and waste
	 * flows removed. If the target graph already contains this node (identified
	 * via the model ID) this node will be returned.
	 */
	private Node forTarget(Node sourceNode) {
		Node n = target.getNode(sourceNode.modelID);
		if (n != null)
			return n;
		n = sourceNode.clone();
		List<Exchange> elemFlows = new ArrayList<>();
		for (Exchange e : n.process.getExchanges()) {
			Flow f = e.flow;
			if (f == null || f.getFlowType() != FlowType.ELEMENTARY_FLOW)
				continue;
			elemFlows.add(e);
		}
		n.process.getExchanges().clear();
		n.process.getExchanges().addAll(elemFlows);
		target.putNode(n);
		return n;
	}

	private void mapRefFlow() {
		Exchange ref = source.root.process.getQuantitativeReference();
		if (ref == null || ref.flow == null) {
			log.warn("Ref. process of source graph has no reference flow.");
			return;
		}
		ref = ref.clone();
		Flow flow = ref.flow.clone();
		if (ref.isInput) {
			flow.setFlowType(FlowType.WASTE_FLOW);
		} else {
			flow.setFlowType(FlowType.PRODUCT_FLOW);
		}
		setFlow(flow, ref);
		target.root.process.getExchanges().add(ref);
		target.root.process.setQuantitativeReference(ref);
	}
}
