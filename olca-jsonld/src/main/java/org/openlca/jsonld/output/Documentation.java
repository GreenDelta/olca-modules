package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonObject;

class Documentation {

	static JsonObject create(Process process, Consumer<RootEntity> refFn) {
		ProcessDocumentation d = process.getDocumentation();
		if (d == null)
			return null;
		JsonObject o = new JsonObject();
		Out.put(o, "@type", "ProcessDocumentation");
		mapSimpleDocFields(d, o);
		Out.put(o, "reviewer", d.getReviewer(), refFn);
		Out.put(o, "dataDocumentor", d.getDataDocumentor(), refFn);
		Out.put(o, "dataGenerator", d.getDataGenerator(), refFn);
		Out.put(o, "dataSetOwner", d.getDataSetOwner(), refFn);
		Out.put(o, "publication", d.getPublication(), refFn);
		Out.put(o, "sources", d.getSources(), refFn);
		return o;
	}

	private static void mapSimpleDocFields(ProcessDocumentation d, JsonObject o) {
		Out.put(o, "timeDescription", d.getTime());
		Out.put(o, "technologyDescription", d.getTechnology());
		Out.put(o, "dataCollectionDescription", d.getDataCollectionPeriod());
		Out.put(o, "completenessDescription", d.getCompleteness());
		Out.put(o, "dataSelectionDescription", d.getDataSelection());
		Out.put(o, "reviewDetails", d.getReviewDetails());
		Out.put(o, "dataTreatmentDescription", d.getDataTreatment());
		Out.put(o, "inventoryMethodDescription", d.getInventoryMethod());
		Out.put(o, "modelingConstantsDescription", d.getModelingConstants());
		Out.put(o, "samplingDescription", d.getSampling());
		Out.put(o, "restrictionsDescription", d.getRestrictions());
		Out.put(o, "copyright", d.isCopyright());
		Out.put(o, "validFrom", d.getValidFrom());
		Out.put(o, "validUntil", d.getValidUntil());
		Out.put(o, "creationDate", d.getCreationDate());
		Out.put(o, "intendedApplication", d.getIntendedApplication());
		Out.put(o, "projectDescription", d.getProject());
		Out.put(o, "geographyDescription", d.getGeography());
	}

}
