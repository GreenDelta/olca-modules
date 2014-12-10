package org.openlca.jsonld;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcessWriter implements Writer<Process> {

	@Override
	public void write(Process entity, EntityStore store) {

	}

	@Override
	public JsonElement serialize(Process process, Type type,
			JsonSerializationContext jsonSerializationContext) {
		JsonObject obj = new JsonObject();
		JsonWriter.addContext(obj);
		map(process, obj);
		return obj;
	}

	static void map(Process process, JsonObject obj) {
		if (process == null || obj == null)
			return;
		JsonWriter.addAttributes(process, obj);
		mapProcessType(process, obj);
		obj.addProperty("defaultAllocationMethod", getAllocationType(
				process.getDefaultAllocationMethod()));
		obj.add("location", JsonWriter.createRef(process.getLocation()));
		obj.add("processDocumentation", createDoc(process));
		mapExchanges(process, obj);
	}

	private static void mapExchanges(Process process, JsonObject obj) {
		JsonArray exchanges = new JsonArray();
		for (Exchange exchange : process.getExchanges()) {
			JsonObject exchangeObj = new JsonObject();
			ExchangeWriter.map(exchange, exchangeObj);
			exchanges.add(exchangeObj);
		}
		obj.add("exchanges", exchanges);
		Exchange qRef = process.getQuantitativeReference();
		if (qRef != null) {
			JsonObject qRefObj = new JsonObject();
			qRefObj.addProperty("@type", "Exchange");
			qRefObj.addProperty("@id", qRef.getId());
			obj.add("quantitativeReference", qRefObj);
		}
	}

	private static JsonObject createDoc(Process process) {
		ProcessDocumentation d = process.getDocumentation();
		if (d == null)
			return null;
		JsonObject o = new JsonObject();
		mapSimpleDocFields(d, o);
		o.add("reviewer", JsonWriter.createRef(d.getReviewer()));
		o.add("dataDocumentor", JsonWriter.createRef(d.getDataDocumentor()));
		o.add("dataGenerator", JsonWriter.createRef(d.getDataGenerator()));
		o.add("dataSetOwner", JsonWriter.createRef(d.getDataSetOwner()));
		o.add("publication", JsonWriter.createRef(d.getPublication()));
		mapSources(d, o);
		return o;
	}

	private static void mapSources(ProcessDocumentation d, JsonObject o) {
		if (d.getSources().isEmpty())
			return;
		JsonArray sources = new JsonArray();
		for (Source source : d.getSources()) {
			JsonObject ref = JsonWriter.createRef(source);
			sources.add(ref);
		}
		o.add("sources", sources);
	}

	private static void mapSimpleDocFields(ProcessDocumentation d, JsonObject o) {
		o.addProperty("timeDescription", d.getTime());
		o.addProperty("technologyDescription", d.getTechnology());
		o.addProperty("dataCollectionDescription", d.getDataCollectionPeriod());
		o.addProperty("completenessDescription", d.getCompleteness());
		o.addProperty("dataSelectionDescription", d.getDataSelection());
		o.addProperty("reviewDetails", d.getReviewDetails());
		o.addProperty("dataTreatmentDescription", d.getDataTreatment());
		o.addProperty("inventoryMethodDescription", d.getInventoryMethod());
		o.addProperty("modelingConstantsDescription", d.getModelingConstants());
		o.addProperty("samplingDescription", d.getSampling());
		o.addProperty("restrictionsDescription", d.getRestrictions());
		o.addProperty("copyright", d.isCopyright());
		o.addProperty("validFrom", asXmlDate(d.getValidFrom()));
		o.addProperty("validUntil", asXmlDate(d.getValidUntil()));
		o.addProperty("creationDate", asXmlDate(d.getCreationDate()));
		o.addProperty("intendedApplication", d.getIntendedApplication());
		o.addProperty("projectDescription", d.getProject());
		o.addProperty("geographyDescription", d.getGeography());
	}

	private static String getAllocationType(AllocationMethod method) {
		if (method == null)
			return null;
		switch (method) {
		case CAUSAL:
			return "CAUSAL_ALLOCATION";
		case ECONOMIC:
			return "ECONOMIC_ALLOCATION";
		case PHYSICAL:
			return "PHYSICAL_ALLOCATION";
		default:
			return null;
		}
	}

	private static void mapProcessType(Process process, JsonObject obj) {
		ProcessType type = process.getProcessType();
		if (type == null)
			return;
		obj.addProperty("processTyp", type.name());
	}

	private static String asXmlDate(Date date) {
		if (date == null)
			return null;
		try {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			XMLGregorianCalendar xml = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(cal);
			return xml.toXMLFormat();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ProcessWriter.class);
			log.error("Could not convert to XML date format", e);
			return null;
		}
	}

}
