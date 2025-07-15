package examples;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.openlca.core.DataDir;
import org.openlca.core.io.maps.FlowMap;
import org.openlca.io.hestia.HestiaClient;
import org.openlca.io.hestia.HestiaImport;
import org.openlca.jsonld.Json;

public class HestiaImportExample {

	public static void main(String[] args) throws Exception {
		var cycleId = "bananaFruit-brazil-2010-2025-20250430";

		var apiKey = Files.readString(
				Paths.get("target", ".hestia-api-key")).strip();
		try (var db = DataDir.get().openDatabase("hestia-tests");
				 var client = HestiaClient.of(apiKey)
		) {
			var cycle = client.getCycle(cycleId);
			Json.write(
					cycle.value().json(),
					new File("target/cycle_" + cycleId + ".json"));

			new HestiaImport(client, db, FlowMap.empty())
					.importCycle(cycleId);
		}
	}
}
