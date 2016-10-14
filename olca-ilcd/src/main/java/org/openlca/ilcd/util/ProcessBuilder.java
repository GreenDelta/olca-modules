package org.openlca.ilcd.util;

import java.util.List;

import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.ParameterSection;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInfo;
import org.openlca.ilcd.processes.QuantitativeReference;
import org.openlca.ilcd.processes.Representativeness;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.Technology;
import org.openlca.ilcd.processes.Validation;

public class ProcessBuilder {

	private Process process;
	private DataSetInfo dataSetInfo;
	private Integer refFlowId;
	private Time time;
	private Geography geography;
	private Technology technology;
	private List<Parameter> parameters;
	private Method lciMethod;
	private Representativeness representativeness;
	private List<Review> reviews;
	private AdminInfo adminInfo;
	private List<Exchange> exchanges;

	private ProcessBuilder() {
		process = new Process();
		process.version = "1.1";
	}

	public static ProcessBuilder makeProcess() {
		return new ProcessBuilder();
	}

	public ProcessBuilder withDataSetInfo(DataSetInfo dataSetInfo) {
		this.dataSetInfo = dataSetInfo;
		return this;
	}

	public ProcessBuilder withReferenceFlowId(Integer id) {
		this.refFlowId = id;
		return this;
	}

	public ProcessBuilder withTime(Time time) {
		this.time = time;
		return this;
	}

	public ProcessBuilder withGeography(Geography geography) {
		this.geography = geography;
		return this;
	}

	public ProcessBuilder withTechnology(Technology technology) {
		this.technology = technology;
		return this;
	}

	public ProcessBuilder withParameters(List<Parameter> parameters) {
		this.parameters = parameters;
		return this;
	}

	public ProcessBuilder with(Method lciMethod) {
		this.lciMethod = lciMethod;
		return this;
	}

	public ProcessBuilder withRepresentativeness(
			Representativeness representativeness) {
		this.representativeness = representativeness;
		return this;
	}

	public ProcessBuilder withReviews(List<Review> reviews) {
		this.reviews = reviews;
		return this;
	}

	public ProcessBuilder withAdminInfo(AdminInfo adminInfo) {
		this.adminInfo = adminInfo;
		return this;
	}

	public ProcessBuilder withExchanges(List<Exchange> exchanges) {
		this.exchanges = exchanges;
		return this;
	}

	public Process getProcess() {
		fill();
		return process;
	}

	private void fill() {
		fillProcessInfo();
		fillModelling();
		if (adminInfo != null) {
			process.adminInfo = adminInfo;
		}
		fillExchanges();
	}

	private void fillProcessInfo() {
		ProcessInfo information = new ProcessInfo();
		process.processInfo = information;
		if (dataSetInfo != null) {
			information.dataSetInfo = dataSetInfo;
		}
		if (geography != null) {
			information.geography = geography;
		}
		if (time != null) {
			information.time = time;
		}
		if (technology != null) {
			information.technology = technology;
		}
		makeQuanRef(information);
		addParameters(information);
	}

	private void makeQuanRef(ProcessInfo information) {
		if (refFlowId != null) {
			QuantitativeReference qRef = new QuantitativeReference();
			information.quantitativeReference = qRef;
			qRef.type = QuantitativeReferenceType.REFERENCE_FLOWS;
			qRef.referenceFlows.add(refFlowId);
		}
	}

	private void addParameters(ProcessInfo information) {
		if (parameters == null || parameters.isEmpty())
			return;
		ParameterSection list = information.parameters;
		if (list == null) {
			list = new ParameterSection();
			information.parameters = list;
		}
		list.parameters.addAll(parameters);
	}

	private void fillModelling() {
		Modelling mav = new Modelling();
		process.modelling = mav;
		if (lciMethod != null) {
			mav.method = lciMethod;
		}
		if (representativeness != null) {
			mav.representativeness = representativeness;
		}
		if (reviews != null && !reviews.isEmpty()) {
			Validation validation = new Validation();
			mav.validation = validation;
			validation.reviews.addAll(reviews);
		}
	}

	private void fillExchanges() {
		if (exchanges == null || exchanges.isEmpty())
			return;
		process.exchanges.addAll(exchanges);
	}

}
