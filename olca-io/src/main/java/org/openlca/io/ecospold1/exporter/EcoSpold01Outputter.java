package org.openlca.io.ecospold1.exporter;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
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
import org.openlca.ecospold.io.EcoSpoldIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates EcoSpold01 xml files for openLCA processes and impact methods
 */
public class EcoSpold01Outputter {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private int datasetCounter = 0;
	private int exchangeCounter = 0;
	private int personCounter = 0;
	private int sourceCounter = 0;

	private Map<Long, IExchange> exchangeToES1Exchange = new HashMap<>();
	private Map<Long, ISource> sourceToES1Source = new HashMap<>();
	private Map<Long, IPerson> actorToES1Person = new HashMap<>();

	private File outDir;

	/**
	 * Creates a new outputter which writes into the given directory.
	 */
	public EcoSpold01Outputter(File directory) {
		log.trace("Initialize EcoSpold 01 export");
		File dir = new File(directory, "EcoSpold01");
		if (!dir.exists())
			dir.mkdirs();
		outDir = dir;
	}

	private void clearLocalCache() {
		exchangeToES1Exchange.clear();
		actorToES1Person.clear();
		sourceToES1Source.clear();
		exchangeCounter = 0;
		personCounter = 0;
		sourceCounter = 0;
		datasetCounter = 0;
	}

	private IEcoSpold convertLCIAMethod(ImpactMethod method) {
		IEcoSpoldFactory factory = DataSetType.IMPACT_METHOD.getFactory();
		IEcoSpold ecoSpold = factory.createEcoSpold();
		for (ImpactCategory category : method.getImpactCategories()) {
			IDataSet iDataSet = factory.createDataSet();
			DataSet dataSet = new DataSet(iDataSet, factory);
			mapLCIACategory(category, dataSet, factory);
			dataSet.getReferenceFunction().setCategory(method.getName());
			dataSet.getReferenceFunction().setGeneralComment(
					method.getDescription());
			ecoSpold.getDataset().add(iDataSet);
		}
		return ecoSpold;
	}

	private IEcoSpold convertProcess(Process process) {

		IEcoSpoldFactory factory = DataSetType.PROCESS.getFactory();
		IEcoSpold ecoSpold = factory.createEcoSpold();
		IDataSet iDataSet = factory.createDataSet();
		DataSet dataSet = new DataSet(iDataSet, factory);
		dataSet.setNumber(0);

		ProcessDocumentation doc = process.getDocumentation();
		if (doc != null) {
			mapModelingAndValidation(doc, dataSet, factory);
			mapAdminInfo(process, dataSet, factory);
			mapTime(doc, dataSet, factory);
			mapTechnology(doc, dataSet, factory);
			if (doc.getGeography() != null) {
				IGeography geography = factory.createGeography();
				geography.setText(doc.getGeography());
				dataSet.setGeography(geography);
			}
		}

		// map exchanges
		Exchange qRef = process.getQuantitativeReference();
		for (Exchange exchange : process.getExchanges()) {
			if (qRef != null && qRef.equals(exchange)) {
				IReferenceFunction refFun = mapQuantitativeReference(exchange,
						factory);
				dataSet.setReferenceFunction(refFun);
				refFun.setGeneralComment(process.getDescription());
				refFun.setInfrastructureProcess(process
						.isInfrastructureProcess());
			}
			dataSet.getExchanges().add(
					mapExchange(exchange, isMultiOutput(process), factory));
		}

		// map location
		if (process.getLocation() != null) {
			IGeography geography = dataSet.getGeography();
			if (geography == null) {
				geography = factory.createGeography();
				dataSet.setGeography(geography);
			}
			if (process.getLocation().getCode() != null) {
				geography.setLocation(process.getLocation().getCode());
			} else if (process.getLocation().getName() != null) {
				geography.setLocation(process.getLocation().getName());
			}
		}
		if (dataSet.getDataSetInformation() == null) {
			dataSet.setDataSetInformation(factory.createDataSetInformation());
		}

		mapProcessType(process, dataSet);

		// TODO: map allocation factors
		// mapAllocations(process, dataSet, factory);

		ecoSpold.getDataset().add(iDataSet);
		return ecoSpold;
	}

	private void mapProcessType(Process process, DataSet dataSet) {
		if (process.getProcessType() == ProcessType.LCI_RESULT) {
			dataSet.getDataSetInformation().setType(2);
		} else {
			if (isMultiOutput(process))
				dataSet.getDataSetInformation().setType(5);
			else
				dataSet.getDataSetInformation().setType(1);
		}
	}

