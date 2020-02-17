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
		try (FileOutputStream fout = new FileOutputStream(file);
			 OutputStreamWriter writer = new OutputStreamWriter(
					 fout, "windows-1252");
			 BufferedWriter buffer = new BufferedWriter(writer)) {
			writerHeader(buffer);
			ProcessDao dao = new ProcessDao(db);
			for (ProcessDescriptor p : processes) {
				Process process = dao.getForId(p.id);
				if (process == null)
					continue;
				classifyElemFlows(process);
				writeProcess(buffer, process);
			}
			writeDummies(buffer);
			writeQuantities(buffer);
			writeReferenceFlows(buffer);
			writeGlobalParameters(buffer);
		} catch (Exception e) {
			throw e instanceof RuntimeException
					? (RuntimeException) e
					: new RuntimeException(e);
		}
	}

	private void writeDummies(BufferedWriter w) {
		for (Flow inputProduct : inputProducts) {
			if (outputProducts.contains(inputProduct))
				continue;
			Process p = new Process();
			p.name = "Dummy: " + inputProduct.name;
			p.id = inputProduct.id;
			p.category = new Category();
			p.category.name = "Dummy processes";
			Exchange qRef = p.exchange(inputProduct);
			qRef.amount = 1.0;
			p.quantitativeReference = qRef;
			qRef.isInput = inputProduct.flowType == FlowType.WASTE_FLOW;
			writeProcess(w, p);
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

	private void writeQuantities(BufferedWriter w) {

		// we always write at least the kilogram data
		// into the quantity sections
		SimaProUnit kg = SimaProUnit.kg;

		// quantities
		Set<String> quantities = units.values().stream()
				.map(u -> u.quantity)
				.collect(Collectors.toSet());
		r(w, "Quantities");
		if (!quantities.contains(kg.quantity)) {
			r(w, kg.quantity, "Yes");
		}
		for (String q : quantities) {
			r(w, q, "Yes");
		}
		r(w, "");
		r(w, "End");
		r(w, "");
		r(w, "");

		// units
		Set<SimaProUnit> us = new HashSet<>(units.values());
		r(w, "Units");
		if (!us.contains(kg)) {
			r(w, kg.symbol,
					kg.quantity,
					Double.toString(kg.factor),
					kg.refUnit);
		}
		for (SimaProUnit u : us) {
			r(w, u.symbol,
					u.quantity,
					Double.toString(u.factor),
					u.refUnit);
		}
		r(w, "");
		r(w, "End");
		r(w, "");
		r(w, "");
	}

	@SuppressWarnings("unchecked")
	private void writeReferenceFlows(BufferedWriter w) {

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
		for (ElementaryFlowType type : ElementaryFlowType.values()) {
			r(w, type.getReferenceHeader());

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

				r(w, unsep(name),
						unit,
						flow.casNumber != null ? flow.casNumber : "",
						"");
			}
			r(w, "");
			r(w, "End");
			r(w, "");
			r(w, "");
		}
	}

	private void writeGlobalParameters(BufferedWriter w) {
		String[] sections = {
				"Database Input parameters",
				"Database Calculated parameters",
				"Project Input parameters",
				"Project Calculated parameters",
		};
		for (String s : sections) {
			r(w, s);
			r(w, "");
			r(w, "End");
			r(w, "");
		}
	}

	private void writeProcess(BufferedWriter w, Process p) {
		writeProcessDoc(w, p);

		r(w, "Products");
		for (Exchange e : p.exchanges) {
			if (!isProductOutput(e))
				continue;
			outputProducts.add(e.flow);
			r(w, unsep(e.flow.name),
					unit(e.unit),
					Double.toString(e.amount),
					"100",
					"not defined",
					category(e.flow),
					"");
		}
		r(w, "");

		r(w, "Avoided products");
		r(w, "");

		writeElemExchanges(w, p, ElementaryFlowType.RESOURCES);
		writeProductInputs(w, p);

		r(w, "Electricity/heat");
		r(w, "");

		writeElemExchanges(w, p, ElementaryFlowType.EMISSIONS_TO_AIR);
		writeElemExchanges(w, p, ElementaryFlowType.EMISSIONS_TO_WATER);
		writeElemExchanges(w, p, ElementaryFlowType.EMISSIONS_TO_SOIL);
		writeElemExchanges(w, p, ElementaryFlowType.FINAL_WASTE_FLOWS);
		writeElemExchanges(w, p, ElementaryFlowType.NON_MATERIAL_EMISSIONS);
		writeElemExchanges(w, p, ElementaryFlowType.SOCIAL_ISSUES);
		writeElemExchanges(w, p, ElementaryFlowType.ECONOMIC_ISSUES);

		String[] sections = {
				"Waste to treatment",
				"Input parameters",
				"Calculated parameters",
		};
		for (String s : sections) {
			r(w, s);
			r(w, "");
		}

		r(w, "End");
		r(w, "");
	}

	private void writeProductInputs(
			BufferedWriter w, Process p) {
		r(w, "Materials/fuels");
		for (Exchange e : p.exchanges) {
			if (!isProductInput(e))
				continue;
			inputProducts.add(e.flow);
			r(w, unsep(e.flow.name),
					unit(e.unit),
					Double.toString(e.amount),
					"Undefined",
					"0",
					"0",
					"0",
					"");
		}
		r(w, "");
	}

	private void writeElemExchanges(
			BufferedWriter w, Process p, ElementaryFlowType type) {
		r(w, type.getExchangeHeader());
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
				r(w, unsep(e.flow.name),
						comp.sub.getValue(),
						unit(e.unit),
						Double.toString(e.amount),
						"Undefined",
						"0",
						"0",
						"0",
						"");
				continue;
			}

			// handle a mapped flow
			FlowRef target = mapEntry.targetFlow;
			String unit = target.unit != null
					? unit(target.unit.name)
					: SimaProUnit.kg.symbol;
			r(w, unsep(target.flow.name),
					comp.sub.getValue(),
					unit,
					Double.toString(e.amount * mapEntry.factor),
					"Undefined",
					"0",
					"0",
					"0",
					"");
		}
		r(w, "");
	}

	private void writeProcessDoc(BufferedWriter w, Process p) {
		r(w, "Process");
		r(w, "");

		r(w, "Category type");
		r(w, "material");
		r(w, "");

		r(w, "Process identifier");
		r(w, "Standard" + String.format("%015d", p.id));
		r(w, "");

		r(w, "Type");
		r(w, p.processType == ProcessType.UNIT_PROCESS
				? "Unit process"
				: "System");
		r(w, "");

		r(w, "Process name");
		r(w, unsep(p.name));
		r(w, "");

		r(w, "Status");
		r(w, "");
		r(w, "");

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
			r(w, uSection);
			r(w, "Unspecified");
			r(w, "");
		}

		r(w, "Infrastructure");
		r(w, "No");
		r(w, "");

		r(w, "Date");
		r(w, new SimpleDateFormat("dd.MM.yyyy")
				.format(new Date()));
		r(w, "");

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
			r(w, s);
			r(w, "");
			r(w, "");
		}

		r(w, "System description");
		r(w, ";");
		r(w, "");
	}

	public void writerHeader(BufferedWriter w) {
		r(w, "{SimaPro 8.5.0.0}");
		r(w, "{processes}");

		// date
		String date = new SimpleDateFormat("dd.MM.yyyy")
				.format(new Date());
		r(w, "{Date: " + date + "}");

		// time
		String time = new SimpleDateFormat("HH:mm:ss")
				.format(new Date());
		r(w, "{Time: " + time + "}");

		r(w, "{Project: " + db.getName() + "}");
		r(w, "{CSV Format version: 8.0.5}");
		r(w, "{CSV separator: Semicolon}");
		r(w, "{Decimal separator: .}");
		r(w, "{Date separator: .}");
		r(w, "{Short date format: dd.MM.yyyy}");
		r(w, "");
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

	private void r(BufferedWriter w, String... s) {
		String row = s.length == 1
				? s[0]
				: String.join(";", s);
		try {
			w.write(row);
			w.write("\r\n"); // write Windows line endings
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String unsep(String s) {
		if (s == null)
			return "";
		return s.replace(';', ',').replace('\n', ' ');
	}

	private FlowMapEntry mappedFlow(Flow flow) {
		if (flowMap == null || flow == null)
			return null;
		return flowMap.getEntry(flow.refId);
	}
}
