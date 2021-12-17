package org.openlca.io.ilcd.input;

import java.util.HashMap;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.openlca.core.model.Location;
import org.openlca.core.model.ResultFlow;
import org.openlca.core.model.ResultModel;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;

public record EpdImport(ImportConfig config, Process dataSet) {

	public ResultModel run() {
		var result = config.db().get(ResultModel.class, dataSet.getUUID());
		return result != null
			? result
			: createNew();
	}

	private ResultModel createNew() {
		var result = new ResultModel();
		result.refId = dataSet.getUUID();
		result.name = Strings.cut(
			Processes.fullName(dataSet, config.langOrder()), 2024);
		var info = Processes.getDataSetInfo(dataSet);
		if (info != null) {
			result.description = config.str(info.comment);
		}

		var locations = new HashMap<String, Location>();
		var index = new TIntObjectHashMap<ResultFlow>();
		for (var e : dataSet.exchanges) {
			if (e.flow == null || e.flow.uuid == null)
				continue;
			var syncFlow = FlowImport.get(config, e.flow.uuid);
			if (syncFlow.isEmpty()) {
				config.log().warn("EPD '" + result.refId
					+ "' contains invalid flow references");
				continue;
			}
			var resultFlow = new ResultFlow();
			resultFlow.flow = syncFlow.flow();
			resultFlow.flowPropertyFactor = syncFlow.property();
			resultFlow.unit = syncFlow.unit();
			resultFlow.amount = e.resultingAmount == null
				? e.meanAmount
				: e.resultingAmount;
			resultFlow.isInput = e.direction == ExchangeDirection.INPUT;
			resultFlow.description =config.str(e.comment);
			resultFlow.location = locationOf(e.location, locations);
			result.inventory.add(resultFlow);
			index.put(e.id, resultFlow);
		}

		if (dataSet.lciaResults != null) {
			for (var r : dataSet.lciaResults) {
				if (r.method == null || r.method.uuid == null)
					continue;

			}
		}

		return config.db().insert(result);
	}

	private Location locationOf(String code, HashMap<String, Location> locations) {
		if (Strings.nullOrEmpty(code))
			return null;
		if (locations.isEmpty()) {
			config.db().allOf(Location.class).forEach(
				loc -> locations.put(loc.code, loc));
		}
		var cached = locations.get(code);
		if (cached != null)
			return cached;
		var loc = new Location();
		loc.refId = KeyGen.get(code);
		loc.code = code;
		loc.name = code;
		config.db().insert(loc);
		locations.put(code, loc);
		config.log().ok("created new location for unknown code: '"
			+ code + "'", Descriptor.of(loc));
		return loc;
	}


}
