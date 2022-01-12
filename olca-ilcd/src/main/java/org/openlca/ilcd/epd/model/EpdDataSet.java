package org.openlca.ilcd.epd.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.epd.model.content.ContentDeclaration;
import org.openlca.ilcd.epd.model.qmeta.QMetaData;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.ProcessName;
import org.openlca.ilcd.util.Processes;

public class EpdDataSet {

	public final Process process;
	public String profile;
	public SubType subType;
	public LocalDate publicationDate;

	public SafetyMargins safetyMargins;
	public ContentDeclaration contentDeclaration;
	public QMetaData qMetaData;

	public final List<IndicatorResult> results = new ArrayList<>();
	public final List<ModuleEntry> moduleEntries = new ArrayList<>();
	public final List<Scenario> scenarios = new ArrayList<>();

	public final List<Ref> publishers = new ArrayList<>();
	public final List<Ref> originalEPDs = new ArrayList<>();

	public EpdDataSet(Process process) {
		this.process = Objects.requireNonNull(process);
	}

	public EpdDataSet() {
		this(new Process());
	}

	public IndicatorResult getResult(Indicator indicator) {
		if (indicator == null)
			return null;
		for (var result : results) {
			if (Objects.equals(result.indicator, indicator))
				return result;
		}
		return null;
	}

	public EpdDescriptor toDescriptor(String lang) {
		EpdDescriptor d = new EpdDescriptor();
		if (process == null)
			return d;
		d.refId = process.getUUID();
		ProcessName name = Processes.getProcessName(process);
		if (name != null)
			d.name = LangString.getFirst(name.name, lang, "en");
		return d;
	}

	@Override
	public EpdDataSet clone() {
		var clone = new EpdDataSet(process.clone());
		clone.profile = profile;
		clone.subType = subType;

		if (publicationDate != null) {
			clone.publicationDate = LocalDate.of(
					publicationDate.getYear(),
					publicationDate.getMonthValue(),
					publicationDate.getDayOfMonth());
		}

		clone.safetyMargins = safetyMargins != null
				? safetyMargins.clone()
				: null;
		clone.contentDeclaration = contentDeclaration != null
				? contentDeclaration.clone()
				: null;
		clone.qMetaData = qMetaData != null
				? qMetaData.clone()
				: null;

		for (var result : results) {
			clone.results.add(result.clone());
		}
		for (var entry : moduleEntries) {
			clone.moduleEntries.add(entry.clone());
		}
		for (var scenario : scenarios) {
			clone.scenarios.add(scenario.clone());
		}

		for (var ref : publishers) {
			clone.publishers.add(ref.clone());
		}
		for (var ref : originalEPDs) {
			clone.originalEPDs.add(ref.clone());
		}

		return clone;
	}

	/**
	 * Returns the product exchange of the EPD data set. If the EPD does not
	 * have such a reference exchange it will be directly created when this
	 * method is called.
	 */
	public Exchange productExchange() {
		var qRef = Processes.forceQuantitativeReference(process);
		qRef.type = QuantitativeReferenceType.REFERENCE_FLOWS;
		if (qRef.referenceFlows.isEmpty())
			qRef.referenceFlows.add(1);
		int id = qRef.referenceFlows.get(0);
		for (Exchange exchange : process.exchanges) {
			if (id == exchange.id)
				return exchange;
		}
		var e = Processes.createExchange(process);
		e.meanAmount = 1d;
		e.resultingAmount = 1d;
		e.id = id;
		return e;
	}
}
