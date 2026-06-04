package org.openlca.io.ecospold1.output;

import java.util.Map;

import org.openlca.commons.Strings;
import org.openlca.core.database.LocationDao;
import org.openlca.core.matrix.cache.ProviderMap;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.io.ecospold1.output.EcoSpold1Export.EcoSpold1Config;
import org.openlca.util.Exchanges;

class FlowNameFormatter {

	private final ProviderMap providers;
	private final EcoSpold1Config config;
	private Map<Long, String> locationCodes;

	FlowNameFormatter(EcoSpold1Config config) {
		this.config = config;
		if (config.withLocationSuffixes
			|| config.withProcessSuffixes
			|| config.withTypeSuffixes) {
			providers = ProviderMap.create(config.db);
		} else {
			providers = null;
		}
	}

	String of(Process owner, Exchange e) {
		if (e == null || e.flow == null)
			return "";
		if (owner == null
			|| providers == null
			|| e.flow.flowType == null
			|| e.flow.flowType == FlowType.ELEMENTARY_FLOW)
			return baseNameOf(e.flow);

		var base = baseNameOf(e.flow);
		if (Exchanges.isProviderFlow(e))
			return Provider.of(owner).addSuffix(base, config);
		if (!Exchanges.isLinkable(e))
			return base;

		var p = providerOf(e);
		return p != null
			? new Provider(p.name, locationOf(p), typeOf(p)).addSuffix(base, config)
			: base;
	}

	private RootDescriptor providerOf(Exchange e) {
		if (e.defaultProviderId != 0) {
			var p = providers.getProvider(e.defaultProviderId);
			if (p != null)
				return p;
		}
		var ps = providers.getProvidersOf(e.flow.id);
		if (ps.isEmpty())
			return null;
		for (var p : ps) {
			if (p.isProcess())
				return p.provider();
		}
		return null;
	}

	private String locationOf(RootDescriptor d) {
		if (!config.withLocationSuffixes
			|| !(d instanceof ProcessDescriptor proc)
		|| proc.location == null)
			return null;
		if (locationCodes == null) {
			locationCodes = new LocationDao(config.db).getCodes();
		}
		return locationCodes.get(proc.location);
	}

	private String typeOf(RootDescriptor d) {
		if (!config.withTypeSuffixes || !(d instanceof ProcessDescriptor proc))
			return null;
		return proc.processType == ProcessType.LCI_RESULT ? "S" : "U";
	}


	private String baseNameOf(Flow flow) {
		var name = flow.name;
		return Strings.isNotBlank(name) ? flow.name.trim() : "";
	}

	private record Provider(String name, String location, String type) {

		static Provider of(Process proc) {
			var loc = proc.location != null && Strings.isNotBlank(proc.location.code)
				? proc.location.code
				: null;
			var type = proc.processType == ProcessType.LCI_RESULT ? "S" : "U";
			return new Provider(proc.name, loc, type);
		}

		String addSuffix(String base, EcoSpold1Config conf) {
			var full = base;
			if (conf.withLocationSuffixes && Strings.isNotBlank(location)) {
				full += " {" + location.trim() + "}";
			}
			if (conf.withProcessSuffixes && Strings.isNotBlank(name)) {
				full += " | " + name.trim();
			}
			if (conf.withTypeSuffixes && Strings.isNotBlank(type)) {
				full += " | " + type;
			}
			return full;
		}
	}
}
