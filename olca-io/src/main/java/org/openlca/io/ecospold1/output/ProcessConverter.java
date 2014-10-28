package org.openlca.io.ecospold1.output;

import java.util.Objects;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Uncertainty;
import org.openlca.ecospold.IDataEntryBy;
import org.openlca.ecospold.IDataGeneratorAndPublication;
import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.IDataSetInformation;
import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IGeography;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.IRepresentativeness;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.ITechnology;
import org.openlca.ecospold.ITimePeriod;
import org.openlca.ecospold.IValidation;
import org.openlca.ecospold.io.DataSet;
import org.openlca.ecospold.io.DataSetType;

class ProcessConverter {

	private Process process;
	private IEcoSpoldFactory factory = DataSetType.PROCESS.getFactory();

	static IEcoSpold convert(Process process) {
		return new ProcessConverter(process).doIt();
	}

	private ProcessConverter(Process process) {
		this.process = process;
	}

	private IEcoSpold doIt() {
		IEcoSpold ecoSpold = factory.createEcoSpold();
		IDataSet iDataSet = factory.createDataSet();
		DataSet dataSet = new DataSet(iDataSet, factory);
		Util.setDataSetAttributes(dataSet, process);
		mapDocumentation(dataSet);
		mapExchanges(dataSet);
		// TODO: map allocation factors
		// mapAllocations(process, dataSet, factory);
		ecoSpold.getDataset().add(iDataSet);
		return ecoSpold;
	}

	private void mapDocumentation(DataSet dataSet) {
		ProcessDocumentation doc = process.getDocumentation();
		if (doc == null)
			return;
		mapTime(doc, dataSet);
		mapTechnology(doc, dataSet);
		mapGeography(doc, dataSet);
		mapModelingAndValidation(doc, dataSet);
		mapAdminInfo(doc, dataSet);
	}

	private void mapGeography(ProcessDocumentation doc, DataSet dataSet) {
		IGeography geography = factory.createGeography();
		dataSet.setGeography(geography);
		Location location = process.getLocation();
		if (location != null)
			geography.setLocation(location.getCode());
		if (doc.getGeography() != null)
			geography.setText(doc.getGeography());
	}

	private void mapModelingAndValidation(ProcessDocumentation doc,
			DataSet dataSet) {
		if (doc == null)
			return;
		mapValidation(doc, dataSet);
		for (Source source : doc.getSources())
			mapSource(source, dataSet);
		if (doc.getSampling() == null)
			return;
		IRepresentativeness repr = dataSet.getRepresentativeness();
		if (repr == null) {
			repr = factory.createRepresentativeness();
			dataSet.setRepresentativeness(repr);
		}
		repr.setSamplingProcedure(doc.getSampling());
	}

	private void mapValidation(ProcessDocumentation doc, DataSet dataSet) {
		if (doc.getReviewer() == null)
			return;
		IValidation validation = dataSet.getValidation();
		if (validation == null) {
			validation = factory.createValidation();
			dataSet.setValidation(validation);
		}
		int reviewer = mapActor(doc.getReviewer(), dataSet);
		if (reviewer > 0)
			validation.setProofReadingValidator(reviewer);
		if (doc.getReviewDetails() != null)
			validation.setProofReadingDetails(doc.getReviewDetails());
		else
			validation.setProofReadingDetails("none");
	}

	private void mapAdminInfo(ProcessDocumentation doc, DataSet dataset) {
		IDataGeneratorAndPublication generator = dataset
				.getDataGeneratorAndPublication();
		if (generator == null) {
			generator = factory.createDataGeneratorAndPublication();
			dataset.setDataGeneratorAndPublication(generator);
		}
		generator.setCopyright(doc.isCopyright());
		generator.setAccessRestrictedTo(0);
		addDataSetInformation(doc, dataset);
		if (doc.getDataGenerator() != null) {
			int n = mapActor(doc.getDataGenerator(), dataset);
			generator.setPerson(n);
		}
		mapEntryBy(doc, dataset);
		if (doc.getPublication() != null) {
			int source = mapSource(doc.getPublication(), dataset);
			generator.setReferenceToPublishedSource(source);
		}
	}

