package org.openlca.io.ilcd.input;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.InventoryMethod;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.util.Processes;

class ProcessSources {

	static List<Ref> allOf(Process p) {
		var refs = new ArrayList<Ref>();
		getFrom(Processes.getRepresentativeness(p), refs);
		getFrom(Processes.getDataEntry(p), refs);
		getFrom(Processes.getInventoryMethod(p), refs);
		getFrom(Processes.getPublication(p), refs);
		getFrom(Processes.getTechnology(p), refs);
		complianceSystems(p, refs);
		for (var review : Processes.getReviews(p)) {
			getFrom(review, refs);
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

	private static void getFrom(Representativeness repr, List<Ref> refs) {
		if (repr == null)
			return;
		refs.addAll(repr.getSources());
	}

	private static void getFrom(DataEntry entry, List<Ref> refs) {
		if (entry == null)
			return;
		if (entry.getOriginalDataSet() != null) {
			refs.add(entry.getOriginalDataSet());
		}
	}

	private static void getFrom(InventoryMethod method, List<Ref> refs) {
		if (method == null)
			return;
		refs.addAll(method.getSources());
	}

	private static void getFrom(Publication pub, List<Ref> refs) {
		if (pub == null || pub.getRepublication() == null)
			return;
		refs.add(pub.getRepublication());
	}

	private static void getFrom(Technology tec, List<Ref> refs) {
		if (tec == null)
			return;
		if (tec.getPictogram() != null) {
			refs.add(tec.getPictogram());
		}
		refs.addAll(tec.getPictures());
	}

	private static void getFrom(Review rev, List<Ref> refs) {
		if (rev == null || rev.getReport() == null)
			return;
		refs.add(rev.getReport());
	}
}
