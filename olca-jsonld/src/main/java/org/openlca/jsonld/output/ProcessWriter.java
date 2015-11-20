package org.openlca.jsonld.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialAspect;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ProcessWriter extends Writer<Process> {

	private Process process;
	private Consumer<RootEntity> refFn;

	@Override
	JsonObject write(Process process, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(process, refFn);
		if (obj == null)
			return null;
		this.process = process;
		this.refFn = refFn;
		ProcessType type = process.getProcessType();
		if (type != null)
			obj.addProperty("processTyp", type.name());
		obj.addProperty("defaultAllocationMethod",
				getAllocationType(process.getDefaultAllocationMethod()));
		obj.add("location", References.create(process.getLocation(), refFn));
		obj.add("processDocumentation", Documentation.create(process, refFn));
		obj.add("currency", References.create(process.currency, refFn));
		mapParameters(obj);
		mapExchanges(obj);
		mapSocialAspects(obj);
		mapAllocationFactors(obj);
		return obj;
	}

	private void mapParameters(JsonObject obj) {
		JsonArray parameters = new JsonArray();
		for (Parameter p : process.getParameters()) {
			JsonObject pObj = new ParameterWriter().write(p, ref -> {
			});
			parameters.add(pObj);
		}
		obj.add("parameters", parameters);
	}

	private void mapExchanges(JsonObject obj) {
		JsonArray exchanges = new JsonArray();
		for (Exchange e : process.getExchanges()) {
			JsonObject eObj = new JsonObject();
			Exchanges.map(e, eObj, refFn);
			if (Objects.equals(process.getQuantitativeReference(), e))
				eObj.addProperty("quantitativeReference", true);
			exchanges.add(eObj);
		}
		obj.add("exchanges", exchanges);
	}

	private void mapSocialAspects(JsonObject obj) {
		JsonArray aspects = new JsonArray();
		for (SocialAspect a : process.socialAspects) {
			JsonObject aObj = new JsonObject();
			aObj.add("socialIndicator", References.create(a.indicator, refFn));
			aObj.addProperty("@type", "SocialAspect");
			aObj.addProperty("comment", a.comment);
			aObj.addProperty("quality", a.quality);
			aObj.addProperty("rawAmount", a.rawAmount);
			aObj.addProperty("activityValue", a.activityValue);
			String riskLevel = null;
			if (a.riskLevel != null)
				riskLevel = a.riskLevel.name();
			aObj.addProperty("riskLevel", riskLevel);
			aObj.add("source", References.create(a.source, refFn));
			aspects.add(aObj);
		}
		obj.add("socialAspects", aspects);
	}

	private void mapAllocationFactors(JsonObject obj) {
		JsonArray factors = new JsonArray();
		for (AllocationFactor factor : process.getAllocationFactors()) {
			obj.addProperty("rawAmount", factor.getValue());
		}
		obj.add("allocationFactors", factors);
	}

	private String getAllocationType(AllocationMethod method) {
		if (method == null)
			return null;
		switch (method) {
		case CAUSAL:
			return "CAUSAL_ALLOCATION";
		case ECONOMIC:
			return "ECONOMIC_ALLOCATION";
		case PHYSICAL:
			return "PHYSICAL_ALLOCATION";
		default:
			return null;
		}
	}

}
