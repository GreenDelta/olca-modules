package org.openlca.jsonld.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.RiskLevel;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialAspect;
import org.openlca.jsonld.Enums;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ProcessWriter extends Writer<Process> {

	private Process process;
	private Consumer<RootEntity> refFn;
	private Map<Long, String> idToRefId = new HashMap<>();
	private ExportConfig conf;

	ProcessWriter(ExportConfig conf) {
		this.conf = conf;
	}

	@Override
	JsonObject write(Process process, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(process, refFn);
		if (obj == null)
			return null;
		this.process = process;
		this.refFn = refFn;
		obj.addProperty("processTyp",
				Enums.getLabel(process.getProcessType(), ProcessType.class));
		obj.addProperty("defaultAllocationMethod", Enums.getLabel(
				process.getDefaultAllocationMethod(), AllocationMethod.class));
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
			String id = Exchanges.map(e, eObj, conf, refFn);
			if (id == null)
				continue;
			idToRefId.put(e.getId(), id);
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
			aObj.addProperty("riskLevel",
					Enums.getLabel(a.riskLevel, RiskLevel.class));
			aObj.add("source", References.create(a.source, refFn));
			aspects.add(aObj);
		}
		obj.add("socialAspects", aspects);
	}

	private void mapAllocationFactors(JsonObject obj) {
		JsonArray factors = new JsonArray();
		for (AllocationFactor factor : process.getAllocationFactors()) {
			JsonObject fObj = new JsonObject();
			String exchangeId = null;
			if (factor.getExchange() != null)
				exchangeId = idToRefId.get(factor.getExchange().getId());
			fObj.addProperty("exchange", exchangeId);
			Flow product = findProduct(factor.getProductId());
			JsonObject productRef = References.create(product, refFn);
			fObj.add("product", productRef);
			fObj.addProperty("value", factor.getValue());
			fObj.addProperty("allocationType", Enums.getLabel(
					factor.getAllocationType(), AllocationMethod.class));
			factors.add(fObj);
		}
		obj.add("allocationFactors", factors);
	}

	private Flow findProduct(long id) {
		for (Exchange e : process.getExchanges())
			if (e.getFlow().getId() == id)
				return e.getFlow();
		return null;
	}

}
