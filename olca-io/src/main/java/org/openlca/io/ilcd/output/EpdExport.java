package org.openlca.io.ilcd.output;

import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.ProcessType;
import org.openlca.ilcd.commons.QuantitativeReferenceType;
import org.openlca.ilcd.epd.conversion.EpdExtensions;
import org.openlca.ilcd.epd.model.Amount;
import org.openlca.ilcd.epd.model.EpdDataSet;
import org.openlca.ilcd.epd.model.Indicator;
import org.openlca.ilcd.epd.model.Indicator.Type;
import org.openlca.ilcd.epd.model.IndicatorResult;
import org.openlca.ilcd.epd.model.Module;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.processes.Review;
import org.openlca.io.Xml;

import java.util.HashMap;

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
				.getDataSetInfo()
				.withUUID(epd.refId);
		var name = info.withProcessName();
		exp.add(name::withBaseName, epd.name);
		exp.add(info::withComment, epd.description);
		Categories.toClassification(epd.category)
				.ifPresent(c -> info.withClassifications().add(c));

		p.withModelling()
				.withInventoryMethod()
				.withProcessType(ProcessType.EPD);
		writeRefFlow(epd, p);
		writeReview(epd, p);
		writePublication(epd, p);

		var ext = new EpdDataSet(p);
		writeResults(epd, ext);
		EpdExtensions.write(ext);
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

	private void writeResults(Epd epd, EpdDataSet ext) {
		var results = new HashMap<String, IndicatorResult>();
		for (var m : epd.modules) {
			if (m.result == null)
				continue;
			var module = new Module();
			module.name = m.name;

			for (var r : m.result.impactResults) {
				if (r.indicator == null)
					continue;
				var result = results.computeIfAbsent(r.indicator.refId, id -> {
					exp.write(r.indicator);
					var ir = new IndicatorResult();
					ir.indicator = new Indicator();
					ir.indicator.name = r.indicator.name;
					ir.indicator.uuid = r.indicator.refId;
					ir.indicator.unit = r.indicator.referenceUnit;
					ir.indicator.type = Type.LCIA;
					ext.results.add(ir);
					return ir;
				});

				var amount = new Amount();
				amount.value = r.amount;
				amount.module = module;
				result.amounts.add(amount);
			}
		}
	}
}
