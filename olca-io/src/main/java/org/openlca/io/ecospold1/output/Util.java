package org.openlca.io.ecospold1.output;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.commons.Strings;
import org.openlca.core.model.Flow;
import org.openlca.core.model.RefEntity;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.io.DataSet;
import org.openlca.io.Xml;
import org.openlca.io.ecospold1.output.EcoSpold1Export.EcoSpold1Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Util {

	private static final String INCLUDED_PROCESSES_HEADER = "# Included processes";

	private Util() {
	}

	static String comment(RefEntity model, EcoSpold1Config config) {
		if (model == null) return null;
		if (config == null || !config.writeRefIdInfo)
			return model.description;
		var refIdInfo = "# openLCA export\n"
			+ "This data set was exported from openLCA. The UUID of "
			+ "the data set in openLCA was:\n" + model.refId;
		return Strings.isNotBlank(model.description)
			? model.description + "\n\n" + refIdInfo
			: refIdInfo;
	}

	static String technologyComment(String text) {
		if (Strings.isBlank(text))
			return text;
		var block = includedProcesses(text);
		if (Strings.isBlank(block))
			return text;
		var start = text.indexOf(INCLUDED_PROCESSES_HEADER);
		if (start < 0)
			return text;
		var end = endOfIncludedProcesses(text, start);
		var prefix = text.substring(0, start).stripTrailing();
		var suffix = text.substring(end).stripLeading();
		if (prefix.isEmpty())
			return suffix.isEmpty() ? null : suffix;
		if (suffix.isEmpty())
			return prefix;
		return prefix + "\n\n" + suffix;
	}

	static String includedProcesses(String text) {
		if (Strings.isBlank(text))
			return null;
		var start = text.indexOf(INCLUDED_PROCESSES_HEADER);
		if (start < 0)
			return null;
		var blockStart = start + INCLUDED_PROCESSES_HEADER.length();
		var end = endOfIncludedProcesses(text, start);
		var block = text.substring(blockStart, end).strip();
		return block.isEmpty() ? null : block;
	}

	private static int endOfIncludedProcesses(String text, int start) {
		var nextHeader = text.indexOf("\n# ", start + INCLUDED_PROCESSES_HEADER.length());
		return nextHeader >= 0 ? nextHeader + 1 : text.length();
	}

	static XMLGregorianCalendar toXml(Short year) {
		if (year == null)
			return null;
		try {
			var xmlCal = Xml.types().newXMLGregorianCalendar();
			xmlCal.setYear(year);
			return xmlCal;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Util.class);
			log.warn("failed to set year of source ", e);
			return null;
		}
	}

	static void setDataSetAttributes(DataSet dataSet, RefEntity model) {
		if (model != null)
			dataSet.setNumber((int) model.id);
		dataSet.setGenerator("openLCA");
		dataSet.setTimestamp(Xml.calendar(new Date()));
		// setting a link to the categories file results in an error in the
		// EcoSpold access tool
		// dataSet.setValidCategories("../categories.xml");
	}

	static void mapFlowInformation(IExchange exchange, Flow flow) {
		exchange.setCASNumber(flow.casNumber);
		exchange.setFormula(flow.formula);
		exchange.setInfrastructureProcess(flow.infrastructureFlow);
		if (flow.location != null) {
			if (flow.location.code != null) {
				exchange.setLocation(flow.location.code);
			} else if (flow.location.name != null) {
				exchange.setLocation(flow.location.name);
			}
		}
		exchange.setInfrastructureProcess(flow.infrastructureFlow);
	}

}
