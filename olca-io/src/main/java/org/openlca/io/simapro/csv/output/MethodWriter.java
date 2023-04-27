package org.openlca.io.simapro.csv.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.io.simapro.csv.Compartment;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.method.ImpactCategoryBlock;
import org.openlca.simapro.csv.method.ImpactCategoryRow;
import org.openlca.simapro.csv.method.ImpactFactorRow;
import org.openlca.simapro.csv.method.ImpactMethodBlock;
import org.openlca.simapro.csv.method.NwSetBlock;
import org.openlca.simapro.csv.method.NwSetFactorRow;
import org.openlca.simapro.csv.method.VersionRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
					.version(versionOf(m))
					.comment(m.description)
					.useAddition(false)
					.useDamageAssessment(false);
			setNwFlagsOf(m, block);
			ds.methods().add(block);
			for (var impact : m.impactCategories) {
				block.impactCategories().add(blockOf(impact));
			}
			addNwBlocks(m, block);
		}
		ds.write(file);
	}

	private VersionRow versionOf(ImpactMethod method) {
		var v = new Version(method.version);
		return new VersionRow()
				.major(v.getMajor())
				.minor(v.getMinor());
	}

	private void setNwFlagsOf(ImpactMethod method, ImpactMethodBlock block) {
		int state = 0;
		String weightingUnit = null;
		for (var nws : method.nwSets) {
			if (nws.weightedScoreUnit != null && weightingUnit == null) {
				weightingUnit = nws.weightedScoreUnit;
			}
			for (var f : nws.factors) {
				if (f.normalisationFactor != null) {
					state |= 1;
				}
				if (f.weightingFactor != null) {
					state |= 2;
				}
				if (state == 3)
					break;
			}
			if (state == 3)
				break;
		}

		block.useNormalization((state & 1) == 1);
		if ((state & 2) == 2) {
			block.useWeighting(true);
			block.weightingUnit(weightingUnit);
		}
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

	private void addNwBlocks(ImpactMethod m, ImpactMethodBlock b) {
		for (var nws : m.nwSets) {
			var block = new NwSetBlock().name(nws.name);
			b.nwSets().add(block);

			for (var f : nws.factors) {
				if (f.impactCategory == null)
					continue;

				if (f.normalisationFactor != null
						&& f.normalisationFactor != 0) {
					// double nf = 1 / f.normalisationFactor;
					var nf = new NwSetFactorRow()
							.impactCategory(f.impactCategory.name)
							.factor(1 / f.normalisationFactor);
					block.normalizationFactors().add(nf);
				}

				if (f.weightingFactor != null) {
					var wf = new NwSetFactorRow()
							.impactCategory(f.impactCategory.name)
							.factor(f.weightingFactor);
					block.weightingFactors().add(wf);
				}
			}
		}
	}
}
