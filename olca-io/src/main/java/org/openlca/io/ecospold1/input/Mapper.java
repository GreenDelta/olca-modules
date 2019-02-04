package org.openlca.io.ecospold1.input;

import java.util.Date;

import org.openlca.core.model.Actor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.ecospold.IDataGeneratorAndPublication;
import org.openlca.ecospold.IDataSetInformation;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.IRepresentativeness;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.IValidation;
import org.openlca.ecospold.io.DataSet;

import com.google.common.base.Joiner;

class Mapper {

	public static void mapPerson(IPerson inPerson, Actor ioActor) {
		ioActor.name = inPerson.getName();
		ioActor.address = inPerson.getAddress();
		if (inPerson.getCountryCode() != null)
			ioActor.country = inPerson.getCountryCode().value();
		ioActor.email = inPerson.getEmail();
		ioActor.telefax = inPerson.getTelefax();
		ioActor.telephone = inPerson.getTelephone();
	}

	public static void mapSource(ISource inSource, Source ioSource) {
		ioSource.name = inSource.getFirstAuthor();
		ioSource.description = inSource.getText();
		ioSource.textReference = inSource.getTitle();
		if (inSource.getYear() != null) {
			ioSource.year = (short) inSource.getYear().getYear();
		}
	}

	public static FlowType getFlowType(IExchange inExchange) {
		if (inExchange.getInputGroup() != null) {
			return FlowTypes.forInputGroup(inExchange.getInputGroup());
		} else if (inExchange.getOutputGroup() != null) {
			return FlowTypes.forOutputGroup(inExchange.getOutputGroup());
		} else {
			return FlowType.ELEMENTARY_FLOW;
		}
	}

	public static ProcessType getProcessType(DataSet dataset) {
		if (dataset == null || dataset.getDataSetInformation() == null)
			return ProcessType.UNIT_PROCESS;
		IDataSetInformation info = dataset.getDataSetInformation();
		if (info.getType() == 2)
			return ProcessType.LCI_RESULT;
		else
			return ProcessType.UNIT_PROCESS;
	}

	public static void mapModellingAndValidation(DataSet dataSet,
			ProcessDocumentation doc) {
		IValidation validation = dataSet.getValidation();
		if (validation != null) {
			String evaluation = Joiner
					.on(" ")
					.skipNulls()
					.join(validation.getProofReadingDetails(),
							validation.getOtherDetails());
			doc.reviewDetails = evaluation;
		}
		IRepresentativeness representativeness = dataSet
				.getRepresentativeness();
		if (representativeness != null)
			doc.sampling = representativeness.getSamplingProcedure();
	}

	public static void mapAdminInfo(DataSet dataSet, Process process) {
		if (process == null || process.documentation == null)
			return;
		ProcessDocumentation doc = process.documentation;
		mapDataGeneratorAndPublication(dataSet, doc);
		IDataSetInformation info = dataSet.getDataSetInformation();
		if (info != null && info.getTimestamp() != null) {
			Date lastChange = info.getTimestamp().toGregorianCalendar()
					.getTime();
			process.lastChange = lastChange.getTime();
			doc.creationDate = lastChange;
		}
	}

	private static void mapDataGeneratorAndPublication(DataSet dataSet,
			ProcessDocumentation doc) {
		IDataGeneratorAndPublication gen = dataSet
				.getDataGeneratorAndPublication();
		if (gen == null)
			return;
		doc.copyright = gen.isCopyright();
		Integer restrictedTo = gen.getAccessRestrictedTo();
		if (restrictedTo == null)
			return;
		switch (restrictedTo) {
		case 0:
			doc.restrictions = "All information can be accessed by everybody.";
			break;
		case 2:
			doc.restrictions = "Ecoinvent clients have access to LCI results "
			+ "but not to unit process raw data. Members of "
			+ "the ecoinvent quality network (ecoinvent centre) "
			+ "have access to all information.";
			break;
		case 3:
			doc.restrictions = "The ecoinvent administrator has full access to "
			+ "information. Via the web only LCI results are "
			+ "accessible (for ecoinvent clients and "
			+ "for members of the ecoinvent centre).";
			break;
		}
	}

}
