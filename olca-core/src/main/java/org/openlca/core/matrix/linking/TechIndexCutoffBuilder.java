package org.openlca.core.matrix.linking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TechIndexCutoffBuilder implements ITechIndexBuilder {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final MatrixCache cache;
	private final ProviderSearch providers;
	private final ProductSystem system;
	private final double cutoff;

	public TechIndexCutoffBuilder(MatrixCache cache, ProductSystem system,
		LinkingConfig config) {
		this.cache = cache;
		this.cutoff = config.cutoff().isPresent()
			? config.cutoff().getAsDouble()
			: 0;
		this.system = system;
		this.providers = new ProviderSearch(cache.getProcessTable(), config);
	}

	@Override
	public TechIndex build(TechFlow refProduct) {
		log.trace("build product index for {} with cutoff={}", refProduct, cutoff);
		TechIndex index = new TechIndex(refProduct);
		addSystemLinks(index);
		Graph g = new Graph(refProduct, 1.0); // TODO: demand value could be != 1
		while (!g.next.isEmpty())
			g.handleNext();
		fillIndex(g, index);
		log.trace("created the index with {} products", index.size());
		return index;
	}

	private void addSystemLinks(TechIndex index) {
		if (system == null)
			return;
		for (var link : system.processLinks) {
			var provider = providers.getProvider(link.providerId, link.flowId);
			if (provider == null)
				continue;
			var exchange = LongPair.of(link.processId, link.exchangeId);
			index.putLink(exchange, provider);
		}
	}

	private void fillIndex(Graph g, TechIndex index) {
		for (Node node : g.nodes.values()) {
			if (node.state != NodeState.FOLLOWED)
				continue;
			for (Link link : node.links) {
				if (Math.abs(link.demand) < cutoff)
					continue;
				Node provider = link.provider;
				LongPair exchange = LongPair.of(
					node.flow.providerId(), link.exchangeId);
				index.putLink(exchange, provider.flow);
			}
		}
	}

	private class Graph {

		Node root;

		List<Node> next = new ArrayList<>();
		HashMap<TechFlow, Node> nodes = new HashMap<>();

		Graph(TechFlow refProduct, double demand) {
			this.root = new Node(refProduct, demand);
			root.flow = refProduct;
			root.state = NodeState.WAITING;
			nodes.put(refProduct, root);
			next.add(root);
		}

		void handleNext() {

			log.trace("handle next layer with {} product nodes", next.size());

			// to minimize the re-scale calls we first sort the nodes by their
			// demands (see the compareTo method in Node)
			Collections.sort(next);

			Map<Long, List<CalcExchange>> nextExchanges = fetchNextExchanges();
			List<Node> nextLayer = new ArrayList<>();
			for (Node n : next) {
				n.state = NodeState.PROGRESS;
				List<CalcExchange> exchanges = nextExchanges.get(
					n.flow.providerId());
				CalcExchange provider = getProviderFlow(n, exchanges);
				if (provider == null)
					continue;
				n.amount = amount(provider);
				n.scalingFactor = n.demand / n.amount;
				followLinks(n, exchanges, nextLayer);
				n.state = NodeState.FOLLOWED;
			}
			next.clear();
			next.addAll(nextLayer);
		}

		private void followLinks(Node node, List<CalcExchange> exchanges,
			List<Node> nextLayer) {
			for (CalcExchange linkExchange : providers
				.getLinkCandidates(exchanges)) {
				TechFlow provider = providers.find(linkExchange);
				if (provider == null)
					continue;
				double amount = amount(linkExchange);
				double demand = node.scalingFactor * amount;
				Node providerNode = nodes.get(provider);
				if (providerNode != null)
					checkSubGraph(demand, providerNode, nextLayer, false);
				else {
					providerNode = createNode(demand, provider, nextLayer);
				}
				Link link = new Link(providerNode, linkExchange.exchangeId,
					amount, demand);
				node.links.add(link);
			}
		}

		private Node createNode(double demand, TechFlow product,
			List<Node> nextLayer) {
			Node node = new Node(product, demand);
			nodes.put(product, node);
			if (Math.abs(demand) < cutoff)
				node.state = NodeState.EXCLUDED;
			else {
				node.state = NodeState.WAITING;
				nextLayer.add(node);
			}
			return node;
		}

		private void checkSubGraph(double demand, Node provider,
			List<Node> nextLayer, boolean recursion) {
			if (Math.abs(demand) < cutoff
				|| Math.abs(demand) <= Math.abs(provider.demand))
				return;
			provider.demand = demand;
			if (provider.state == NodeState.EXCLUDED) {
				provider.state = NodeState.WAITING;
				nextLayer.add(provider);
			}
			if (provider.state == NodeState.FOLLOWED) {
				provider.state = NodeState.RESCALED;
				rescaleSubGraph(provider, nextLayer);
				if (!recursion) {
					log.trace("rescaled a sub graph");
					unsetScaleState();
				}
			}
		}

		private void rescaleSubGraph(Node start, List<Node> nextLayer) {
			start.scalingFactor = start.demand / start.amount;
			for (Link link : start.links) {
				link.demand = link.amount * start.scalingFactor;
				Node provider = link.provider;
				checkSubGraph(link.demand, provider, nextLayer, true);
			}
		}

		private void unsetScaleState() {
			for (Node node : nodes.values()) {
				if (node.state == NodeState.RESCALED) {
					node.state = NodeState.FOLLOWED;
				}
			}
		}

		/**
		 * Get the provider flow that matches the given node from the given
		 * exchange list.
		 */
		private CalcExchange getProviderFlow(Node node,
			List<CalcExchange> all) {
			for (CalcExchange e : all) {
				if (node.flow.flowId() != e.flowId)
					continue;
				if (e.flowType == FlowType.PRODUCT_FLOW && !e.isInput)
					return e;
				if (e.flowType == FlowType.WASTE_FLOW && e.isInput)
					return e;
			}
			return null;
		}

		private double amount(CalcExchange e) {
			if (e == null)
				return 0;
			return e.amount * e.conversionFactor;
		}

		private Map<Long, List<CalcExchange>> fetchNextExchanges() {
			if (next.isEmpty())
				return Collections.emptyMap();
			Set<Long> processIds = new HashSet<>();
			for (Node node : next)
				processIds.add(node.flow.providerId());
			try {
				return cache.getExchangeCache().getAll(processIds);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("failed to load exchanges from cache", e);
				return Collections.emptyMap();
			}
		}
	}
}
