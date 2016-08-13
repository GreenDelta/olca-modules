package org.openlca.jsonld.input;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.ExchangeKey;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ProductSystemImport extends BaseImport<ProductSystem> {

	private ProductSystemImport(String refId, ImportConfig conf) {
		super(ModelType.PRODUCT_SYSTEM, refId, conf);
	}

	static ProductSystem run(String refId, ImportConfig conf) {
		return new ProductSystemImport(refId, conf).run();
	}

	@Override
	ProductSystem map(JsonObject json, long id) {
		if (json == null)
			return null;
		ProductSystem s = new ProductSystem();
		In.mapAtts(json, s, id, conf);
		String processRefId = In.getRefId(json, "referenceProcess");
		if (processRefId != null)
			s.setReferenceProcess(ProcessImport.run(processRefId, conf));
		s.setReferenceExchange(findExchange(json, s));
		s.setTargetAmount(In.getDouble(json, "targetAmount", 1d));
		s.setTargetFlowPropertyFactor(findFactor(json, s));
		s.setTargetUnit(findUnit(json, s));
		addProcesses(json, s);
		addLinks(json, s);
		addParameters(json, s);
		return conf.db.put(s);
	}

	private Exchange findExchange(JsonObject json, ProductSystem s) {
		Process p = s.getReferenceProcess();
		if (p == null)
			return null;
		String refId = In.getRefId(json, "referenceExchange");
		if (refId == null)
			return null;
		// try exact match
		for (Exchange e : p.getExchanges()) {
			String key = ExchangeKey.get(p.getRefId(), null, e);
			if (refId.equals(key))
				return e;
		}
		// get by flow if no exact match
		for (Exchange e : p.getExchanges()) {
			JsonObject eObj = In.getObject(json, "referenceExchange");
			String flowRefId = In.getRefId(eObj, "flow");
			if (e.getFlow().getRefId().equals(flowRefId))
				return e;
		}
		return null;
	}

	private FlowPropertyFactor findFactor(JsonObject json, ProductSystem s) {
		Exchange e = s.getReferenceExchange();
		if (e == null)
			return null;
		String propertyRefId = In.getRefId(json, "targetFlowProperty");
		for (FlowPropertyFactor f : e.getFlow().getFlowPropertyFactors())
			if (f.getFlowProperty().getRefId().equals(propertyRefId))
				return f;
		return null;
	}

	private Unit findUnit(JsonObject json, ProductSystem s) {
		FlowPropertyFactor f = s.getTargetFlowPropertyFactor();
		if (f == null)
			return null;
		String unitRefId = In.getRefId(json, "targetUnit");
		UnitGroup ug = f.getFlowProperty().getUnitGroup();
		for (Unit u : ug.getUnits())
			if (u.getRefId().equals(unitRefId))
				return u;
		return null;
	}

	private void addProcesses(JsonObject json, ProductSystem s) {
		JsonArray array = In.getArray(json, "processes");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			JsonObject ref = element.getAsJsonObject();
			String refId = In.getString(ref, "@id");
			Process p = ProcessImport.run(refId, conf);
			if (p != null)
				s.getProcesses().add(p.getId());
		}
	}

	private void addLinks(JsonObject json, ProductSystem s) {
		JsonArray array = In.getArray(json, "processLinks");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			JsonObject ref = element.getAsJsonObject();
			ProcessLink link = new ProcessLink();
			String providerRefId = In.getRefId(ref, "provider");
			Process provider = ProcessImport.run(providerRefId, conf);
			if (provider == null)
				continue;
			link.providerId = provider.getId();
			String recipientRefId = In.getRefId(ref, "recipient");
			Process recipient = ProcessImport.run(recipientRefId, conf);
			if (recipient == null)
				continue;
			link.processId = recipient.getId();
			JsonObject oObj = In.getObject(ref, "providerOutput");
			String flowRefId = In.getRefId(oObj, "flow");
			Flow flow = FlowImport.run(flowRefId, conf);
			if (flow == null)
				continue;
			link.flowId = flow.getId();
			// TODO: set exchange ID
			s.getProcessLinks().add(link);
		}
	}

	private void addParameters(JsonObject json, ProductSystem s) {
		JsonArray array = In.getArray(json, "parameterRedefs");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			JsonObject ref = element.getAsJsonObject();
			ParameterRedef p = new ParameterRedef();
			p.setName(In.getString(ref, "name"));
			p.setValue(In.getDouble(ref, "value", 0));
			p.setUncertainty(Uncertainties.read(In
					.getObject(ref, "uncertainty")));
			JsonObject context = In.getObject(ref, "context");
			if (context == null) {
				s.getParameterRedefs().add(p);
				continue;
			}
			String type = In.getString(context, "@type");
			if (!Process.class.getSimpleName().equals(type))
				continue;
			String refId = In.getString(context, "@id");
			Process model = ProcessImport.run(refId, conf);
			if (model == null)
				continue;
			p.setContextType(ModelType.PROCESS);
			p.setContextId(model.getId());
			s.getParameterRedefs().add(p);
		}
	}

}
