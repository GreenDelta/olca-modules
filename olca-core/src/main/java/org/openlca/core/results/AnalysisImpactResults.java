package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.Cache;
import org.openlca.core.matrices.LongIndex;
import org.openlca.core.model.NormalizationWeightingFactor;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public final class AnalysisImpactResults {

	public static Set<ProcessDescriptor> getProcesses(AnalysisResult result,
			Cache cache) {
		return Results.getProcessDescriptors(result.getProductIndex(), cache);
	}

	public static Set<ImpactCategoryDescriptor> getImpacts(
			AnalysisResult result, Cache cache) {
		LongIndex impactIndex = result.getImpactIndex();
		if (impactIndex == null)
			return Collections.emptySet();
		return Results.getImpactDescriptors(impactIndex, cache);
	}

	public static List<AnalysisImpactResult> getForImpact(
			AnalysisResult result, ImpactCategoryDescriptor impact, Cache cache) {
		List<AnalysisImpactResult> results = new ArrayList<>();
		for (ProcessDescriptor process : getProcesses(result, cache)) {
			AnalysisImpactResult r = getResult(result, process, impact);
			results.add(r);
		}
		return results;
	}

	public static List<AnalysisImpactResult> getForProcess(
			AnalysisResult result, ProcessDescriptor process, Cache cache) {
		List<AnalysisImpactResult> results = new ArrayList<>();
		for (ImpactCategoryDescriptor impact : getImpacts(result, cache)) {
			AnalysisImpactResult r = getResult(result, process, impact);
			results.add(r);
		}
		return results;
	}

	public static AnalysisImpactResult getResult(AnalysisResult result,
			ProcessDescriptor process, ImpactCategoryDescriptor impact) {
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

	public static AnalysisImpactResult getResult(AnalysisResult result,
			ProcessDescriptor process, ImpactCategoryDescriptor impact,
			NormalizationWeightingSet nwset) {
		AnalysisImpactResult r = getResult(result, process, impact);
		NormalizationWeightingFactor factor = nwset.getFactor(impact.getId());
		if (factor == null)
			return r;
		if (factor.getNormalizationFactor() != null)
			r.setNormalizationFactor(factor.getNormalizationFactor());
		if (factor.getWeightingFactor() != null)
			r.setWeightingFactor(factor.getWeightingFactor());
		r.setWeightingUnit(nwset.getUnit());
		return r;
	}

}
