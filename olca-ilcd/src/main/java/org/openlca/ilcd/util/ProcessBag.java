package org.openlca.ilcd.util;

import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.CommissionerAndGoal;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.Completeness;
import org.openlca.ilcd.processes.DataEntry;
import org.openlca.ilcd.processes.DataGenerator;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.ParameterSection;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.processes.Publication;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.processes.Validation;

/**
 * @deprecated Processes should be used instead
 */
@Deprecated
public class ProcessBag implements IBag<Process> {

	private final Process process;
	private final String[] langs;

	public ProcessBag(Process process, String... langs) {
		this.process = process;
		this.langs = langs;
	}

	@Override
	public Process getValue() {
		return process;
	}

	@Override
	public String getId() {
		return process == null ? null : process.getUUID();
	}

	public String getSynonyms() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getFirst(info.synonyms, langs);
		return null;
	}

	public String getComment() {
		DataSetInfo info = getDataSetInformation();
		if (info != null)
			return LangString.getFirst(info.comment, langs);
		return null;
	}

	public Time getTime() {
		ProcessInfo info = process.processInfo;
		if (info != null) {
			return info.time;
		}
		return null;
	}

	public Geography getGeography() {
		ProcessInfo info = process.processInfo;
		if (info != null)
			return info.geography;
		return null;
	}

	public List<Integer> getReferenceFlowIds() {
		ProcessInfo info = process.processInfo;
		if (info != null) {
			QuantitativeReference qRef = info.quantitativeReference;
			if (qRef != null)
				return qRef.referenceFlows;
		}
		return Collections.emptyList();
	}

	public Technology getTechnology() {
		ProcessInfo info = process.processInfo;
		if (info != null)
			return info.technology;
		return null;
	}

	public List<Parameter> getParameters() {
		ProcessInfo info = process.processInfo;
		if (info != null) {
			ParameterSection list = info.parameters;
			if (list != null) {
				return list.parameters;
			}
		}
		return Collections.emptyList();
	}

	public ProcessType getProcessType() {
		Modelling mav = process.modelling;
		if (mav != null) {
			Method method = mav.method;
			if (method != null)
				return method.processType;
		}
		return null;
	}

	public Representativeness getRepresentativeness() {
		Modelling mav = process.modelling;
		if (mav != null)
			return mav.representativeness;
		return null;
	}

	public Completeness getCompleteness() {
		Modelling mav = process.modelling;
		if (mav != null)
			return mav.completeness;
		return null;
	}

	public List<Review> getReviews() {
		Modelling mav = process.modelling;
		if (mav != null) {
			Validation validation = mav.validation;
			if (validation != null) {
				return validation.reviews;
			}
		}
		return Collections.emptyList();
	}

	public CommissionerAndGoal getCommissionerAndGoal() {
		AdminInfo info = process.adminInfo;
		if (info != null)
			return info.commissionerAndGoal;
		return null;
	}

	public DataGenerator getDataGenerator() {
		AdminInfo info = process.adminInfo;
		if (info != null)
			return info.dataGenerator;
		return null;
	}

	public DataEntry getDataEntry() {
		AdminInfo info = process.adminInfo;
		if (info != null)
			return info.dataEntry;
		return null;
	}

	public Publication getPublication() {
		AdminInfo info = process.adminInfo;
		if (info != null)
			return info.publication;
		return null;
	}

	public List<Exchange> getExchanges() {
		return process.exchanges;
	}

	public Method getLciMethod() {
		Modelling mav = process.modelling;
		if (mav != null)
			return mav.method;
		return null;
	}

	private DataSetInfo getDataSetInformation() {
		if (process.processInfo != null)
			return process.processInfo.dataSetInfo;
		return null;
	}

	public List<Ref> getAllSources() {
		return SourceRefCollection.getAll(process, langs);
	}

}
