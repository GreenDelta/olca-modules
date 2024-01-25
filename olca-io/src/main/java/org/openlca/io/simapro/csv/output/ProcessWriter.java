package org.openlca.io.simapro.csv.output;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.core.model.ProcessType;
import org.openlca.io.simapro.csv.SimaProUnit;
import org.openlca.simapro.csv.enums.ElementaryFlowType;
import org.openlca.simapro.csv.enums.ProcessCategory;
import org.openlca.util.Exchanges;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openlca.io.simapro.csv.output.Util.uncertainty;

/**
 * Writes a set of processes to a SimaPro CSV file. It is not an official export
 * that is exposed to the openLCA user interface as there are too many edge
 * cases to get a file that is finally accepted by SimaPro. The idea of this
 * class is to provide a basic export that handles most of the common cases.
 */
class ProcessWriter {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final SimaProExport exp;
	private final CsvWriter w;

	private final FlowClassifier flows;
	private final UnitMap units;
	private final ProductLabeler products;

	private final Set<Flow> linkedFlows = new HashSet<>();
	private final Set<Flow> providerFlows = new HashSet<>();

	ProcessWriter(SimaProExport exp, CsvWriter writer) {
		this.exp = exp;
		this.units = new UnitMap();
		this.products = ProductLabeler.of(exp);
		this.flows = exp.flowMap != null
				? FlowClassifier.of(units, exp.flowMap)
				: FlowClassifier.of(units);
		this.w = writer;
	}

	void write() {
		var dao = new ProcessDao(exp.db);
		for (var descriptor : exp.processes) {
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
		for (Flow flow : linkedFlows) {
			if (providerFlows.contains(flow))
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
				var mapping = flows.mappingOf(flow);
				var name = mapping == null
						? flow.name
						: mapping.flow();
				var unit = mapping == null
						? units.get(flow.getReferenceUnit())
						: units.get(mapping.unit());
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

		var globals = new ParameterDao(exp.db)
				.getGlobalParameters();

		w.ln("Project Input parameters");
		for (var param : globals) {
			if (!param.isInputParameter)
				continue;
			var u = uncertainty(param.value, param.uncertainty, 1);
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
		writeProductOutputs(p);
		writeWasteInputs(p);

		w.ln("Avoided products");
		for (var e : p.exchanges) {
			if (!e.isAvoided || !Exchanges.isProduct(e))
				continue;
			linkedFlows.add(e.flow);
			var ref = Util.toReferenceAmount(e);
			var u = uncertainty(ref.amount, ref.uncertainty, 1);
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

		writeWasteOutputs(p);

		// input parameters
		w.ln("Input parameters");
		for (var param : p.parameters) {
			if (!param.isInputParameter)
				continue;
			var u = uncertainty(param.value, param.uncertainty, 1);
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

	private void writeProductOutputs(Process p) {
		var outputs = p.exchanges.stream()
				.filter(e -> Exchanges.isProviderFlow(e) && Exchanges.isProduct(e))
				.toList();
		if (outputs.isEmpty())
			return;

		w.ln("Products");
		for (var e : outputs) {
			providerFlows.add(e.flow);
			var ref = Util.toReferenceAmount(e);

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
					CategoryPath.of(exp, e.flow).path(),
					e.description);
		}
		w.ln();
	}

	private void writeWasteInputs(Process p) {
		var inputs = p.exchanges.stream()
				.filter(e -> Exchanges.isProviderFlow(e) && Exchanges.isWaste(e))
				.toList();
		if (inputs.isEmpty())
			return;
		w.ln("Waste treatment");
		for (var e : inputs) {
			providerFlows.add(e.flow);
			var ref = Util.toReferenceAmount(e);
			w.ln(products.labelOf(e.flow, p),
					units.get(ref.unit),
					ref.amount,
					"All waste types",
					CategoryPath.of(exp, e.flow).path(),
					e.description);
		}
		w.ln();
	}

	private void writeProductInputs(Process p) {
		w.ln("Materials/fuels");
		for (var e : p.exchanges) {
			if (!Exchanges.isLinkable(e) || !Exchanges.isProduct(e) || e.isAvoided)
				continue;
			linkedFlows.add(e.flow);
			var ref = Util.toReferenceAmount(e);
			var u = uncertainty(ref.amount, ref.uncertainty, 1);
			w.ln(products.labelOfInput(e),
					units.get(ref.unit),
					ref.amount,
					u[0], u[1], u[2], u[3],
					e.description);
		}
		w.ln();
	}

	private void writeWasteOutputs(Process p) {
		w.ln("Waste to treatment");
		for (var e : p.exchanges) {
			if (!Exchanges.isLinkable(e) || !Exchanges.isWaste(e) || e.isAvoided)
				continue;
			linkedFlows.add(e.flow);
			var ref = Util.toReferenceAmount(e);
			var u = uncertainty(ref.amount, ref.uncertainty, 1);
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
				var ref = Util.toReferenceAmount(e);
				var u = uncertainty(ref.amount, ref.uncertainty, 1);
				w.ln(e.flow.name,
						comp.sub().toString(),
						units.get(ref.unit),
						ref.amount,
						u[0], u[1], u[2], u[3],
						e.description);
				continue;
			}

			// handle a mapped flow
			var u = uncertainty(e.amount, e.uncertainty, mapping.factor());
			w.ln(mapping.flow(),
					comp.sub().toString(),
					mapping.unit(),
					e.amount * mapping.factor(),
					u[0], u[1], u[2], u[3],
					e.description);
		}
		w.ln();
	}

	private void writeProcessDoc(Process p) {
		if (p.documentation == null) {
			p.documentation = new ProcessDoc();
		}
		var doc = p.documentation;

		w.ln("Process");
		w.ln();

		var categoryType = Util.categoryTypeOf(exp, p);
		w.ln("Category type");
		w.ln(categoryType.toString());
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

		List.of(
				"Time period",
				"Geography",
				"Technology",
				"Representativeness").forEach(s -> {
			w.ln(s);
			w.ln("Unspecified");
			w.ln();
		});

		if (categoryType == ProcessCategory.WASTE_TREATMENT
				|| categoryType == ProcessCategory.WASTE_SCENARIO) {
			w.ln("Waste treatment allocation");
			w.ln("Unspecified");
			w.ln();
		} else {
			List.of(
					"Multiple output allocation",
					"Substitution allocation").forEach(s -> {
				w.ln(s);
				w.ln("Unspecified");
				w.ln();
			});
		}

		List.of(
				"Cut off rules",
				"Capital goods",
				"Boundary with nature").forEach(s -> {
			w.ln(s);
			w.ln("Unspecified");
			w.ln();
		});

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
		w.ln(doc.samplingProcedure);
		w.ln();

		w.ln("Data treatment");
		w.ln(doc.dataTreatment);
		w.ln();

		w.ln("Verification");
		w.ln(Util.reviewDetailsOf(p));
		w.ln();

		w.ln("Comment");
		w.ln(Util.commentOf(p));
		w.ln();

		w.ln("Allocation rules");
		w.ln(doc.inventoryMethod);
		w.ln();

		w.ln("System description");
		w.ln("", "");
		w.ln();
	}

}