	private void mapEntryBy(ProcessDocumentation doc, DataSet dataset) {
		if (doc.getDataDocumentor() == null)
			return;
		int n = mapActor(doc.getDataDocumentor(), dataset);
		IDataEntryBy entryBy = dataset.getDataEntryBy();
		if (entryBy == null) {
			entryBy = factory.createDataEntryBy();
			dataset.setDataEntryBy(entryBy);
		}
		entryBy.setPerson(n);
	}

	private void addDataSetInformation(ProcessDocumentation doc, DataSet dataSet) {
		IDataSetInformation info = factory.createDataSetInformation();
		dataSet.setDataSetInformation(info);
		if (process.getLastChange() != 0)
			info.setTimestamp(Util.toXml(process.getLastChange()));
		else if (doc.getCreationDate() != null)
			info.setTimestamp(Util.toXml(doc.getCreationDate()));
		mapProcessType(info);
	}

	private void mapTechnology(ProcessDocumentation doc, DataSet dataset) {
		if (doc == null || doc.getTechnology() == null)
			return;
		ITechnology technology = factory.createTechnology();
		technology.setText(doc.getTechnology());
		dataset.setTechnology(technology);
	}

	private void mapTime(ProcessDocumentation doc, DataSet dataset) {
		if (doc == null)
			return;
		ITimePeriod timePeriod = factory.createTimePeriod();
		if (doc.getValidFrom() != null)
			timePeriod.setStartDate(Util.toXml(doc.getValidFrom()));
		if (doc.getValidUntil() != null)
			timePeriod.setEndDate(Util.toXml(doc.getValidUntil()));
		timePeriod.setText(doc.getTime());
		dataset.setTimePeriod(timePeriod);
	}

	private void mapProcessType(IDataSetInformation info) {
		if (process.getProcessType() == ProcessType.LCI_RESULT) {
			info.setType(2);
		} else {
			if (isMultiOutput())
				info.setType(5);
			else
				info.setType(1);
		}
	}

	private boolean isMultiOutput() {
		int count = 0;
		for (Exchange e : process.getExchanges()) {
			if (e.isInput() || e.getFlow() == null)
				continue;
			if (e.getFlow().getFlowType() == FlowType.PRODUCT_FLOW)
				count++;
		}
		return count > 1;
	}

	private int mapActor(Actor actor, DataSet dataset) {
		if (actor == null)
			return -1;
		int id = (int) actor.getId();
		for (IPerson p : dataset.getPersons()) {
			if (p.getNumber() == id)
				return id;
		}
		IPerson person = factory.createPerson();
		person.setNumber(id);
		person.setCompanyCode("unknown");
		person.setName(actor.getName());
		person.setAddress(actor.getAddress());
		person.setCountryCode(factory.getCountryCode(actor.getCountry()));
		person.setEmail(actor.getEmail());
		person.setTelefax(actor.getTelefax());
		person.setTelephone(actor.getTelephone());
		dataset.getPersons().add(person);
		return id;
	}

	private int mapSource(Source inSource, DataSet dataset) {
		if (inSource == null)
			return -1;
		int id = (int) inSource.getId();
		for (ISource s : dataset.getSources()) {
			if (s.getNumber() == id)
				return id;
		}
		ISource source = factory.createSource();
		source.setNumber(id);
		source.setFirstAuthor(inSource.getName());
		source.setText(inSource.getDescription());
		source.setTitle(inSource.getTextReference());
		source.setYear(Util.toXml(inSource.getYear()));
		source.setPlaceOfPublications("unknown");
		dataset.getSources().add(source);
		return id;
	}

	private void mapExchanges(DataSet dataSet) {
		Exchange qRef = process.getQuantitativeReference();
		for (Exchange exchange : process.getExchanges()) {
			IExchange iExchange = mapExchange(exchange);
			dataSet.getExchanges().add(iExchange);
			if (Objects.equals(exchange, qRef)) {
				iExchange.setOutputGroup(0);
				IReferenceFunction refFun = mapQuantitativeReference(exchange);
				dataSet.setReferenceFunction(refFun);
				refFun.setGeneralComment(process.getDescription());
				refFun.setInfrastructureProcess(process
						.isInfrastructureProcess());
			}
		}
	}

