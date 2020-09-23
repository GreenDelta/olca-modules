package org.openlca.io.simapro.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.io.maps.FlowRef;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.SubCompartment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes a set of processes to a SimaPro CSV file. It is not an official export
 * that is exposed to the openLCA user interface as there are too many edge
 * cases to get a file that is finally accepted by SimaPro. The idea of this
 * class is to provide a basic export that handles most of the common cases.
 */
public class ProcessWriter {

	private final IDatabase db;
	private FlowMap flowMap;
	private BufferedWriter writer;

	private final Map<String, SimaProUnit> units = new HashMap<>();
	private final Map<Category, Compartment> compartments = new HashMap<>();
	private final Map<Flow, Compartment> flowCompartments = new HashMap<>();
	private final Set<Flow> inputProducts = new HashSet<>();
	private final Set<Flow> outputProducts = new HashSet<>();

	public ProcessWriter(IDatabase db) {
		this.db = db;
	}

	public void setFlowMap(FlowMap flowMap) {
		this.flowMap = flowMap;
	}

	public void write(Collection<ProcessDescriptor> processes, File file) {
		if (processes == null || file == null)
			return;
		try (var fout = new FileOutputStream(file);
			 var w = new OutputStreamWriter(fout, "windows-1252");
			 var buffer = new BufferedWriter(w)) {
			this.writer = buffer;
			writerHeader();
			var dao = new ProcessDao(db);
			for (var descriptor : processes) {
				var process = dao.getForId(descriptor.id);
				if (process == null)
					continue;
				classifyElemFlows(process);
				writeProcess(process);
			}
			writeDummies();
			writeQuantities();
			writeReferenceFlows();
			writeGlobalParameters();
			this.writer = null;
		} catch (Exception e) {
			throw e instanceof RuntimeException
					? (RuntimeException) e
					: new RuntimeException(e);
		}
	}

	private void writeDummies() {
		for (Flow flow : inputProducts) {
			if (outputProducts.contains(flow))
				continue;
			var p = Process.of("Dummy: " + flow.name, flow);
			p.id = flow.id;
			p.category = new Category();
			p.category.name = "Dummy processes";
			writeProcess(p);
		}
	}

