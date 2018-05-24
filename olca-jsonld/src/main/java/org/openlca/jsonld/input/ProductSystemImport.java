package org.openlca.jsonld.input;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.jsonld.Json;

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
		String processRefId = Json.getRefId(json, "referenceProcess");
		if (processRefId != null)
			s.referenceProcess = ProcessImport.run(processRefId, conf);
		s.targetAmount = Json.getDouble(json, "targetAmount", 1d);
		addProcesses(json, s);
		addParameters(json, s);
		addInventory(json, s);
		importLinkRefs(json, s);
		ProductSystemExchanges.map(json, conf, s);
		return conf.db.put(s);
	}

	private void addProcesses(JsonObject json, ProductSystem s) {
		JsonArray array = Json.getArray(json, "processes");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			JsonObject ref = element.getAsJsonObject();
			String refId = Json.getString(ref, "@id");
			Process p = ProcessImport.run(refId, conf);
			if (p != null)
				s.processes.add(p.getId());
		}
	}

	private void importLinkRefs(JsonObject json, ProductSystem s) {
		JsonArray array = Json.getArray(json, "processLinks");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			JsonObject obj = element.getAsJsonObject();
			String providerRefId = Json.getRefId(obj, "provider");
			ProcessImport.run(providerRefId, conf);
			String processRefId = Json.getRefId(obj, "process");
			ProcessImport.run(processRefId, conf);
			String flowRefId = Json.getRefId(obj, "flow");
			FlowImport.run(flowRefId, conf);
		}
	}

	private void addParameters(JsonObject json, ProductSystem s) {
		JsonArray array = Json.getArray(json, "parameterRedefs");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			JsonObject ref = element.getAsJsonObject();
			ParameterRedef p = new ParameterRedef();
			p.setName(Json.getString(ref, "name"));
			p.setValue(Json.getDouble(ref, "value", 0));
			p.setUncertainty(Uncertainties.read(Json
					.getObject(ref, "uncertainty")));
			JsonObject context = Json.getObject(ref, "context");
			if (context == null) {
				s.parameterRedefs.add(p);
				continue;
			}
			String type = Json.getString(context, "@type");
			if (!Process.class.getSimpleName().equals(type))
				continue;
			String refId = Json.getString(context, "@id");
			Process model = ProcessImport.run(refId, conf);
			if (model == null)
				continue;
			p.setContextType(ModelType.PROCESS);
			p.setContextId(model.getId());
			s.parameterRedefs.add(p);
		}
	}

	private void addInventory(JsonObject json, ProductSystem s) {
		s.inventory.clear();
		JsonArray array = Json.getArray(json, "inventory");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement element : array) {
			JsonObject ref = element.getAsJsonObject();
			Exchange e = Exchanges.map(ref, conf);
			s.inventory.add(e);
		}
	}
	
}