	private IReferenceFunction mapQuantitativeReference(Exchange exchange) {
		IReferenceFunction refFun = factory.createReferenceFunction();
		Flow flow = exchange.getFlow();
		refFun.setCASNumber(flow.getCasNumber());
		refFun.setFormula(flow.getFormula());
		refFun.setName(exchange.getFlow().getName());
		refFun.setLocalName(refFun.getName());
		refFun.setUnit(exchange.getUnit().getName());
		refFun.setInfrastructureProcess(flow.isInfrastructureFlow());
		refFun.setAmount(exchange.getAmountValue());
		Category category = flow.getCategory();
		if (category != null) {
			if (category.getParentCategory() == null)
				refFun.setCategory(category.getName());
			else {
				refFun.setCategory(category.getParentCategory().getName());
				refFun.setSubCategory(category.getName());
			}
		}
		refFun.setLocalCategory(refFun.getCategory());
		refFun.setLocalSubCategory(refFun.getSubCategory());
		return refFun;
	}

	private IExchange mapExchange(Exchange inExchange) {
		IExchange exchange = factory.createExchange();
		Flow flow = inExchange.getFlow();
		exchange.setNumber((int) flow.getId());
		exchange.setName(inExchange.getFlow().getName());
		if (inExchange.isInput())
			exchange.setInputGroup(mapFlowType(flow.getFlowType(), true));
		else
			exchange.setOutputGroup(mapFlowType(flow.getFlowType(), false));
		Util.mapFlowCategory(exchange, inExchange.getFlow().getCategory());
		Util.mapFlowInformation(exchange, inExchange.getFlow());
		exchange.setUnit(inExchange.getUnit().getName());
		if (inExchange.getUncertainty() == null)
			exchange.setMeanValue(inExchange.getAmountValue());
		else
			mapUncertainty(inExchange, exchange);
		return exchange;
	}

	private int mapFlowType(FlowType flowType, boolean input) {
		if (input) {
			if (flowType == FlowType.ELEMENTARY_FLOW)
				return 4;
			else
				return 5;
		} else {
			if (flowType == FlowType.ELEMENTARY_FLOW)
				return 4;
			if (flowType == FlowType.WASTE_FLOW)
				return 3;
			else {
				if (isMultiOutput())
					return 2;
				else
					return 0;
			}
		}
	}

	private void mapUncertainty(Exchange oExchange, IExchange exchange) {
		Uncertainty uncertainty = oExchange.getUncertainty();
		if (uncertainty == null || uncertainty.getDistributionType() == null)
			return;
		switch (uncertainty.getDistributionType()) {
		case NORMAL:
			exchange.setMeanValue(uncertainty.getParameter1Value());
			exchange.setStandardDeviation95(uncertainty.getParameter2Value() * 2);
			exchange.setUncertaintyType(2);
			break;
		case LOG_NORMAL:
			exchange.setMeanValue(uncertainty.getParameter1Value());
			double sd = uncertainty.getParameter2Value();
			exchange.setStandardDeviation95(Math.pow(sd, 2));
			exchange.setUncertaintyType(1);
			break;
		case TRIANGLE:
			exchange.setMinValue(uncertainty.getParameter1Value());
			exchange.setMostLikelyValue(uncertainty.getParameter2Value());
			exchange.setMaxValue(uncertainty.getParameter3Value());
			exchange.setMeanValue(oExchange.getAmountValue());
			exchange.setUncertaintyType(3);
			break;
		case UNIFORM:
			exchange.setMinValue(uncertainty.getParameter1Value());
			exchange.setMaxValue(uncertainty.getParameter2Value());
			exchange.setMeanValue(oExchange.getAmountValue());
			exchange.setUncertaintyType(4);
			break;
		default:
			exchange.setMeanValue(oExchange.getAmountValue());
			exchange.setUncertaintyType(0);
		}
	}

	// TODO: map allocation factors
	// private void mapAllocations(Process process, DataSet dataset,
	// IEcoSpoldFactory factory) {
	// for (AllocationFactor inFactor : process.getAllocationFactors()) {
	// IAllocation factor = factory.createAllocation();
	// factor.setFraction((float) (inFactor.getValue() * 100));
	// factor.setReferenceToCoProduct(exchangeToES1Exchange.get(
	// inFactor.getProductId()).getNumber());
	// factor.setAllocationMethod(-1);
	// dataset.getAllocations().add(factor);
	// factor.getReferenceToInputOutput().add(
	// exchangeToES1Exchange.get(exchange.getId()).getNumber());
	// }
	// }

}
