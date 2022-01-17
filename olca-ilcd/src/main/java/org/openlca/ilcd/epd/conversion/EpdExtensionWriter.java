package org.openlca.ilcd.epd.conversion;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.epd.model.EpdDataSet;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Modelling;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;

class EpdExtensionWriter {

	private final EpdDataSet epd;

	private EpdExtensionWriter(EpdDataSet dataSet) {
		this.epd = dataSet;
	}

	/**
	 * Writes the EPD extensions into the wrapped ILCD process of the given EPD
	 * data set.
	 */
	static void write(EpdDataSet epd) {
		new EpdExtensionWriter(epd).write();
	}

	private void write() {
		if (epd == null)
			return;
		var process = epd.process;
		clearResults(process);
		Results.writeResults(epd);
		writeExtensions();
		// set the format version
		process.otherAttributes.put(
				new QName(Vocab.NS_EPDv2, "epd-version", "epd2"), "1.2");
		process.version = "1.1";
		Cleanup.on(epd);
	}

	/**
	 * Remove all result exchanges.
	 */
	private void clearResults(Process p) {
		if (p == null)
			return;
		var qRef = Processes.getQuantitativeReference(p);
		List<Integer> refFlows = qRef == null
				? Collections.emptyList()
				: qRef.referenceFlows;
		p.exchanges.removeIf(e -> !refFlows.contains(e.id));
		p.lciaResults = null;
	}

	private void writeExtensions() {
		var doc = Dom.createDocument();

		// write the Q-Meta data
		var qMeta = epd.qMetaData;
		if (qMeta == null) {
			Modelling mod = Processes.getModelling(epd.process);
			if (mod != null) {
				// remove possible old data
				mod.other = null;
			}
		} else {
			Modelling mod = Processes.forceModelling(epd.process);
			if (mod.other == null) {
				mod.other = new Other();
			}
			epd.qMetaData.write(mod.other, doc);
		}

		// write info extensions
		var info = Processes.forceDataSetInfo(epd.process);
		Other infoOther = info.other;
		if (infoOther == null) {
			infoOther = new Other();
			info.other = infoOther;
		}
		ModuleConverter.writeModules(epd, infoOther, doc);
		ScenarioConverter.writeScenarios(epd, infoOther, doc);
		SafetyMarginsConverter.write(epd, infoOther, doc);
		if (epd.contentDeclaration != null) {
			epd.contentDeclaration.write(infoOther, doc);
		}
		if (Dom.isEmpty(infoOther)) {
			info.other = null;
		}

		writeProfile();
		writeSubType();
		writePublicationDate();
		PublisherRef.write(epd);
		OriginalEPDRef.write(epd);
	}

	private void writeSubType() {
		if (epd.subType == null) {
			Method m = Processes.getMethod(epd.process);
			if (m == null)
				return;
			m.other = null;
			return;
		}
		var method = Processes.forceMethod(epd.process);
		method.other = new Other();
		var elem = Dom.createElement(Vocab.NS_EPD, "subType");
		if (elem != null) {
			elem.setTextContent(epd.subType.getLabel());
			method.other.any.add(elem);
		}
	}

	private void writeProfile() {
		Map<QName, String> atts = epd.process.otherAttributes;
		if (epd.profile != null) {
			atts.put(Vocab.PROFILE_ATTR, epd.profile);
		} else {
			atts.remove(Vocab.PROFILE_ATTR);
		}
	}

	private void writePublicationDate() {
		var t = Processes.getTime(epd.process);
		var pubDate = epd.publicationDate;
		if (pubDate == null && t == null)
			return;
		var time = t == null
			? Processes.forceTime(epd.process)
			: t;
		if (pubDate == null && time.other == null)
			return;
		var tag = "publicationDateOfEPD";

		// delete it if publication date is null
		if (pubDate == null) {
			Dom.clear(time.other, tag);
			if (Dom.isEmpty(time.other)) {
				time.other = null;
			}
			return;
		}

		// create or update the element
		var elem = Dom.getElement(time.other, tag);
		if (elem != null) {
			elem.setTextContent(pubDate.toString());
			return;
		}
		var newElem = Dom.createElement(Vocab.NS_EPDv2, tag);
		if (newElem == null)
			return;
		newElem.setTextContent(pubDate.toString());
		if (time.other == null) {
			time.other = new Other();
		}
		time.other.any.add(newElem);
	}
}
