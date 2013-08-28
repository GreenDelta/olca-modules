package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.Cache;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * A class that generates instances of {@link AnalysisFlowResult} from an
 * analysis result.
 */
public final class AnalysisFlowResults {

	private final AnalysisResult result;

	AnalysisFlowResults(AnalysisResult result) {
		this.result = result;
	}

	public Set<FlowDescriptor> getFlows(Cache cache) {
		return Results.getFlowDescriptors(result.getFlowIndex(), cache);
	}

	public Set<ProcessDescriptor> getProcesses(Cache cache) {
		return Results.getProcessDescriptors(result.getProductIndex(), cache);
	}

	public List<AnalysisFlowResult> getForFlow(FlowDescriptor flow, Cache cache) {
		List<AnalysisFlowResult> results = new ArrayList<>();
		for (ProcessDescriptor process : getProcesses(cache)) {
			AnalysisFlowResult r = getResult(process, flow);
			results.add(r);
		}
		return results;
	}

	public List<AnalysisFlowResult> getForProcess(ProcessDescriptor process,
			Cache cache) {
		List<AnalysisFlowResult> results = new ArrayList<>();
		for (FlowDescriptor flow : getFlows(cache)) {
			AnalysisFlowResult r = getResult(process, flow);
			results.add(r);
		}
		return results;
	}

	public AnalysisFlowResult getResult(ProcessDescriptor process,
			FlowDescriptor flow) {
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
