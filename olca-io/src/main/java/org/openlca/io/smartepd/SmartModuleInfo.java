package org.openlca.io.smartepd;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

import com.google.gson.JsonObject;

class SmartModuleInfo {

	private SmartModuleInfo() {
	}

	static void write(SmartEpd epd, Set<SmartModule> modules) {
		if (epd == null || modules == null || modules.isEmpty())
			return;

		var stages = new EnumMap<SmartStage, Set<SmartModule>>(SmartStage.class);
		for (var m : modules) {
			var list = stages.computeIfAbsent(
				m.stage(), k -> EnumSet.noneOf(SmartModule.class));
			list.add(m);
		}

		var scope = "Cradle to gate";
		var info = new JsonObject();
		for (var stage : SmartStage.values()) {
			var list = stages.get(stage);
			if (list == null || list.isEmpty())
				continue;

			if (stage != SmartStage.PRODUCTION) {
				scope = "Cradle to gate with other options";
			}
			addStage(info, stage, list);
		}
		Json.put(epd.json(), "information_modules", info);

		// write the scope and scope description
		epd.scope(scope);
		if (Strings.nullOrEmpty(epd.scopeDescription())) {
			var d = new StringBuilder();
			for (var mod : modules) {
				if (!d.isEmpty()) {
					d.append(", ");
				}
				d.append(mod.name());
			}
			epd.scopeDescription(d.toString());
		}
	}

	private static void addStage(
		JsonObject info, SmartStage stage, Set<SmartModule> modules
	) {
		var obj = new JsonObject();

		// aggregation type: 1 = A1-A3, 2 = A1, A2, A3
		if (stage == SmartStage.PRODUCTION) {
			int aggType = modules.contains(SmartModule.A1A2A3) ? 1 : 2;
			Json.put(obj, "show_as", aggType);
		}

		for (var mod : modules) {
			if (mod == SmartModule.A1A2A3) {
				Json.put(obj, "a1", true);
				Json.put(obj, "a2", true);
				Json.put(obj, "a3", true);
				continue;
			}
			Json.put(obj, mod.name().toLowerCase(), true);
		}

		Json.put(info, keyOf(stage), obj);
	}

	private static String keyOf(SmartStage stage) {
		if (stage == null)
			return "null";
		return switch (stage) {
			case PRODUCTION -> "production_stage";
			case CONSTRUCTION -> "construction_stage";
			case USE -> "use_stage";
			case END_OF_LIFE -> "end_of_life_stage";
			case BENEFITS -> "benefits_stage";
		};
	}
}
