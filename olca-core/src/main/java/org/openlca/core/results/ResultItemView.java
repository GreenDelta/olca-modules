package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.core.matrix.index.IndexFlow;
import org.openlca.core.matrix.index.ProcessProduct;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

/**
 * A view on the elements (processes, flows, impact categories) of a result. The
 * view holds its own lists which are directly returned in the respective
 * methods. You can sort these lists via {@code List.sort} or
 * {@code Collections.sort} and they keep sorted in the view but except of this
 * you should not modify the returned lists or their elements. Also note, that
 * an instance of this class is not thread-safe.
 */
public class ResultItemView {

	private final List<? extends IResult> results;
	private final boolean hasFlows;
	private final boolean hasImpacts;
	private final boolean hasCosts;

	private List<IndexFlow> flows;
	private List<ImpactDescriptor> impacts;
	private List<ProcessProduct> products;
	private List<CategorizedDescriptor> processes;

	private ResultItemView(List<? extends IResult> results) {
		this.results = results;
		this.hasFlows = results.stream()
			.anyMatch(IResult::hasFlowResults);
		this.hasImpacts = results.stream()
			.anyMatch(IResult::hasImpactResults);
		this.hasCosts = results.stream()
			.anyMatch(IResult::hasCostResults);
	}

	public static ResultItemView of(IResult result) {
		var list = Collections.singletonList(result);
		return new ResultItemView(list);
	}

	public static ResultItemView of(ProjectResult result) {
		var list = result.getVariants()
			.stream()
			.map(result::getResult)
			.collect(Collectors.toList());
		return new ResultItemView(list);
	}

	public boolean hasFlows() {
		return hasFlows;
	}

	public boolean hasImpacts() {
		return hasImpacts;
	}

	public boolean hasCosts() {
		return hasCosts;
	}

	public List<IndexFlow> getFlows() {
		if (flows != null)
			return flows;
		if (!hasFlows) {
			flows = Collections.emptyList();
			return flows;
		}
		var set = new HashSet<IndexFlow>();
		for (var result : results) {
			if (!result.hasFlowResults())
				continue;
			var index = result.flowIndex();
			if (index == null)
				continue;
			for (var flow : index) {
				set.add(flow);
			}
		}
		flows = new ArrayList<>(set);
		return flows;
	}
}
