package org.openlca.io.simapro.csv.input;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.openlca.core.DataDir;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

/**
 * Specifically for the ecoinvent database, the process names in
 * openLCA and SimaPro follow a structural pattern of activity, flow,
 * and location name. When importing a SimaPro CSV with processes,
 * we can use these patterns to link these processes against a
 * background database in openLCA.
 */
public class SmartProviderResolver {

	private final Map<String, Provider> providers;

	private SmartProviderResolver(Map<String, Provider> providers) {
		this.providers = providers;
	}

	public static SmartProviderResolver forProcessesOf(IDatabase db) {
		if (db == null)
			return new SmartProviderResolver(Collections.emptyMap());

		var log = LoggerFactory.getLogger(SmartProviderResolver.class);
		log.info("collect providers from database");
		var locations = new LocationDao(db).getCodes();
		var map = new HashMap<String, Provider>();
		for (var tf : TechIndex.of(db)) {

			if (!(tf.provider() instanceof ProcessDescriptor p)
					|| p.location == null
					|| Strings.nullOrEmpty(p.name)
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
		return new SmartProviderResolver(map);
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
		if (spName == null)
			return Optional.empty();

		var parts = spName.split("\\{");
		if (parts.length < 2)
			return Optional.empty();
		var flow = parts[0].strip();

		parts = parts[1].split("}");
		if (parts.length < 2)
			return Optional.empty();
		var location = parts[0].strip();

		parts = spName.split("\\|");
		if (parts.length < 2)
			return Optional.empty();
		var process = norm(parts[1]);

		if (process.equals("market for")
				|| process.equals("market group for")) {
			process += " " + flow;
		} else if (process.equals("production mix")) {
			process = flow + ", " + process;
		}

		var key = keyOf(process, flow, location);
		return Optional.ofNullable(providers.get(key));
	}

	public record Provider(String key, TechFlow techFlow) {

		@Override
		public String toString() {
			return key;
		}

	}

	public static void main(String[] args) {

		try (var db = DataDir.get().openDatabase("ecoinvent_v3_10_Cutoff_Unit_Processes_2024_01_22")) {
			var resolver = SmartProviderResolver.forProcessesOf(db);

			var names = List.of(
					"Molybdenum {GLO}| market for | Cut-off, S",
					"Cast iron {GLO}| market for | Cut-off, S",
					"Ceramic tile {GLO}| market for | Cut-off, S",
					"Nickel, class 1 {GLO}| market for nickel, class 1 | Cut-off, S",
					"Silicon carbide {GLO}| market for | Cut-off, S",
					"Silver {GLO}| market for | Cut-off, S"
			);

			for (var name : names) {
				System.out.println("Resolve: " + name);
				var p = resolver.resolve(name).orElse(null);
				if (p != null) {
					System.out.println("  ==> " + p);
				} else {
					System.out.println("  NONE");
				}
			}
		}
	}
}
