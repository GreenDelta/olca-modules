package org.openlca.io.ecospold1.output;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.core.model.Flow;
import org.openlca.core.model.RefEntity;
import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.io.DataSet;
import org.openlca.io.Xml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Util {

	private Util() {
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
