package org.openlca.io;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Unit;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.jsonld.Json;
import org.openlca.util.KeyGen;
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
			var sheet = Json.getObject(obj, "HSCSimFlowsheet");
			if (sheet == null)
				return Optional.empty();
			var process = initProcess(sheet);
			var inputs = Json.getArray(obj, "input_streams");
			if (inputs != null) {
				addExchanges(process, inputs, true);
			}
			var outputs  = Json.getArray(obj, "output_streams");
			if (outputs != null) {
				addExchanges(process, outputs, false);
			}
			return Optional.of(new ProcessDao(db).insert(process));
		}

		private Process initProcess(JsonObject sheet) {
			var process = new Process();
			process.refId = UUID.randomUUID().toString();
			var info = Json.getObject(sheet, "info");
			if (info != null) {
				process.name = Json.getString(info, "processname");
			}
			process.processType = ProcessType.LCI_RESULT;
			process.category = new CategoryDao(db)
					.sync(ModelType.PROCESS, "HSC Flow Sheets");
			return process;
		}

		private void addExchanges(
				Process process, JsonArray streams, boolean asInputs) {
			if (streams == null)
				return;
			for (var elem : streams) {
				if (!elem.isJsonObject())
					continue;
				var stream = elem.getAsJsonObject();
				var amount = Json.getDouble(stream, "amount");
				if (amount.isEmpty())
					continue;
				var e = exchange(stream);
				if (e.isEmpty())
					continue;
				var exchange = e.get();

				// the exchange.amount field contains a possible
				// conversion factor
				exchange.amount *= amount.get();
				exchange.isInput = asInputs;

				// set the quantitative reference
				var type = Json.getString(stream, "type");
				if (type != null && type.equals("main product")) {
					process.quantitativeReference = exchange;
				}

				process.add(exchange);
			}
		}

		private Optional<Exchange> exchange(JsonObject stream) {
			var id = flowKey(stream);
			var mapEntry = map.getEntry(id);
			if (mapEntry != null) {
				var e = fromMapped(mapEntry);
				if (e.isPresent())
					return e;
			}

			return Optional.empty(); // TODO
		}

		private String flowKey(JsonObject stream) {
			var name = Json.getString(stream, "name");
			var unit = Json.getString(stream, "unit");
			return KeyGen.get("hsc", "stream", name, unit);
		}

		/**
		 * Creates an exchange from a mapped flow.
		 */
		private Optional<Exchange> fromMapped(FlowMapEntry fme) {
			if (fme == null
					|| fme.targetFlow == null
					|| fme.targetFlow.flow == null)
				return Optional.empty();

			var flow = new FlowDao(db).getForRefId(
					fme.targetFlow.flow.refId);
			if (flow == null)
				return Optional.empty();

			// get the flow property
			FlowProperty property = null;
			if (fme.targetFlow.property != null) {
				property = new FlowPropertyDao(db).getForRefId(
						fme.targetFlow.property.refId);
			}
			if (property == null) {
				property = flow.referenceFlowProperty;
			}

			// get the unit
			Unit unit = null;
			if (fme.targetFlow.unit != null) {
				unit = new UnitDao(db).getForRefId(
						fme.targetFlow.unit.refId);
			}
			if (unit == null) {
				unit = flow.getReferenceUnit();
			}

			var exchange = Exchange.of(flow, property, unit);
			exchange.amount = fme.factor;
			return Optional.empty();
		}
	}
}
