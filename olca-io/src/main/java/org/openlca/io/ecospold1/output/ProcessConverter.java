package org.openlca.io.ecospold1.output;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.openlca.commons.Strings;
import org.openlca.core.io.maps.FlowMapEntry;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
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
	private final Map<String, FlowMapEntry> flowMap;

	static IDataSet convert(
		Process process, EcoSpold1Config config, FlowNameFormatter flowNames) {
		return new ProcessConverter(process, config, flowNames).doIt();
	}

	private ProcessConverter(
		Process process, EcoSpold1Config config, FlowNameFormatter flowNames) {
		this.process = process;
		this.config = config;
		this.flowNames = flowNames;
		this.flowMap = config.flowMap != null
			? config.flowMap.index()
			: Collections.emptyMap();
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

	private void mapDocumentation(DataSet ds) {
		var doc = process.documentation;
		if (doc == null)
			return;
		mapDataSetInformation(doc, ds);
		mapTime(doc, ds);
		mapTechnology(doc, ds);
		mapGeography(doc, ds);
		mapModelingAndValidation(doc, ds);
		mapAdminInfo(doc, ds);
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
		info.setVersion(Util.versionOf(process));
		info.setInternalVersion(Util.internalVersionOf(process));
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
		if (Strings.isBlank(doc.samplingProcedure)
			&& Strings.isBlank(doc.dataTreatment)
			&& Strings.isBlank(doc.dataSelection)
			&& Strings.isBlank(doc.dataCompleteness))
			return;
		var repr = ds.withRepresentativeness();
		repr.setSamplingProcedure(doc.samplingProcedure);
		repr.setExtrapolations(doc.dataTreatment);
		repr.setUncertaintyAdjustments(doc.dataSelection);
		repr.setProductionVolume(doc.dataCompleteness);
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
		if (parts.length == 1) {
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
		var b = ExchangeBuilder.of(ds, flowMap, process, flowNames);
		for (var e : process.exchanges) {
			var ix = b.create(e);
			if (ix == null)
				continue;
			mapComment(e, ix);

			// input/output group
			boolean isQRef = e.equals(qRef);
			if (Exchanges.isProviderFlow(e)) {
				ix.setOutputGroup(isQRef ? 0 : 2);
			} else if (Exchanges.isLinkable(e)) {
				ix.setInputGroup(5);
			} else if (e.isInput) {
				ix.setInputGroup(4);
			} else {
				ix.setOutputGroup(4);
			}
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

	private void mapComment(Exchange o, IExchange ex) {
		if (Strings.isBlank(o.description)) {
			ex.setGeneralComment(o.dqEntry);
		} else if (Strings.isBlank(o.dqEntry)) {
			ex.setGeneralComment(o.description);
		} else {
			ex.setGeneralComment(o.dqEntry + "; " + o.description);
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
