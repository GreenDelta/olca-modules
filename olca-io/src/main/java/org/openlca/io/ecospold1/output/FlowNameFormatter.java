package org.openlca.io.ecospold1.output;

import java.util.Objects;

import org.openlca.commons.Strings;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.io.ecospold1.output.EcoSpold1Export.EcoSpold1Config;
import org.openlca.util.Exchanges;

class FlowNameFormatter {

	private final EcoSpold1Config config;

	FlowNameFormatter(EcoSpold1Config config) {
		this.config = config;
	}

	String of(Exchange exchange, Process process) {
		if (exchange == null || exchange.flow == null)
			return "?";
		if (!isProduct(exchange.flow))
			return baseName(exchange.flow);
		return Exchanges.isProviderFlow(exchange)
				? of(exchange.flow, process)
				: of(exchange.flow);
	}

	String of(Flow flow, Process process) {
		if (flow == null || Strings.isBlank(flow.name))
			return "?";
		if (!isProduct(flow))
			return baseName(flow);
		if (process == null
				|| Strings.isBlank(process.name)
				|| process.name.startsWith("Dummy: "))
			return of(flow);

		var label = baseName(flow);
		if (config.withProcessSuffixes
				&& !Objects.equals(flow.name, process.name)) {
			label += " | " + process.name;
		}
		if (config.withLocationSuffixes) {
			var location = process.location != null
					? process.location
					: flow.location;
			var suffix = suffixOf(location);
			if (suffix != null) {
				label += suffix;
			}
		}
		if (config.withTypeSuffixes) {
			label += process.processType == ProcessType.LCI_RESULT
					? ", S"
					: ", U";
		}
		return label;
	}

	String of(Flow flow) {
		if (flow == null || Strings.isBlank(flow.name))
			return "?";
		var label = baseName(flow);
		if (!isProduct(flow) || !config.withLocationSuffixes)
			return label;
		var suffix = suffixOf(flow.location);
		return suffix != null
				? label + suffix
				: label;
	}

	private String baseName(Flow flow) {
		return flow.name.trim();
	}

	private boolean isProduct(Flow flow) {
		return flow != null && flow.flowType != FlowType.ELEMENTARY_FLOW;
	}

	private String suffixOf(Location location) {
		return location != null && Strings.isNotBlank(location.code)
				? " {" + location.code + "}"
				: null;
	}
}
