package org.openlca.jsonld.input;

import org.openlca.core.model.Actor;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class ProcessDocReader {

	private final JsonImport conf;
	private JsonObject json;

	private ProcessDocReader(JsonImport conf) {
		this.conf = conf;
	}

	static ProcessDocumentation read(JsonObject json, JsonImport conf) {
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
		doc.reviewer = actor("reviewer");
		doc.dataDocumentor = actor("dataDocumentor");
		doc.dataGenerator = actor("dataGenerator");
		doc.dataSetOwner = actor("dataSetOwner");
		String pupId = Json.getRefId(json, "publication");
		doc.publication = SourceImport.run(pupId, conf);
		addSources(doc);
		return doc;
	}

	private Actor actor(String field) {
		String refId = Json.getRefId(json, field);
		return ActorImport.run(refId, conf);
	}

	private void mapSimpleFields(ProcessDocumentation doc) {
		doc.time = Json.getString(json, "timeDescription");
		doc.technology = Json.getString(json, "technologyDescription");
		doc.dataCollectionPeriod = Json.getString(json,
				"dataCollectionDescription");
		doc.completeness = Json.getString(json, "completenessDescription");
		doc.dataSelection = Json.getString(json, "dataSelectionDescription");
		doc.reviewDetails = Json.getString(json, "reviewDetails");
		doc.dataTreatment = Json.getString(json, "dataTreatmentDescription");
		doc.inventoryMethod = Json.getString(json, "inventoryMethodDescription");
		doc.modelingConstants = Json.getString(json,
				"modelingConstantsDescription");
		doc.sampling = Json.getString(json, "samplingDescription");
		doc.restrictions = Json.getString(json, "restrictionsDescription");
		doc.intendedApplication = Json.getString(json, "intendedApplication");
		doc.project = Json.getString(json, "projectDescription");
		doc.geography = Json.getString(json, "geographyDescription");
		doc.copyright = Json.getBool(json, "copyright", false);
		doc.validFrom = Json.getDate(json, "validFrom");
		doc.validUntil = Json.getDate(json, "validUntil");
		doc.creationDate = Json.getDate(json, "creationDate");
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
				doc.sources.add(source);
		}
	}
}
