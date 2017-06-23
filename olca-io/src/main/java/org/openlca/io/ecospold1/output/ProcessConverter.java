package org.openlca.io.ecospold1.output;

import java.util.Date;
import java.util.Objects;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.Version;
import org.openlca.ecospold.IDataEntryBy;
import org.openlca.ecospold.IDataGeneratorAndPublication;
import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.IDataSetInformation;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IGeography;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.IRepresentativeness;
import org.openlca.ecospold.ITechnology;
import org.openlca.ecospold.ITimePeriod;
import org.openlca.ecospold.IValidation;
import org.openlca.ecospold.io.DataSet;
import org.openlca.ecospold.io.DataSetType;

class ProcessConverter {

	private Process process;
	private ExportConfig config;
	private IEcoSpoldFactory factory = DataSetType.PROCESS.getFactory();
	private ActorSourceMapper actorSourceMapper;

	static IDataSet convert(Process process, ExportConfig config) {
		return new ProcessConverter(process, config).doIt();
	}

	private ProcessConverter(Process process, ExportConfig config) {
		this.process = process;
		this.config = config;
		actorSourceMapper = new ActorSourceMapper(factory, config);
	}

	private IDataSet doIt() {
		IDataSet iDataSet = factory.createDataSet();
		DataSet dataSet = new DataSet(iDataSet, factory);
		Util.setDataSetAttributes(dataSet, process);
		mapDocumentation(dataSet);
		mapExchanges(dataSet);
		// TODO: map allocation factors
		// mapAllocations(process, dataSet, factory);
		if (config.isCreateDefaults())
			StructureDefaults.add(dataSet, factory);
		return iDataSet;
	}

	private void mapDocumentation(DataSet dataSet) {
		ProcessDocumentation doc = process.getDocumentation();
		if (doc == null)
			return;
		mapDataSetInformation(doc, dataSet);
		mapTime(doc, dataSet);
		mapTechnology(doc, dataSet);
		mapGeography(doc, dataSet);
		mapModelingAndValidation(doc, dataSet);
		mapAdminInfo(doc, dataSet);
	}

	private void mapDataSetInformation(ProcessDocumentation doc, DataSet dataSet) {
		IDataSetInformation info = factory.createDataSetInformation();
		dataSet.setDataSetInformation(info);
		info.setEnergyValues(0);
		info.setImpactAssessmentResult(false);
		info.setLanguageCode(factory.getLanguageCode("en"));
		info.setLocalLanguageCode(factory.getLanguageCode("en"));
		if (process.getLastChange() != 0)
			info.setTimestamp(Util.toXml(process.getLastChange()));
		else if (doc.getCreationDate() != null)
			info.setTimestamp(Util.toXml(doc.getCreationDate()));
		else
			info.setTimestamp(Util.toXml(new Date()));
		info.setType(getProcessType());
		Version version = new Version(process.getVersion());
		info.setVersion(version.getMajor());
		info.setInternalVersion(version.getMinor());
	}

	private int getProcessType() {
		if (process.getProcessType() == ProcessType.LCI_RESULT)
			return 2;
		if (isMultiOutput())
			return 5;
		else
			return 1;
	}

	private void mapGeography(ProcessDocumentation doc, DataSet dataSet) {
		IGeography geography = factory.createGeography();
		dataSet.setGeography(geography);
		Location location = process.getLocation();
		if (location != null)
			geography.setLocation(location.getCode());
		if (doc.getGeography() != null)
			geography.setText(doc.getGeography());
		if (!config.isCreateDefaults())
			return;
		if (geography.getLocation() == null)
			geography.setLocation("GLO");
	}

	private void mapModelingAndValidation(ProcessDocumentation doc,
			DataSet dataSet) {
		mapValidation(doc, dataSet);
		for (Source source : doc.getSources())
			actorSourceMapper.map(source, dataSet);
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
		int reviewer = actorSourceMapper.map(doc.getReviewer(), dataSet);
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
		generator.setDataPublishedIn(0);
		if (doc.getDataGenerator() != null) {
			int n = actorSourceMapper.map(doc.getDataGenerator(), dataset);
			generator.setPerson(n);
		}
		mapEntryBy(doc, dataset);
		if (doc.getPublication() != null) {
			int source = actorSourceMapper.map(doc.getPublication(), dataset);
			generator.setReferenceToPublishedSource(source);
		}
	}

	private void mapEntryBy(ProcessDocumentation doc, DataSet dataset) {
		if (doc.getDataDocumentor() == null)
			return;
		int n = actorSourceMapper.map(doc.getDataDocumentor(), dataset);
		IDataEntryBy entryBy = dataset.getDataEntryBy();
		if (entryBy == null) {
			entryBy = factory.createDataEntryBy();
			dataset.setDataEntryBy(entryBy);
		}
		entryBy.setPerson(n);
	}

	private void mapTechnology(ProcessDocumentation doc, DataSet dataset) {
		ITechnology technology = factory.createTechnology();
		technology.setText(doc.getTechnology());
		dataset.setTechnology(technology);
	}

