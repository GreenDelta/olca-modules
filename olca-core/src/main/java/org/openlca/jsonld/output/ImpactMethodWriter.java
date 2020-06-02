package org.openlca.jsonld.output;

import org.openlca.core.model.ImpactMethod;

import com.google.gson.JsonObject;

class ImpactMethodWriter extends Writer<ImpactMethod> {

	ImpactMethodWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(ImpactMethod m) {
		JsonObject obj = super.write(m);
		if (obj == null)
			return null;
		Out.put(obj, "impactCategories", m.impactCategories, conf, Out.FORCE_EXPORT);
		Out.put(obj, "nwSets", m.nwSets, conf, Out.FORCE_EXPORT);
		return obj;
	}

}
