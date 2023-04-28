package org.openlca.io.simapro.csv.output;

import static org.openlca.io.simapro.csv.output.Util.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.io.simapro.csv.SimaProUnit;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.util.Exchanges;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes a set of processes to a SimaPro CSV file. It is not an official export
 * that is exposed to the openLCA user interface as there are too many edge
 * cases to get a file that is finally accepted by SimaPro. The idea of this
 * class is to provide a basic export that handles most of the common cases.
 */
class ProcessWriter {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final SimaProExport config;
	private final CsvWriter w;

	private final FlowClassifier flows;
	private final UnitMap units;
	private final ProductLabeler products;

	private final Set<Flow> inputProducts = new HashSet<>();
	private final Set<Flow> outputProducts = new HashSet<>();

	ProcessWriter(SimaProExport config, CsvWriter writer) {
		this.config = config;
		this.units = new UnitMap();
		this.products = ProductLabeler.of(config);
		this.flows = config.flowMap != null
				? FlowClassifier.withMapping(config.flowMap)
				: FlowClassifier.withoutMapping();
		this.w = writer;
	}

	void write() {
		var dao = new ProcessDao(config.db);
		for (var descriptor : config.processes) {
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
			if (e.flow == null || e.flow.flowType != FlowType.ELEMENTARY_FLOW)
				continue;
			var c = flows.compartmentOf(e.flow);
			if (c == null) {
				log.warn("could not assign compartment to elementary flow {};"
						+ " this flow will be skipped in the export", e.flow);
			}
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
		w.ln("Quantities");
		if (!quantities.contains(kg.quantity)) {
			w.ln(kg.quantity, "Yes");
		}
		for (String q : quantities) {
			w.ln(q, "Yes");
		}
		w.endSection();
		w.ln();

		// units
		Set<SimaProUnit> us = new HashSet<>(units.values());
		w.ln("Units");
		if (!us.contains(kg)) {
			w.ln(kg.symbol,
					kg.quantity,
					Double.toString(kg.factor),
					kg.refUnit);
		}
		for (SimaProUnit u : us) {
			w.ln(u.symbol,
					u.quantity,
					Double.toString(u.factor),
					u.refUnit);
		}
		w.endSection();
		w.ln();
	}

	private void writeReferenceFlows() {

		// write the flow information
		var groups = flows.groupFlows();
		for (var type : ElementaryFlowType.values()) {
			var group = groups.get(type);
			if (group == null || group.isEmpty())
				continue;

			w.ln(type.blockHeader());

			// duplicate names are not allowed here
			HashSet<String> handledNames = new HashSet<>();
			for (var flow : group) {

				// select name & unit
				String name;
				String unit = null;
				var mapping = flows.mappingOf(flow);
				if (mapping != null && mapping.targetFlow().flow != null) {
					// handle mapped flows
					name = mapping.targetFlow().flow.name;
					if (mapping.targetFlow().unit != null) {
						unit = units.get(mapping.targetFlow().unit.name);
					}
				} else {
					// handle unmapped flows
					name = flow.name;
					unit = units.get(flow.getReferenceUnit());
				}
				if (name == null || unit == null)
					continue;

				String id = name.trim().toLowerCase();
				if (handledNames.contains(id))
					continue;
				handledNames.add(id);

				w.ln(name, unit, flow.casNumber, "");
			}
			w.endSection();
			w.ln();
		}
	}

	private void writeGlobalParameters() {

		String[] sections = {
				"Database Input parameters",
				"Database Calculated parameters",
		};
		for (String s : sections) {
			w.ln(s);
			w.endSection();
		}

		var globals = new ParameterDao(config.db)
				.getGlobalParameters();

		w.ln("Project Input parameters");
		for (var param : globals) {
			if (!param.isInputParameter)
				continue;
			var u = uncertainty(param.value, param.uncertainty);
			w.ln(param.name,
					param.value,
					u[0], u[1], u[2], u[3],
					"No",
					param.description);
		}
		w.endSection();

		w.ln("Project Calculated parameters");
		for (var param : globals) {
			if (param.isInputParameter)
				continue;
			w.ln(param.name,
					param.formula,
					param.description);
		}
		w.endSection();
	}

	private void writeProcess(Process p) {
		writeProcessDoc(p);

		w.ln("Products");
		for (Exchange e : p.exchanges) {
			if (!Exchanges.isProviderFlow(e))
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

			w.ln(products.labelOf(e.flow, p),
					units.get(ref.unit),
					ref.amount,
					allocation,
					"not defined",
					productCategoryOf(e.flow),
					e.description);
		}
		w.ln();

		w.ln("Avoided products");
		for (var e : p.exchanges) {
			if (!e.isAvoided)
				continue;
			inputProducts.add(e.flow);
			var ref = toReferenceAmount(e);
			var u = uncertainty(ref.amount, ref.uncertainty);
			w.ln(products.labelOfInput(e),
					units.get(ref.unit),
					ref.amount,
					u[0], u[1], u[2], u[3],
					e.description);
		}
		w.ln();

		writeElemExchanges(p, ElementaryFlowType.RESOURCES);
		writeProductInputs(p);

		w.ln("Electricity/heat");
		w.ln();

		writeElemExchanges(p, ElementaryFlowType.EMISSIONS_TO_AIR);
		writeElemExchanges(p, ElementaryFlowType.EMISSIONS_TO_WATER);
		writeElemExchanges(p, ElementaryFlowType.EMISSIONS_TO_SOIL);
		writeElemExchanges(p, ElementaryFlowType.FINAL_WASTE_FLOWS);
		writeElemExchanges(p, ElementaryFlowType.NON_MATERIAL_EMISSIONS);
		writeElemExchanges(p, ElementaryFlowType.SOCIAL_ISSUES);
		writeElemExchanges(p, ElementaryFlowType.ECONOMIC_ISSUES);

		w.ln("Waste to treatment");
		w.ln();

		// input parameters
		w.ln("Input parameters");
		for (var param : p.parameters) {
			if (!param.isInputParameter)
				continue;
			var u = uncertainty(param.value, param.uncertainty);
			w.ln(param.name,
					param.value,
					u[0], u[1], u[2], u[3],
					"No",
					param.description);
		}
		w.ln();

		// calculated parameters
		w.ln("Calculated parameters");
		for (var param : p.parameters) {
			if (param.isInputParameter)
				continue;
			w.ln(param.name,
					param.formula,
					param.description);
		}

		w.endSection();
	}

	private void writeProductInputs(Process p) {
		w.ln("Materials/fuels");
		for (var e : p.exchanges) {
			if (!Exchanges.isLinkable(e))
				continue;
			inputProducts.add(e.flow);
			var ref = toReferenceAmount(e);
			var u = uncertainty(ref.amount, ref.uncertainty);
			w.ln(products.labelOfInput(e),
					units.get(ref.unit),
					ref.amount,
					u[0], u[1], u[2], u[3],
					e.description);
		}
		w.ln();
	}

	private void writeElemExchanges(Process p, ElementaryFlowType type) {
		w.ln(type.exchangeHeader());
		for (Exchange e : p.exchanges) {
			if (e.flow == null || e.flow.flowType != FlowType.ELEMENTARY_FLOW)
				continue;
			var comp = flows.compartmentOf(e.flow);
			if (comp == null || comp.type() != type)
				continue;

			var mapping = flows.mappingOf(e.flow);
			if (mapping == null) {
				// we have an unmapped flow
				var ref = toReferenceAmount(e);
				var u = uncertainty(ref.amount, ref.uncertainty);
				w.ln(e.flow.name,
						comp.sub().toString(),
						units.get(ref.unit),
						ref.amount,
						u[0], u[1], u[2], u[3],
						e.description);
				continue;
			}

			// handle a mapped flow
			var target = mapping.targetFlow();
			String unit = target.unit != null
					? units.get(target.unit.name)
					: SimaProUnit.kg.symbol;
			var u = uncertainty(e.amount, e.uncertainty, mapping.factor());
			w.ln(target.flow.name,
					comp.sub().toString(),
					unit,
					e.amount * mapping.factor(),
					u[0], u[1], u[2], u[3],
					e.description);
		}
		w.ln();
	}

	private void writeProcessDoc(Process p) {
		if (p.documentation == null) {
			p.documentation = new ProcessDocumentation();
		}
		var doc = p.documentation;

		w.ln("Process");
		w.ln();

		w.ln("Category type");
		w.ln("material");
		w.ln();

		w.ln("Process identifier");
		w.ln("Standard" + String.format("%015d", p.id));
		w.ln();

		w.ln("Type");
		w.ln(p.processType == ProcessType.UNIT_PROCESS
				? "Unit process"
				: "System");
		w.ln();

		w.ln("Process name");
		w.ln(p.name);
		w.ln();

		w.ln("Status");
		w.ln();
		w.ln();

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
			w.ln(uSection);
			w.ln("Unspecified");
			w.ln();
		}

		w.ln("Infrastructure");
		w.ln("No");
		w.ln();

		w.ln("Date");
		w.ln(new SimpleDateFormat("dd.MM.yyyy")
				.format(new Date()));
		w.ln();

		w.ln("Record");
		w.ln(doc.dataDocumentor != null
				? doc.dataDocumentor.name
				: "");
		w.ln();

		w.ln("Generator");
		w.ln(doc.dataGenerator != null
				? doc.dataGenerator.name
				: "");
		w.ln();

		// sources
		w.ln("External documents");
		int sourceCount = 0;
		if (doc.publication != null) {
			w.ln(doc.publication.name, doc.publication.description);
			sourceCount++;
		}
		for (var source : doc.sources) {
			w.ln(source.name, source.description);
			sourceCount++;
		}
		if (sourceCount == 0) {
			w.ln();
		}
		w.ln();

		// We do not write sources as literature references as these are stored
		// database wide in SimaPro. There are problems when the same literature
		// reference occurs in different SimaPro CSV files. The import then just
		// stops. Instead, we import them as external docs (see above)
		w.ln("Literature references");
		w.ln();
		w.ln();

		w.ln("Collection method");
		w.ln(doc.sampling);
		w.ln();

		w.ln("Data treatment");
		w.ln(doc.dataTreatment);
		w.ln();

		w.ln("Verification");
		w.ln(doc.reviewDetails);
		w.ln();

		w.ln("Comment");
		w.ln(comment(p));
		w.ln();

		w.ln("Allocation rules");
		w.ln(doc.inventoryMethod);
		w.ln();

		w.ln("System description");
		w.ln("", "");
		w.ln();
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

	/**
	 * In SimaPro you cannot have multiple flow properties for a flow. Thus, we
	 * convert everything into the reference flow property and unit. Otherwise,
	 * the SimaPro import will throw errors when the same flow is present with
	 * units from different quantities.
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
}
