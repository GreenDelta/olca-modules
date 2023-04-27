package org.openlca.io.simapro.csv.output;

import org.openlca.core.DataDir;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.io.simapro.csv.Compartment;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.method.ImpactCategoryBlock;
import org.openlca.simapro.csv.method.ImpactCategoryRow;
import org.openlca.simapro.csv.method.ImpactFactorRow;
import org.openlca.simapro.csv.method.ImpactMethodBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MethodWriter {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final File file;
	private final IDatabase db;
	private final Map<Category, Compartment> compartments = new HashMap<>();
	private final Set<Category> unmappedCompartments = new HashSet<>();
	private final UnitMap units = new UnitMap();

	private Compartment defaultCompartment;

	public MethodWriter(IDatabase db, File file) {
		this.db = db;
		this.file = file;
	}

	/**
	 * Set the default compartment for flows for which no valid SimaPro
	 * compartment can be inferred from the flow category. If no such
	 * default compartment is provided, such flows are not written to
	 * the resulting CSV file.
	 */
	public MethodWriter withDefaultCompartment(Compartment comp) {
		this.defaultCompartment = comp;
		return this;
	}

	public void write(Collection<ImpactMethodDescriptor> methods) {
		if (db == null
				|| file == null
				|| methods == null
				|| methods.isEmpty())
			return;

		var ds = new CsvDataSet();
		ds.header().project("methods");

		for (var d : methods) {
			var m = db.get(ImpactMethod.class, d.id);
			if (m == null)
				continue;

			var block = new ImpactMethodBlock()
					.name(m.name)
					.comment(m.description);
			ds.methods().add(block);

			for (var impact : m.impactCategories) {
				block.impactCategories().add(blockOf(impact));
			}

		}
		ds.write(file);
	}

	private ImpactCategoryBlock blockOf(ImpactCategory impact) {
		var info = new ImpactCategoryRow()
				.name(impact.name)
				.unit(impact.referenceUnit);
		var block = new ImpactCategoryBlock().info(info);
	  for (var f : impact.impactFactors) {
			var row = rowOf(f);
			if (row != null) {
				block.factors().add(row);
			}
		}
		return block;
	}

	private ImpactFactorRow rowOf(ImpactFactor f) {
		var comp = compartmentOf(f.flow);
		if (comp == null)
			return null;
		return new ImpactFactorRow()
				.flow(f.flow.name)
				.compartment(comp.type().compartment())
				.subCompartment(comp.sub().toString())
				.casNumber(f.flow.casNumber)
				.factor(f.value)
				.unit(units.get(f.unit));
	}

	private Compartment compartmentOf(Flow flow) {
		if (flow == null)
			return null;
		if (flow.category == null) {
			var action = defaultCompartment != null
				? "applied default compartment"
				: "skipped";
			log.warn(
				"could not classify flow {}: no category; {}",
				flow.refId,
				action);
			return defaultCompartment;
		}

		var comp = compartments.get(flow.category);
		if (comp != null)
			return comp;
		if (unmappedCompartments.contains(flow.category)) {
			return defaultCompartment;
		}

		comp = Compartment.of(flow.category);
		if (comp != null) {
			compartments.put(flow.category, comp);
			return comp;
		}

		var action = defaultCompartment != null
			? "all flows of this category mapped to default"
			: "all flows of this category skipped";
		unmappedCompartments.add(flow.category);
		log.warn(
			"could not map category {} to a compartment; {}",
			flow.category,
			action);
		return defaultCompartment;
	}

	// just for tests
	public static void main(String[] args) {
		var file = new File("target/test.csv");
		try (var db = DataDir.get().openDatabase("ei39_cutoff")) {
			var dao = new ImpactMethodDao(db);
			var d = dao.getDescriptorForRefId(
				"4436e504-b912-4c49-85cd-88e46908b5c3");
			var writer = new MethodWriter(db, file);
			writer.write(List.of(d));
		}
	}
}