	private void mapTime(ProcessDocumentation doc, DataSet dataset) {
		ITimePeriod time = factory.createTimePeriod();
		time.setDataValidForEntirePeriod(true);
		if (doc.getValidFrom() != null)
			time.setStartDate(Util.toXml(doc.getValidFrom()));
		if (doc.getValidUntil() != null)
			time.setEndDate(Util.toXml(doc.getValidUntil()));
		time.setText(doc.getTime());
		dataset.setTimePeriod(time);
		if (!config.isCreateDefaults())
			return;
		if (time.getStartDate() == null)
			time.setStartDate(Util.toXml(new Date(253370761200000L)));
		if (time.getEndDate() == null)
			time.setEndDate(Util.toXml(new Date(253402210800000L)));
	}

	private boolean isMultiOutput() {
		int count = 0;
		for (Exchange e : process.getExchanges()) {
			if (e.isInput || e.flow == null)
				continue;
			if (e.flow.getFlowType() == FlowType.PRODUCT_FLOW)
				count++;
		}
		return count > 1;
	}

	private void mapExchanges(DataSet dataSet) {
		Exchange qRef = process.getQuantitativeReference();
		for (Exchange exchange : process.getExchanges()) {
			IExchange iExchange = mapExchange(exchange);
			dataSet.getExchanges().add(iExchange);
			if (Objects.equals(exchange, qRef)) {
				mapRefFlow(dataSet, exchange, iExchange);
			}
		}
	}

	private void mapRefFlow(DataSet dataSet, Exchange exchange,
			IExchange iExchange) {
		iExchange.setOutputGroup(0);
		IReferenceFunction refFun = mapQuantitativeReference(exchange);
		dataSet.setReferenceFunction(refFun);
		refFun.setGeneralComment(process.getDescription());
		refFun.setInfrastructureProcess(process.isInfrastructureProcess());
		Location location = process.getLocation();
		if (location != null)
			iExchange.setLocation(location.getCode());
		else if (config.isCreateDefaults())
			iExchange.setLocation("GLO");
	}

	private IReferenceFunction mapQuantitativeReference(Exchange exchange) {
		IReferenceFunction refFun = factory.createReferenceFunction();
		Flow flow = exchange.flow;
		refFun.setCASNumber(flow.getCasNumber());
		refFun.setFormula(flow.getFormula());
		refFun.setName(exchange.flow.getName());
		refFun.setLocalName(refFun.getName());
		refFun.setUnit(exchange.unit.getName());
		refFun.setInfrastructureProcess(flow.isInfrastructureFlow());
		refFun.setAmount(exchange.amount);
		Categories.map(flow, refFun, config);
		refFun.setLocalCategory(refFun.getCategory());
		refFun.setLocalSubCategory(refFun.getSubCategory());
		return refFun;
	}

	private IExchange mapExchange(Exchange inExchange) {
		IExchange exchange = factory.createExchange();
		Flow flow = inExchange.flow;
		exchange.setNumber((int) flow.getId());
		exchange.setName(inExchange.flow.getName());
		if (inExchange.isInput) {
			exchange.setInputGroup(mapFlowType(flow.getFlowType(), true));
		} else {
			exchange.setOutputGroup(mapFlowType(flow.getFlowType(), false));
		}
		Categories.map(flow.getCategory(), exchange, config);
		Util.mapFlowInformation(exchange, inExchange.flow);
		if (inExchange.unit != null) {
			exchange.setUnit(inExchange.unit.getName());
		}
		if (inExchange.uncertainty == null) {
			exchange.setMeanValue(inExchange.amount);
		} else {
			mapUncertainty(inExchange, exchange);
		}
		mapComment(inExchange, exchange);
		return exchange;
	}

	private void mapComment(Exchange inExchange, IExchange exchange) {
		if (inExchange.description == null) {
			exchange.setGeneralComment(inExchange.dqEntry);
		} else if (inExchange.dqEntry == null) {
			exchange.setGeneralComment(inExchange.description);
		} else {
			exchange.setGeneralComment(inExchange.dqEntry + "; " + inExchange.description);
		}
	}

	private int mapFlowType(FlowType flowType, boolean input) {
		if (input)
			return flowType == FlowType.ELEMENTARY_FLOW ? 4 : 5;
		if (flowType == FlowType.ELEMENTARY_FLOW)
			return 4;
		if (flowType == FlowType.WASTE_FLOW)
			return 3;
		else
			return isMultiOutput() ? 2 : 0;
	}

	private void mapUncertainty(Exchange oExchange, IExchange exchange) {
		Uncertainty uncertainty = oExchange.uncertainty;
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
			exchange.setMeanValue(oExchange.amount);
			exchange.setUncertaintyType(3);
			break;
		case UNIFORM:
			exchange.setMinValue(uncertainty.getParameter1Value());
			exchange.setMaxValue(uncertainty.getParameter2Value());
			exchange.setMeanValue(oExchange.amount);
			exchange.setUncertaintyType(4);
			break;
		default:
			exchange.setMeanValue(oExchange.amount);
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
