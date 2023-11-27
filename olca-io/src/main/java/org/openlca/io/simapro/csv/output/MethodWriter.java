package org.openlca.io.simapro.csv.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.simapro.csv.CsvDataSet;
import org.openlca.simapro.csv.method.ImpactCategoryBlock;
import org.openlca.simapro.csv.method.ImpactCategoryRow;
import org.openlca.simapro.csv.method.ImpactFactorRow;
import org.openlca.simapro.csv.method.ImpactMethodBlock;
import org.openlca.simapro.csv.method.NwSetBlock;
import org.openlca.simapro.csv.method.NwSetFactorRow;
import org.openlca.simapro.csv.method.VersionRow;
import org.openlca.util.KeyGen;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

public class MethodWriter {

	private final File file;
	private final IDatabase db;
	private final UnitMap units;
	private final FlowClassifier flows;

	public MethodWriter(IDatabase db, File file) {
		this.db = db;
		this.file = file;
		this.units = new UnitMap();
		this.flows = FlowClassifier.of(units);
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

		// write reference data
		flows.writeGroupsTo(ds);
		units.writeQuantitiesTo(ds);

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
		var handled = new HashSet<String>();
		for (var f : impact.impactFactors) {
			var row = rowOf(f);
			if (row == null)
				continue;

			// duplicates within an impact category
			// are not allowed in SimaPro CSV
			var key = KeyGen.toPath(
					row.compartment(),
					row.subCompartment(),
					row.flow());
			if (handled.contains(key))
				continue;

			handled.add(key);
			block.factors().add(row);
		}
		return block;
	}

	private ImpactFactorRow rowOf(ImpactFactor f) {
		var comp = flows.compartmentOf(f.flow);
		if (comp == null)
			return null;
		var mapping = flows.mappingOf(f.flow);
		if (mapping == null)
			return new ImpactFactorRow()
					.flow(f.flow.name)
					.compartment(comp.type().compartment())
					.subCompartment(comp.sub().toString())
					.casNumber(f.flow.casNumber)
					.factor(f.value)
					.unit(units.get(f.unit));
		return new ImpactFactorRow()
				.flow(mapping.flow())
				.compartment(comp.type().compartment())
				.subCompartment(comp.sub().toString())
				.factor(f.value / mapping.factor())
				.unit(mapping.unit());
	}

	private void addNwBlocks(ImpactMethod m, ImpactMethodBlock b) {
		for (var nws : m.nwSets) {
			boolean hasNormalization = hasNormalization(nws);
			boolean hasWeighting = hasWeighting(nws);
			if (!hasNormalization && !hasWeighting)
				continue;

			var block = new NwSetBlock().name(nws.name);
			b.nwSets().add(block);

			for (var f : nws.factors) {
				if (f.impactCategory == null)
					continue;

				if (hasNormalization) {
					double value = f.normalisationFactor != null &&
							f.normalisationFactor != 0
							? 1 / f.normalisationFactor
							: 0;
					var nf = new NwSetFactorRow()
							.impactCategory(f.impactCategory.name)
							.factor(value);
					block.normalizationFactors().add(nf);
				}

				if (hasWeighting) {
					double value = f.weightingFactor != null
							? f.weightingFactor
							: 0;
					var wf = new NwSetFactorRow()
							.impactCategory(f.impactCategory.name)
							.factor(value);
					block.weightingFactors().add(wf);
				}
			}
		}
	}

	private boolean hasNormalization(NwSet nws) {
		if (nws == null)
			return false;
		for (var f : nws.factors) {
			if (f.impactCategory == null)
				continue;
			if (f.normalisationFactor != null)
				return true;
		}
		return false;
	}

	private boolean hasWeighting(NwSet nws) {
		if (nws == null)
			return false;
		for (var f : nws.factors) {
			if (f.impactCategory == null)
				continue;
			if (f.weightingFactor != null)
				return true;
		}
		return false;
	}
}
