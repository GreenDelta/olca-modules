package org.openlca.ilcd.epd.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.ExchangeFunction;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.epd.model.EpdDataSet;
import org.openlca.ilcd.epd.model.EpdProfile;
import org.openlca.ilcd.epd.model.Indicator;
import org.openlca.ilcd.epd.model.IndicatorResult;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.LCIAResult;
import org.openlca.ilcd.processes.Process;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class Results {

	static List<IndicatorResult> readResults(
		Process process, EpdProfile profile) {
		if (process == null || profile == null)
			return Collections.emptyList();

		List<IndicatorResult> results = new ArrayList<>();

		// LCI indicator results
		for (var e : process.exchanges) {
			if (e.exchangeFunction != ExchangeFunction.GENERAL_REMINDER_FLOW)
				continue;
			var indicator = profile.indicatorOf(e);
			if (indicator == null)
				continue;
			var result = new IndicatorResult();
			result.indicator = indicator;
			result.amounts.addAll(Amounts.readFrom(e.other, profile));
			results.add(result);
		}

		// LCIA indicator results
		if (process.lciaResults == null)
			return results;
		for (var impact : process.lciaResults) {
			var indicator = profile.indicatorOf(impact);
			if (indicator == null)
				continue;
			var result = new IndicatorResult();
			result.indicator = indicator;
			result.amounts.addAll(Amounts.readFrom(impact.other, profile));
			results.add(result);
		}

		return results;
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
			Amounts.writeAmounts(result.amounts, other, doc);
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
