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
import org.openlca.ilcd.util.Processes;
import org.openlca.io.Xml;

public class EpdExport {

	private final Export exp;

	public EpdExport(Export exp) {
		this.exp = exp;
	}

	public Process run(Epd epd) {
		var p = new Process();
		if (epd == null)
			return p;

		var info = Processes.forceDataSetInfo(p);
		info.uuid = epd.refId;
		var name = Processes.forceProcessName(p);
		exp.add(name.name, epd.name);
		exp.add(info.comment, epd.description);
		Categories.toClassification(epd.category)
				.ifPresent(info.classifications::add);

		Processes.forceMethod(p).processType = ProcessType.EPD;
		writeRefFlow(epd, p);
		writeReview(epd, p);
		writePublication(epd, p);

		var ext = new EpdDataSet(p);


		for (var m : epd.modules) {
			if (m.result == null)
				continue;

			var module = new Module();
			module.name = m.name;

			for (var r : m.result.impactResults) {
				if (r.indicator == null)
					continue;
				exp.write(r.indicator);

				var ir = new IndicatorResult();
				ir.indicator = new Indicator();
				ir.indicator.name = r.indicator.name;
				ir.indicator.uuid = r.indicator.refId;
				ir.indicator.unit = r.indicator.referenceUnit;
				ir.indicator.type = Type.LCIA;

				ext.results.add(ir);

				var amount = new Amount();
				amount.value = r.amount;
				amount.module = module;

				ir.amounts.add(amount);
			}

		}
		EpdExtensions.write(ext);

		exp.store.put(p);
		return p;
	}

	private void writeRefFlow(Epd epd, Process process) {
		var product = epd.product;
		if (product == null)
			return;
		var qref = Processes.forceQuantitativeReference(process);
		qref.referenceFlows.add(0);
		qref.type = QuantitativeReferenceType.REFERENCE_FLOWS;
		var exchange = new Exchange();
		exchange.flow = exp.writeRef(epd.product.flow);
		var property = product.flow != null
				? product.flow.getFactor(product.property)
				: null;
		exchange.meanAmount = ReferenceAmount.get(
				product.amount, product.unit, property);
		exchange.resultingAmount = exchange.meanAmount;
		process.exchanges.add(exchange);
	}

	private void writePublication(Epd epd, Process process) {
		var pub = Processes.forcePublication(process);
		pub.version = Version.asString(epd.version);
		pub.lastRevision = Xml.calendar(epd.lastChange);
		if (epd.manufacturer != null) {
			pub.owner = exp.writeRef(epd.manufacturer);
		}
		if (epd.programOperator != null) {
			pub.registrationAuthority = exp.writeRef(epd.programOperator);
		}
	}

	private void writeReview(Epd epd, Process p) {
		if (epd.verifier != null) {
			var reviewer = exp.writeRef(epd.verifier);
			var validation = Processes.forceValidation(p);
			var review = new Review();
			review.reviewers.add(reviewer);
			validation.reviews.add(review);
		}
	}

}
