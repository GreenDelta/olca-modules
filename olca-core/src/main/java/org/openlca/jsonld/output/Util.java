package org.openlca.jsonld.output;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.model.Process;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

class Util {

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

	public static <T extends RootEntity> JsonObject init(T entity) {
		var obj = new JsonObject();
		mapBasicAttributes(entity, obj);
		return obj;
	}

	public static void mapBasicAttributes(RefEntity entity, JsonObject obj) {
		if (entity == null || obj == null)
			return;
		var type = entity.getClass().getSimpleName();
		Json.put(obj, "@type", type);
		Json.put(obj, "@id", entity.refId);
		Json.put(obj, "name", entity.name);
		Json.put(obj, "description", entity.description);
		if (entity instanceof RootEntity re) {

			if (re.category != null) {
				Json.put(obj, "category", re.category.toPath());
			}
			Json.put(obj, "version", Version.asString(re.version));
			if (re.lastChange != 0) {
				var instant = Instant.ofEpochMilli(re.lastChange);
				Json.put(obj, "lastChange", instant.toString());
			}

			// tags
			if (!Strings.nullOrEmpty(re.tags)) {
				var tags = new JsonArray();
				Arrays.stream(re.tags.split(","))
					.map(String::trim)
					.filter(tag -> !Strings.nullOrEmpty(tag))
					.forEach(tags::add);
				if (tags.size() > 0) {
					obj.add("tags", tags);
				}
			}
		}
	}
}
