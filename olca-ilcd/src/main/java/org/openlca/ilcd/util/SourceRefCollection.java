package org.openlca.ilcd.util;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.processes.ComplianceDeclaration;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.LCIMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.Technology;

class SourceRefCollection {

	static List<DataSetReference> getAll(Process process, String... langs) {
		ProcessBag bag = new ProcessBag(process, langs);
		List<DataSetReference> refs = new ArrayList<>();
		refs.addAll(getFrom(bag.getRepresentativeness()));
		refs.addAll(getFrom(bag.getDataEntry()));
		refs.addAll(getFrom(bag.getLciMethod()));
		refs.addAll(getFrom(bag.getPublication()));
		refs.addAll(getFrom(bag.getTechnology()));
		if (bag.getComplianceDeclarations() != null)
			for (ComplianceDeclaration decl : bag.getComplianceDeclarations())
				refs.addAll(getFrom(decl));
		if (bag.getReviews() != null)
			for (Review review : bag.getReviews())
				refs.addAll(getFrom(review));
		return refs;
	}

	private static List<DataSetReference> getFrom(Representativeness repr) {
		List<DataSetReference> refs = new ArrayList<>();
		if (repr == null)
			return refs;
		for (DataSetReference ref : repr.referenceToDataSource)
			refs.add(ref);
		return refs;
	}

	private static List<DataSetReference> getFrom(ComplianceDeclaration decl) {
		List<DataSetReference> refs = new ArrayList<>();
		if (decl == null)
			return refs;
		refs.add(decl.referenceToComplianceSystem);
		return refs;
	}

	private static List<DataSetReference> getFrom(DataEntry entry) {
		List<DataSetReference> refs = new ArrayList<>();
		if (entry == null)
			return refs;
		refs.add(entry.originalDataSet);
		if (entry.formats != null)
			refs.addAll(entry.formats);
		return refs;
	}

	private static List<DataSetReference> getFrom(LCIMethod method) {
		List<DataSetReference> refs = new ArrayList<>();
		if (method == null)
			return refs;
		if (method.referenceToLCAMethodDetails != null)
			refs.addAll(method.referenceToLCAMethodDetails);
		return refs;
	}

	private static List<DataSetReference> getFrom(Publication pub) {
		List<DataSetReference> refs = new ArrayList<>();
		if (pub == null)
			return refs;
		refs.add(pub.republication);
		return refs;
	}

	private static List<DataSetReference> getFrom(Technology tec) {
		List<DataSetReference> refs = new ArrayList<>();
		if (tec == null)
			return refs;
		refs.add(tec.referenceToTechnologyPictogramme);
		if (tec.referenceToTechnologyFlowDiagrammOrPicture != null)
			refs.addAll(tec.referenceToTechnologyFlowDiagrammOrPicture);
		return refs;
	}

	private static List<DataSetReference> getFrom(Review rev) {
		List<DataSetReference> refs = new ArrayList<>();
		if (rev == null)
			return refs;
		refs.add(rev.referenceToCompleteReviewReport);
		return refs;
	}

}
