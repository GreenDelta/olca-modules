package org.openlca.core.results;

import java.util.HashMap;

import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.model.ProcessLink;

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

	/**
	 * Maps product links to shares: output-process-product -> input-process ->
	 * share.
	 */
	private HashMap<LongPair, HashMap<Long, Double>> shares = new HashMap<>();

	private LinkContributions() {
	}

	public static LinkContributions calculate(IMatrix technologyMatrix,
			TechIndex index, double[] scalingFactors) {
		LinkContributions contributions = new LinkContributions();
		contributions.calculateShares(technologyMatrix, index, scalingFactors);
		return contributions;
	}

	/**
	 * Get the contribution share of the outgoing process product (provider) to
	 * the product input (recipient) of the given link and the calculated
	 * product system. The returned share is a value between 0 and 1.
	 */
	public double getShare(ProcessLink link) {
		if (link == null)
			return 0;
		LongPair output = new LongPair(link.providerId, link.flowId);
		HashMap<Long, Double> map = shares.get(output);
		if (map == null)
			return 0;
		Double share = map.get(link.processId);
		return share == null ? 0 : share;
	}

	double getShare(LongPair provider, LongPair recipient) {
		if (provider == null || provider == null)
			return 0;
		HashMap<Long, Double> map = shares.get(provider);
		if (map == null)
			return 0;
		Double share = map.get(recipient.getFirst());
		return share == null ? 0 : share;
	}

	private void calculateShares(IMatrix matrix, TechIndex index,
			double[] scalingFactors) {
		for (int i = 0; i < index.size(); i++) {
			LongPair outProduct = index.getProviderAt(i);
			double outVal = scalingFactors[i] * matrix.get(i, i);
			if (outVal == 0)
				continue;
			for (int k = 0; k < index.size(); k++) {
				if (k == i)
					continue;
				double rawInVal = matrix.get(i, k);
				if (rawInVal == 0)
					continue;
				double contr = -(scalingFactors[k] * rawInVal) / outVal;
				LongPair inProduct = index.getProviderAt(k);
				putShare(outProduct, inProduct, contr);
			}
		}
	}

	private void putShare(LongPair output, LongPair input, double share) {
		HashMap<Long, Double> map = shares.get(output);
		if (map == null) {
			map = new HashMap<>();
			shares.put(output, map);
		}
		Long inputProcess = input.getFirst();
		Double oldShare = map.get(inputProcess);
		if (oldShare == null)
			map.put(inputProcess, share);
		else {
			double newShare = oldShare + share;
			if (newShare < 1)
				map.put(inputProcess, newShare);
			else
				map.put(inputProcess, 1d); // assert v <= 1
		}
	}

}
