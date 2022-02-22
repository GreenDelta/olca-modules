package org.openlca.jsonld.output;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.google.gson.JsonArray;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

class Documentation {

	static JsonObject create(Process process, JsonExport exp) {
		ProcessDocumentation d = process.documentation;
		if (d == null)
			return null;
		JsonObject o = new JsonObject();
		mapSimpleDocFields(d, o);
		Json.put(o, "reviewer", exp.handleRef(d.reviewer));
		Json.put(o, "dataDocumentor", exp.handleRef(d.dataDocumentor));
		Json.put(o, "dataGenerator", exp.handleRef(d.dataGenerator));
		Json.put(o, "dataSetOwner", exp.handleRef(d.dataSetOwner));
		Json.put(o, "publication", exp.handleRef(d.publication));
		Json.put(o, "sources", exp.handleRefs(d.sources));
		return o;
	}

	private static void mapSimpleDocFields(ProcessDocumentation d, JsonObject o) {
		Json.put(o, "timeDescription", d.time);
		Json.put(o, "technologyDescription", d.technology);
		Json.put(o, "dataCollectionDescription", d.dataCollectionPeriod);
		Json.put(o, "completenessDescription", d.completeness);
		Json.put(o, "dataSelectionDescription", d.dataSelection);
		Json.put(o, "reviewDetails", d.reviewDetails);
		Json.put(o, "dataTreatmentDescription", d.dataTreatment);
		Json.put(o, "inventoryMethodDescription", d.inventoryMethod);
		Json.put(o, "modelingConstantsDescription", d.modelingConstants);
		Json.put(o, "samplingDescription", d.sampling);
		Json.put(o, "restrictionsDescription", d.restrictions);
		Json.put(o, "copyright", d.copyright);
		Json.put(o, "intendedApplication", d.intendedApplication);
		Json.put(o, "projectDescription", d.project);
		Json.put(o, "geographyDescription", d.geography);

		// time stamps
		if (d.creationDate != null) {
			Json.put(o, "creationDate", d.creationDate);
		}
		if (d.validFrom != null) {
			Json.put(o, "validFrom", date(d.validFrom));
		}
		if (d.validUntil != null) {
			Json.put(o, "validUntil", date(d.validUntil));
		}
	}

	private static String date(Date date) {
		if (date == null)
			return null;
		var instant = date.toInstant();
		var local = LocalDate.ofInstant(instant, ZoneId.systemDefault());
		return local.toString();
	}
}