	/**
	 * Classifies elementary flows into compartments.
	 */
	private void classifyElemFlows(Process p) {
		for (Exchange e : p.exchanges) {
			if (e.flow == null
					|| e.flow.flowType != FlowType.ELEMENTARY_FLOW)
				continue;

			// 1) the flow was already classified
			Compartment c = flowCompartments.get(e.flow);
			if (c != null)
				continue;

			// 2) check if we have a mapped flow
			FlowMapEntry mapEntry = mappedFlow(e.flow);
			if (mapEntry != null) {
				c = Compartment.of(mapEntry.targetFlow.flowCategory);
				if (c != null) {
					flowCompartments.put(e.flow, c);
					continue;
				}
			}

			// 3) get the compartment from the category path
			c = compartments.computeIfAbsent(
					e.flow.category, Compartment::of);
			if (c == null) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.warn("could not assign compartment to flow {};" +
						" took default air/unspecified", e.flow);
				c = Compartment.of(ElementaryFlowType.EMISSIONS_TO_AIR,
						SubCompartment.UNSPECIFIED);
			}
			flowCompartments.put(e.flow, c);
		}
	}

	private void writeQuantities() {

		// we always write at least the kilogram data
		// into the quantity sections
		SimaProUnit kg = SimaProUnit.kg;

		// quantities
		Set<String> quantities = units.values().stream()
				.map(u -> u.quantity)
				.collect(Collectors.toSet());
		writeln("Quantities");
		if (!quantities.contains(kg.quantity)) {
			writeln(kg.quantity, "Yes");
		}
		for (String q : quantities) {
			writeln(q, "Yes");
		}
		writeln();
		writeln("End");
		writeln();
		writeln();

		// units
		Set<SimaProUnit> us = new HashSet<>(units.values());
		writeln("Units");
		if (!us.contains(kg)) {
			writeln(kg.symbol,
					kg.quantity,
					Double.toString(kg.factor),
					kg.refUnit);
		}
		for (SimaProUnit u : us) {
			writeln(u.symbol,
					u.quantity,
					Double.toString(u.factor),
					u.refUnit);
		}
		writeln();
		writeln("End");
		writeln();
		writeln();
	}

	@SuppressWarnings("unchecked")
	private void writeReferenceFlows() {

		// order flows by their type
		int n = ElementaryFlowType.values().length;
		List<Flow>[] buckets = new List[n];
		for (int i = 0; i < n; i++) {
			buckets[i] = new ArrayList<>();
		}
		for (Map.Entry<Flow, Compartment> e : flowCompartments.entrySet()) {
			ElementaryFlowType type = e.getValue().type;
			buckets[type.ordinal()].add(e.getKey());
		}

		// write the flow information
		for (var type : ElementaryFlowType.values()) {
			writeln(type.getReferenceHeader());

			// duplicate names are not allowed here
			HashSet<String> handledNames = new HashSet<>();
			for (Flow flow : buckets[type.ordinal()]) {

				String name;
				String unit = null;

				FlowMapEntry mapEntry = mappedFlow(flow);
				if (mapEntry != null) {
					// handle mapped flows
					name = mapEntry.targetFlow.flow.name;
					if (mapEntry.targetFlow.unit != null) {
						unit = unit(mapEntry.targetFlow.unit.name);
					}
				} else {
					// handle unmapped flows
					name = flow.name;
					Unit refUnit = null;
					if (flow.referenceFlowProperty != null) {
						if (flow.referenceFlowProperty.unitGroup != null) {
							refUnit = flow.referenceFlowProperty.unitGroup.referenceUnit;
						}
					}
					unit = unit(refUnit);
				}

				if (name == null || unit == null)
					continue;

				String id = name.trim().toLowerCase();
				if (handledNames.contains(id))
					continue;
				handledNames.add(id);

				writeln(name, unit, flow.casNumber, "");
			}
			writeln();
			writeln("End");
			writeln();
			writeln();
		}
	}

	private void writeGlobalParameters() {
		String[] sections = {
				"Database Input parameters",
				"Database Calculated parameters",
				"Project Input parameters",
				"Project Calculated parameters",
		};
		for (String s : sections) {
			writeln(s);
			writeln();
			writeln("End");
			writeln();
		}
	}

	private void writeProcess(Process p) {
		writeProcessDoc(p);

		writeln("Products");
		for (Exchange e : p.exchanges) {
			if (!isProductOutput(e))
				continue;
			outputProducts.add(e.flow);
			writeln(e.flow.name,
					unit(e.unit),
					e.amount,
					100,
					"not defined",
					category(e.flow),
					"");
		}
		writeln();

		writeln("Avoided products");
		writeln();

		writeElemExchanges(p, ElementaryFlowType.RESOURCES);
		writeProductInputs(p);

		writeln("Electricity/heat");
		writeln();

		writeElemExchanges(p, ElementaryFlowType.EMISSIONS_TO_AIR);
		writeElemExchanges(p, ElementaryFlowType.EMISSIONS_TO_WATER);
		writeElemExchanges(p, ElementaryFlowType.EMISSIONS_TO_SOIL);
		writeElemExchanges(p, ElementaryFlowType.FINAL_WASTE_FLOWS);
		writeElemExchanges(p, ElementaryFlowType.NON_MATERIAL_EMISSIONS);
		writeElemExchanges(p, ElementaryFlowType.SOCIAL_ISSUES);
		writeElemExchanges(p, ElementaryFlowType.ECONOMIC_ISSUES);

		writeln("Waste to treatment");
		writeln();

		// input parameters
		writeln("Input parameters");
		for (var param : p.parameters) {
			writeln(param.name,
					param.value,
					"Undefined",
					0,
					0,
					0,
					"No",
					param.description);
		}

		String[] sections = {
				"Waste to treatment",
				"Input parameters",
				"Calculated parameters",
		};
		for (String s : sections) {
			writeln(s);
			writeln();
		}

		writeln("End");
		writeln();
	}

	private void writeProductInputs(Process p) {
		writeln("Materials/fuels");
		for (Exchange e : p.exchanges) {
			if (!isProductInput(e))
				continue;
			inputProducts.add(e.flow);
			writeln(e.flow.name,
					unit(e.unit),
					e.amount,
					"Undefined",
					0,
					0,
					0,
					e.description);
		}
		writeln();
	}

	private void writeElemExchanges(Process p, ElementaryFlowType type) {
		writeln(type.getExchangeHeader());
		for (Exchange e : p.exchanges) {
			if (e.flow == null
					|| e.flow.flowType != FlowType.ELEMENTARY_FLOW)
				continue;
			Compartment comp = flowCompartments.get(e.flow);
			if (comp == null || comp.type != type)
				continue;

			FlowMapEntry mapEntry = mappedFlow(e.flow);
			if (mapEntry == null) {
				// we have an unmapped flow
				writeln(e.flow.name,
						comp.sub.getValue(),
						unit(e.unit),
						e.amount,
						"Undefined",
						0,
						0,
						0,
						"");
				continue;
			}

			// handle a mapped flow
			FlowRef target = mapEntry.targetFlow;
			String unit = target.unit != null
					? unit(target.unit.name)
					: SimaProUnit.kg.symbol;
			writeln(target.flow.name,
					comp.sub.getValue(),
					unit,
					e.amount * mapEntry.factor,
					"Undefined",
					0,
					0,
					0,
					"");
		}
		writeln();
	}

	private void writeProcessDoc(Process p) {
		writeln("Process");
		writeln();

		writeln("Category type");
		writeln("material");
		writeln();

		writeln("Process identifier");
		writeln("Standard" + String.format("%015d", p.id));
		writeln();

		writeln("Type");
		writeln(p.processType == ProcessType.UNIT_PROCESS
				? "Unit process"
				: "System");
		writeln();

		writeln("Process name");
		writeln(p.name);
		writeln();

		writeln("Status");
		writeln();
		writeln();

		// these sections all get an `Unspecified` value
		String[] uSections = {
				"Time period",
				"Geography",
				"Technology",
				"Representativeness",
				"Multiple output allocation",
				"Substitution allocation",
				"Cut off rules",
				"Capital goods",
				"Boundary with nature",
		};
		for (String uSection : uSections) {
			writeln(uSection);
			writeln("Unspecified");
			writeln();
		}

		writeln("Infrastructure");
		writeln("No");
		writeln();

		writeln("Date");
		writeln(new SimpleDateFormat("dd.MM.yyyy")
				.format(new Date()));
		writeln();

		// we keep the following sections empty
		String[] eSections = {
				"Record",
				"Generator",
				"External documents",
				"Literature references",
				"Collection method",
				"Data treatment",
				"Verification",
				"Comment",
				"Allocation rules",
		};
		for (String s : eSections) {
			writeln(s);
			writeln();
			writeln();
		}

		writeln("System description");
		writeln("", "");
		writeln();
	}

	public void writerHeader() {
		writeln("{SimaPro 8.5.0.0}");
		writeln("{processes}");

		// date
		String date = new SimpleDateFormat("dd.MM.yyyy")
				.format(new Date());
		writeln("{Date: " + date + "}");

		// time
		String time = new SimpleDateFormat("HH:mm:ss")
				.format(new Date());
		writeln("{Time: " + time + "}");

		writeln("{Project: " + db.getName() + "}");
		writeln("{CSV Format version: 8.0.5}");
		writeln("{CSV separator: Semicolon}");
		writeln("{Decimal separator: .}");
		writeln("{Date separator: .}");
		writeln("{Short date format: dd.MM.yyyy}");
		writeln();
	}

	private boolean isProductOutput(Exchange e) {
		if (e == null || e.flow == null || e.isAvoided)
			return false;
		FlowType ft = e.flow.flowType;
		return (ft == FlowType.PRODUCT_FLOW && !e.isInput)
				|| (ft == FlowType.WASTE_FLOW && e.isInput);
	}

	private boolean isProductInput(Exchange e) {
		if (e == null || e.flow == null || e.isAvoided)
			return false;
		FlowType ft = e.flow.flowType;
		return (ft == FlowType.PRODUCT_FLOW && e.isInput)
				|| (ft == FlowType.WASTE_FLOW && !e.isInput);
	}

	private String unit(Unit u) {
		if (u == null)
			return SimaProUnit.kg.symbol;
		SimaProUnit unit = units.get(u.name);
		if (unit != null)
			return unit.symbol;
		unit = SimaProUnit.find(u.name);
		if (unit != null) {
			units.put(u.name, unit);
			return unit.symbol;
		}
		if (u.synonyms != null) {
			for (String syn : u.synonyms.split(";")) {
				unit = SimaProUnit.find(syn);
				if (unit != null) {
					units.put(u.name, unit);
					return unit.symbol;
				}
			}
		}
		Logger log = LoggerFactory.getLogger(getClass());
		log.warn("No corresponding SimaPro unit" +
				" for '{}' found; fall back to 'kg'", u.name);
		units.put(u.name, SimaProUnit.kg);
		return SimaProUnit.kg.symbol;
	}

	private String unit(String u) {
		if (u == null)
			return SimaProUnit.kg.symbol;
		SimaProUnit unit = units.get(u);
		if (unit != null)
			return unit.symbol;
		unit = SimaProUnit.find(u);
		if (unit != null) {
			units.put(u, unit);
			return unit.symbol;
		}
		Logger log = LoggerFactory.getLogger(getClass());
		log.warn("No corresponding SimaPro unit" +
				" for '{}' found; fall back to 'kg'", u);
		units.put(u, SimaProUnit.kg);
		return SimaProUnit.kg.symbol;
	}

	private String category(CategorizedEntity e) {
		if (e == null)
			return "";
		StringBuilder path = null;
		Category c = e.category;
		while (c != null) {
			if (path == null) {
				path = new StringBuilder(c.name);
			} else {
				path.insert(0, c.name + '\\');
			}
			c = c.category;
		}
		return path == null ? "" : path.toString();
	}

	private void writeln(Object... objects) {
		try {
			if (objects.length == 0) {
				writer.write("\r\n");
				return;
			}

			var strings = new String[objects.length];
			for (int i = 0; i < objects.length; i++) {
				var obj = objects[i];
				if (obj == null) {
					strings[i] = "";
					continue;
				}
				if (obj instanceof String) {
					var s = ((String) obj)
							.replace(';', ',')
							.replace('\n', ' ');
					if (s.contains("\"")) {
						s = "\"" + s + "\"";
					}
					strings[i] = s;
					continue;
				}
				if (obj instanceof Boolean) {
					strings[i] = ((Boolean) obj)
							? "Yes"
							: "No";
				}
				strings[i] = obj.toString();
			}

			var row = strings.length == 1
					? strings[0]
					: String.join(";", strings);

			writer.write(row);
			writer.write("\r\n");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private FlowMapEntry mappedFlow(Flow flow) {
		if (flowMap == null || flow == null)
			return null;
		return flowMap.getEntry(flow.refId);
	}

	public static void main(String[] args) throws Exception {
		var dbDir = "C:/Users/ms/openLCA-data-1.4/databases/_sp_exp";
		var db = new DerbyDatabase(new File(dbDir));
		var writer = new ProcessWriter(db);
		writer.write(
				new ProcessDao(db).getDescriptors(),
				new File("C:/Users/ms/Desktop/rems/spout.CSV"));
		db.close();
	}
}
