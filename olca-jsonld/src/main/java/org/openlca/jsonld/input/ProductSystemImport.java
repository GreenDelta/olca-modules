package org.openlca.jsonld.input;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

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
		s.setTargetAmount(In.getDouble(json, "targetAmount", 1d));
		s.setTargetFlowPropertyFactor(findFactor(json, s));
		s.setTargetUnit(findUnit(json, s));
		addProcesses(json, s);
		addParameters(json, s);
		importLinkRefs(json, s);
		ProductSystemExchanges.map(json, conf, s);
		return conf.db.put(s);
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

	private void importLinkRefs(JsonObject json, ProductSystem s) {
		JsonArray array = In.getArray(json, "processLinks");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			JsonObject obj = element.getAsJsonObject();
			String providerRefId = In.getRefId(obj, "provider");
			ProcessImport.run(providerRefId, conf);
			String processRefId = In.getRefId(obj, "process");
			ProcessImport.run(processRefId, conf);
			String flowRefId = In.getRefId(obj, "flow");
			FlowImport.run(flowRefId, conf);
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
