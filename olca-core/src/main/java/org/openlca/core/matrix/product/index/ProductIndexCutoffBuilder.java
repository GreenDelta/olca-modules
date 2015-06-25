package org.openlca.core.matrix.product.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductIndexCutoffBuilder implements IProductIndexBuilder {

	private Logger log = LoggerFactory.getLogger(getClass());

	private MatrixCache cache;
	private ProviderSearch providerSearch;
	private double cutoff;

	public ProductIndexCutoffBuilder(MatrixCache cache, double cutoff) {
		this.cache = cache;
		this.cutoff = cutoff;
		this.providerSearch = new ProviderSearch(cache.getProcessTable(),
				ProcessType.LCI_RESULT);
	}

	@Override
	public void setPreferredType(ProcessType type) {
		providerSearch.setPreferredType(type);
	}

	@Override
	public ProductIndex build(LongPair refProduct) {
		return build(refProduct, 1.0);
	}

	@Override
	public ProductIndex build(LongPair refProduct, double demand) {
		log.trace("build product index for {} with cutoff=", refProduct, cutoff);
		ProductIndex index = new ProductIndex(refProduct);
		index.setDemand(demand);
		Graph g = new Graph(refProduct, demand);
		while (!g.next.isEmpty())
			g.handleNext();
		fillIndex(g, index);
		log.trace("created the index with {} products", index.size());
		return index;
	}

	private void fillIndex(Graph g, ProductIndex index) {
		for(Node node : g.nodes.values()) {
			if(node.state != NodeState.FOLLOWED)
				continue;
			for(Link link : node.inputLinks) {
				// TODO: check if we should annotate the links with 'followed'
				// currently we include all links to a followed node (also 
				// the links with a cutoff under the demand value)
				Node provider = link.provider;
				if(provider.state != NodeState.FOLLOWED)
					continue;
				LongPair input = LongPair.of(node.product.getFirst(), 
						provider.product.getSecond());
				index.putLink(input, provider.product);
			}
		}
	}

	private class Graph {

		Node root;

		List<Node> next = new ArrayList<>();
		HashMap<LongPair, Node> nodes = new HashMap<>();

		Graph(LongPair refProduct, double demand) {
			this.root = new Node(refProduct, demand);
			root.product = refProduct;
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
			for (Node node : next) {
				node.state = NodeState.PROGRESS;
				List<CalcExchange> exchanges = nextExchanges.get(
						node.product.getFirst());
				CalcExchange output = getOutput(node, exchanges);
				if (output == null)
					continue;
				node.outputAmount = amount(output);
				node.scalingFactor = node.demand / node.outputAmount;
				followInputs(node, exchanges, nextLayer);
				node.state = NodeState.FOLLOWED;
			}
			next.clear();
			next.addAll(nextLayer);
		}

		private void followInputs(Node node, List<CalcExchange> exchanges,
				List<Node> nextLayer) {
			for (CalcExchange input : getInputs(node, exchanges)) {
				LongPair inputProduct = providerSearch.find(input);
				if (inputProduct == null)
					continue;
				double inputAmount = amount(input);
				double inputDemand = node.scalingFactor * inputAmount;
				Node providerNode = nodes.get(inputProduct);
				if (providerNode != null)
					checkSubGraph(inputDemand, providerNode, nextLayer, false);
				else {
					providerNode = createNode(inputDemand, inputProduct,
							nextLayer);
				}
				node.addLink(providerNode, inputAmount);
			}
		}

		private Node createNode(double inputDemand, LongPair product,
				List<Node> nextLayer) {
			Node node = new Node(product, inputDemand);
			nodes.put(product, node);
			if (inputDemand < cutoff)
				node.state = NodeState.EXCLUDED;
			else {
				node.state = NodeState.WAITING;
				nextLayer.add(node);
			}
			return node;
		}

		private void checkSubGraph(double demand, Node provider,
				List<Node> nextLayer, boolean recursion) {
			if (demand <= provider.demand || demand < cutoff)
				return;
			provider.demand = demand;
			if (provider.state == NodeState.EXCLUDED) {
				provider.state = NodeState.WAITING;
				nextLayer.add(provider);
			}
			if (provider.state == NodeState.FOLLOWED) {
				provider.state = NodeState.RESCALED;
				rescaleSubGraph(provider, nextLayer);
				if(!recursion) {
					log.trace("rescaled a sub graph");
					unsetScaleState();
				}
			}
		}

		private void rescaleSubGraph(Node start, List<Node> nextLayer) {
			start.scalingFactor = start.demand / start.outputAmount;
			for (Link link : start.inputLinks) {
				double inputDemand = link.inputAmount * start.scalingFactor;
				Node provider = link.provider;
				checkSubGraph(inputDemand, provider, nextLayer, true);
			}
		}
		
		private void unsetScaleState() {
			for(Node node : nodes.values()) {
				if(node.state == NodeState.RESCALED) {
					node.state = NodeState.FOLLOWED;
				}
			}
		}

		private CalcExchange getOutput(Node node, List<CalcExchange> all) {
			for (CalcExchange e : all) {
				if (e.isInput()
						|| e.getFlowType() != FlowType.PRODUCT_FLOW
						|| e.getFlowId() != node.product.getSecond())
					continue;
				return e;
			}
			return null;
		}

		private List<CalcExchange> getInputs(Node node, List<CalcExchange> all) {
			List<CalcExchange> inputs = new ArrayList<>();
			for (CalcExchange e : all) {
				if (e.isInput() && e.getFlowType() == FlowType.PRODUCT_FLOW)
					inputs.add(e);
			}
			return inputs;
		}

		private double amount(CalcExchange e) {
			if (e == null)
				return 0;
			return e.getAmount() * e.getConversionFactor();
		}

		private Map<Long, List<CalcExchange>> fetchNextExchanges() {
			if (next.isEmpty())
				return Collections.emptyMap();
			Set<Long> processIds = new HashSet<>();
			for (Node node : next)
				processIds.add(node.product.getFirst());
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
