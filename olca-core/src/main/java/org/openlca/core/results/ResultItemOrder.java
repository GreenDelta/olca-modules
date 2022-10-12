package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

/**
 * Stores the index elements of one or more results in a specific order. A
 * result index maps elements like processes, flows, or impact categories to
 * rows and columns of result matrices. An instance of this class provides a
 * view on these elements. It holds its own lists which are directly returned
 * from the respective methods. You can sort  these lists via {@code List.sort}
 * or {@code Collections.sort} and they keep sorted in the view. Note, that an
 * instance of this class is not thread-safe.
 */
public class ResultItemOrder {

	private final List<? extends IResult> results;
	private final boolean hasEnviFlows;
	private final boolean hasImpacts;
	private final boolean hasCosts;

	private List<EnviFlow> enviFlows;
	private List<ImpactDescriptor> impacts;
	private List<TechFlow> techFlows;
	private List<RootDescriptor> providers;

	private ResultItemOrder(List<? extends IResult> results) {
		this.results = results;
		this.hasEnviFlows = results.stream()
			.anyMatch(IResult::hasEnviFlows);
		this.hasImpacts = results.stream()
			.anyMatch(IResult::hasImpacts);
		this.hasCosts = results.stream()
			.anyMatch(IResult::hasCosts);
	}

	public static ResultItemOrder of(IResult result) {
		var list = Collections.singletonList(result);
		return new ResultItemOrder(list);
	}

	public static ResultItemOrder of(ProjectResult result) {
		var list = result.getVariants()
			.stream()
			.map(result::getResult)
			.collect(Collectors.toList());
		return new ResultItemOrder(list);
	}

	public boolean hasEnviFlows() {
		return hasEnviFlows;
	}

	public boolean hasImpacts() {
		return hasImpacts;
	}

	public boolean hasCosts() {
		return hasCosts;
	}

	public List<TechFlow> techFlows() {
		if (techFlows != null)
			return techFlows;
		var set = new HashSet<TechFlow>();
		for (var result : results) {
			var index = result.techIndex();
			if (index == null)
				continue;
			set.addAll(index.content());
		}
		techFlows = new ArrayList<>(set);
		return techFlows;
	}

	public List<RootDescriptor> providers() {
		if (providers != null)
			return providers;
		var set = new HashSet<RootDescriptor>();
		for (var techFlow : techFlows()) {
			set.add(techFlow.provider());
		}
		providers = new ArrayList<>(set);
		return providers;
	}

	public List<EnviFlow> enviFlows() {
		if (enviFlows != null)
			return enviFlows;
		if (!hasEnviFlows) {
			enviFlows = Collections.emptyList();
			return enviFlows;
		}
		var set = new HashSet<EnviFlow>();
		for (var result : results) {
			if (!result.hasEnviFlows())
				continue;
			var index = result.enviIndex();
			if (index == null)
				continue;
			for (var flow : index) {
				set.add(flow);
			}
		}
		enviFlows = new ArrayList<>(set);
		return enviFlows;
	}

	public List<ImpactDescriptor> impacts() {
		if (impacts != null)
			return impacts;
		if (!hasImpacts) {
			impacts = Collections.emptyList();
			return impacts;
		}
		var set = new HashSet<ImpactDescriptor>();
		for (var result : results) {
			if (!result.hasImpacts())
				continue;
			var index = result.impactIndex();
			if (index == null)
				continue;
			for (var impact : index) {
				set.add(impact);
			}
		}
		impacts = new ArrayList<>(set);
		return impacts;
	}
}
