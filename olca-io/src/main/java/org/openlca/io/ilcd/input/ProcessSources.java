package org.openlca.io.ilcd.input;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.InventoryMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.util.Processes;

import java.util.ArrayList;
import java.util.List;

class ProcessSources {

	static List<Ref> allOf(Process p) {
		List<Ref> refs = new ArrayList<>();
		refs.addAll(getFrom(Processes.getRepresentativeness(p)));
		refs.addAll(getFrom(Processes.getDataEntry(p)));
		refs.addAll(getFrom(Processes.getInventoryMethod((p))));
		refs.addAll(getFrom(Processes.getPublication(p)));
		refs.addAll(getFrom(Processes.getTechnology(p)));
		complianceSystems(p, refs);
		for (var review : Processes.getReviews(p)) {
			refs.addAll(getFrom(review));
		}
		return refs;
	}

	private static void complianceSystems(Process p, List<Ref> refs) {
		for (var dec : Processes.getComplianceDeclarations(p)) {
			var ref = dec.getSystem();
			if (ref == null)
				continue;
			refs.add(ref);
		}
	}

	private static List<Ref> getFrom(Representativeness repr) {
		List<Ref> refs = new ArrayList<>();
		if (repr == null)
			return refs;
		refs.addAll(repr.getSources());
		return refs;
	}

	private static List<Ref> getFrom(DataEntry entry) {
		List<Ref> refs = new ArrayList<>();
		if (entry == null)
			return refs;
		if (entry.getOriginalDataSet() != null) {
			refs.add(entry.getOriginalDataSet());
		}
		refs.addAll(entry.getFormats());
		return refs;
	}

	private static List<Ref> getFrom(InventoryMethod method) {
		List<Ref> refs = new ArrayList<>();
		if (method == null)
			return refs;
		refs.addAll(method.getSources());
		return refs;
	}

	private static List<Ref> getFrom(Publication pub) {
		List<Ref> refs = new ArrayList<>();
		if (pub == null || pub.getRepublication() == null)
			return refs;
		refs.add(pub.getRepublication());
		return refs;
	}

	private static List<Ref> getFrom(Technology tec) {
		List<Ref> refs = new ArrayList<>();
		if (tec == null)
			return refs;
		if (tec.getPictogram() != null) {
			refs.add(tec.getPictogram());
		}
		refs.addAll(tec.getPictures());
		return refs;
	}

	private static List<Ref> getFrom(Review rev) {
		List<Ref> refs = new ArrayList<>();
		if (rev == null || rev.getReport() == null)
			return refs;
		refs.add(rev.getReport());
		return refs;
	}

}
