package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.model.descriptors.BaseDescriptor;

/**
 * Maps the upstream results of the product system graph to a tree where the
 * root is the reference process of the product system.
 */
public class UpstreamTree {

	public final UpstreamNode root;

	/**
	 * An optional reference to a model (e.g. flow or LCIA category) to which
	 * the upstream tree is related.
	 */
	public final BaseDescriptor ref;

	private final double[] intensityRow;
	private final FullResult r;

	public UpstreamTree(FullResult r, double[] u) {
		this(null, r, u);
	}

	public UpstreamTree(BaseDescriptor ref, FullResult r, double[] u) {
		this.ref = ref;
		this.r = r;
		root = new UpstreamNode();
		root.scaling = 1.0;
		root.provider = r.techIndex.getRefFlow();
		root.index = r.techIndex.getIndex(root.provider);
		root.result = u[root.index];
		intensityRow = new double[u.length];
		if (r.loopFactor == 1) {
			for (int i = 0; i < intensityRow.length; i++) {
				intensityRow[i] = u[i] / r.totalRequirements[i];
			}
		} else {
			for (int i = 0; i < intensityRow.length; i++) {
				intensityRow[i] = u[i]
						/ (r.totalRequirements[i] * r.loopFactor);
			}
		}
	}

	public List<UpstreamNode> childs(UpstreamNode parent) {
		if (parent.childs != null)
			return parent.childs;
		parent.childs = new ArrayList<>();
		if (parent.scaling == 0)
			return parent.childs;
		for (int row = 0; row < r.techMatrix.rows(); row++) {
			if (row == parent.index)
				continue;
			double val = r.techMatrix.get(row, parent.index);
			if (val == 0)
				continue;
			val *= parent.scaling;
			UpstreamNode child = new UpstreamNode();
			double refVal = r.techMatrix.get(row, row);
			child.scaling = -val / refVal;
			child.index = row;
			child.provider = r.techIndex.getProviderAt(row);
			child.result = intensityRow[row] * refVal * child.scaling;
			parent.childs.add(child);
		}
		Collections.sort(parent.childs,
				(n1, n2) -> Double.compare(n2.result, n1.result));
		return parent.childs;
	}

}
