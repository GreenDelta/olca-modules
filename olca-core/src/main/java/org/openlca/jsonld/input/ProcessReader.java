package org.openlca.jsonld.input;

import com.google.gson.JsonObject;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.jsonld.Json;

public class ProcessReader implements EntityReader<Process> {

	private final EntityResolver resolver;
	private final TIntObjectHashMap<Exchange> exchanges = new TIntObjectHashMap();


	public ProcessReader(EntityResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public Process read(JsonObject json) {
		var process = new Process();
		update(process, json);
		return process;
	}

	@Override
	public void update(Process p, JsonObject json) {
		Util.mapBase(p, json, resolver);
		p.processType = Json.getEnum(json, "processType", ProcessType.class);
		p.infrastructureProcess = Json.getBool(json, "isInfrastructureProcess", false);
		p.defaultAllocationMethod = Json.getEnum(json, "defaultAllocationMethod", AllocationMethod.class);
		// TODO p.documentation = ProcessDocReader.read(json, conf);

		var locId = Json.getRefId(json, "location");
		p.location = resolver.get(Location.class, locId);

		// DQ systems
		var dqSystemId = Json.getRefId(json, "dqSystem");
		p.dqSystem = resolver.get(DQSystem.class, dqSystemId);
		p.dqEntry = Json.getString(json, "dqEntry");
		var exchangeDqSystemId = Json.getRefId(json, "exchangeDqSystem");
		p.exchangeDqSystem = resolver.get(DQSystem.class, exchangeDqSystemId);
		var socialDqSystemId = Json.getRefId(json, "socialDqSystem");
		p.socialDqSystem = resolver.get(DQSystem.class, socialDqSystemId);

		readParameters(json, p);
		readExchanges(json, p);
		addSocialAspects(json, p);
		addAllocationFactors(json, p);
	}

	private void readParameters(JsonObject json, Process p) {
		p.parameters.clear();
		var parameters = Json.getArray(json, "parameters");
		if (parameters == null || parameters.size() == 0)
			return;
		for (var e : parameters) {
			if (!e.isJsonObject())
				continue;
			var o = e.getAsJsonObject();
			var param = new Parameter();
			ParameterReader.mapFields(param, o);
			param.scope = ParameterScope.PROCESS;
			p.parameters.add(param);
		}
	}

	private void readExchanges(JsonObject json, Process p) {

		var array = Json.getArray(json, "exchanges");
		if (array == null || array.size() == 0) {
			p.exchanges.clear();
			p.quantitativeReference = null;
			return;
		}

		// index the old exchanges, that we may update
		var oldIdx = new TIntObjectHashMap<Exchange>();
		for (var old : p.exchanges) {
			oldIdx.put(old.internalId, old);
		}

		p.lastInternalId = Json.getInt(json, "lastInternalId", 0);
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var o = e.getAsJsonObject();
			int internalId = Json.getInt(json, "internalId", -1);
			var exchange = oldIdx.get(internalId);
			if (exchange == null) {
				exchange = new Exchange();
				exchange.internalId = ++p.lastInternalId;
			}
			exchanges.put(exchange.internalId, exchange);

			var providerId = Json.getRefId(o, "defaultProvider");

			if (providerId != null) {
				conf.providers().add(providerId, ex);
			}
			p.exchanges.add(ex);
			boolean isRef = Json.getBool(o, "isQuantitativeReference", false);
			if (isRef)
				p.quantitativeReference = ex;
		}
	}
}
