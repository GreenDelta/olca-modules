package examples;

import java.io.File;

import org.openlca.core.DataDir;
import org.openlca.io.hestia.HestiaClient;
import org.openlca.io.hestia.SearchQuery;
import org.openlca.jsonld.Json;

public class HestiaCycleFetch {

	public static void main(String[] args) {

		var dataDir = DataDir.get();
		var apiKey = Json.readObject(new File(dataDir.root(), ".hestia.json"))
			.orElseThrow()
			.get("apiKey")
			.getAsString();

		try (var client = HestiaClient.of(apiKey)) {
			System.out.println("Get releases");
			var release = client.getReleases()
				.orElseThrow()
				.getFirst();
			System.out.println("  -> selected release: " + release.version());

			System.out.println("Search for 'banana'");
			var query = new SearchQuery(10, "banana", true, release.version());
			var result = client.search(query)
				.orElseThrow()
				.getFirst();
			System.out.println("  -> selected cycle: " + result.name());

			System.out.println("Download cycle: " + result.id());
			var cycle = client.getCycle(result.id(), release.version())
				.orElseThrow();
			System.out.println(Json.toPrettyString(cycle.json()));
		}
	}
}
