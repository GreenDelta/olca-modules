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
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Review;
import org.openlca.ilcd.processes.epd.EpdValue;
import org.openlca.ilcd.util.EpdIndicatorResult;
import org.openlca.io.Xml;

public class EpdExport {

	private final Export exp;

	public EpdExport(Export exp) {
		this.exp = exp;
	}

	public void write(Epd epd) {
		if (epd == null || exp.store.contains(Process.class, epd.refId))
			return;

		var p = new Process();
		var info = p.withProcessInfo()
				.withDataSetInfo()
				.withUUID(epd.refId);
		var name = info.withProcessName();
		exp.add(name::withBaseName, epd.name);
		exp.add(info::withComment, epd.description);
		Categories.toClassification(epd.category, info::withClassifications);

		p.withModelling()
				.withInventoryMethod()
				.withProcessType(ProcessType.EPD);
		writeRefFlow(epd, p);
		writeReview(epd, p);
		writePublication(epd, p);

		writeResults(epd, p);
		exp.store.put(p);
	}

	private void writeRefFlow(Epd epd, Process process) {
		var product = epd.product;
		if (product == null)
			return;
		var qref = process
				.withProcessInfo()
				.withQuantitativeReference()
				.withType(QuantitativeReferenceType.REFERENCE_FLOWS);
		qref.withReferenceFlows().add(0);

		var exchange = new Exchange()
				.withFlow(exp.writeRef(epd.product.flow));
		var property = product.flow != null
				? product.flow.getFactor(product.property)
				: null;
		exchange.withMeanAmount(
				ReferenceAmount.get(product.amount, product.unit, property));
		exchange.withResultingAmount(exchange.getMeanAmount());
		process.withExchanges().add(exchange);
	}

	private void writePublication(Epd epd, Process process) {
		process.withAdminInfo()
				.withPublication()
				.withVersion(Version.asString(epd.version))
				.withLastRevision(Xml.calendar(epd.lastChange))
				.withOwner(exp.writeRef(epd.manufacturer))
				.withRegistrationAuthority(exp.writeRef(epd.programOperator));
	}


	private void writeReview(Epd epd, Process p) {
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

	private void writeResults(Epd epd, Process ds) {
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
}
