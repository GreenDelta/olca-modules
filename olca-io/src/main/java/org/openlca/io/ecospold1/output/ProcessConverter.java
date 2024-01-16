package org.openlca.io.ecospold1.output;

import java.util.Date;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDoc;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.Version;
import org.openlca.ecospold.IDataEntryBy;
import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IGeography;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.ITechnology;
import org.openlca.ecospold.io.DataSet;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.io.Xml;
import org.openlca.util.Exchanges;
import org.openlca.util.Processes;

class ProcessConverter {

	private final Process process;
	private final ExportConfig config;
	private final IEcoSpoldFactory factory = DataSetType.PROCESS.getFactory();
	private final ActorSourceMapper actorSourceMapper;

	static IDataSet convert(Process process, ExportConfig config) {
		return new ProcessConverter(process, config).doIt();
	}

	private ProcessConverter(Process process, ExportConfig config) {
		this.process = process;
		this.config = config;
		actorSourceMapper = new ActorSourceMapper(factory, config);
	}

	private IDataSet doIt() {
		var ds = factory.createDataSet();
		var dataSet = new DataSet(ds, factory);
		Util.setDataSetAttributes(dataSet, process);
		mapDocumentation(dataSet);
		mapExchanges(dataSet);
		// TODO: map allocation factors
		// mapAllocations(process, dataSet, factory);
		if (config.isCreateDefaults()) {
			StructureDefaults.add(dataSet, factory);
		}
		return ds;
	}

	private void mapDocumentation(DataSet dataSet) {
		var doc = process.documentation;
		if (doc == null)
			return;
		mapDataSetInformation(doc, dataSet);
		mapTime(doc, dataSet);
		mapTechnology(doc, dataSet);
		mapGeography(doc, dataSet);
		mapModelingAndValidation(doc, dataSet);
		mapAdminInfo(doc, dataSet);
	}

	private void mapDataSetInformation(ProcessDoc doc, DataSet ds) {
		var info = factory.createDataSetInformation();
		ds.setDataSetInformation(info);
		info.setEnergyValues(0);
		info.setImpactAssessmentResult(false);
		info.setLanguageCode(factory.getLanguageCode("en"));
		info.setLocalLanguageCode(factory.getLanguageCode("en"));
		if (process.lastChange != 0) {
			info.setTimestamp(Xml.calendar(process.lastChange));
		} else {
			var date = doc.creationDate != null
					? doc.creationDate
					: new Date();
			info.setTimestamp(Xml.calendar(date));
		}
		info.setType(getProcessType());
		var v = new Version(process.version);
		info.setVersion(v.getMajor());
		info.setInternalVersion(v.getMinor());
	}

	private int getProcessType() {
		if (process.processType == ProcessType.LCI_RESULT)
			return 2;
		return Processes.isMultiFunctional(process)
				? 5
				: 1;
	}

	private void mapGeography(ProcessDoc doc, DataSet dataSet) {
		IGeography geography = factory.createGeography();
		dataSet.setGeography(geography);
		Location location = process.location;
		if (location != null)
			geography.setLocation(location.code);
		if (doc.geography != null)
			geography.setText(doc.geography);
		if (!config.isCreateDefaults())
			return;
		if (geography.getLocation() == null)
			geography.setLocation("GLO");
	}

	private void mapModelingAndValidation(ProcessDoc doc, DataSet ds) {
		mapValidation(doc, ds);
		for (Source source : doc.sources)
			actorSourceMapper.map(source, ds);
		if (doc.samplingProcedure == null)
			return;
		var repr = ds.getRepresentativeness();
		if (repr == null) {
			repr = factory.createRepresentativeness();
			ds.setRepresentativeness(repr);
		}
		repr.setSamplingProcedure(doc.samplingProcedure);
	}

	private void mapValidation(ProcessDoc doc, DataSet ds) {
		if (doc.reviews.isEmpty())
			return;
		var r = doc.reviews.get(0);
		var validation = ds.getValidation();
		if (validation == null) {
			validation = factory.createValidation();
			ds.setValidation(validation);
		}
		int reviewer = actorSourceMapper.map(r.reviewer, ds);
		if (reviewer > 0) {
			validation.setProofReadingValidator(reviewer);
		}
		validation.setProofReadingDetails(
				r.details != null ? r.details : "none");
	}

	private void mapAdminInfo(ProcessDoc doc, DataSet dataset) {
		var generator = dataset.getDataGeneratorAndPublication();
		if (generator == null) {
			generator = factory.createDataGeneratorAndPublication();
			dataset.setDataGeneratorAndPublication(generator);
		}
		generator.setCopyright(doc.copyright);
		generator.setAccessRestrictedTo(0);
		generator.setDataPublishedIn(0);
		if (doc.dataGenerator != null) {
			int n = actorSourceMapper.map(doc.dataGenerator, dataset);
			generator.setPerson(n);
		}
		mapEntryBy(doc, dataset);
		if (doc.publication != null) {
			int source = actorSourceMapper.map(doc.publication, dataset);
			generator.setReferenceToPublishedSource(source);
		}
	}

