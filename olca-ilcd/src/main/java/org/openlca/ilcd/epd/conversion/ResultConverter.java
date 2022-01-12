package org.openlca.ilcd.epd.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.ExchangeFunction;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.epd.model.Amount;
import org.openlca.ilcd.epd.model.EpdDataSet;
import org.openlca.ilcd.epd.model.EpdProfile;
import org.openlca.ilcd.epd.model.Indicator;
import org.openlca.ilcd.epd.model.IndicatorResult;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.LCIAResult;
import org.openlca.ilcd.processes.Process;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class ResultConverter {

	static List<IndicatorResult> readResults(
		Process process, EpdProfile profile) {
		if (process == null || profile == null)
			return Collections.emptyList();
		List<IndicatorResult> results = new ArrayList<>();
		results.addAll(readLciResults(process, profile));
		results.addAll(readLciaResults(process, profile));
		return results;
	}

	private static List<IndicatorResult> readLciResults(
		Process process, EpdProfile profile) {
		List<IndicatorResult> results = new ArrayList<>();
		for (Exchange exchange : process.exchanges) {
			IndicatorResult result = readResult(exchange.flow,
				exchange.other, profile);
			if (result != null)
				results.add(result);
		}
		return results;
	}

	private static List<IndicatorResult> readLciaResults(
		Process process, EpdProfile profile) {
		List<IndicatorResult> results = new ArrayList<>();
		if (process.lciaResults == null)
			return results;
		for (LCIAResult element : process.lciaResults) {
			IndicatorResult result = readResult(element.method,
				element.other, profile);
			if (result != null)
				results.add(result);
		}
		return results;
	}

	private static IndicatorResult readResult(
		Ref ref, Other extension, EpdProfile profile) {
		if (ref == null)
			return null;
		Indicator indicator = profile.indicator(ref.uuid);
		if (indicator == null)
			return null;
		IndicatorResult result = new IndicatorResult();
		result.indicator = indicator;
		List<Amount> amounts = AmountConverter.readAmounts(
			extension, profile);
		result.amounts.addAll(amounts);
		return result;
	}

	static void writeResults(EpdDataSet epd) {
		if (epd == null || epd.process == null)
			return;
		var doc = Dom.createDocument();
		for (var result : epd.results) {
			var indicator = result.indicator;
			if (indicator == null)
				continue;
			var other = indicator.type == Indicator.Type.LCI
				? initFlow(epd.process, indicator)
				: initImpact(epd.process, indicator);
			AmountConverter.writeAmounts(result.amounts, other, doc);
			addUnitRef(other, indicator, doc);
		}
	}

	private static Other initFlow(Process p, Indicator indicator) {
		int nextId = 1;
		for (var e : p.exchanges) {
			if (e.id >= nextId) {
				nextId = e.id + 1;
			}
		}
		var e = new Exchange();
		e.id = nextId;
		p.exchanges.add(e);
		e.flow = refOf(indicator);
		e.exchangeFunction = ExchangeFunction.GENERAL_REMINDER_FLOW;
		e.direction = indicator.isInput != null && indicator.isInput
			? ExchangeDirection.INPUT
			: ExchangeDirection.OUTPUT;

		Other other = new Other();
		e.other = other;
		return other;
	}

	private static Other initImpact(Process process, Indicator indicator) {
		var r = new LCIAResult();
		process.add(r);
		r.method = refOf(indicator);
		Other other = new Other();
		r.other = other;
		return other;
	}

	private static Ref refOf(Indicator indicator) {
		if (indicator == null)
			return null;
		return indicator.getRef("en");
	}

	private static void addUnitRef(
		Other other, Indicator indicator, Document doc) {
		if (other == null || indicator == null)
			return;
		Element root = doc.createElementNS(Vocab.NS_EPD,
			"epd:referenceToUnitGroupDataSet");
		root.setAttribute("type", "unit group data set");
		root.setAttribute("refObjectId", indicator.unitGroupUUID);
		String uri = "../unitgroups/" + indicator.unitGroupUUID;
		root.setAttribute("uri", uri);
		Element description = doc.createElementNS(
			"http://lca.jrc.it/ILCD/Common", "common:shortDescription");
		description.setTextContent(indicator.unit);
		root.appendChild(description);
		other.any.add(root);
	}
}
