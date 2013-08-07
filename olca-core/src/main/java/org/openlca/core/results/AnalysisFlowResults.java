package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.Cache;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * A helper class that generates instances of {@link AnalysisFlowResult} from an
 * analysis result.
 */
public final class AnalysisFlowResults {

	private AnalysisFlowResults() {
	}

	public static Set<FlowDescriptor> getFlows(AnalysisResult result,
			Cache cache) {
		return Results.getFlowDescriptors(result.getFlowIndex(), cache);
	}

	public static Set<ProcessDescriptor> getProcesses(AnalysisResult result,
			Cache cache) {
		return Results.getProcessDescriptors(result.getProductIndex(), cache);
	}

	public static List<AnalysisFlowResult> getForFlow(AnalysisResult result,
			FlowDescriptor flow, Cache cache) {
		List<AnalysisFlowResult> results = new ArrayList<>();
		for (ProcessDescriptor process : getProcesses(result, cache)) {
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
