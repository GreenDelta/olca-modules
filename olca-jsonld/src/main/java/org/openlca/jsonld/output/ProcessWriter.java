package org.openlca.jsonld.output;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Source;
import org.openlca.jsonld.EntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

class ProcessWriter implements Writer<Process> {

	private EntityStore store;

	public ProcessWriter() {
	}

	public ProcessWriter(EntityStore store) {
		this.store = store;
	}

	@Override
	public void write(Process process) {
		if (process == null || store == null)
			return;
		if (store.contains(ModelType.PROCESS, process.getRefId()))
			return;
		JsonObject obj = serialize(process, null, null);
		store.add(ModelType.PROCESS, process.getRefId(), obj);
	}

	@Override
	public JsonObject serialize(Process process, Type type,
			JsonSerializationContext jsonSerializationContext) {
		JsonObject obj = new JsonObject();
		map(process, obj);
		return obj;
	}

	private void map(Process process, JsonObject obj) {
		if (process == null || obj == null)
			return;
		JsonExport.addAttributes(process, obj, store);
		mapProcessType(process, obj);
		obj.addProperty("defaultAllocationMethod", getAllocationType(
				process.getDefaultAllocationMethod()));
		obj.add("location", Out.put(process.getLocation(), store));
		obj.add("processDocumentation", createDoc(process));
		mapExchanges(process, obj);
	}

	private void mapExchanges(Process process, JsonObject obj) {
		JsonArray exchanges = new JsonArray();
		for (Exchange exchange : process.getExchanges()) {
			JsonObject exchangeObj = new JsonObject();
			new ExchangeWriter(store).map(exchange, exchangeObj);
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

	private JsonObject createDoc(Process process) {
		ProcessDocumentation d = process.getDocumentation();
		if (d == null)
			return null;
		JsonObject o = new JsonObject();
		mapSimpleDocFields(d, o);
		o.add("reviewer", Out.put(d.getReviewer(), store));
		o.add("dataDocumentor", Out.put(d.getDataDocumentor(), store));
		o.add("dataGenerator", Out.put(d.getDataGenerator(), store));
		o.add("dataSetOwner", Out.put(d.getDataSetOwner(), store));
		o.add("publication", Out.put(d.getPublication(), store));
		mapSources(d, o);
		return o;
	}

	private void mapSources(ProcessDocumentation d, JsonObject o) {
		if (d.getSources().isEmpty())
			return;
		JsonArray sources = new JsonArray();
		for (Source source : d.getSources()) {
			JsonObject ref = Out.put(source, store);
			sources.add(ref);
		}
		o.add("sources", sources);
	}

	private void mapSimpleDocFields(ProcessDocumentation d, JsonObject o) {
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

	private String getAllocationType(AllocationMethod method) {
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

	private void mapProcessType(Process process, JsonObject obj) {
		ProcessType type = process.getProcessType();
		if (type == null)
			return;
		obj.addProperty("processTyp", type.name());
	}

	private String asXmlDate(Date date) {
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
