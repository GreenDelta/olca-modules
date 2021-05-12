package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

/**
 * A view on the elements in the indices of result. A result index maps elements
 * like processes, flows, or impact categories to the result matrices. An
 * instance of this class provides a view on these elements. It holds its own
 * lists which are directly returned in the respective methods. You can sort
 * these lists via {@code List.sort} or {@code Collections.sort} and they keep
 * sorted in the view, but except of this you should not modify the returned
 * lists or their elements. Also note, that an instance of this class is not
 * thread-safe.
 */
public class ResultIndexView {

	private final List<? extends IResult> results;
	private final boolean hasFlows;
	private final boolean hasImpacts;
	private final boolean hasCosts;

	private List<EnviFlow> flows;
	private List<ImpactDescriptor> impacts;
	private List<TechFlow> products;
	private List<CategorizedDescriptor> processes;

	private ResultIndexView(List<? extends IResult> results) {
		this.results = results;
		this.hasFlows = results.stream()
			.anyMatch(IResult::hasFlowResults);
		this.hasImpacts = results.stream()
			.anyMatch(IResult::hasImpactResults);
		this.hasCosts = results.stream()
			.anyMatch(IResult::hasCostResults);
	}

	public static ResultIndexView of(IResult result) {
		var list = Collections.singletonList(result);
		return new ResultIndexView(list);
	}

	public static ResultIndexView of(ProjectResult result) {
		var list = result.getVariants()
			.stream()
			.map(result::getResult)
			.collect(Collectors.toList());
		return new ResultIndexView(list);
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

	public List<CategorizedDescriptor> processes() {
		if (processes != null)
			return processes;
		var set = new HashSet<CategorizedDescriptor>();
		for (var result : results) {
			var index = result.techIndex();
			if (index == null)
				continue;
			for (var product : index) {
				set.add(product.process());
			}
		}
		return processes;
	}

	public List<EnviFlow> flows() {
		if (flows != null)
			return flows;
		if (!hasFlows) {
			flows = Collections.emptyList();
			return flows;
		}
		var set = new HashSet<EnviFlow>();
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
