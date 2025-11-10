package org.openlca.io.ilcd.output;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Version;
import org.openlca.ilcd.methods.Factor;
import org.openlca.ilcd.methods.ImpactMethod;
import org.openlca.io.Xml;
import org.openlca.commons.Strings;

import java.util.ArrayList;
import java.util.List;

public class ImpactCategoryExport {

	private final Export exp;
	private final ImpactCategory impact;

	public ImpactCategoryExport(Export exp, ImpactCategory impact) {
		this.exp = exp;
		this.impact = impact;
	}

	public void write() {
		if (impact == null || exp.store.contains(ImpactMethod.class, impact.refId))
			return;
		var m = new ImpactMethod();
		addMetaData(impact, m);
		addFactors(impact, m);
		exp.store.put(m);
	}

	private void addMetaData(ImpactCategory impact, ImpactMethod m) {

		var info = m.withMethodInfo()
				.withDataSetInfo()
				.withUUID(impact.refId);
		info.withMethods().addAll(methodsOf(impact));

		exp.add(info::withName, impact.name);
		info.withImpactCategories().add(impact.name);
		exp.add(info::withComment, impact.description);
		Categories.toClassification(impact.category, info::withClassifications);

		if (Strings.isNotBlank(impact.referenceUnit)) {
			var qRef = m.withMethodInfo()
					.withQuantitativeReference()
					.withQuantity();
			exp.add(qRef::withName, impact.referenceUnit);
		}

		var admin = m.withAdminInfo();
		admin.withPublication()
				.withVersion(Version.asString(impact.version))
				.withLastRevision(Xml.calendar(impact.lastChange));
		admin.withDataEntry()
				.withTimeStamp(Xml.calendar(impact.lastChange));
	}

	private List<String> methodsOf(ImpactCategory impact) {
		var q = """
				select m.name from tbl_impact_categories i
				inner join tbl_impact_links link
				on link.f_impact_category = i.id
				inner join tbl_impact_methods m
				on link.f_impact_method = m.id
				where i.id =\s""" + impact.id;
		var names = new ArrayList<String>();
		NativeSql.on(exp.db).query(q, r -> {
			names.add(r.getString(1));
			return true;
		});
		return names;
	}

	private void addFactors(ImpactCategory impact, ImpactMethod m) {
		for (var factor : impact.impactFactors) {
			// TODO: uncertainty values + formulas
			var f = new Factor()
					.withMeanValue(getRefAmount(factor))
					.withFlow(exp.writeRef(factor.flow));
			if (factor.location != null) {
				f.withLocation(factor.location.code);
			}
			m.withFactors().add(f);
		}
	}

	private double getRefAmount(ImpactFactor factor) {
		double val = factor.value;
		var unit = factor.unit;
		if (unit != null && unit.conversionFactor != 0) {
			val /= unit.conversionFactor;
		}
		var propFactor = factor.flowPropertyFactor;
		if (propFactor != null) {
			val *= propFactor.conversionFactor;
		}
		return val;
	}
}
