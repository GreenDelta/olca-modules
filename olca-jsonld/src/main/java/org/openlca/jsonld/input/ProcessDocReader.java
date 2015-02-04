package org.openlca.jsonld.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.Actor;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.Source;
import org.openlca.jsonld.EntityStore;

class ProcessDocReader {

	private JsonObject json;
	private EntityStore store;
	private Db db;

	private ProcessDocReader(JsonObject json, EntityStore store, Db db) {
		this.json = json;
		this.store = store;
		this.db = db;
	}

	static ProcessDocumentation read(JsonObject json, EntityStore store, Db db) {
		return new ProcessDocReader(json, store, db).read();
	}

	private ProcessDocumentation read() {
		if (json == null)
			return null;
		ProcessDocumentation doc = new ProcessDocumentation();
		mapSimpleFields(doc);
		doc.setReviewer(actor("reviewer"));
		doc.setDataDocumentor(actor("dataDocumentor"));
		doc.setDataGenerator(actor("dataGenerator"));
		doc.setDataSetOwner(actor("dataSetOwner"));
		String pupId = In.getRefId(json, "publication");
		doc.setPublication(SourceImport.run(pupId, store, db));
		addSources(doc);
		return doc;
	}

	private Actor actor(String field) {
		String refId = In.getRefId(json, field);
		return ActorImport.run(refId, store, db);
	}

	private void mapSimpleFields(ProcessDocumentation doc) {
		doc.setTime(In.getString(json, "timeDescription"));
		doc.setTechnology(In.getString(json, "technologyDescription"));
		doc.setDataCollectionPeriod(In.getString(json, "dataCollectionDescription"));
		doc.setCompleteness(In.getString(json, "completenessDescription"));
		doc.setDataSelection(In.getString(json, "dataSelectionDescription"));
		doc.setReviewDetails(In.getString(json, "reviewDetails"));
		doc.setDataTreatment(In.getString(json, "dataTreatmentDescription"));
		doc.setInventoryMethod(In.getString(json, "inventoryMethodDescription"));
		doc.setModelingConstants(In.getString(json, "modelingConstantsDescription"));
		doc.setSampling(In.getString(json, "samplingDescription"));
		doc.setRestrictions(In.getString(json, "restrictionsDescription"));
		doc.setIntendedApplication(In.getString(json, "intendedApplication"));
		doc.setProject(In.getString(json, "projectDescription"));
		doc.setGeography(In.getString(json, "geographyDescription"));
		doc.setCopyright(In.getBool(json, "copyright", false));
		doc.setValidFrom(In.getDate(json, "validFrom"));
		doc.setValidUntil(In.getDate(json, "validUntil"));
		doc.setCreationDate(In.getDate(json, "creationDate"));
	}

	private void addSources(ProcessDocumentation doc) {
		JsonElement sources = json.get("sources");
		if (sources == null || !sources.isJsonArray())
			return;
		for (JsonElement e : sources.getAsJsonArray()) {
			if (!e.isJsonObject())
				return;
			String refId = In.getString(e.getAsJsonObject(), "@id");
			Source source = SourceImport.run(refId, store, db);
			if (source != null)
				doc.getSources().add(source);
		}
	}
}
