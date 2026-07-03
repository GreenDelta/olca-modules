package org.openlca.jsonld.output;

import java.time.Instant;
import java.util.Arrays;

import org.openlca.commons.Strings;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class Util {

	@SuppressWarnings("unchecked")
	static <T extends RefEntity> JsonWriter<T> writerOf(
			T entity, JsonExport export
	) {
		return (JsonWriter<T>) switch (entity) {
			case Actor _ -> new ActorWriter(export);
			case Currency _ -> new CurrencyWriter(export);
			case Epd _ -> new EpdWriter(export);
			case FlowProperty _ -> new FlowPropertyWriter(export);
			case Flow _ -> new FlowWriter(export);
			case ImpactCategory _ -> new ImpactCategoryWriter(export);
			case ImpactMethod _ -> new ImpactMethodWriter(export);
			case Location _ -> new LocationWriter(export);
			case Parameter _ -> new ParameterWriter(export);
			case Process _ -> new ProcessWriter(export);
			case Result _ -> new ResultWriter(export);
			case Source _ -> new SourceWriter(export);
			case UnitGroup _ -> new UnitGroupWriter(export);
			case SocialIndicator _ -> new SocialIndicatorWriter(export);
			case ProductSystem _ -> new ProductSystemWriter(export);
			case Project _ -> new ProjectWriter(export);
			case DQSystem _ -> new DQSystemWriter(export);
			case Unit _ -> new UnitWriter(export);
			case null, default -> null;
		};
	}

	static <T extends RootEntity> JsonObject init(JsonExport exp, T entity) {
		var obj = new JsonObject();
		mapBasicAttributes(entity, obj);
		if (exp != null
				&& entity != null
				&& exp.writeLibraryFields
				&& entity.isFromLibrary()) {
			Json.put(obj, "library", entity.library);
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
			if (Strings.isNotBlank(re.tags)) {
				var tags = new JsonArray();
				Arrays.stream(re.tags.split(","))
					.map(String::trim)
					.filter(Strings::isNotBlank)
					.forEach(tags::add);
				if (!tags.isEmpty()) {
					obj.add("tags", tags);
				}
			}
		}
	}
}
