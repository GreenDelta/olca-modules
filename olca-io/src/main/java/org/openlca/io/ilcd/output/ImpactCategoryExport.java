package org.openlca.io.ilcd.output;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Version;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.methods.DataSetInfo;
import org.openlca.ilcd.methods.Factor;
import org.openlca.ilcd.methods.FactorList;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.methods.MethodInfo;
import org.openlca.ilcd.methods.QuantitativeReference;
import org.openlca.ilcd.util.Methods;
import org.openlca.io.Xml;
import org.openlca.util.Strings;

import java.util.ArrayList;
import java.util.List;

public class ImpactCategoryExport {

	private final Export exp;

	public ImpactCategoryExport(Export exp) {
		this.exp = exp;
	}

	public void write(ImpactCategory impact) {
		if (impact == null || exp.store.contains(LCIAMethod.class, impact.refId))
			return;
		var m = new LCIAMethod();
		addMetaData(impact, m);
		addFactors(impact, m);
		exp.store.put(m);
	}

	private void addMetaData(ImpactCategory impact, LCIAMethod m) {
		m.methodInfo = new MethodInfo();
		var info = new DataSetInfo();
		m.methodInfo.dataSetInfo = info;
		info.uuid = impact.refId;
		info.methods.addAll(methodsOf(impact));
		exp.add(info.name, impact.name);
		info.impactCategories.add(impact.name);
		exp.add(info.comment, impact.description);
		Categories.toClassification(impact.category)
				.ifPresent(info.classifications::add);

		if (Strings.notEmpty(impact.referenceUnit)) {
			var qRef = new QuantitativeReference();
			qRef.quantity = new Ref();
			exp.add(qRef.quantity.name, impact.referenceUnit);
		}

		var pub = Methods.forcePublication(m);
		pub.version = Version.asString(impact.version);
		pub.lastRevision = Xml.calendar(impact.lastChange);
		var entry = Methods.forceDataEntry(m);
		entry.timeStamp = Xml.calendar(impact.lastChange);
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

	private void addFactors(ImpactCategory impact, LCIAMethod m) {
		var list = new FactorList();
		m.characterisationFactors = list;
		for (var factor : impact.impactFactors) {
			var f = new Factor();
			list.factors.add(f);
			// TODO: uncertainty values + formulas
			f.meanValue = getRefAmount(factor);
			f.flow = exp.writeRef(factor.flow);
			if (factor.location != null) {
				f.location = factor.location.code;
			}
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
