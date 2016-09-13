package org.openlca.ilcd.util;

import java.math.BigInteger;
import java.util.List;

import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.commons.Time;
import org.openlca.ilcd.processes.AdminInfo;
import org.openlca.ilcd.processes.DataSetInfo;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.ExchangeList;
import org.openlca.ilcd.processes.Geography;
import org.openlca.ilcd.processes.LCIMethod;
import org.openlca.ilcd.processes.ModellingAndValidation;
import org.openlca.ilcd.processes.Parameter;
import org.openlca.ilcd.processes.ParameterList;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessInformation;
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
	private LCIMethod lciMethod;
	private Representativeness representativeness;
	private List<Review> reviews;
	private AdminInfo adminInfo;
	private List<Exchange> exchanges;

	private ProcessBuilder() {
		process = new Process();
		process.setVersion("1.1");
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

	public ProcessBuilder with(LCIMethod lciMethod) {
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
			process.setAdministrativeInformation(adminInfo);
		}
		fillExchanges();
	}

	private void fillProcessInfo() {
		ProcessInformation information = new ProcessInformation();
		process.setProcessInformation(information);
		if (dataSetInfo != null) {
			information.setDataSetInformation(dataSetInfo);
		}
		if (geography != null) {
			information.setGeography(geography);
		}
		if (time != null) {
			information.setTime(time);
		}
		if (technology != null) {
			information.setTechnology(technology);
		}
		makeQuanRef(information);
		addParameters(information);
	}

	private void makeQuanRef(ProcessInformation information) {
		if (refFlowId != null) {
			QuantitativeReference qRef = new QuantitativeReference();
			information.setQuantitativeReference(qRef);
			qRef.setType(QuantitativeReferenceType.REFERENCE_FLOW_S);
			qRef.getReferenceToReferenceFlow().add(
					BigInteger.valueOf(refFlowId));
		}
	}

	private void addParameters(ProcessInformation information) {
		if (parameters == null || parameters.isEmpty())
			return;
		ParameterList list = information.getParameters();
		if (list == null) {
			list = new ParameterList();
			information.setParameters(list);
		}
		list.getParameters().addAll(parameters);
	}

	private void fillModelling() {
		ModellingAndValidation mav = new ModellingAndValidation();
		process.setModellingAndValidation(mav);
		if (lciMethod != null) {
			mav.setLciMethod(lciMethod);
		}
		if (representativeness != null) {
			mav.setRepresentativeness(representativeness);
		}
		if (reviews != null && !reviews.isEmpty()) {
			Validation validation = new Validation();
			mav.setValidation(validation);
			validation.getReview().addAll(reviews);
		}
	}

	private void fillExchanges() {
		if (exchanges == null || exchanges.isEmpty())
			return;
		ExchangeList list = process.getExchanges();
		if (list == null) {
			list = new ExchangeList();
			process.setExchanges(list);
		}
		list.exchanges.addAll(exchanges);
	}

}
