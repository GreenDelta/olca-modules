/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.io.ecospold1.exporter;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.core.database.DataProviderException;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.AdminInfo;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.LCIACategory;
import org.openlca.core.model.LCIAFactor;
import org.openlca.core.model.LCIAMethod;
import org.openlca.core.model.ModelingAndValidation;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Technology;
import org.openlca.core.model.Time;
import org.openlca.core.model.UncertaintyDistributionType;
import org.openlca.ecospold.IAllocation;
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
import org.openlca.ecospold.io.EcoSpoldIO;
import org.openlca.ecospold.io.DataSetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates EcoSpold01 xml files for openLCA processes and LCIA methods
 */
public class EcoSpold01Outputter {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private IDatabase database;
	private int datasetCounter = 0;
	private int exchangeCounter = 0;
	private int personCounter = 0;
	private int sourceCounter = 0;

	private Map<String, IExchange> exchangeToES1Exchange = new HashMap<>();
	private Map<String, ISource> sourceToES1Source = new HashMap<>();
	private Map<String, IPerson> actorToES1Person = new HashMap<>();
	private Map<String, Category> categoryCache = new HashMap<>();

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

	private IEcoSpold convertLCIAMethod(LCIAMethod method)
			throws DataProviderException {
		IEcoSpoldFactory factory = DataSetType.IMPACT_METHOD.getFactory();
		IEcoSpold ecoSpold = factory.createEcoSpold();
		for (LCIACategory category : method.getLCIACategories()) {
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

	private IEcoSpold convertProcess(Process process)
			throws DataProviderException {
		IEcoSpoldFactory factory = DataSetType.PROCESS.getFactory();
		IEcoSpold ecoSpold = factory.createEcoSpold();
		IDataSet iDataSet = factory.createDataSet();
		DataSet dataSet = new DataSet(iDataSet, factory);
		dataSet.setNumber(0);

		mapModelingAndValidation(process.getId(), dataSet, factory);
		mapAdminInfo(process.getId(), dataSet, factory);
		mapTime(process.getId(), dataSet, factory);
		mapTechnology(process.getId(), dataSet, factory);

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
			dataSet.getExchanges()
					.add(mapExchange(
							exchange,
							process.getOutputs(FlowType.ProductFlow).length > 1,
							factory));
		}

		if (process.getGeographyComment() != null) {
			IGeography geography = factory.createGeography();
			geography.setText(process.getGeographyComment());
			dataSet.setGeography(geography);
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

		// map type
		if (process.getProcessType() == ProcessType.LCI_Result) {
			dataSet.getDataSetInformation().setType(2);
		} else {
			if (process.getOutputs(FlowType.ProductFlow).length > 1) {
				dataSet.getDataSetInformation().setType(5);
			} else {
				dataSet.getDataSetInformation().setType(1);
			}
		}

		mapAllocations(process, dataSet, factory);

		ecoSpold.getDataset().add(iDataSet);
		return ecoSpold;
	}

	private Category getCategory(String id) throws DataProviderException {
		Category category = categoryCache.get(id);
		if (category == null) {
			category = database.select(Category.class, id);
			categoryCache.put(id, category);
		}
		return category;
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
		person.setEmail(inActor.getEMail());
		person.setTelefax(inActor.getTelefax());
		person.setTelephone(inActor.getTelephone());
		actorToES1Person.put(inActor.getId(), person);
		dataset.getPersons().add(person);
		return person;
	}

	private void mapAdminInfo(String id, DataSet dataset,
			IEcoSpoldFactory factory) throws DataProviderException {
		AdminInfo adminInfo = database.select(AdminInfo.class, id);
		if (adminInfo == null)
			return;
		IDataGeneratorAndPublication generator = dataset
				.getDataGeneratorAndPublication();
		if (generator == null) {
			generator = factory.createDataGeneratorAndPublication();
			dataset.setDataGeneratorAndPublication(generator);
		}
		generator.setCopyright(adminInfo.getCopyright());
		if (adminInfo.getAccessAndUseRestrictions() != null) {
			if (adminInfo.getAccessAndUseRestrictions().contains(
					"All information can be accesses by everybody.")) {
				generator.setAccessRestrictedTo(0);
			} else if (adminInfo
					.getAccessAndUseRestrictions()
					.contains(
							"Ecoinvent clients have access to LCI results but not to unit process raw data. Members of the ecoinvent quality network (ecoinvent centre) have access to all information.")) {
				generator.setAccessRestrictedTo(2);
			} else if (adminInfo
					.getAccessAndUseRestrictions()
					.contains(
							"The ecoinvent administrator has full access to information. Via the web only LCI results are accessible (for ecoinvent clients and for members of the ecoinvent centre).")) {
				generator.setAccessRestrictedTo(3);
			}
		}

		if (adminInfo.getLastChange() != null
				|| adminInfo.getCreationDate() != null) {
			IDataSetInformation information = factory
					.createDataSetInformation();
			if (adminInfo.getLastChange() != null) {
				information.setTimestamp(toXml(adminInfo.getLastChange()));
			} else if (adminInfo.getCreationDate() != null) {
				information.setTimestamp(toXml(adminInfo.getCreationDate()));
			}
			dataset.setDataSetInformation(information);
		}

		if (adminInfo.getDataGenerator() != null) {
			IPerson dataGenerator = mapActor(adminInfo.getDataGenerator(),
					dataset, factory);
			generator.setPerson(dataGenerator.getNumber());
		}
		if (adminInfo.getDataDocumentor() != null) {
			IPerson dataDocumentor = mapActor(adminInfo.getDataDocumentor(),
					dataset, factory);
			IDataEntryBy entryBy = dataset.getDataEntryBy();
			if (entryBy == null) {
				entryBy = factory.createDataEntryBy();
				dataset.setDataEntryBy(entryBy);
			}
			entryBy.setPerson(dataDocumentor.getNumber());
		}
		if (adminInfo.getPublication() != null) {
			ISource source = mapSource(adminInfo.getPublication(), dataset,
					factory);
			generator.setReferenceToPublishedSource(source.getNumber());
		}
	}

	private void mapAllocations(Process process, DataSet dataset,
			IEcoSpoldFactory factory) {
		Map<String, IAllocation> semIdToFactor = new HashMap<>();
		for (Exchange exchange : process.getExchanges()) {
			for (AllocationFactor inFactor : exchange.getAllocationFactors()) {
				IAllocation factor = semIdToFactor.get(inFactor.getProductId()
						+ inFactor.getValue());
				if (factor == null) {
					factor = factory.createAllocation();
					factor.setFraction((float) (inFactor.getValue() * 100));
					factor.setReferenceToCoProduct(exchangeToES1Exchange.get(
							inFactor.getProductId()).getNumber());
					factor.setAllocationMethod(-1);
					dataset.getAllocations().add(factor);
					semIdToFactor.put(
							inFactor.getProductId() + inFactor.getValue(),
							factor);
				}
				factor.getReferenceToInputOutput()
						.add(exchangeToES1Exchange.get(exchange.getId())
								.getNumber());
			}
		}
	}

	private IExchange mapExchange(Exchange inExchange, boolean multiOutput,
			IEcoSpoldFactory factory) throws DataProviderException {
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

		mapFlowCategory(exchange, inExchange.getFlow().getCategoryId());
		mapFlowInformation(exchange, inExchange.getFlow());
		exchange.setUnit(inExchange.getUnit().getName());

		if (inExchange.getDistributionType() == null
				|| inExchange.getDistributionType() == UncertaintyDistributionType.NONE) {
			exchange.setMeanValue(inExchange.getResultingAmount().getValue());
		} else {
			mapUncertainty(inExchange, exchange);
		}
		exchangeToES1Exchange.put(inExchange.getId(), exchange);

		return exchange;
	}

	private void mapFlowCategory(IExchange exchange, String categoryId)
			throws DataProviderException {
		Category category = getCategory(categoryId);
		if (!category.getId().equals(category.getComponentClass())) {
			if (category.getParentCategory() != null
					&& !category
							.getParentCategory()
							.getId()
							.equals(category.getParentCategory()
									.getComponentClass())) {
				exchange.setSubCategory(category.getName());
				exchange.setCategory(category.getParentCategory().getName());
			} else if (!category.getId().equals(category.getComponentClass())) {
				exchange.setCategory(category.getName());
			}
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
			if (flowType == FlowType.ElementaryFlow) {
				group = 4;
			} else if (flowType == FlowType.WasteFlow
					|| flowType == FlowType.ProductFlow) {
				group = 5;
			}
		} else {
			if (flowType == FlowType.ElementaryFlow) {
				group = 4;
			} else if (flowType == FlowType.WasteFlow) {
				group = 3;
			} else if (flowType == FlowType.ProductFlow) {
				if (multiOutput) {
					group = 2;
				} else {
					group = 0;
				}
			}
		}
		return group;
	}

	private void mapLCIACategory(LCIACategory category, DataSet dataSet,
			IEcoSpoldFactory factory) throws DataProviderException {
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
		for (final LCIAFactor factor : category.getLCIAFactors()) {
			dataSet.getExchanges().add(mapLCIAFactor(factor, factory));
		}
		refFun.setSubCategory(subCategory);
		refFun.setName(name);
		refFun.setUnit(category.getReferenceUnit());
	}

	private IExchange mapLCIAFactor(LCIAFactor factor, IEcoSpoldFactory factory)
			throws DataProviderException {
		IExchange exchange = factory.createExchange();
		exchange.setNumber(exchangeCounter++);
		mapFlowCategory(exchange, factor.getFlow().getCategoryId());
		mapFlowInformation(exchange, factor.getFlow());
		exchange.setUnit(factor.getUnit().getName());
		exchange.setName(factor.getFlow().getName());
		exchange.setMeanValue(factor.getValue());
		return exchange;
	}

	private void mapModelingAndValidation(String id, DataSet dataset,
			IEcoSpoldFactory factory) throws DataProviderException {
		ModelingAndValidation modelingAndValidation = database.select(
				ModelingAndValidation.class, id);
		if (modelingAndValidation == null)
			return;
		if (modelingAndValidation.getDataSetOtherEvaluation() != null) {
			IValidation validation = dataset.getValidation();
			if (validation == null) {
				validation = factory.createValidation();
				dataset.setValidation(validation);
			}
			if (modelingAndValidation.getDataSetOtherEvaluation().contains(
					"Proof reading details: ")) {
				String proofReadingDetails = modelingAndValidation
						.getDataSetOtherEvaluation()
						.substring(
								modelingAndValidation
										.getDataSetOtherEvaluation().indexOf(
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
				String otherDetails = modelingAndValidation
						.getDataSetOtherEvaluation();
				if (otherDetails.contains("Other details: ")) {
					otherDetails = otherDetails.substring(15);
				}
				validation.setOtherDetails(otherDetails);
			}
		}
		if (modelingAndValidation.getSampling() != null) {
			IRepresentativeness representativeness = dataset
					.getRepresentativeness();
			if (representativeness == null) {
				representativeness = factory.createRepresentativeness();
				dataset.setRepresentativeness(representativeness);
			}
			representativeness.setSamplingProcedure(modelingAndValidation
					.getSampling());
		}

		if (modelingAndValidation.getReviewer() != null) {
			IValidation validation = dataset.getValidation();
			if (validation == null) {
				validation = factory.createValidation();
				dataset.setValidation(validation);
			}
			IPerson reviewer = mapActor(modelingAndValidation.getReviewer(),
					dataset, factory);
			if (reviewer != null)
				validation.setProofReadingValidator(reviewer.getNumber());
		}

		for (Source source : modelingAndValidation.getSources()) {
			mapSource(source, dataset, factory);
		}

	}

	private IReferenceFunction mapQuantitativeReference(Exchange exchange,
			IEcoSpoldFactory factory) throws DataProviderException {
		IReferenceFunction referenceFunction = factory
				.createReferenceFunction();
		Flow flow = exchange.getFlow();
		referenceFunction.setCASNumber(flow.getCasNumber());
		referenceFunction.setFormula(flow.getFormula());
		referenceFunction.setName(exchange.getFlow().getName());
		referenceFunction.setUnit(exchange.getUnit().getName());
		referenceFunction.setInfrastructureProcess(flow.isInfrastructureFlow());
		Category category = getCategory(exchange.getFlow().getCategoryId());
		if (!category.getId().equals(category.getComponentClass())) {
			if (category.getParentCategory() != null
					&& !category
							.getParentCategory()
							.getId()
							.equals(category.getParentCategory()
									.getComponentClass())) {
				referenceFunction.setSubCategory(category.getName());
				referenceFunction.setCategory(category.getParentCategory()
						.getName());
			} else if (!category.getId().equals(category.getComponentClass())) {
				referenceFunction.setCategory(category.getName());
			}
		}
		referenceFunction.setAmount(exchange.getResultingAmount().getValue());
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

	private void mapTechnology(String id, DataSet dataset,
			IEcoSpoldFactory factory) throws DataProviderException {
		Technology tech = database.select(Technology.class, id);
		if (tech == null || tech.getDescription() == null)
			return;
		ITechnology technology = factory.createTechnology();
		technology.setText(tech.getDescription());
		dataset.setTechnology(technology);
	}

	private void mapTime(String id, DataSet dataset, IEcoSpoldFactory factory)
			throws DataProviderException {
		Time inTime = database.select(Time.class, id);
		if (inTime == null)
			return;
		ITimePeriod timePeriod = factory.createTimePeriod();
		if (inTime.getStartDate() != null)
			timePeriod.setStartDate(toXml(inTime.getStartDate()));
		if (inTime.getEndDate() != null)
			timePeriod.setEndDate(toXml(inTime.getStartDate()));

		timePeriod.setText(inTime.getComment());
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
		if (oExchange.getDistributionType() != null) {
			switch (oExchange.getDistributionType()) {
			case NORMAL:
				exchange.setMeanValue(oExchange.getUncertaintyParameter1()
						.getValue());
				exchange.setStandardDeviation95(oExchange
						.getUncertaintyParameter2().getValue() * 2);
				exchange.setUncertaintyType(2);
				break;
			case LOG_NORMAL:
				exchange.setMeanValue(oExchange.getUncertaintyParameter1()
						.getValue());
				double sd = oExchange.getUncertaintyParameter2().getValue();
				exchange.setStandardDeviation95(Math.pow(sd, 2));
				exchange.setUncertaintyType(1);
				break;
			case TRIANGLE:
				exchange.setMinValue(oExchange.getUncertaintyParameter1()
						.getValue());
				exchange.setMaxValue(oExchange.getUncertaintyParameter2()
						.getValue());
				exchange.setMostLikelyValue(oExchange
						.getUncertaintyParameter3().getValue());
				exchange.setMeanValue(oExchange.getResultingAmount().getValue());
				exchange.setUncertaintyType(3);
				break;
			case UNIFORM:
				exchange.setMinValue(oExchange.getUncertaintyParameter1()
						.getValue());
				exchange.setMaxValue(oExchange.getUncertaintyParameter2()
						.getValue());
				exchange.setMeanValue(oExchange.getResultingAmount().getValue());
				exchange.setUncertaintyType(4);
				break;
			default:
				exchange.setMeanValue(oExchange.getResultingAmount().getValue());
				exchange.setUncertaintyType(0);
			}
		}
	}

	public void clearGlobalCache() {
		categoryCache.clear();
	}

	public void exportLCIAMethod(LCIAMethod method, final IDatabase database)
			throws Exception {
		this.database = database;
		IEcoSpold spold = convertLCIAMethod(method);
		String fileName = "lcia_method_" + method.getId() + ".xml";
		File file = new File(outDir, fileName);
		EcoSpoldIO.writeTo(file, spold, DataSetType.IMPACT_METHOD);
		clearLocalCache();
	}

	public void exportProcess(Process process, IDatabase database)
			throws Exception {
		this.database = database;
		IEcoSpold spold = convertProcess(process);
		String fileName = "process_" + process.getId() + ".xml";
		File file = new File(outDir, fileName);
		EcoSpoldIO.writeTo(file, spold, DataSetType.PROCESS);
		clearLocalCache();
	}
}
