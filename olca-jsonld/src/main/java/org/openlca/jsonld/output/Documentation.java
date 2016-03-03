package org.openlca.jsonld.output;

import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;

import com.google.gson.JsonObject;

class Documentation {

	static JsonObject create(Process process, ExportConfig conf) {
		ProcessDocumentation d = process.getDocumentation();
		if (d == null)
			return null;
		JsonObject o = new JsonObject();
		Out.put(o, "@type", ProcessDocumentation.class.getSimpleName());
		mapSimpleDocFields(d, o);
		Out.put(o, "reviewer", d.getReviewer(), conf);
		Out.put(o, "dataDocumentor", d.getDataDocumentor(), conf);
		Out.put(o, "dataGenerator", d.getDataGenerator(), conf);
		Out.put(o, "dataSetOwner", d.getDataSetOwner(), conf);
		Out.put(o, "publication", d.getPublication(), conf);
		Out.put(o, "sources", d.getSources(), conf);
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
