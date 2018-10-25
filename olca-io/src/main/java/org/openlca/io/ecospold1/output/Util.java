package org.openlca.io.ecospold1.output;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.core.model.Flow;
import org.openlca.core.model.RootEntity;
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
			XMLGregorianCalendar xmlCal = Xml.datatypeFactory
					.newXMLGregorianCalendar();
			xmlCal.setYear(year);
			return xmlCal;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Util.class);
			log.warn("failed to set year of source ", e);
			return null;
		}
	}

	static void setDataSetAttributes(DataSet dataSet, RootEntity model) {
		if (model != null)
			dataSet.setNumber((int) model.getId());
		dataSet.setGenerator("openLCA");
		dataSet.setTimestamp(Xml.calendar(new Date()));
		// setting a link to the categories file results in an error in the
		// EcoSpold access tool
		// dataSet.setValidCategories("../categories.xml");
	}

	static void mapFlowInformation(IExchange exchange, Flow flow) {
		exchange.setCASNumber(flow.getCasNumber());
		exchange.setFormula(flow.getFormula());
		exchange.setInfrastructureProcess(flow.isInfrastructureFlow());
		if (flow.getLocation() != null) {
			if (flow.getLocation().getCode() != null) {
				exchange.setLocation(flow.getLocation().getCode());
			} else if (flow.getLocation().getName() != null) {
				exchange.setLocation(flow.getLocation().getName());
			}
		}
		exchange.setInfrastructureProcess(flow.isInfrastructureFlow());
	}

}
