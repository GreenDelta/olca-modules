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
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.io.maps.FlowMap;
import org.openlca.io.maps.FlowMapEntry;
import org.openlca.io.maps.FlowRef;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.SubCompartment;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes a set of processes to a SimaPro CSV file. It is not an official export
 * that is exposed to the openLCA user interface as there are too many edge
 * cases to get a file that is finally accepted by SimaPro. The idea of this
 * class is to provide a basic export that handles most of the common cases.
 */
public class ProcessWriter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase db;
	private Map<String, FlowMapEntry> flowMap;
	private BufferedWriter writer;

	private final Map<String, SimaProUnit> units = new HashMap<>();
	private final Map<Category, Compartment> compartments = new HashMap<>();
	private final Map<Flow, Compartment> flowCompartments = new HashMap<>();
	private final Set<Flow> inputProducts = new HashSet<>();
	private final Set<Flow> outputProducts = new HashSet<>();

	/**
	 * Only used when we need to link product providers during the export, so
	 * when no default providers are given in the product inputs and waste
	 * outputs.
	 */
	private ProcessTable processTable;

	public ProcessWriter(IDatabase db) {
		this.db = db;
	}

	public void setFlowMap(FlowMap flowMap) {
		if (flowMap != null) {
			this.flowMap = flowMap.index();
		}
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
				c = Compartment.fromPath(mapEntry.targetFlow().flowCategory);
				if (c != null) {
					flowCompartments.put(e.flow, c);
					continue;
				}
			}

			// 3) get the compartment from the category path
			c = compartments.computeIfAbsent(
					e.flow.category, Compartment::of);
			if (c == null) {
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
			ElementaryFlowType type = e.getValue().type();
			buckets[type.ordinal()].add(e.getKey());
		}

		// write the flow information
		for (var type : ElementaryFlowType.values()) {
			writeln(type.blockHeader());

			// duplicate names are not allowed here
			HashSet<String> handledNames = new HashSet<>();
			for (Flow flow : buckets[type.ordinal()]) {

				String name;
				String unit = null;

				FlowMapEntry mapEntry = mappedFlow(flow);
				if (mapEntry != null) {
					// handle mapped flows
					name = mapEntry.targetFlow().flow.name;
					if (mapEntry.targetFlow().unit != null) {
						unit = unit(mapEntry.targetFlow().unit.name);
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
		};
		for (String s : sections) {
			writeln(s);
			writeln();
			writeln("End");
			writeln();
		}

		var globals = new ParameterDao(db)
				.getGlobalParameters();

		writeln("Project Input parameters");
		for (var param : globals) {
			if (!param.isInputParameter)
				continue;
			var u = uncertainty(param.value, param.uncertainty);
			writeln(param.name,
					param.value,
					u[0], u[1], u[2], u[3],
					"No",
					param.description);
		}
		writeln();
		writeln("End");
		writeln();

		writeln("Project Calculated parameters");
		for (var param : globals) {
			if (param.isInputParameter)
				continue;
			writeln(param.name,
					param.formula,
					param.description);
		}
		writeln();
		writeln("End");
		writeln();
	}

	private void writeProcess(Process p) {
		writeProcessDoc(p);

		writeln("Products");
		for (Exchange e : p.exchanges) {
			if (!isProductOutput(e))
				continue;
			outputProducts.add(e.flow);
			var ref = toReferenceAmount(e);

			double allocation = 100;
			for (var f : p.allocationFactors) {
				if (f.method != AllocationMethod.PHYSICAL)
					continue;
				if (f.productId == e.flow.id) {
					allocation = 100 * f.value;
					break;
				}
			}

			writeln(productName(p, e.flow),
					unit(ref.unit),
					ref.amount,
					allocation,
					"not defined",
					productCategory(e.flow),
					e.description);
		}
		writeln();

		writeln("Avoided products");
		for (var e : p.exchanges) {
			if (!e.isAvoided)
				continue;
			inputProducts.add(e.flow);
			Process provider = null;
			if (e.defaultProviderId > 0) {
				provider = db.get(
						Process.class, e.defaultProviderId);
			}
			var ref = toReferenceAmount(e);
			var u = uncertainty(ref.amount, ref.uncertainty);
			writeln(productName(provider, e.flow),
					unit(ref.unit),
					ref.amount,
					u[0], u[1], u[2], u[3],
					e.description);
		}
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
			if (!param.isInputParameter)
				continue;
			var u = uncertainty(param.value, param.uncertainty);
			writeln(param.name,
					param.value,
					u[0], u[1], u[2], u[3],
					"No",
					param.description);
		}
		writeln();

		// calculated parameters
		writeln("Calculated parameters");
		for (var param : p.parameters) {
			if (param.isInputParameter)
				continue;
			writeln(param.name,
					param.formula,
					param.description);
		}
		writeln();

		writeln("End");
		writeln();
	}

	private void writeProductInputs(Process p) {
		writeln("Materials/fuels");
		for (var e : p.exchanges) {
			if (!isProductInput(e))
				continue;
			inputProducts.add(e.flow);
			Process provider = null;
			if (e.defaultProviderId > 0) {
				provider = db.get(
						Process.class, e.defaultProviderId);
			}

			var ref = toReferenceAmount(e);
			var u = uncertainty(ref.amount, ref.uncertainty);
			writeln(productName(provider, e.flow),
					unit(ref.unit),
					ref.amount,
					u[0], u[1], u[2], u[3],
					e.description);
		}
		writeln();
	}

	private void writeElemExchanges(Process p, ElementaryFlowType type) {
		writeln(type.exchangeHeader());
		for (Exchange e : p.exchanges) {
			if (e.flow == null
					|| e.flow.flowType != FlowType.ELEMENTARY_FLOW)
				continue;
			Compartment comp = flowCompartments.get(e.flow);
			if (comp == null || comp.type() != type)
				continue;

			FlowMapEntry mapEntry = mappedFlow(e.flow);
			if (mapEntry == null) {
				// we have an unmapped flow
				var ref = toReferenceAmount(e);
				var u = uncertainty(ref.amount, ref.uncertainty);
				writeln(e.flow.name,
						comp.sub().toString(),
						unit(ref.unit),
						ref.amount,
						u[0], u[1], u[2], u[3],
						e.description);
				continue;
			}

			// handle a mapped flow
			FlowRef target = mapEntry.targetFlow();
			String unit = target.unit != null
					? unit(target.unit.name)
					: SimaProUnit.kg.symbol;
			var u = uncertainty(e.amount, e.uncertainty, mapEntry.factor());
			writeln(target.flow.name,
					comp.sub().toString(),
					unit,
					e.amount * mapEntry.factor(),
					u[0], u[1], u[2], u[3],
					e.description);
		}
		writeln();
	}

	private void writeProcessDoc(Process p) {
		if (p.documentation == null) {
			p.documentation = new ProcessDocumentation();
		}
		var doc = p.documentation;

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

		writeln("Record");
		writeln(doc.dataDocumentor != null
				? doc.dataDocumentor.name
				: "");
		writeln();

		writeln("Generator");
		writeln(doc.dataGenerator != null
				? doc.dataGenerator.name
				: "");
		writeln();

		// sources
		writeln("External documents");
		int sourceCount = 0;
		if (doc.publication != null) {
			writeln(doc.publication.name, doc.publication.description);
			sourceCount++;
		}
		for (var source : doc.sources) {
			writeln(source.name, source.description);
			sourceCount++;
		}
		if (sourceCount == 0) {
			writeln();
		}
		writeln();

		// we do not write sources as literature references as these are
		// are stored database wide in SimaPro. there are problems when
		// the same literature reference occurs in different SimaPro CSV
		// files. the import then just stops. instead we import them as
		// external docs (see above)
		writeln("Literature references");
		writeln();
		writeln();

		writeln("Collection method");
		writeln(doc.sampling);
		writeln();

		writeln("Data treatment");
		writeln(doc.dataTreatment);
		writeln();

		writeln("Verification");
		writeln(doc.reviewDetails);
		writeln();

		writeln("Comment");
		writeln(comment(p));
		writeln();

		writeln("Allocation rules");
		writeln(doc.inventoryMethod);
		writeln();

		writeln("System description");
		writeln("", "");
		writeln();
	}

	private String comment(Process p) {
		var sections = new ArrayList<String>();
		var texts = new ArrayList<String>();
		BiConsumer<String, String> fn = (title, text) -> {
			if (Strings.nullOrEmpty(text))
				return;
			sections.add(title);
			texts.add(text);
		};

		fn.accept("Description", p.description);
		if (p.documentation != null) {
			var doc = p.documentation;
			fn.accept("Time", doc.time);
			fn.accept("Geography", doc.geography);
			fn.accept("Technology", doc.technology);
			fn.accept("Intended application", doc.intendedApplication);
			if (doc.dataSetOwner != null) {
				fn.accept("Data set owner", doc.dataSetOwner.name);
			}
			if (doc.publication != null) {
				fn.accept("Publication", doc.publication.name);
			}
			fn.accept("Access and use restrictions", doc.restrictions);
			fn.accept("Project", doc.project);
			fn.accept("Copyright", doc.copyright ? "Yes" : "No");
			fn.accept("Modeling constants", doc.modelingConstants);
			fn.accept("Data completeness", doc.completeness);
			fn.accept("Data selection", doc.dataSelection);
			if (doc.reviewer != null) {
				fn.accept("Reviewer", doc.reviewer.name);
			}
		}

		if (texts.isEmpty())
			return "";
		if (texts.size() == 1)
			return texts.get(0);

		var buff = new StringBuilder();
		for (int i = 0; i < sections.size(); i++) {
			buff.append("# ")
					.append(sections.get(i))
					.append('\n')
					.append(texts.get(i))
					.append("\n\n");
		}

		return buff.toString();
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

	/**
	 * Returns the corresponding SimaPro name of the given unit.
	 */
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
		log.warn("No corresponding SimaPro unit" +
				" for '{}' found; fall back to 'kg'", u.name);
		units.put(u.name, SimaProUnit.kg);
		return SimaProUnit.kg.symbol;
	}

	/**
	 * Returns the corresponding SimaPro name of the given unit name.
	 */
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
		log.warn("No corresponding SimaPro unit" +
				" for '{}' found; fall back to 'kg'", u);
		units.put(u, SimaProUnit.kg);
		return SimaProUnit.kg.symbol;
	}

	private String productCategory(Flow e) {
		if (e == null)
			return "";
		StringBuilder path = null;
		Category c = e.category;
		while (c != null) {
			var name = Strings.cut(c.name, 40);
			if (path == null) {
				path = new StringBuilder(name);
			} else {
				path.insert(0, name + '\\');
			}
			c = c.category;
		}
		return path == null
				? "Other"
				: path.toString();
	}

	private String productName(Process process, Flow product) {
		if (product == null || product.name == null)
			return "?";
		var flowName = product.name.trim();
		String processName;
		if (process != null) {
			processName = process.name;
		} else {
			// try to find a default provider if required
			if (processTable == null) {
				processTable = ProcessTable.create(db);
			}
			var providers = processTable.getProviders(product.id);
			if (providers.isEmpty()) {
				log.warn("no providers found for flow {}", flowName);
				return flowName;
			}
			if (providers.size() > 1) {
				log.warn("multiple providers found for flow {}", flowName);
			}
			processName = providers.get(0).provider().name;
		}

		if (Strings.nullOrEmpty(processName)
				|| processName.startsWith("Dummy: "))
			return flowName;
		processName = processName.trim();
		return processName.equalsIgnoreCase(flowName)
				? flowName
				: flowName + " - " + processName;
	}

	private FlowMapEntry mappedFlow(Flow flow) {
		if (flowMap == null || flow == null)
			return null;
		return flowMap.get(flow.refId);
	}

	/**
	 * In SimaPro you cannot have multiple flow properties for
	 * a flow. Thus we convert everything into the reference
	 * flow property and unit. Otherwise the SimaPro import will
	 * throw errors when the same flow is present with units from
	 * different quantities.
	 */
	public Exchange toReferenceAmount(Exchange e) {
		if (e == null || e.flow == null)
			return e;
		var refProp = e.flow.getReferenceFactor();
		var refUnit = e.flow.getReferenceUnit();
		if (Objects.equals(refProp, e.flowPropertyFactor)
				&& Objects.equals(refUnit, e.unit))
			return e;
		var clone = e.copy();
		clone.flowPropertyFactor = refProp;
		clone.unit = refUnit;
		clone.amount = ReferenceAmount.get(e);
		if (e.amount == 0) {
			return clone;
		}
		var factor = clone.amount / e.amount;
		if (Strings.notEmpty(clone.formula)) {
			clone.formula = factor + " * (" + clone.formula + ")";
		}
		if (clone.uncertainty != null) {
			clone.uncertainty.scale(factor);
		}
		return clone;
	}

	/**
	 * Converts the given uncertainty into a SimaPro entry. Passing null into
	 * this function is totally fine. Note that SimaPro does some validation
	 * checks in the import (e.g. min <= mean <= max), so that we have to pass
	 * also the mean value into this function.
	 */
	private Object[] uncertainty(double mean, Uncertainty u, double... factor) {
		var row = new Object[]{"Undefined", 0, 0, 0};
		if (u == null || u.distributionType == null)
			return row;
		double f = factor.length > 0
				? factor[0]
				: 1;
		switch (u.distributionType) {
			case LOG_NORMAL:
				row[0] = "Lognormal";
				row[1] = u.parameter2 == null ? 0 : u.parameter2;
				return row;
			case NORMAL:
				row[0] = "Normal";
				row[1] = u.parameter2 == null ? 0 : f * u.parameter2;
				return row;
			case TRIANGLE:
				var tmin = u.parameter1 == null ? 0 : f * u.parameter1;
				var tmax = u.parameter3 == null ? 0 : f * u.parameter3;
				if (tmin > mean || tmax < mean)
					return row;
				row[0] = "Triangle";
				row[2] = tmin;
				row[3] = tmax;
				return row;
			case UNIFORM:
				var umin = u.parameter1 == null ? 0 : f * u.parameter1;
				var umax = u.parameter2 == null ? 0 : f * u.parameter2;
				if (umin > mean || umax < mean)
					return row;
				row[0] = "Uniform";
				row[2] = umin;
				row[3] = umax;
				return row;
			default:
				return row;
		}
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
							.replace("\r", "")
							.replace('\n', '\u007F');
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
}