	private boolean isMultiOutput(Process process) {
		int count = 0;
		for (Exchange e : process.getExchanges()) {
			if (e.isInput() || e.getFlow() == null)
				continue;
			if (e.getFlow().getFlowType() == FlowType.PRODUCT_FLOW)
				count++;
		}
		return count > 1;
	}

	private IPerson mapActor(Actor inActor, DataSet dataset,
			IEcoSpoldFactory factory) {
		IPerson person = actorToES1Person.get(inActor.getId());
		if (person != null)
			return person;
		person = factory.createPerson();
		person.setNumber(personCounter++);
		person.setName(inActor.getName());
		person.setAddress(inActor.getAddress());
		person.setCountryCode(factory.getCountryCode(inActor.getCountry()));
		person.setEmail(inActor.getEmail());
		person.setTelefax(inActor.getTelefax());
		person.setTelephone(inActor.getTelephone());
		actorToES1Person.put(inActor.getId(), person);
		dataset.getPersons().add(person);
		return person;
	}

	private void mapAdminInfo(Process process, DataSet dataset,
			IEcoSpoldFactory factory) {
		if (process == null || process.getDocumentation() == null)
			return;
		ProcessDocumentation doc = process.getDocumentation();
		IDataGeneratorAndPublication generator = dataset
				.getDataGeneratorAndPublication();
		if (generator == null) {
			generator = factory.createDataGeneratorAndPublication();
			dataset.setDataGeneratorAndPublication(generator);
		}
		generator.setCopyright(doc.isCopyright());
		setDataSetRestrictions(doc, generator);
		addDataSetInformation(process, dataset, factory);

		if (doc.getDataGenerator() != null) {
			IPerson dataGenerator = mapActor(doc.getDataGenerator(), dataset,
					factory);
			generator.setPerson(dataGenerator.getNumber());
		}
		if (doc.getDataDocumentor() != null) {
			IPerson dataDocumentor = mapActor(doc.getDataDocumentor(), dataset,
					factory);
			IDataEntryBy entryBy = dataset.getDataEntryBy();
			if (entryBy == null) {
				entryBy = factory.createDataEntryBy();
				dataset.setDataEntryBy(entryBy);
			}
			entryBy.setPerson(dataDocumentor.getNumber());
		}
		if (doc.getPublication() != null) {
			ISource source = mapSource(doc.getPublication(), dataset, factory);
			generator.setReferenceToPublishedSource(source.getNumber());
		}
	}

	private void setDataSetRestrictions(ProcessDocumentation doc,
			IDataGeneratorAndPublication generator) {
		if (doc.getRestrictions() != null) {
			if (doc.getRestrictions().contains(
					"All information can be accesses by everybody.")) {
				generator.setAccessRestrictedTo(0);
			} else if (doc
					.getRestrictions()
					.contains(
							"Ecoinvent clients have access to LCI results but not to unit process raw data. Members of the ecoinvent quality network (ecoinvent centre) have access to all information.")) {
				generator.setAccessRestrictedTo(2);
			} else if (doc
					.getRestrictions()
					.contains(
							"The ecoinvent administrator has full access to information. Via the web only LCI results are accessible (for ecoinvent clients and for members of the ecoinvent centre).")) {
				generator.setAccessRestrictedTo(3);
			}
		}
	}

