package org.openlca.io.ilcd.input.models;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.models.ModelName;
import org.openlca.ilcd.models.Publication;
import org.openlca.ilcd.util.Models;
import org.openlca.util.Strings;

/**
 * A utility class for mapping data between eILCD models and product systems.
 */
class IO {

	private IO() {
	}

	static List<ParameterRedef> parametersSetOf(ProductSystem system ) {
		if (system == null)
			return new ArrayList<>();
		if (!system.parameterSets.isEmpty())
			return system.parameterSets.get(0).parameters;
		var s = new ParameterRedefSet();
		s.name = "Baseline";
		s.isBaseline = true;
		system.parameterSets.add(s);
		return s.parameters;
	}

	static void mapMetaData(Model model, ProductSystem system) {
		if (model == null || system == null)
			return;
		system.refId = model.getUUID();
		system.name = getName(model);
		Publication pub = Models.getPublication(model);
		if (pub != null && pub.version != null) {
			system.version = Version.fromString(pub.version).getValue();
		}
	}

	@SuppressWarnings("unchecked")
	private static String getName(Model m) {
		ModelName mn = Models.getModelName(m);
		if (mn == null)
			return "";
		List<?>[] parts = new List<?>[] {
				mn.name,
				mn.technicalDetails,
				mn.mixAndLocation,
				mn.flowProperties
		};
		String name = "";
		for (List<?> part : parts) {
			String s = LangString.getFirst((List<LangString>) part, "en");
			if (Strings.nullOrEmpty(s))
				continue;
			if (name.length() > 0)
				name += "; ";
			name += s.trim();
		}
		return name;
	}

}
