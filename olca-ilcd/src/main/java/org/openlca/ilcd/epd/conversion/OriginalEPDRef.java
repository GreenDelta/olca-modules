package org.openlca.ilcd.epd.conversion;

import java.util.stream.Collectors;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.epd.model.EpdDataSet;
import org.openlca.ilcd.util.Processes;

@XmlRootElement(name = "referenceToOriginalEPD", namespace = Vocab.NS_EPDv2)
class OriginalEPDRef extends Ref {

	private static OriginalEPDRef wrap(Ref other) {
		var ref = new OriginalEPDRef();
		JaxbRefs.copyFields(other, ref);
		ref.type = DataSetType.SOURCE;
		return ref;
	}

	static void write(EpdDataSet epd) {
		if (epd == null)
			return;

		// remove possible DOM elements
		if (epd.originalEPDs.isEmpty()) {
			var rep = Processes.getRepresentativeness(epd.process);
			if (rep == null || rep.other == null)
				return;
			// currently nothing else is written to this extension
			// point; so we can just drop it
			rep.other.any.clear();
			rep.other = null;
			return;
		}

		var rep = Processes.forceRepresentativeness(epd.process);
		if (rep.other == null) {
			rep.other = new Other();
		}
		rep.other.any.clear();
		var refs = epd.originalEPDs.stream()
				.map(OriginalEPDRef::wrap)
				.collect(Collectors.toList());
		JaxbRefs.write(OriginalEPDRef.class, refs, rep.other);
	}

	static void read(EpdDataSet epd) {
		if (epd == null)
			return;
		var rep = Processes.getRepresentativeness(epd.process);
		if (rep == null || rep.other == null)
			return;
		var refs = JaxbRefs.read(OriginalEPDRef.class, rep.other);
		if (refs.isEmpty())
			return;
		epd.originalEPDs.addAll(refs);
	}

}
