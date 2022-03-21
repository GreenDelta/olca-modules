package org.openlca.jsonld.output;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.openlca.core.model.ParameterRedef;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.model.Process;
import org.openlca.jsonld.Json;

class Util {

	/**
	 * Maps the given parameter redefinitions to a JSON array. If necessary, it
	 * exports the respective parameter context (i.e. global parameters or LCIA
	 * category parameters).
	 */
	static JsonArray mapRedefs(List<ParameterRedef> redefs, JsonExport exp) {
		var array = new JsonArray();
		for (var p : redefs) {
			var obj = new JsonObject();
			Json.put(obj, "name", p.name);
			Json.put(obj, "value", p.value);
			Json.put(obj, "uncertainty", Uncertainties.map(p.uncertainty));
			Json.put(obj, "isProtected", p.isProtected);
			if (p.contextId != null && p.contextType != null) {
				Json.put(obj, "context", exp.handleRef(p.contextType, p.contextId));
			}
			array.add(obj);
		}
		return array;
	}

	static JsonObject mapDocOf(Process process, JsonExport exp) {
		var d = process.documentation;
		if (d == null)
			return null;
		JsonObject o = new JsonObject();

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
		Json.put(o, "isCopyrightProtected", d.copyright);
		Json.put(o, "intendedApplication", d.intendedApplication);
		Json.put(o, "projectDescription", d.project);
		Json.put(o, "geographyDescription", d.geography);
		Json.put(o, "creationDate", d.creationDate);
		Json.put(o, "validFrom", date(d.validFrom));
		Json.put(o, "validUntil", date(d.validUntil));

		Json.put(o, "reviewer", exp.handleRef(d.reviewer));
		Json.put(o, "dataDocumentor", exp.handleRef(d.dataDocumentor));
		Json.put(o, "dataGenerator", exp.handleRef(d.dataGenerator));
		Json.put(o, "dataSetOwner", exp.handleRef(d.dataSetOwner));
		Json.put(o, "publication", exp.handleRef(d.publication));
		Json.put(o, "sources", exp.handleRefs(d.sources));
		return o;
	}

	private static String date(Date date) {
		if (date == null)
			return null;
		var instant = date.toInstant();
		var local = LocalDate.ofInstant(instant, ZoneId.systemDefault());
		return local.toString();
	}
}
