package org.openlca.jsonld.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Source;
import org.openlca.core.model.doc.AspectMap;
import org.openlca.core.model.doc.ComplianceDeclaration;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.doc.Review;
import org.openlca.core.model.doc.ReviewScopeMap;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

class ProcessDocs {

	private final JsonObject json;
	private final EntityResolver db;
	private final ProcessDoc doc;

	private ProcessDocs(JsonObject docJson, EntityResolver db) {
		this.json = docJson;
		this.db = db;
		this.doc = new ProcessDoc();
	}

	static ProcessDoc read(JsonObject json, EntityResolver db) {
		var docJson = Json.getObject(json, "processDocumentation");
		return docJson != null
				? new ProcessDocs(docJson, db).map()
				: new ProcessDoc();
	}

	private ProcessDoc map() {
		doc.validFrom = Json.getDate(json, "validFrom");
		doc.validUntil = Json.getDate(json, "validUntil");
		doc.time = Json.getString(json, "timeDescription");
		doc.geography = Json.getString(json, "geographyDescription");
		doc.technology = Json.getString(json, "technologyDescription");

		doc.inventoryMethod = Json.getString(json, "inventoryMethodDescription");
		doc.modelingConstants = Json.getString(json, "modelingConstantsDescription");

		doc.dataCompleteness = Json.getString(json, "completenessDescription");
		doc.dataSelection = Json.getString(json, "dataSelectionDescription");
		doc.dataTreatment = Json.getString(json, "dataTreatmentDescription");
		doc.samplingProcedure = Json.getString(json, "samplingDescription");
		doc.dataCollectionPeriod = Json.getString(json, "dataCollectionDescription");
		doc.useAdvice = Json.getString(json, "useAdvice");
		addSources();

		doc.flowCompleteness.putAll(
				AspectMap.fromJson(Json.getArray(json, "flowCompleteness")));
		addComplianceDeclarations();
		addReviews();

		doc.intendedApplication = Json.getString(json, "intendedApplication");
		doc.project = Json.getString(json, "projectDescription");

		doc.dataGenerator = actor(json, "dataGenerator");
		doc.dataDocumentor = actor(json, "dataDocumentor");
		doc.creationDate = Json.getDate(json, "creationDate");
		doc.publication = source(json, "publication");
		doc.dataOwner = actor(json, "dataSetOwner");
		doc.copyright = Json.getBool(json, "isCopyrightProtected", false);
		doc.accessRestrictions = Json.getString(json, "restrictionsDescription");

		return doc;
	}

	private void addSources() {
		Json.forEachObject(json, "sources", obj -> {
			var refId = Json.getString(obj, "@id");
			var source = db.get(Source.class, refId);
			if (source != null) {
				doc.sources.add(source);
			}
		});
	}

	private void addComplianceDeclarations() {
		Json.forEachObject(json, "complianceDeclarations", obj -> {
			var dec = new ComplianceDeclaration();
			dec.system = source(obj, "system");
			dec.comment = Json.getString(obj, "comment");
			dec.aspects.putAll(AspectMap.fromJson(obj.get("aspects")));
			doc.complianceDeclarations.add(dec);
		});
	}

	private void addReviews() {
		Json.forEachObject(json, "reviews", obj -> {
			var rev = new Review();
			rev.type = Json.getString(obj, "reviewType");
			ReviewScopeMap.fromJson(obj.get("scopes"))
					.values()
					.forEach(rev.scopes::put);
			rev.details = Json.getString(obj, "details");
			rev.report = source(obj, "report");
			rev.assessment.putAll(
					AspectMap.fromJson(obj.get("assessment")));
			Json.forEachObject(obj, "reviewers", revObj -> {
				var refId = Json.getString(revObj, "@id");
				var reviewer = db.get(Actor.class, refId);
				if (reviewer != null) {
					rev.reviewers.add(reviewer);
				}
			});
			doc.reviews.add(rev);
		});
	}

	private Actor actor(JsonObject obj, String field) {
		String refId = Json.getRefId(obj, field);
		return db.get(Actor.class, refId);
	}

	private Source source(JsonObject obj, String field) {
		var refId = Json.getRefId(obj, field);
		return db.get(Source.class, refId);
	}

}
