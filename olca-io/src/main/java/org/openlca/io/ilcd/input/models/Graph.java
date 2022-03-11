package org.openlca.io.ilcd.input.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.ilcd.models.Group;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.models.Technology;
import org.openlca.ilcd.util.Models;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Graph {

	Node root;

	// private final List<> nodes = new ArrayList<>();
	private final Map<Integer, Node> nodes = new HashMap<>();

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

		var groups = new HashMap<Integer, Group>();
		for (var group : tech.groups) {
			groups.put(group.id, group);
		}

		for (var pi : tech.processes) {
			if (pi.process == null || pi.process.uuid == null) {
				log.warn("Invalid process reference node={}", pi.id);
				continue;
			}
			var process = db.get(Process.class, pi.process.uuid);
			if (process == null) {
				log.warn("Could not find process {}; skip node {}",
					pi.process.uuid, pi.id);
				continue;
			}
			var node = Node.init(pi, process);
			if (!pi.groupRefs.isEmpty()) {
				var gr = pi.groupRefs.get(0);
				node.group = groups.get(gr.groupID);
			}
			g.putNode(node);
		}
		buildLinks(g, model);

		var qRef = model.info.quantitativeReference;
		if (qRef != null && qRef.refProcess != null) {
			g.root = g.getNode(qRef.refProcess);
		}

		return g;
	}

	private static void buildLinks(Graph g, Model model) {
		// in the eILCD format the process links are modeled in downstream
		// direction. However, in some implementations downstream and output
		// means upstream and input (especially for waste flows). We try to
		// also support such things here...
		for (var pi : Models.getProcesses(model)) {
			var refNode = g.getNode(pi.id);
			if (refNode == null)
				continue;
			for (var con : pi.connections) {
				Exchange output = refNode.findOutput(con.outputFlow);
				Exchange input = null;
				if (output == null) {
					input = refNode.findInput(con.outputFlow);
					if (input == null)
						continue;
				}
				for (var dLink : con.downstreamLinks) {
					Node provider;
					Node recipient;
					if (output != null) {
						provider = refNode;
						recipient = g.getNode(dLink.process);
						if (recipient == null)
							continue;
						input = recipient.findInput(dLink.inputFlow);
					} else {
						recipient = refNode;
						provider = g.getNode(dLink.process);
						if (provider == null)
							continue;
						output = provider.findOutput(dLink.inputFlow);
					}
					if (input == null || output == null)
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
		nodes.put(n.id, n);
	}

	void putLink(Link link) {
		link.id = links.size();
		links.add(link);
		int recipientID = link.recipient.id;
		var inList = inLinks.computeIfAbsent(recipientID, k -> new ArrayList<>());
		inList.add(link.id);
		int providerID = link.provider.id;
		var outList = outLinks.computeIfAbsent(providerID, k -> new ArrayList<>());
		outList.add(link.id);
	}

	/**
	 * Returns the node for the ID as used in the eILCD model.
	 */
	Node getNode(int modelID) {
		return nodes.get(modelID);
	}

	void eachNode(Consumer<Node> fn) {
		if (fn == null)
			return;
		for (var node : nodes.values()) {
			fn.accept(node);
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
