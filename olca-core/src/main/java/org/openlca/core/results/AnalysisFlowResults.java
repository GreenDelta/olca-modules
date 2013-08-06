package org.openlca.core.results;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openlca.core.database.Cache;
import org.openlca.core.indices.FlowIndex;
import org.openlca.core.indices.LongPair;
import org.openlca.core.indices.ProductIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * A helper class that generates instances of {@link AnalysisFlowResult} from an
 * analysis result.
 */
public final class AnalysisFlowResults {

	private AnalysisFlowResults() {
	}

	public static List<FlowDescriptor> getFlows(AnalysisResult result,
			Cache cache) {
		FlowIndex flowIndex = result.getFlowIndex();
		List<FlowDescriptor> flows = new ArrayList<>();
		for (int i = 0; i < flowIndex.size(); i++) {
			long flowId = flowIndex.getFlowAt(i);
			FlowDescriptor flow = cache.getFlowDescriptor(flowId);
			if (flow != null)
				flows.add(flow);
		}
		return flows;
	}

	public static List<AnalysisFlowResult> getForFlow(AnalysisResult result,
			FlowDescriptor flow, Cache cache) {
		List<AnalysisFlowResult> results = new ArrayList<>();
		ProductIndex index = result.getProductIndex();
		HashSet<Long> handled = new HashSet<>();
		for (int i = 0; i < index.size(); i++) {
			LongPair processProduct = index.getProductAt(i);
			long processId = processProduct.getFirst();
			if (handled.contains(processId))
				continue;
			handled.add(processId);
			ProcessDescriptor process = cache.getProcessDescriptor(processId);
			if (process == null)
				continue;
			AnalysisFlowResult r = getResult(result, process, flow);
			results.add(r);

		}
		return results;
	}

	public static List<AnalysisFlowResult> getForProcess(AnalysisResult result,
			ProcessDescriptor process, Cache cache) {
		List<AnalysisFlowResult> results = new ArrayList<>();
		for (FlowDescriptor flow : getFlows(result, cache)) {
			AnalysisFlowResult r = getResult(result, process, flow);
			results.add(r);
		}
		return results;
	}

	public static AnalysisFlowResult getResult(AnalysisResult result,
			ProcessDescriptor process, FlowDescriptor flow) {
		long flowId = flow.getId();
		long processId = process.getId();
		double single = result.getSingleFlowResult(processId, flowId);
		double total = result.getTotalFlowResult(processId, flowId);
		AnalysisFlowResult r = new AnalysisFlowResult();
		r.setFlow(flow);
		r.setProcess(process);
		r.setSingleResult(single);
		r.setTotalResult(total);
		r.setInput(result.getFlowIndex().isInput(flowId));
		return r;
	}

}
