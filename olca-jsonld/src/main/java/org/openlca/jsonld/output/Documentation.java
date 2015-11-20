package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Dates;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class Documentation {

	static JsonObject create(Process process, Consumer<RootEntity> refFn) {
		ProcessDocumentation d = process.getDocumentation();
		if (d == null)
			return null;
		JsonObject o = new JsonObject();
		o.addProperty("@type", "ProcessDocumentation");
		mapSimpleDocFields(d, o);
		o.add("reviewer", References.create(d.getReviewer(), refFn));
		o.add("dataDocumentor", References.create(d.getDataDocumentor(), refFn));
		o.add("dataGenerator", References.create(d.getDataGenerator(), refFn));
		o.add("dataSetOwner", References.create(d.getDataSetOwner(), refFn));
		o.add("publication", References.create(d.getPublication(), refFn));
		JsonArray sources = new JsonArray();
		for (Source source : d.getSources())
			sources.add(References.create(source, refFn));
		o.add("sources", sources);
		return o;
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
		o.addProperty("validFrom", Dates.toString(d.getValidFrom()));
		o.addProperty("validUntil", Dates.toString(d.getValidUntil()));
		o.addProperty("creationDate", Dates.toString(d.getCreationDate()));
		o.addProperty("intendedApplication", d.getIntendedApplication());
		o.addProperty("projectDescription", d.getProject());
		o.addProperty("geographyDescription", d.getGeography());
	}

}
