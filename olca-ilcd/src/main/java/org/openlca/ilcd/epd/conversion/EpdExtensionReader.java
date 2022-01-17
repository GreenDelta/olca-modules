package org.openlca.ilcd.epd.conversion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.epd.model.Amount;
import org.openlca.ilcd.epd.model.EpdDataSet;
import org.openlca.ilcd.epd.model.EpdProfile;
import org.openlca.ilcd.epd.model.IndicatorResult;
import org.openlca.ilcd.epd.model.ModuleEntry;
import org.openlca.ilcd.epd.model.Scenario;
import org.openlca.ilcd.epd.model.SubType;
import org.openlca.ilcd.epd.model.content.ContentDeclaration;
import org.openlca.ilcd.epd.model.qmeta.QMetaData;
import org.openlca.ilcd.epd.util.Strings;
import org.openlca.ilcd.processes.Method;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;
import org.slf4j.LoggerFactory;

/**
 * Converts an ILCD process data set to an EPD data set.
 */
record EpdExtensionReader(Process process, EpdProfile profile) {

	static EpdDataSet read(Process process, EpdProfile profile) {
		return new EpdExtensionReader(process, profile).read();
	}

	private EpdDataSet read() {
		var epd = new EpdDataSet(process);
		readExtensions(epd);
		mapResults(epd);
		return epd;
	}

	private void readExtensions(EpdDataSet epd) {
		epd.profile = process.otherAttributes.get(Vocab.PROFILE_ATTR);
		readSubType(epd);
		readPublicationDate(epd);
		PublisherRef.read(epd);
		OriginalEPDRef.read(epd);
		epd.qMetaData = QMetaData.read(process);

		// read the extensions that are stored under `dataSetInformation`
		var info = Processes.getDataSetInfo(process);
		if (info == null || info.other == null)
			return;
		Other other = info.other;
		List<Scenario> scenarios = ScenarioConverter.readScenarios(other);
		epd.scenarios.addAll(scenarios);
		List<ModuleEntry> modules = ModuleConverter.readModules(other, profile);
		epd.moduleEntries.addAll(modules);
		epd.safetyMargins = SafetyMarginsConverter.read(other);
		epd.contentDeclaration = ContentDeclaration.read(other);
	}

	private void readSubType(EpdDataSet dataSet) {
		if (process.modelling == null)
			return;
		Method method = process.modelling.method;
		if (method == null || method.other == null)
			return;
		var elem = Dom.getElement(method.other, "subType");
		if (elem != null) {
			dataSet.subType = SubType.fromLabel(elem.getTextContent());
		}
	}

	private void readPublicationDate(EpdDataSet epd) {
		var time = Processes.getTime(epd.process);
		if (time == null || time.other == null)
			return;
		var elem = Dom.getElement(time.other, "publicationDateOfEPD");
		if (elem == null)
			return;
		var text = elem.getTextContent();
		if (Strings.nullOrEmpty(text))
			return;
		try {
			epd.publicationDate = LocalDate.parse(
				text, DateTimeFormatter.ISO_DATE);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("Invalid format for publication date: " + text, e);
		}
	}

	private void mapResults(EpdDataSet dataSet) {
		var results = Results.readResults(process, profile);
		dataSet.results.addAll(results);
		// data sets may not have the module-entry extension, thus we have to
		// find the module entries for such data sets from the results
		for (IndicatorResult result : results) {
			for (Amount amount : result.amounts) {
				ModuleEntry entry = findModuleEntry(dataSet, amount);
				if (entry != null)
					continue;
				entry = new ModuleEntry();
				entry.module = amount.module;
				entry.scenario = amount.scenario;
				dataSet.moduleEntries.add(entry);
			}
		}
	}

	private ModuleEntry findModuleEntry(EpdDataSet dataSet, Amount amount) {
		for (ModuleEntry entry : dataSet.moduleEntries) {
			if (Objects.equals(entry.module, amount.module)
					&& Objects
				.equals(entry.scenario, amount.scenario))
				return entry;
		}
		return null;
	}
}
