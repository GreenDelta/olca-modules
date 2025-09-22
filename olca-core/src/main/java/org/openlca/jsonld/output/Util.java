package org.openlca.jsonld.output;

import java.time.Instant;
import java.util.Arrays;

import org.openlca.core.model.Actor;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class Util {

	@SuppressWarnings("unchecked")
	static <T extends RefEntity> JsonWriter<T> writerOf(
			T entity, JsonExport export) {
		return (JsonWriter<T>) switch (entity) {
			case Actor ignored -> new ActorWriter(export);
			case Currency ignored -> new CurrencyWriter(export);
			case Epd ignored -> new EpdWriter(export);
			case FlowProperty ignored -> new FlowPropertyWriter(export);
			case Flow ignored -> new FlowWriter(export);
			case ImpactCategory ignored -> new ImpactCategoryWriter(export);
			case ImpactMethod ignored -> new ImpactMethodWriter(export);
			case Location ignored -> new LocationWriter(export);
			case Parameter ignored -> new ParameterWriter(export);
			case Process ignored -> new ProcessWriter(export);
			case Result ignored -> new ResultWriter(export);
			case Source ignored -> new SourceWriter(export);
			case UnitGroup ignored -> new UnitGroupWriter(export);
			case SocialIndicator ignored -> new SocialIndicatorWriter(export);
			case ProductSystem ignored -> new ProductSystemWriter(export);
			case Project ignored -> new ProjectWriter(export);
			case DQSystem ignored -> new DQSystemWriter(export);
			case Unit ignored -> new UnitWriter(export);
			case null, default -> null;
		};
	}

	static <T extends RootEntity> JsonObject init(JsonExport exp, T entity) {
		var obj = new JsonObject();
		mapBasicAttributes(entity, obj);
		if (exp != null
				&& entity != null
				&& !Strings.nullOrEmpty(entity.dataPackage)) {
			if (exp.dataPackages.isFromLibrary(entity)) {
				Json.put(obj, "library", entity.dataPackage);
			} else {
				Json.put(obj, "dataPackage", entity.dataPackage);				
			}
		}
		mapOtherProperties(entity, obj);
		return obj;
	}

	static <T extends RootEntity> void mapOtherProperties(
			T entity, JsonObject obj) {
		if (entity != null && entity.otherProperties != null) {
			var extProps = entity.readOtherProperties();
			obj.add("otherProperties", extProps);
		}
	}

	static void mapBasicAttributes(RefEntity entity, JsonObject obj) {
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
				if (!tags.isEmpty()) {
					obj.add("tags", tags);
				}
			}
		}
	}
}
