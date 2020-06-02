package org.openlca.io;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.jsonld.Json;
import org.slf4j.LoggerFactory;

/**
 * Import flow sheets from HSC Sim (https://www.hsc-chemistry.com/) as process
 * data sets into openLCA.
 */
public class HSCSim {

	public static Optional<Process> importProcess(
			IDatabase db, File file, FlowMap flowMap) {
		try (var raf = new RandomAccessFile(file, "r");
			 var chan = raf.getChannel();
			 var buff = Channels.newReader(chan, StandardCharsets.UTF_8)) {
			var gson = new Gson();
			var json = gson.fromJson(buff, JsonObject.class);
			return importProcess(db, json, flowMap);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(HSCSim.class);
			log.error("failed to read HSC SIM flow sheet from " + file, e);
			return Optional.empty();
		}
	}

	public static Optional<Process> importProcess(
			IDatabase db, JsonObject obj, FlowMap flowMap) {
		return new Import(db, flowMap).of(obj);
	}

	private static class Import {

		private final IDatabase db;
		private final FlowMap map;

		Import(IDatabase db, FlowMap map) {
			this.db = db;
			this.map = map;
		}

		Optional<Process> of(JsonObject obj) {
			if (obj == null)
				return Optional.empty();

			// TODO: correct the property names
			var sheet = Json.getObject(obj, "HSCSimFlowSheet");
			if (sheet == null)
				return Optional.empty();

			var process = initProcess(sheet);
			var streams = Json.getArray(obj, "streams");
			if (streams != null) {
				for (var elem : streams) {
					if (!elem.isJsonObject())
						continue;
					var stream = elem.getAsJsonObject();
					var amount = Json.getDouble(stream, "amount");
					if (amount.isEmpty())
						continue;
					var flow = mapFlow(stream);
					if (flow.isEmpty())
						continue;

					// TODO: var exchange = Exchange.of(flowEntry.)
				}
			}
			return Optional.of(new ProcessDao(db).insert(process));
		}

		private Process initProcess(JsonObject sheet) {
			var process = new Process();
			process.refId = UUID.randomUUID().toString();
			process.name = Json.getString(sheet, "name"); // TODO
			process.description  = Json.getString(sheet, "description"); // TODO
			process.processType = ProcessType.LCI_RESULT;
			process.category = new CategoryDao(db)
					.sync(ModelType.PROCESS, "HSC Flow Sheets"); // TODO
			return process;
		}

		private Optional<FlowMapEntry> mapFlow(JsonObject stream) {
			return Optional.empty(); // TODO
		}
	}
}
