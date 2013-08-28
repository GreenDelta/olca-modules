package org.openlca.core.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.openlca.core.math.IMatrix;
import org.openlca.core.matrices.LongPair;
import org.openlca.core.matrices.ProductIndex;
import org.openlca.core.model.ProcessLink;
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

	/**
	 * Maps product links to shares: output-process-product -> input-process ->
	 * share.
	 */
	private HashMap<LongPair, HashMap<Long, Double>> shares = new HashMap<>();

	private LinkContributions() {
	}

	public static LinkContributions calculate(IMatrix technologyMatrix,
			ProductIndex index, double[] scalingFactors) {
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
		LongPair output = new LongPair(link.getProviderProcessId(),
				link.getFlowId());
		HashMap<Long, Double> map = shares.get(output);
		if (map == null)
			return 0;
		Double share = map.get(link.getRecipientProcessId());
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

	private void calculateShares(IMatrix matrix, ProductIndex index,
			double[] scalingFactors) {
		Map<LongPair, List<LongPair>> groupedLinks = groupLinks(index);
		for (LongPair output : groupedLinks.keySet()) {
			List<LongPair> inputs = groupedLinks.get(output);
			if (inputs == null || inputs.isEmpty())
				continue;
			if (inputs.size() == 1) {
				putShare(output, inputs.get(0), 1);
				continue;
			}
			double[] inputAmounts = new double[inputs.size()];
			for (int i = 0; i < inputs.size(); i++) {
				LongPair input = inputs.get(i);
				List<LongPair> columns = index.getProducts(input.getFirst());
				for (LongPair column : columns) {
					double val = getValue(matrix, output, column, index,
							scalingFactors);
					inputAmounts[i] += val;
				}
			}
			double sum = Doubles.sum(inputAmounts);
			if (sum == 0)
				continue;
			for (int i = 0; i < inputs.size(); i++) {
				double share = inputAmounts[i] / sum;
				putShare(output, inputs.get(i), share);
			}
		}
	}

	private double getValue(IMatrix matrix, LongPair row, LongPair col,
			ProductIndex idx, double[] scalingFactors) {
		int _row = idx.getIndex(row);
		int _col = idx.getIndex(col);
		if (_row < 0 || _col < 0)
			return 0;
		if (_row >= matrix.getRowDimension()
				|| _col >= matrix.getColumnDimension())
			return 0;
		double val = matrix.getEntry(_row, _col);
		double s = scalingFactors[_col];
		return val * s;
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

	/** Groups the links with equal output products. */
	private Map<LongPair, List<LongPair>> groupLinks(ProductIndex index) {
		List<Pair<LongPair, LongPair>> links = new ArrayList<>();
		for (LongPair input : index.getLinkedInputs()) {
			LongPair output = index.getLinkedOutput(input);
			if (output == null)
				continue;
			links.add(new Pair<>(output, input));
		}
		Map<LongPair, List<LongPair>> groups = new HashMap<>();
		for (Pair<LongPair, LongPair> link : links) {
			LongPair output = link.getFirst();
			LongPair input = link.getSecond();
			List<LongPair> inputs = groups.get(output);
			if (inputs == null) {
				inputs = new ArrayList<>();
				groups.put(output, inputs);
			}
			inputs.add(input);
		}
		return groups;
	}

}
