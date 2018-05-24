package org.openlca.jsonld.input;

import org.openlca.core.model.Actor;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ProcessDocReader {

	private ImportConfig conf;
	private JsonObject json;

	private ProcessDocReader(ImportConfig conf) {
		this.conf = conf;
	}

	static ProcessDocumentation read(JsonObject json, ImportConfig conf) {
		return new ProcessDocReader(conf).read(json);
	}

	private ProcessDocumentation read(JsonObject process) {
		ProcessDocumentation doc = new ProcessDocumentation();
		if (process == null)
			return doc;
		JsonElement elem = process.get("processDocumentation");
		if (elem == null || !elem.isJsonObject())
			return doc;
		json = elem.getAsJsonObject();
		mapSimpleFields(doc);
		doc.setReviewer(actor("reviewer"));
		doc.setDataDocumentor(actor("dataDocumentor"));
		doc.setDataGenerator(actor("dataGenerator"));
		doc.setDataSetOwner(actor("dataSetOwner"));
		String pupId = Json.getRefId(json, "publication");
		doc.setPublication(SourceImport.run(pupId, conf));
		addSources(doc);
		return doc;
	}

	private Actor actor(String field) {
		String refId = Json.getRefId(json, field);
		return ActorImport.run(refId, conf);
	}

	private void mapSimpleFields(ProcessDocumentation doc) {
		doc.setTime(Json.getString(json, "timeDescription"));
		doc.setTechnology(Json.getString(json, "technologyDescription"));
		doc.setDataCollectionPeriod(Json.getString(json,
				"dataCollectionDescription"));
		doc.setCompleteness(Json.getString(json, "completenessDescription"));
		doc.setDataSelection(Json.getString(json, "dataSelectionDescription"));
		doc.setReviewDetails(Json.getString(json, "reviewDetails"));
		doc.setDataTreatment(Json.getString(json, "dataTreatmentDescription"));
		doc.setInventoryMethod(Json.getString(json, "inventoryMethodDescription"));
		doc.setModelingConstants(Json.getString(json,
				"modelingConstantsDescription"));
		doc.setSampling(Json.getString(json, "samplingDescription"));
		doc.setRestrictions(Json.getString(json, "restrictionsDescription"));
		doc.setIntendedApplication(Json.getString(json, "intendedApplication"));
		doc.setProject(Json.getString(json, "projectDescription"));
		doc.setGeography(Json.getString(json, "geographyDescription"));
		doc.setCopyright(Json.getBool(json, "copyright", false));
		doc.setValidFrom(Json.getDate(json, "validFrom"));
		doc.setValidUntil(Json.getDate(json, "validUntil"));
		doc.setCreationDate(Json.getDate(json, "creationDate"));
	}

	private void addSources(ProcessDocumentation doc) {
		JsonElement sources = json.get("sources");
		if (sources == null || !sources.isJsonArray())
			return;
		for (JsonElement e : sources.getAsJsonArray()) {
			if (!e.isJsonObject())
				return;
			String refId = Json.getString(e.getAsJsonObject(), "@id");
			Source source = SourceImport.run(refId, conf);
			if (source != null)
				doc.getSources().add(source);
		}
	}
}
