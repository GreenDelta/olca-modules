package org.openlca.io.ilcd.input.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.ilcd.models.Connection;
import org.openlca.ilcd.models.DownstreamLink;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.models.ProcessInstance;
import org.openlca.ilcd.models.QuantitativeReference;
import org.openlca.ilcd.models.Technology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Graph {

	Node root;

	private final List<Node> nodes = new ArrayList<>();
	private final Map<Integer, Integer> nodeIndex = new HashMap<>();

	private final List<Link> links = new ArrayList<>();
	private final Map<Integer, List<Integer>> inLinks = new HashMap<>();
	private final Map<Integer, List<Integer>> outLinks = new HashMap<>();

	/** Creates an empty graph. */
	Graph() {
	}

	/**
	 * Creates a graph that synchronizes the given eILCD model with the given
	 * database.
	 */
	static Graph build(Model model, IDatabase db) {
		Graph g = new Graph();
		Logger log = LoggerFactory.getLogger(Graph.class);
		if (model == null || model.info == null || db == null) {
			log.warn("Invalid constraints; return empty index");
			return g;
		}
		Technology tech = model.info.technology;
		if (tech == null) {
			log.warn("No processes in model; return empty index");
			return g;
		}
		ProcessDao dao = new ProcessDao(db);
		for (ProcessInstance pi : tech.processes) {
			if (pi.process == null || pi.process.uuid == null) {
				log.warn("Invalid process reference node={}", pi.id);
				continue;
			}
			String refID = pi.process.uuid;
			Process process = dao.getForRefId(refID);
			if (process == null) {
				log.warn("Could not find process {}; skip node {}", refID, pi.id);
				continue;
			}
			Node n = Node.init(pi, process);
			g.putNode(n);
		}
		buildLinks(g, model);

		QuantitativeReference qRef = model.info.quantitativeReference;
		if (qRef != null && qRef.refProcess != null) {
			g.root = g.getNode(qRef.refProcess);
		}

		return g;
	}

	private static void buildLinks(Graph g, Model model) {
		for (ProcessInstance pi : model.info.technology.processes) {
			Node provider = g.getNode(pi.id);
			if (provider == null)
				continue;
			for (Connection con : pi.connections) {
				Exchange output = provider.findOutput(con.outputFlow);
				if (output == null)
					continue;
				for (DownstreamLink dlink : con.downstreamLinks) {
					Node recipient = g.getNode(dlink.process);
					if (recipient == null)
						continue;
					Exchange input = recipient.findInput(dlink.inputFlow);
					if (input == null)
						continue;
					Link link = new Link();
					link.provider = provider;
					link.output = output;
					link.recipient = recipient;
					link.input = input;
					g.putLink(link);
				}
			}
		}
	}

	void putNode(Node n) {
		int i = nodes.size();
		nodes.add(n);
		nodeIndex.put(n.modelID, i);
	}

	void putLink(Link link) {
		link.id = links.size();
		links.add(link);
		int recipientID = link.recipient.modelID;
		List<Integer> inList = inLinks.get(recipientID);
		if (inList == null) {
			inList = new ArrayList<>();
			inLinks.put(recipientID, inList);
		}
		inList.add(link.id);
		int providerID = link.provider.modelID;
		List<Integer> outList = outLinks.get(providerID);
		if (outList == null) {
			outList = new ArrayList<>();
			outLinks.put(providerID, outList);
		}
		outList.add(link.id);
	}

	/**
	 * Returns the node for the ID as used in the eILCD model.
	 */
	Node getNode(int modelID) {
		Integer i = nodeIndex.get(modelID);
		if (i == null)
			return null;
		return nodes.get(i);
	}

	void eachNode(Consumer<Node> fn) {
		if (fn == null)
			return;
		for (Node n : nodes) {
			fn.accept(n);
		}
	}

	void eachLink(Consumer<Link> fn) {
		if (fn == null)
			return;
		for (Link link : links) {
			fn.accept(link);
		}
	}

	List<Link> getInputLinks(int nodeID) {
		List<Integer> inList = inLinks.get(nodeID);
		if (inList == null)
			return Collections.emptyList();
		List<Link> list = new ArrayList<>();
		for (int i : inList) {
			list.add(links.get(i));
		}
		return list;
	}

	List<Link> getOutputLinks(int nodeID) {
		List<Integer> outList = outLinks.get(nodeID);
		if (outList == null)
			return Collections.emptyList();
		List<Link> list = new ArrayList<>();
		for (int i : outList) {
			list.add(links.get(i));
		}
		return list;
	}
}
