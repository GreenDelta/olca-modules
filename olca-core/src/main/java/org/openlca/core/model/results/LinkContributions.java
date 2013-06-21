package org.openlca.core.model.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.math.ProductIndex;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.util.Doubles;

/**
 * Calculates the contributions of process products that are inputs in other
 * processes. If a process product is input of only one process it's
 * contribution to this process is 100%, whereas this contribution is split into
 * the respective shares if this product goes into multiple processes. For
 * example, if we have a process product p that goes with 5 kg into process x
 * and with 3 kg into process y the contribution share of product p to process x
 * is 5/8 = 62.5% and to process y 3/8 = 37.5%.
 */
public class LinkContributions {

	private HashMap<String, Double> shares = new HashMap<>();

	private LinkContributions() {
	}

	public static LinkContributions calculate(ProductSystem system,
			ProductIndex index, double[] scalingFactors) {
		LinkContributions contributions = new LinkContributions();
		if (system == null)
			return contributions;
		contributions.calculateShares(system, index, scalingFactors);
		return contributions;
	}

	/**
	 * Get the contribution share of the outgoing process product (provider) to
	 * the product input (recipient) of the given link and the calculated
	 * product system. The returned share is a value between 0 and 1.
	 */
	public double getShare(ProcessLink link) {
		if (link == null || link.getId() == null)
			return 0;
		Double share = shares.get(link.getId());
		return share == null ? 0 : share;
	}

	private void calculateShares(ProductSystem system, ProductIndex index,
			double[] scalingFactors) {
		List<List<ProcessLink>> groups = groupLinks(system);
		Map<Process, Double> factors = calcScalingFactors(system, index,
				scalingFactors);
		for (List<ProcessLink> group : groups) {
			if (group == null || group.size() == 0)
				continue;
			if (group.size() == 1)
				shares.put(group.get(0).getId(), 1.0);
			else
				putShares(group, factors);
		}
	}

	/**
	 * Calculate the total scaling factors of the processes in the product
	 * system (total means for all output products of this process).
	 */
	private Map<Process, Double> calcScalingFactors(ProductSystem system,
			ProductIndex index, double[] scalingFactors) {
		Map<Process, Double> factors = new HashMap<>();
		for (Process p : system.getProcesses()) {
			double factor = calcScalingFactor(p, index, scalingFactors);
			factors.put(p, factor);
		}
		return factors;
	}

	/**
	 * Calculate the scaling factor of the given process. This is the sum of all
	 * scaling factors of the products provided by this process.
	 */
	private double calcScalingFactor(Process process, ProductIndex index,
			double[] scalingFactors) {
		double factor = 0;
		for (Exchange exchange : process.getExchanges()) {
			if (exchange.isInput()
					|| exchange.getFlow() == null
					|| exchange.getFlow().getFlowType() == FlowType.ELEMENTARY_FLOW)
				continue;
			int idx = index.getIndex(process, exchange);
			if (idx < 0 || idx >= scalingFactors.length)
				continue;
			factor += scalingFactors[idx];
		}
		return factor;
	}

	/** Calculate the shares for the input amounts of the given links. */
	private void putShares(List<ProcessLink> group,
			Map<Process, Double> scalingFactors) {
		double[] inputAmounts = new double[group.size()];
		for (int i = 0; i < group.size(); i++) {
			ProcessLink link = group.get(i);
			double rawInput = link.getRecipientInput().getConvertedResult();
			Double factor = scalingFactors.get(link.getRecipientProcess());
			double scaledAmount = factor == null ? 0 : factor * rawInput;
			inputAmounts[i] = scaledAmount;
		}
		double sum = Doubles.sum(inputAmounts);
		if (sum == 0)
			return;
		for (int i = 0; i < group.size(); i++) {
			double share = inputAmounts[i] / sum;
			shares.put(group.get(i).getId(), share);
		}
	}

	/** Groups the links with equal output products. */
	private List<List<ProcessLink>> groupLinks(ProductSystem system) {
		Map<String, List<ProcessLink>> groups = new HashMap<>();
		for (ProcessLink link : system.getProcessLinks()) {
			if (!valid(link))
				continue;
			String key = link.getProviderProcess().getId()
					+ link.getProviderOutput().getId();
			List<ProcessLink> links = groups.get(key);
			if (links == null) {
				links = new ArrayList<>();
				groups.put(key, links);
			}
			links.add(link);
		}
		return new ArrayList<>(groups.values());
	}

	private boolean valid(ProcessLink link) {
		return notNull(link, link.getProviderOutput(),
				link.getProviderProcess(), link.getRecipientInput(),
				link.getRecipientProcess());
	}

	private boolean notNull(Object... vals) {
		for (Object val : vals) {
			if (val == null)
				return false;
		}
		return true;
	}

}
