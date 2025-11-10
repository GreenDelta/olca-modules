package org.openlca.io.ilcd.output;

import java.util.ArrayList;
import java.util.HashMap;

import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.DataSetType;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.epd.EpdIndicatorResult;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.epd.EpdSubType;
import org.openlca.ilcd.processes.epd.EpdValue;
import org.openlca.ilcd.util.Epds;
import org.openlca.io.Xml;
import org.openlca.commons.Strings;

public class EpdExport {

	private final Export exp;
	private final Epd epd;

	public EpdExport(Export exp, Epd epd) {
		this.exp = exp;
		this.epd = epd;
	}

	public void write() {
		if (epd == null || exp.store.contains(Process.class, epd.refId))
			return;

		var ds = new Process();
		var info = ds.withProcessInfo()
				.withDataSetInfo()
				.withUUID(epd.refId);
		var name = info.withProcessName();
		exp.add(name::withBaseName, epd.name);
		exp.add(info::withComment, epd.description);
		Categories.toClassification(epd.category, info::withClassifications);

		ds.withModelling()
				.withInventoryMethod()
				.withProcessType(ProcessType.EPD);
		writeRefFlow(ds);
		writeReview(ds);
		writePublication(ds);
		writeMetaData(ds);

		writeResults(ds);
		exp.store.put(ds);
	}

	private void writeRefFlow(Process process) {
		var product = epd.product;
		if (product == null)
			return;
		var qref = process
				.withProcessInfo()
				.withQuantitativeReference()
				.withType(QuantitativeReferenceType.REFERENCE_FLOWS);
		qref.withReferenceFlows().add(0);

		var e = new Exchange()
				.withFlow(exp.writeRef(epd.product.flow));
		var property = product.flow != null
				? product.flow.getFactor(product.property)
				: null;
		e.withMeanAmount(
				ReferenceAmount.get(product.amount, product.unit, property));
		e.withResultingAmount(e.getMeanAmount());
		process.withExchanges().add(e);
	}

	private void writePublication(Process ds) {
		Epds.withPublication(ds)
				.withVersion(Version.asString(epd.version))
				.withLastRevision(Xml.calendar(epd.lastChange))
				.withOwner(exp.writeRef(epd.manufacturer))
				.withRegistrationAuthority(exp.writeRef(epd.programOperator))
				.withRegistrationNumber(epd.registrationId);
	}


	private void writeReview(Process p) {
		var reviewer = exp.writeRef(epd.verifier);
		if (reviewer != null) {
			var review = new Review();
			review.withReviewers().add(reviewer);
			p.withModelling()
					.withValidation()
					.withReviews()
					.add(review);
		}
	}

	private void writeResults(Process ds) {
		var results = new HashMap<String, EpdIndicatorResult>();
		for (var m : epd.modules) {
			if (m.result == null)
				continue;

			for (var r : m.result.impactResults) {
				if (r.indicator == null)
					continue;
				var result = results.computeIfAbsent(r.indicator.refId, id -> {
					exp.write(r.indicator);
					var indicator = new Ref()
							.withType(DataSetType.IMPACT_METHOD)
							.withUUID(r.indicator.refId);
					indicator.withName().add(
							new LangString(r.indicator.name, "en"));
					var unit = new Ref()
							.withType(DataSetType.UNIT_GROUP);
					unit.withName().add(
							new LangString(r.indicator.referenceUnit, "en"));
					return EpdIndicatorResult.of(indicator, unit);
				});

				var v = new EpdValue()
						.withModule(m.name)
						.withAmount(r.amount);
				result.values().add(v);
			}
		}

		var values = new ArrayList<>(results.values());
		EpdIndicatorResult.writeClean(ds, values);

	}

	private void writeMetaData(Process ds) {

		// PCR
		var pcr = exp.writeRef(epd.pcr);
		if (pcr != null) {
			Epds.withInventoryMethod(ds)
					.withSources()
					.add(pcr);
		}

		// EPD sub-type
		if (epd.epdType != null) {
			Epds.withSubType(ds, switch (epd.epdType) {
				case AVERAGE_DATASET -> EpdSubType.AVERAGE_DATASET;
				case GENERIC_DATASET -> EpdSubType.GENERIC_DATASET;
				case REPRESENTATIVE_DATASET -> EpdSubType.REPRESENTATIVE_DATASET;
				case SPECIFIC_DATASET -> EpdSubType.SPECIFIC_DATASET;
				case TEMPLATE_DATASET -> EpdSubType.TEMPLATE_DATASET;
			});
		}

		// time
		if (epd.validFrom != null) {
			Epds.withTime(ds)
					.withReferenceYear(Export.getYear(epd.validFrom))
					.withEpdExtension()
					.withPublicationDate(Xml.calendar(epd.validFrom));
		}
		if (epd.validUntil != null) {
			Epds.withTime(ds)
					.withValidUntil(Export.getYear(epd.validUntil));
		}

		// tech. description
		exp.add(() -> Epds.withTechnology(ds).withDescription(), epd.manufacturing);
		exp.add(() -> Epds.withTechnology(ds).withApplicability(), epd.productUsage);

		// location
		if (epd.location != null && Strings.isNotBlank(epd.location.code)) {
			Epds.withLocation(ds).withCode(epd.location.code);
		}

		// use advice & original EPD
		exp.add(() -> Epds.withRepresentativeness(ds).withUseAdvice(), epd.useAdvice);
		var origEpd = exp.writeRef(epd.originalEpd);
		if (origEpd != null) {
			Epds.withRepresentativeness(ds)
					.withEpdExtension()
					.withOriginalEpds()
					.add(origEpd);
		}

		// data generator
		var gen = exp.writeRef(epd.dataGenerator);
		if (gen != null) {
			Epds.withDataGenerator(ds)
					.withContacts()
					.add(gen);
		}
	}
}
