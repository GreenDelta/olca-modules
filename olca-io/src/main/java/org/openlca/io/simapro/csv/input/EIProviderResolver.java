package org.openlca.io.simapro.csv.input;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.openlca.commons.Strings;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.Result;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.slf4j.LoggerFactory;

/**
 * Specifically for the ecoinvent database, the process names in
 * openLCA and SimaPro follow a structural pattern of activity, flow,
 * and location name. When importing a SimaPro CSV with processes,
 * we can use these patterns to link these processes against a
 * background database in openLCA.
 */
public class EIProviderResolver {

	private final Map<String, Provider> providers;

	private EIProviderResolver(Map<String, Provider> providers) {
		this.providers = providers;
	}

	static EIProviderResolver empty() {
		return new EIProviderResolver(Collections.emptyMap());
	}

	public static EIProviderResolver forProcessesOf(IDatabase db) {
		if (db == null)
			return new EIProviderResolver(Collections.emptyMap());

		var log = LoggerFactory.getLogger(EIProviderResolver.class);
		log.info("collect providers from database");
		var locations = new LocationDao(db).getCodes();
		var map = new HashMap<String, Provider>();
		for (var tf : TechIndex.of(db)) {

			if (!(tf.provider() instanceof ProcessDescriptor p)
					|| p.location == null
					|| Strings.isBlank(p.name)
					|| tf.flow() == null)
				continue;

			var loc = locations.get(p.location);
			if (loc == null)
				continue;

			// the process name need to follow the pattern:
			// {activity} | {flow} ...
			var name = tf.provider().name;
			var parts = name.split("\\|");
			if (parts.length < 2 || !matchesFlow(parts[1], tf))
				continue;

			var key = keyOf(parts[0], parts[1], loc);
			if (map.containsKey(key)) {
				log.warn("key mapped to multiple providers: {}", key);
				continue;
			}
			map.put(key, new Provider(key, tf));
		}

		log.info("collected {} providers", map.size());
		return new EIProviderResolver(map);
	}

	/// Creates a provider resolver for the results of the given database. The
	/// result names in the database need to follow the pattern:
	/// `{process} | {flow} - {location code}`
	/// The process and flow names are case-insensitive and the location code
	/// needs to start with an uppercase letter.
	public static EIProviderResolver forResultsOf(IDatabase db) {
		if (db == null)
			return new EIProviderResolver(Collections.emptyMap());

		var log = LoggerFactory.getLogger(EIProviderResolver.class);
		log.info("collect result providers from database");
		var map = new HashMap<String, Provider>();

		for (var d : db.getDescriptors(Result.class)) {
			var result = db.get(Result.class, d.id);
			var refFlow = result.referenceFlow;
			if (Strings.isBlank(result.name) || refFlow == null)
				continue;

			var name = result.name;
			var parts = name.split("\\|");
			if (parts.length < 2)
				continue;
			var process = parts[0].strip();

			String flow = null;
			String location = null;
			var rest = parts[1].strip();
			for (int i = 0; i < rest.length(); i++) {
				if (rest.charAt(i) != '-' || (i + 1) == rest.length())
					continue;
				var code = rest.substring(i + 1).strip();
				if (Character.isUpperCase(code.charAt(0))) {
					flow = rest.substring(0, i).strip();
					location = code;
					break;
				}
			}

			if (flow == null)
				continue;
			var key = keyOf(process, flow, location);
			map.put(key, new Provider(key, TechFlow.of(result)));
		}

		log.info("collected {} providers", map.size());
		return new EIProviderResolver(map);
	}

	private static String keyOf(String process, String flow, String location) {
		return norm(process) + " | " + norm(flow) + " | " + norm(location);
	}

	private static String norm(String s) {
		return s != null
				? s.strip().toLowerCase(Locale.US)
				: "";
	}

	private static boolean matchesFlow(String s, TechFlow tf) {
		return s != null
				&& tf != null
				&& tf.flow() != null
				&& tf.flow().name != null
				&& s.strip().equalsIgnoreCase(tf.flow().name.strip());
	}

	public Optional<Provider> resolve(String spName) {
		if (spName == null || providers.isEmpty())
			return Optional.empty();

		// split the SimaPro name
		var parts = spName.split("\\{");
		if (parts.length < 2)
			return Optional.empty();
		var flow = norm(parts[0]);

		parts = parts[1].split("}");
		if (parts.length < 2)
			return Optional.empty();
		var location = norm(parts[0]);
		if (location.equals("global")) {
			location = "glo";
		}

		parts = spName.split("\\|");
		if (parts.length < 2)
			return Optional.empty();
		var process = norm(parts[1]);

		// match name parts against openLCA names

		// test market processes
		if (process.equals("market for") || process.equals("market group for")) {
			var p = providerOf(process + " " + flow, flow, location);
			if (p != null)
				return Optional.of(p);
		}

		// test "{process} | {flow} |..." pattern
		var p = providerOf(process, flow, location);
		if (p != null)
			return Optional.of(p);

		// test "{flow}{separator}{process} | ..." pattern
		if ((p = providerOf(flow + ", " + process, flow, location)) != null) {
			return Optional.of(p);
		}
		if ((p = providerOf(flow + " " + process, flow, location)) != null) {
			return Optional.of(p);
		}

		// test "{flow-pre} {process}, {flow-suf} | {flow} | ..." pattern
		int commaPos = flow.indexOf(',');
		if (commaPos > 0) {
			var pre = flow.substring(0, commaPos);
			var suf = flow.substring(commaPos);
			p = providerOf(pre + " " + process + suf, flow, location);
			if (p != null)
				return Optional.of(p);
		}

		// test "{proc-pre} {flow}, {proc-suf} | {flow} ..." pattern
		commaPos = process.indexOf(',');
		if (commaPos > 0) {
			var pre = process.substring(0, commaPos);
			var suf = process.substring(commaPos);
			p = providerOf(pre + " " + flow + suf, flow, location);
			if (p != null)
				return Optional.of(p);
		}

		// specific "{flow} | {flow} ..." patterns
		if (process.isEmpty() || process.equals("processing")) {
			p = providerOf(flow, flow, location);
			if (p != null)
				return Optional.of(p);
		}

		return Optional.empty();
	}

	private Provider providerOf(String process, String flow, String location) {
		var key = keyOf(process, flow, location);
		return providers.get(key);
	}

	public boolean isEmpty() {
		return providers.isEmpty();
	}

	public record Provider(String key, TechFlow techFlow) {

		@Override
		public String toString() {
			return key;
		}

	}
}
