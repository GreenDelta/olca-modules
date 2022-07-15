package org.openlca.jsonld.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

class ProcessDocs {


	static ProcessDocumentation read(JsonObject json, EntityResolver resolver) {
		var doc = new ProcessDocumentation();
		var docJson = Json.getObject(json, "processDocumentation");
		if (docJson == null)
			return doc;
		mapSimpleFields(doc, docJson);
		doc.reviewer = actor(docJson, "reviewer", resolver);
		doc.dataDocumentor = actor(docJson, "dataDocumentor", resolver);
		doc.dataGenerator = actor(docJson, "dataGenerator", resolver);
		doc.dataSetOwner = actor(docJson, "dataSetOwner", resolver);
		String pupId = Json.getRefId(docJson, "publication");
		doc.publication = resolver.get(Source.class, pupId);
		addSources(docJson, doc, resolver);
		return doc;
	}

	private static Actor actor(
		JsonObject json, String field, EntityResolver resolver) {
		String refId = Json.getRefId(json, field);
		return resolver.get(Actor.class, refId);
	}

	private static void mapSimpleFields(ProcessDocumentation doc, JsonObject json) {
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
		doc.copyright = Json.getBool(json, "isCopyrightProtected", false);
		doc.validFrom = Json.getDate(json, "validFrom");
		doc.validUntil = Json.getDate(json, "validUntil");
		doc.creationDate = Json.getDate(json, "creationDate");
	}

	private static void addSources(
		JsonObject json, ProcessDocumentation doc, EntityResolver resolver) {
		var sources = Json.getArray(json, "sources");
		if (sources == null)
			return;
		for (var e : sources) {
			if (!e.isJsonObject())
				return;
			var refId = Json.getString(e.getAsJsonObject(), "@id");
			var source = resolver.get(Source.class, refId);
			if (source != null)
				doc.sources.add(source);
		}
	}
}
