package org.openlca.io;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.jsonld.Json;
import org.openlca.util.KeyGen;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
		private final Map<String, FlowMapEntry> map;
		private UnitMapping units;

		Import(IDatabase db, FlowMap map) {
			this.db = db;
			this.map = map == null
				? Collections.emptyMap()
				: map.index();
		}

		Optional<Process> of(JsonObject obj) {
			if (obj == null)
				return Optional.empty();
			var sheet = Json.getObject(obj, "HSCSimFlowsheet");
			if (sheet == null)
				return Optional.empty();
			var process = initProcess(sheet);
			var inputs = Json.getArray(sheet, "input_streams");
			if (inputs != null) {
				addExchanges(process, inputs, true);
			}
			var outputs = Json.getArray(sheet, "output_streams");
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
				var amount = Json.getDouble(stream, "value");
				if (amount.isEmpty())
					continue;
				var e = exchange(stream);
				if (e.isEmpty())
					continue;
				var exchange = e.get();

				// the exchange.amount field contains a possible
				// conversion factor
				exchange.amount *= amount.getAsDouble();
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
			var flowDao = new FlowDao(db);

			// create an exchange from an existing flow
			var flow = flowDao.getForRefId(id);
			if (flow != null)
				return Optional.of(fromExisting(flow, unit(stream)));

			// create an exchange from a mapped flow
			var mapEntry = map.get(id);
			if (mapEntry != null) {
				var e = fromMapped(mapEntry);
				if (e.isPresent())
					return e;
			}

			// create a new product exchange
			var u = mappedUnit(stream);
			var name = Json.getString(stream, "name");
			if (name == null)
				return Optional.empty();
			flow = flowDao.insert(
				Flow.product(name, u.flowProperty));
			var exchange = Exchange.of(
				flow, u.flowProperty, u.unit);
			exchange.amount = 1.0;
			return Optional.of(exchange);
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
				|| fme.targetFlow() == null
				|| fme.targetFlow().flow == null)
				return Optional.empty();

			var flow = new FlowDao(db).getForRefId(
				fme.targetFlow().flow.refId);
			if (flow == null)
				return Optional.empty();

			// get the flow property
			FlowProperty property = null;
			if (fme.targetFlow().property != null) {
				property = new FlowPropertyDao(db).getForRefId(
					fme.targetFlow().property.refId);
			}
			if (property == null) {
				property = flow.referenceFlowProperty;
			}

			// get the unit
			Unit unit = null;
			if (fme.targetFlow().unit != null) {
				unit = new UnitDao(db).getForRefId(
					fme.targetFlow().unit.refId);
			}
			if (unit == null) {
				unit = flow.getReferenceUnit();
			}

			var exchange = Exchange.of(flow, property, unit);
			exchange.amount = fme.factor();
			return Optional.empty();
		}

		private Exchange fromExisting(Flow flow, String unit) {
			FlowProperty prop = null;
			Unit u = null;
			for (var f : flow.flowPropertyFactors) {
				if (f.flowProperty == null
					|| f.flowProperty.unitGroup == null)
					continue;
				u = f.flowProperty.unitGroup.getUnit(unit);
				if (u != null) {
					prop = f.flowProperty;
					break;
				}
			}
			var exchange = prop == null
				? Exchange.of(flow)
				: Exchange.of(flow, prop, u);
			exchange.amount = 1.0;
			return exchange;
		}

		private String unit(JsonObject stream) {
			var unit = Json.getString(stream, "unit");
			if (unit == null)
				return "unit";
			unit = unit.trim();
			return unit.endsWith("/h")
				? unit.substring(0, unit.length() - 2)
				: unit + "h";
		}

		private UnitMappingEntry mappedUnit(JsonObject stream) {
			if (units == null) {
				units = UnitMapping.createDefault(db);
			}
			var symbol = unit(stream);
			var entry = units.getEntry(symbol);
			if (entry != null)
				return entry;
			// create a default unit group and flow property
			// for the unknown unit
			var unit = Unit.of(symbol);
			var group = new UnitGroupDao(db).insert(
				UnitGroup.of("Unit " + symbol, unit));
			var flowProp = new FlowPropertyDao(db).insert(
				FlowProperty.of("Quantity of " + symbol, group));
			entry = new UnitMappingEntry();
			entry.factor = 1.0;
			entry.flowProperty = flowProp;
			entry.unit = group.referenceUnit;
			entry.unitGroup = group;
			entry.unitName = symbol;
			units.put(symbol, entry);
			return entry;
		}
	}
}
