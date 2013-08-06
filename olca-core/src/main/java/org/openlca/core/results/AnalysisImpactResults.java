package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.NormalizationWeightingFactor;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public final class AnalysisImpactResults {

	public static List<AnalysisImpactResult> getForImpact(
			AnalysisResult result, ImpactCategoryDescriptor impact,
			List<ProcessDescriptor> processes) {
		List<AnalysisImpactResult> results = new ArrayList<>();
		for (ProcessDescriptor process : processes) {
			AnalysisImpactResult r = getResult(result, process, impact);
			results.add(r);
		}
		return results;
	}

	public static List<AnalysisImpactResult> getForProcess(
			AnalysisResult result, ProcessDescriptor process,
			List<ImpactCategoryDescriptor> impacts) {
		List<AnalysisImpactResult> results = new ArrayList<>();
		for (ImpactCategoryDescriptor impact : impacts) {
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
