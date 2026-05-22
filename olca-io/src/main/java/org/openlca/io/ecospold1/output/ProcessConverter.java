package org.openlca.io.ecospold1.output;

import java.util.Date;

import org.openlca.commons.Strings;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.Version;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.ecospold.model.DataSet;
import org.openlca.ecospold.model.IDataSet;
import org.openlca.ecospold.model.IExchange;
import org.openlca.ecospold.model.IReferenceFunction;
import org.openlca.io.Xml;
import org.openlca.io.ecospold1.output.EcoSpold1Export.EcoSpold1Config;
import org.openlca.util.Exchanges;
import org.openlca.util.Processes;

class ProcessConverter {

	private final Process process;
	private final EcoSpold1Config config;
	private final FlowNameFormatter flowNames;

	static IDataSet convert(
		Process process, EcoSpold1Config config, FlowNameFormatter flowNames) {
		return new ProcessConverter(process, config, flowNames).doIt();
	}

	private ProcessConverter(
		Process process, EcoSpold1Config config, FlowNameFormatter flowNames) {
		this.process = process;
		this.config = config;
		this.flowNames = flowNames;
	}

	private IDataSet doIt() {
		var ds = DataSet.newProcess();
		Util.setDataSetAttributes(ds, process);
		mapDocumentation(ds);
		mapExchanges(ds);
		// TODO: map allocation factors
		// mapAllocations(process, dataSet, factory);
		if (config.withDefaults) {
			SchemaDefaults.write(ds);
		}
		return ds.root();
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
		var info = ds.withDataSetInformation();
		info.setEnergyValues(0);
		info.setImpactAssessmentResult(false);
		info.setLanguageCode(ds.factory().getLanguageCode("en"));
		info.setLocalLanguageCode(ds.factory().getLanguageCode("en"));
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

	private void mapGeography(ProcessDoc doc, DataSet ds) {
		var geography = ds.withGeography();
		Location location = process.location;
		if (location != null) {
			geography.setLocation(location.code);
		}
		if (doc.geography != null) {
			geography.setText(doc.geography);
		}
	}

	private void mapModelingAndValidation(ProcessDoc doc, DataSet ds) {
		mapValidation(doc, ds);
		for (Source source : doc.sources) {
			Util.sourceOf(source, ds);
		}
		if (doc.samplingProcedure == null)
			return;
		var repr = ds.withRepresentativeness();
		repr.setSamplingProcedure(doc.samplingProcedure);
	}

	private void mapValidation(ProcessDoc doc, DataSet ds) {
		if (doc.reviews.isEmpty())
			return;
		var r = doc.reviews.getFirst();
		var validation = ds.withValidation();
		validation.setProofReadingDetails(
				r.details != null ? r.details : "none");
		if (!r.reviewers.isEmpty()) {
			int reviewer = Util.personOf(r.reviewers.getFirst(), ds);
			if (reviewer > 0) {
				validation.setProofReadingValidator(reviewer);
			}
		}
	}

	private void mapAdminInfo(ProcessDoc doc, DataSet ds) {
		var generator = ds.withDataGeneratorAndPublication();
		generator.setCopyright(doc.copyright);
		generator.setAccessRestrictedTo(0);
		generator.setDataPublishedIn(0);
		if (doc.dataGenerator != null) {
			int n = Util.personOf(doc.dataGenerator, ds);
			generator.setPerson(n);
		}
		mapEntryBy(doc, ds);
		if (doc.publication != null) {
			int source = Util.sourceOf(doc.publication, ds);
			generator.setReferenceToPublishedSource(source);
		}
	}

	private void mapEntryBy(ProcessDoc doc, DataSet ds) {
		if (doc.dataDocumentor == null)
			return;
		int n = Util.personOf(doc.dataDocumentor, ds);
		var entryBy = ds.withDataEntryBy();
		entryBy.setPerson(n);
	}

	private void mapTechnology(ProcessDoc doc, DataSet ds) {
		var text = doc.technology;
		if (Strings.isBlank(text))
			return;
		var parts = text.split("# Included processes");
		if (parts.length == 0) {
			ds.withTechnology().setText(text);
			return;
		}

		var techPart = parts[0].strip();
		if (Strings.isNotBlank(techPart)) {
			ds.withTechnology().setText(techPart);
		}

		var incPart = parts[1].strip();
		if (Strings.isNotBlank(incPart)) {
			ds.withReferenceFunction().setIncludedProcesses(incPart);
		}
	}

	private void mapTime(ProcessDoc doc, DataSet ds) {
		var r = ds.withTimePeriod();
		r.setDataValidForEntirePeriod(true);
		if (doc.validFrom != null) {
			r.setStartDate(Xml.calendar(doc.validFrom));
		}
		if (doc.validUntil != null) {
			r.setEndDate(Xml.calendar(doc.validUntil));
		}
		r.setText(doc.time);
	}

	private void mapExchanges(DataSet ds) {
		var qRef = process.quantitativeReference;
		for (var e : process.exchanges) {
			if (e.flow == null)
				continue;
			boolean isQRef = e.equals(qRef);
			var ix = ds.withExchange();
			ix.setNumber((int) e.flow.id);
			ix.setName(flowNames.of(process, e));

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

			Categories.map(e.flow.category, ix);
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
			if (isQRef) {
				mapRefFlow(ds, e, ix);
			}
		}
	}

	private void mapRefFlow(DataSet ds, Exchange e, IExchange ix) {
		var refFun = mapQuantitativeReference(e, ds);
		refFun.setGeneralComment(Util.comment(process, config));
		refFun.setInfrastructureProcess(process.infrastructureProcess);
		var loc = process.location;
		if (loc != null && !Strings.isBlank(loc.code)) {
			ix.setLocation(loc.code);
		}
	}

	private IReferenceFunction mapQuantitativeReference(Exchange e, DataSet ds) {
		var refFun = ds.withReferenceFunction();
		var flow = e.flow;
		refFun.setDatasetRelatesToProduct(true);
		refFun.setCASNumber(flow.casNumber);
		refFun.setFormula(flow.formula);
		refFun.setName(flowNames.of(process, e));
		refFun.setLocalName(refFun.getName());
		refFun.setUnit(e.unit.name);
		refFun.setInfrastructureProcess(flow.infrastructureFlow);
		refFun.setAmount(e.amount);
		var category = flow.category != null
				? flow.category
				: process.category;
		Categories.map(category, refFun);
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
