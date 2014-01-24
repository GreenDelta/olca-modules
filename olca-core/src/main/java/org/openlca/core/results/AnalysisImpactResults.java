package org.openlca.core.results;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.LongIndex;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.NwSetTable;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class AnalysisImpactResults {

	private final AnalysisResult result;

	public AnalysisImpactResults(AnalysisResult result) {
		this.result = result;
	}

	public Set<ProcessDescriptor> getProcesses(EntityCache cache) {
		return Results.getProcessDescriptors(result.getProductIndex(), cache);
	}

	public Set<ImpactCategoryDescriptor> getImpacts(EntityCache cache) {
		LongIndex impactIndex = result.getImpactIndex();
		if (impactIndex == null)
			return Collections.emptySet();
		return Results.getImpactDescriptors(impactIndex, cache);
	}

	/**
	 * Returns the total result for the given impact category which is the
	 * upstream-total result of the reference process.
	 */
	public double getTotalResult(ImpactCategoryDescriptor impact) {
		LongPair refProduct = result.getProductIndex().getRefProduct();
		return result.getTotalImpactResult(refProduct.getFirst(),
				impact.getId());
	}

	public List<AnalysisImpactResult> getForImpact(
			ImpactCategoryDescriptor impact, EntityCache cache) {
		List<AnalysisImpactResult> results = new ArrayList<>();
		for (ProcessDescriptor process : getProcesses(cache)) {
			AnalysisImpactResult r = getResult(process, impact);
			results.add(r);
		}
		return results;
	}

	public List<AnalysisImpactResult> getForProcess(ProcessDescriptor process,
	                                                EntityCache cache) {
		List<AnalysisImpactResult> results = new ArrayList<>();
		for (ImpactCategoryDescriptor impact : getImpacts(cache)) {
			AnalysisImpactResult r = getResult(process, impact);
			results.add(r);
		}
		return results;
	}

	public AnalysisImpactResult getResult(ProcessDescriptor process,
	                                      ImpactCategoryDescriptor impact) {
		long processId = process.getId();
		long impactId = impact.getId();
		double single = result.getSingleImpactResult(processId, impactId);
		double total = result.getTotalImpactResult(processId, impactId);
		AnalysisImpactResult r = new AnalysisImpactResult();
		r.setImpactCategory(impact);
		r.setProcess(process);
		r.setSingleResult(single);
		r.setTotalResult(total);
		return r;
	}

	public AnalysisImpactResult getResult(ProcessDescriptor process,
	                                      ImpactCategoryDescriptor impact,
	                                      NwSetTable nwset) {
		AnalysisImpactResult r = getResult(process, impact);
		if (nwset == null)
			return r;
		long impactId = impact.getId();
		r.setNormalizationFactor(nwset.getNormalisationFactor(impactId));
		r.setWeightingFactor(nwset.getWeightingFactor(impactId));
		return r;
	}

}