	private void mapEntryBy(ProcessDoc doc, DataSet dataset) {
		if (doc.dataDocumentor == null)
			return;
		int n = actorSourceMapper.map(doc.dataDocumentor, dataset);
		IDataEntryBy entryBy = dataset.getDataEntryBy();
		if (entryBy == null) {
			entryBy = factory.createDataEntryBy();
			dataset.setDataEntryBy(entryBy);
		}
		entryBy.setPerson(n);
	}

	private void mapTechnology(ProcessDoc doc, DataSet dataset) {
		ITechnology technology = factory.createTechnology();
		technology.setText(doc.technology);
		dataset.setTechnology(technology);
	}

	private void mapTime(ProcessDoc doc, DataSet dataset) {
		var time = factory.createTimePeriod();
		time.setDataValidForEntirePeriod(true);
		if (doc.validFrom != null)
			time.setStartDate(Xml.calendar(doc.validFrom));
		if (doc.validUntil != null)
			time.setEndDate(Xml.calendar(doc.validUntil));
		time.setText(doc.time);
		dataset.setTimePeriod(time);
		if (!config.isCreateDefaults())
			return;
		if (time.getStartDate() == null)
			time.setStartDate(Xml.calendar(new Date(253370761200000L)));
		if (time.getEndDate() == null)
			time.setEndDate(Xml.calendar(new Date(253402210800000L)));
	}

	private void mapExchanges(DataSet ds) {
		var qRef = process.quantitativeReference;
		for (var e : process.exchanges) {
			if (e.flow == null)
				continue;
			boolean isQRef = e.equals(qRef);
			var ix = factory.createExchange();
			ix.setNumber((int) e.flow.id);
			ix.setName(e.flow.name);

			// input/output group
			if (Exchanges.isProviderFlow(e)) {
				ix.setOutputGroup(isQRef ? 0 : 2);
			} else if (Exchanges.isLinkable(e)) {
				ix.setInputGroup(5);
			} else if (e.isInput) {
				ix.setInputGroup(4);
			} else {
				ix.setOutputGroup(4);
			}

			Categories.map(e.flow.category, ix, config);
			Util.mapFlowInformation(ix, e.flow);
			if (e.unit != null) {
				ix.setUnit(e.unit.name);
			}
			if (e.uncertainty == null) {
				ix.setMeanValue(e.amount);
			} else {
				mapUncertainty(e, ix);
			}
			mapComment(e, ix);
			ds.getExchanges().add(ix);
			if (isQRef) {
				mapRefFlow(ds, e, ix);
			}
		}
	}

	private void mapRefFlow(DataSet ds, Exchange e, IExchange ix) {
		var refFun = mapQuantitativeReference(e);
		ds.setReferenceFunction(refFun);
		refFun.setGeneralComment(process.description);
		refFun.setInfrastructureProcess(process.infrastructureProcess);
		var loc = process.location;
		if (loc != null) {
			ix.setLocation(loc.code);
		} else if (config.isCreateDefaults()) {
			ix.setLocation("GLO");
		}
	}

	private IReferenceFunction mapQuantitativeReference(Exchange e) {
		var refFun = factory.createReferenceFunction();
		Flow flow = e.flow;
		refFun.setDatasetRelatesToProduct(true);
		refFun.setCASNumber(flow.casNumber);
		refFun.setFormula(flow.formula);
		refFun.setName(e.flow.name);
		refFun.setLocalName(refFun.getName());
		refFun.setUnit(e.unit.name);
		refFun.setInfrastructureProcess(flow.infrastructureFlow);
		refFun.setAmount(e.amount);
		var category = flow.category != null
				? flow.category
				: process.category;
		Categories.map(category, refFun, config);
		refFun.setLocalCategory(refFun.getCategory());
		refFun.setLocalSubCategory(refFun.getSubCategory());
		return refFun;
	}

	private void mapComment(Exchange inExchange, IExchange exchange) {
		if (inExchange.description == null) {
			exchange.setGeneralComment(inExchange.dqEntry);
		} else if (inExchange.dqEntry == null) {
			exchange.setGeneralComment(inExchange.description);
		} else {
			exchange.setGeneralComment(
					inExchange.dqEntry + "; " + inExchange.description);
		}
	}

	private void mapUncertainty(Exchange oExchange, IExchange e) {
		Uncertainty uncertainty = oExchange.uncertainty;
		if (uncertainty == null || uncertainty.distributionType == null)
			return;
		switch (uncertainty.distributionType) {
			case NORMAL -> {
				e.setMeanValue(uncertainty.parameter1);
				e.setStandardDeviation95(uncertainty.parameter2 * 2);
				e.setUncertaintyType(2);
			}
			case LOG_NORMAL -> {
				e.setMeanValue(uncertainty.parameter1);
				double sd = uncertainty.parameter2;
				e.setStandardDeviation95(Math.pow(sd, 2));
				e.setUncertaintyType(1);
			}
			case TRIANGLE -> {
				e.setMinValue(uncertainty.parameter1);
				e.setMostLikelyValue(uncertainty.parameter2);
				e.setMaxValue(uncertainty.parameter3);
				e.setMeanValue(oExchange.amount);
				e.setUncertaintyType(3);
			}
			case UNIFORM -> {
				e.setMinValue(uncertainty.parameter1);
				e.setMaxValue(uncertainty.parameter2);
				e.setMeanValue(oExchange.amount);
				e.setUncertaintyType(4);
			}
			default -> e.setMeanValue(oExchange.amount);
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