	private void addDataSetInformation(Process process, DataSet dataset,
			IEcoSpoldFactory factory) {
		ProcessDocumentation doc = process.getDocumentation();
		IDataSetInformation information = factory.createDataSetInformation();
		dataset.setDataSetInformation(information);
		if (process.getLastChange() != 0)
			information.setTimestamp(toXml(new Date(process.getLastChange())));
		else if (doc != null && doc.getCreationDate() != null)
			information.setTimestamp(toXml(doc.getCreationDate()));
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

	private IExchange mapExchange(Exchange inExchange, boolean multiOutput,
			IEcoSpoldFactory factory) {
		IExchange exchange = factory.createExchange();
		exchange.setNumber(exchangeCounter++);
		exchange.setName(inExchange.getFlow().getName());
		if (inExchange.isInput()) {
			exchange.setInputGroup(mapFlowType(inExchange.getFlow()
					.getFlowType(), true, multiOutput));
		} else {
			exchange.setOutputGroup(mapFlowType(inExchange.getFlow()
					.getFlowType(), false, multiOutput));
		}

		mapFlowCategory(exchange, inExchange.getFlow().getCategory());
		mapFlowInformation(exchange, inExchange.getFlow());
		exchange.setUnit(inExchange.getUnit().getName());

		if (inExchange.getUncertainty() == null) {
			exchange.setMeanValue(inExchange.getAmountValue());
		} else {
			mapUncertainty(inExchange, exchange);
		}
		exchangeToES1Exchange.put(inExchange.getId(), exchange);

		return exchange;
	}

	private void mapFlowCategory(IExchange exchange, Category category) {
		if (category == null)
			return;
		if (category.getParentCategory() == null)
			exchange.setCategory(category.getName());
		else {
			exchange.setCategory(category.getParentCategory().getName());
			exchange.setSubCategory(category.getName());
		}
	}

	private void mapFlowInformation(IExchange exchange, Flow flow) {
		exchange.setCASNumber(flow.getCasNumber());
		exchange.setFormula(flow.getFormula());
		if (flow.getLocation() != null) {
			if (flow.getLocation().getCode() != null) {
				exchange.setLocation(flow.getLocation().getCode());
			} else if (flow.getLocation().getName() != null) {
				exchange.setLocation(flow.getLocation().getName());
			}
		}
		exchange.setInfrastructureProcess(flow.isInfrastructureFlow());

	}

	private int mapFlowType(FlowType flowType, boolean input,
			boolean multiOutput) {
		int group = 0;
		if (input) {
			if (flowType == FlowType.ELEMENTARY_FLOW) {
				group = 4;
			} else if (flowType == FlowType.WASTE_FLOW
					|| flowType == FlowType.PRODUCT_FLOW) {
				group = 5;
			}
		} else {
			if (flowType == FlowType.ELEMENTARY_FLOW) {
				group = 4;
			} else if (flowType == FlowType.WASTE_FLOW) {
				group = 3;
			} else if (flowType == FlowType.PRODUCT_FLOW) {
				if (multiOutput) {
					group = 2;
				} else {
					group = 0;
				}
			}
		}
		return group;
	}

	private void mapLCIACategory(ImpactCategory category, DataSet dataSet,
			IEcoSpoldFactory factory) {
		dataSet.setNumber(datasetCounter++);
		IReferenceFunction refFun = factory.createReferenceFunction();
		dataSet.setReferenceFunction(refFun);
		String subCategory = category.getName();
		String name = null;
		if (subCategory.contains("-")) {
			name = subCategory.substring(subCategory.indexOf("-") + 1);
			while (name.startsWith(" ")) {
				name = name.substring(1);
			}
			subCategory = subCategory.substring(0, subCategory.indexOf("-"));
			while (subCategory.endsWith(" ")) {
				subCategory = subCategory
						.substring(0, subCategory.length() - 1);
			}
		}
		exchangeCounter = 0;
		for (final ImpactFactor factor : category.getImpactFactors()) {
			dataSet.getExchanges().add(mapLCIAFactor(factor, factory));
		}
		refFun.setSubCategory(subCategory);
		refFun.setName(name);
		refFun.setUnit(category.getReferenceUnit());
	}

	private IExchange mapLCIAFactor(ImpactFactor factor,
			IEcoSpoldFactory factory) {
		IExchange exchange = factory.createExchange();
		exchange.setNumber(exchangeCounter++);
		mapFlowCategory(exchange, factor.getFlow().getCategory());
		mapFlowInformation(exchange, factor.getFlow());
		exchange.setUnit(factor.getUnit().getName());
		exchange.setName(factor.getFlow().getName());
		exchange.setMeanValue(factor.getValue());
		return exchange;
	}

	private void mapModelingAndValidation(ProcessDocumentation doc,
			DataSet dataset, IEcoSpoldFactory factory) {
		if (doc == null)
			return;
		if (doc.getReviewDetails() != null) {
			IValidation validation = dataset.getValidation();
			if (validation == null) {
				validation = factory.createValidation();
				dataset.setValidation(validation);
			}
			if (doc.getReviewDetails().contains("Proof reading details: ")) {
				String proofReadingDetails = doc.getReviewDetails().substring(
						doc.getReviewDetails().indexOf(
								"Proof reading details: ") + 23);
				if (proofReadingDetails.contains("Other details: ")) {
					final String otherDetails = proofReadingDetails
							.substring(proofReadingDetails
									.indexOf("Other details: ") + 15);
					validation.setOtherDetails(otherDetails);
					proofReadingDetails = proofReadingDetails.substring(0,
							proofReadingDetails.indexOf("Other details: "));
				}
				validation.setProofReadingDetails(proofReadingDetails);
			} else {
				String otherDetails = doc.getReviewDetails();
				if (otherDetails.contains("Other details: ")) {
					otherDetails = otherDetails.substring(15);
				}
				validation.setOtherDetails(otherDetails);
			}
		}
		if (doc.getSampling() != null) {
			IRepresentativeness representativeness = dataset
					.getRepresentativeness();
			if (representativeness == null) {
				representativeness = factory.createRepresentativeness();
				dataset.setRepresentativeness(representativeness);
			}
			representativeness.setSamplingProcedure(doc.getSampling());
		}

		if (doc.getReviewer() != null) {
			IValidation validation = dataset.getValidation();
			if (validation == null) {
				validation = factory.createValidation();
				dataset.setValidation(validation);
			}
			IPerson reviewer = mapActor(doc.getReviewer(), dataset, factory);
			if (reviewer != null)
				validation.setProofReadingValidator(reviewer.getNumber());
		}

		for (Source source : doc.getSources()) {
			mapSource(source, dataset, factory);
		}

	}

	private IReferenceFunction mapQuantitativeReference(Exchange exchange,
			IEcoSpoldFactory factory) {
		IReferenceFunction referenceFunction = factory
				.createReferenceFunction();
		Flow flow = exchange.getFlow();
		referenceFunction.setCASNumber(flow.getCasNumber());
		referenceFunction.setFormula(flow.getFormula());
		referenceFunction.setName(exchange.getFlow().getName());
		referenceFunction.setUnit(exchange.getUnit().getName());
		referenceFunction.setInfrastructureProcess(flow.isInfrastructureFlow());
		referenceFunction.setAmount(exchange.getAmountValue());
		Category category = flow.getCategory();
		if (category != null) {
			if (category.getParentCategory() == null)
				referenceFunction.setCategory(category.getName());
			else {
				referenceFunction.setCategory(category.getParentCategory()
						.getName());
				referenceFunction.setSubCategory(category.getName());
			}
		}
		return referenceFunction;
	}

	private ISource mapSource(Source inSource, DataSet dataset,
			IEcoSpoldFactory factory) {
		ISource source = sourceToES1Source.get(inSource.getId());
		if (source != null)
			return source;
		source = factory.createSource();
		source.setNumber(sourceCounter++);
		source.setFirstAuthor(inSource.getName());
		source.setText(inSource.getDescription());
		source.setTitle(inSource.getTextReference());
		if (inSource.getYear() != null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.set(Calendar.YEAR, inSource.getYear());
			try {
				source.setYear(DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(cal));
			} catch (Exception e) {
				log.warn("failed to set year of source ", e);
			}
		}
		sourceToES1Source.put(inSource.getId(), source);
		dataset.getSources().add(source);
		return source;
	}

	private void mapTechnology(ProcessDocumentation doc, DataSet dataset,
			IEcoSpoldFactory factory) {
		if (doc == null || doc.getTechnology() == null)
			return;
		ITechnology technology = factory.createTechnology();
		technology.setText(doc.getTechnology());
		dataset.setTechnology(technology);
	}

	private void mapTime(ProcessDocumentation doc, DataSet dataset,
			IEcoSpoldFactory factory) {
		if (doc == null)
			return;
		ITimePeriod timePeriod = factory.createTimePeriod();
		if (doc.getValidFrom() != null)
			timePeriod.setStartDate(toXml(doc.getValidFrom()));
		if (doc.getValidUntil() != null)
			timePeriod.setEndDate(toXml(doc.getValidUntil()));
		timePeriod.setText(doc.getTime());
		dataset.setTimePeriod(timePeriod);
	}

	private XMLGregorianCalendar toXml(Date date) {
		if (date == null)
			return null;
		try {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		} catch (Exception e) {
			log.warn("failed to convert date to XML", e);
			return null;
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

	public void exportLCIAMethod(ImpactMethod method) throws Exception {
		IEcoSpold spold = convertLCIAMethod(method);
		String fileName = "lcia_method_" + method.getRefId() + ".xml";
		File file = new File(outDir, fileName);
		EcoSpoldIO.writeTo(file, spold, DataSetType.IMPACT_METHOD);
		clearLocalCache();
	}

	public void exportProcess(Process process) throws Exception {
		IEcoSpold spold = convertProcess(process);
		String fileName = "process_" + process.getRefId() + ".xml";
		File file = new File(outDir, fileName);
		EcoSpoldIO.writeTo(file, spold, DataSetType.PROCESS);
		clearLocalCache();
	}
}