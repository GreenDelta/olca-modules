package examples;

import java.io.File;

import org.openlca.core.DataDir;
import org.openlca.core.io.maps.FlowMap;
import org.openlca.io.hestia.EcoinventFlowMap;
import org.openlca.io.hestia.HestiaClient;
import org.openlca.jsonld.Json;

public class HestiaEcoinventFlowMapExampleTemp {

	public static void main(String[] args) {
		var dataDir = DataDir.get();
		var apiKey = Json.readObject(new File(dataDir.root(), ".hestia.json"))
			.orElseThrow()
			.get("apiKey")
			.getAsString();

		var dbName = "ecoinvent 3.11 Cutoff Unit-Processes 2025-01-31";
		try (var db = dataDir.openDatabase(dbName);
				 var client = HestiaClient.of(apiKey)) {
			var dir = new File("target/hestia-glossary");
			var flowMap = EcoinventFlowMap
				.buildFrom(db, client, dir)
				.orElseThrow();
			FlowMap.toCsv(flowMap, new File("target/HESTIA_EI.csv"));
		}
	}
}
