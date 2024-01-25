package org.openlca.io.ecospold1.input;

import java.util.Date;

import org.openlca.core.model.Actor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.doc.Review;
import org.openlca.ecospold.IDataSetInformation;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.io.DataSet;

import com.google.common.base.Joiner;
import org.openlca.util.Strings;

class Util {

	static void mapPerson(IPerson p, Actor target) {
		target.name = p.getName();
		target.address = p.getAddress();
		if (p.getCountryCode() != null) {
			target.country = p.getCountryCode().value();
		}
		target.email = p.getEmail();
		target.telefax = p.getTelefax();
		target.telephone = p.getTelephone();
	}

	static void mapSource(ISource s, Source target) {
		target.name = s.getFirstAuthor();
		target.description = s.getText();
		target.textReference = s.getTitle();
		if (s.getYear() != null) {
			target.year = (short) s.getYear().getYear();
		}
	}

	static FlowType getFlowType(IExchange e) {
		if (e.getInputGroup() != null) {
			return FlowTypes.forInputGroup(e.getInputGroup());
		} else if (e.getOutputGroup() != null) {
			return FlowTypes.forOutputGroup(e.getOutputGroup());
		} else {
			return FlowType.ELEMENTARY_FLOW;
		}
	}

	static ProcessType getProcessType(DataSet ds) {
		if (ds == null || ds.getDataSetInformation() == null)
			return ProcessType.UNIT_PROCESS;
		var info = ds.getDataSetInformation();
		return info.getType() == 2
				? ProcessType.LCI_RESULT
				: ProcessType.UNIT_PROCESS;
	}

	static void mapModellingAndValidation(DataSet ds, ProcessDoc doc) {
		var v = ds.getValidation();
		if (v != null) {
			var text = Joiner.on(" ")
					.skipNulls()
					.join(v.getProofReadingDetails(), v.getOtherDetails());
			if (Strings.notEmpty(text)) {
				reviewOf(doc).details = text;
			}
		}
		var repr = ds.getRepresentativeness();
		if (repr != null) {
			doc.samplingProcedure = repr.getSamplingProcedure();
		}
	}

	static void mapAdminInfo(DataSet ds, Process process) {
		if (process == null || process.documentation == null)
			return;
		ProcessDoc doc = process.documentation;
		mapPublication(ds, doc);
		IDataSetInformation info = ds.getDataSetInformation();
		if (info != null && info.getTimestamp() != null) {
			Date lastChange = info.getTimestamp().toGregorianCalendar()
					.getTime();
			process.lastChange = lastChange.getTime();
			doc.creationDate = lastChange;
		}
	}

	private static void mapPublication(DataSet ds, ProcessDoc doc) {
		var gen = ds.getDataGeneratorAndPublication();
		if (gen == null)
			return;
		doc.copyright = gen.isCopyright();
		Integer restrictedTo = gen.getAccessRestrictedTo();
		if (restrictedTo == null)
			return;
		doc.accessRestrictions = switch (restrictedTo) {
			case 0 -> "All information can be accessed by everybody.";
			case 2 -> "Ecoinvent clients have access to LCI results "
					+ "but not to unit process raw data. Members of "
					+ "the ecoinvent quality network (ecoinvent centre) "
					+ "have access to all information.";
			case 3 -> "The ecoinvent administrator has full access to "
					+ "information. Via the web only LCI results are "
					+ "accessible (for ecoinvent clients and "
					+ "for members of the ecoinvent centre).";
			default -> null;
		};
	}

	static Review reviewOf(ProcessDoc doc) {
		if (!doc.reviews.isEmpty())
			return doc.reviews.get(0);
		var r = new Review();
		doc.reviews.add(r);
		return r;
	}

}
