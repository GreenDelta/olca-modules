package org.openlca.io.refdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVPrinter;
import org.openlca.commons.Strings;
import org.openlca.core.model.ImpactCategory;

class ImpactCategoryExport implements Runnable {

	private final ExportConfig config;

	ImpactCategoryExport(ExportConfig config) {
		this.config = config;
	}

	@Override
	public void run() {
		var indicators = config.db().getAll(ImpactCategory.class);
		if (indicators.isEmpty())
			return;
		config.sort(indicators);
		var buffer = new ArrayList<>(6);

		// write LCIA category meta-data
		config.writeTo("lcia_categories.csv", csv -> {

			// write column headers
			csv.printRecord(
					"ID",
					"Name",
					"Description",
					"Category",
					"Reference unit");

			for (var indicator : indicators) {
				buffer.add(indicator.refId);
				buffer.add(indicator.name);
				buffer.add(indicator.description);
				buffer.add(config.toPath(indicator.category));
				buffer.add(indicator.referenceUnit);
				csv.printRecord(buffer);
				buffer.clear();
			}
		});

		// if we can create short IDs write several LCIA factor
		// files, otherwise put everything into one big file
		var shortIds = shortIdsOf(indicators);
		if (shortIds.isEmpty()) {
			config.writeTo("lcia_factors/all.csv", csv -> {
				writeFactorHeaders(csv);
				for (var indicator : indicators) {
					writeFactors(csv, buffer, indicator);
				}
			});
		} else {
			for (var indicator : indicators) {
				if (indicator.impactFactors.isEmpty())
					continue;
				var shortId = shortIds.get(indicator.id);
				config.writeTo("lcia_factors/" + shortId + ".csv", csv -> {
					writeFactorHeaders(csv);
					writeFactors(csv, buffer, indicator);
				});
			}
		}

	}

	private void writeFactorHeaders(CSVPrinter csv) throws IOException {
		csv.printRecord(
				"LCIA category",
				"Flow",
				"Flow property",
				"Flow unit",
				"Location",
				"Factor");
	}

	private void writeFactors(CSVPrinter csv, ArrayList<Object> buffer,
			ImpactCategory indicator) throws IOException {
		for (var factor : indicator.impactFactors) {
			buffer.add(indicator.refId);

			var flowId = factor.flow != null
					? factor.flow.refId
					: "";
			buffer.add(flowId);

			var propFactor = factor.flowPropertyFactor;
			var prop = propFactor != null && propFactor.flowProperty != null
					? propFactor.flowProperty.name
					: "";
			buffer.add(prop);

			var unit = factor.unit != null
					? factor.unit.name
					: "";
			buffer.add(unit);

			var location = factor.location != null
					? factor.location.name
					: "";
			buffer.add(location);

			var value = Strings.isNotBlank(factor.formula)
					? factor.formula
					: factor.value;

			buffer.add(value);

			csv.printRecord(buffer);
			buffer.clear();
		}
	}

	private Map<Long, String> shortIdsOf(List<ImpactCategory> indicators) {
		int len = -1;
		var ids = new HashSet<String>();
		for (int l = 4; l < 36; l++) {
			ids.clear();
			boolean noDup = true;
			for (var ind : indicators) {
				if (ind.refId == null || l >= ind.refId.length())
					return Collections.emptyMap();
				var shortId = ind.refId.substring(0, l);
				if (ids.contains(shortId)) {
					noDup = false;
					break;
				}
				ids.add(shortId);
			}
			if (noDup) {
				len = l;
				break;
			}
		}
		if (len < 0)
			return Collections.emptyMap();
		var map = new HashMap<Long, String>();
		for (var ind : indicators) {
			map.put(ind.id, ind.refId.substring(0, len));
		}
		return map;
	}
}
