package org.openlca.jsonld.output;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.BaseDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.jsonld.ExchangeKey;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ProductSystemWriter extends Writer<ProductSystem> {

	private BaseDao<Exchange> exchangeDao;
	private ProductSystem system;

	ProductSystemWriter(ExportConfig conf) {
		super(conf);
		if (conf.db != null)
			exchangeDao = new BaseDao<>(Exchange.class, conf.db);
	}

	@Override
	JsonObject write(ProductSystem system) {
		JsonObject obj = super.write(system);
		if (obj == null)
			return null;
		this.system = system;
		Out.put(obj, "referenceProcess", system.getReferenceProcess(), conf);
		String processRefId = system.getReferenceProcess().getRefId();
		JsonObject eObj = createExchangeRef(processRefId,
				system.getReferenceExchange());
		Out.put(obj, "referenceExchange", eObj);
		FlowProperty property = null;
		if (system.getTargetFlowPropertyFactor() != null)
			property = system.getTargetFlowPropertyFactor().getFlowProperty();
		Out.put(obj, "targetFlowProperty", property, conf);
		Out.put(obj, "targetUnit", system.getTargetUnit(), conf);
		Out.put(obj, "targetAmount", system.getTargetAmount());
		ParameterRedefs.map(obj, system.getParameterRedefs(), conf.db, conf, (
				type, id) -> createProcessRef(id));
		if (conf.db == null)
			return obj;
		mapProcesses(obj);
		mapLinks(obj);
		return obj;
	}

	private void mapLinks(JsonObject json) {
		JsonArray links = new JsonArray();
		for (ProcessLink link : system.getProcessLinks()) {
			JsonObject obj = new JsonObject();
			JsonObject provider = createProcessRef(link.getProviderId());
			Out.put(obj, "provider", provider);
			String providerRefId = provider.get("@id").getAsString();
			Exchange eOutput = loadExchange(link.getProviderId(),
					link.getFlowId());
			JsonObject output = createExchangeRef(providerRefId, eOutput);
			Out.put(obj, "providerOutput", output);
			JsonObject recipient = createProcessRef(link.getRecipientId());
			Out.put(obj, "recipient", recipient);
			String recipientRefId = recipient.get("@id").getAsString();
			Exchange eInput = loadExchange(link.getRecipientId(),
					link.getFlowId());
			JsonObject input = createExchangeRef(recipientRefId, eInput);
			Out.put(obj, "recipientInput", input);
			links.add(obj);
		}
		Out.put(json, "processLinks", links);
	}

	private void mapProcesses(JsonObject json) {
		JsonArray processes = new JsonArray();
		for (Long pId : system.getProcesses())
			processes.add(createProcessRef(pId));
		Out.put(json, "processes", processes);
	}

	private JsonObject createProcessRef(Long id) {
		if (id == null)
			return null;
		return References.create(ModelType.PROCESS, id, conf, true);
	}

	private JsonObject createFlowRef(Flow flow) {
		if (flow == null)
			return null;
		return References.create(flow, conf, true);
	}

	private JsonObject createExchangeRef(String pRefId, Exchange e) {
		if (e == null)
			return null;
		JsonObject obj = new JsonObject();
		Out.put(obj, "@type", Exchange.class.getSimpleName());
		String id = ExchangeKey.get(pRefId, getProviderRefId(e), e);
		Out.put(obj, "@id", id);
		Out.put(obj, "flow", createFlowRef(e.getFlow()));
		if (!e.isInput())
			return obj;
		Out.put(obj, "defaultProvider",
				createProcessRef(e.getDefaultProviderId()));
		return obj;
	}

	private String getProviderRefId(Exchange e) {
		JsonObject provider = null;
		Long pId = e.getDefaultProviderId();
		provider = References.create(ModelType.PROCESS, pId, conf,
				conf.exportProviders);
		if (provider == null)
			return null;
		return provider.get("@id").getAsString();
	}

	private Exchange loadExchange(long processId, long flowId) {
		String jpql = "SELECT e FROM Process p JOIN p.exchanges e "
				+ "WHERE p.id = :processId AND e.flow.id = :flowId";
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("processId", processId);
		parameters.put("flowId", flowId);
		return exchangeDao.getFirst(jpql, parameters);
	}

	@Override
	boolean isExportExternalFiles() {
		// Product system files are using local ids, this must be changed first,
		// otherwise this leads to problems after import
		return false;
	}

}
