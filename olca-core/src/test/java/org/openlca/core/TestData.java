package org.openlca.core;

import java.util.UUID;

import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.KeyGen;

public class TestData {

	public static Currency currency(String name) {
		String currencyId = KeyGen.get("currency", name);
		CurrencyDao dao = new CurrencyDao(Tests.getDb());
		Currency currency = dao.getForRefId(currencyId);
		if (currency != null)
			return currency;
		currency = new Currency();
		currency.refId = currencyId;
		currency.name = name;
		currency.conversionFactor = 1;
		Currency ref = dao.getReferenceCurrency();
		if (ref != null)
			currency.referenceCurrency = ref;
		else
			currency.referenceCurrency = currency;
		return dao.insert(currency);
	}

	public static Flow flow(String name, String unit, FlowType type) {
		String flowId = KeyGen.get("flow", name, unit);
		FlowDao dao = new FlowDao(Tests.getDb());
		Flow flow = dao.getForRefId(flowId);
		if (flow != null)
			return flow;
		flow = new Flow();
		flow.name = name;
		flow.refId = flowId;
		flow.flowType = type;
		FlowProperty property = property(unit);
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.flowProperty = property;
		factor.conversionFactor = 1;
		flow.flowPropertyFactors.add(factor);
		flow.referenceFlowProperty = property;
		return dao.insert(flow);
	}

	public static FlowProperty property(String unit) {
		String refId = KeyGen.get("property", unit);
		FlowPropertyDao dao = new FlowPropertyDao(Tests.getDb());
		FlowProperty prop = dao.getForRefId(refId);
		if (prop != null)
			return prop;
		prop = new FlowProperty();
		prop.name = "Flow property for " + unit;
		prop.flowPropertyType = FlowPropertyType.PHYSICAL;
		prop.refId = refId;
		prop.unitGroup = unitGroup(unit);
		return dao.insert(prop);
	}

	public static Unit unit(String name) {
		String unitId = KeyGen.get("unit", name);
		UnitDao dao = new UnitDao(Tests.getDb());
		Unit unit = dao.getForRefId(unitId);
		if (unit != null)
			return unit;
		UnitGroup group = unitGroup(name);
		return group.referenceUnit;
	}

	public static UnitGroup unitGroup(String unit) {
		String groupId = KeyGen.get("group", unit);
		UnitGroupDao dao = new UnitGroupDao(Tests.getDb());
		UnitGroup group = dao.getForRefId(groupId);
		if (group != null)
			return group;
		group = new UnitGroup();
		group.name = "Unit group of " + unit;
		group.refId = groupId;
		String unitId = KeyGen.get("unit", unit);
		Unit refUnit = new Unit();
		refUnit.refId = unitId;
		refUnit.name = unit;
		refUnit.conversionFactor = 1.0;
		group.units.add(refUnit);
		group.referenceUnit = refUnit;
		return dao.insert(group);
	}

	public static TestImpact impact(String name) {
		ImpactCategory impact = new ImpactCategory();
		impact.name = name;
		impact.refId = UUID.randomUUID().toString();
		return new TestImpact(impact);
	}

	public static ImpactMethod method(String name, ImpactCategory... impacts) {
		ImpactMethod m = new ImpactMethod();
		m.refId = UUID.randomUUID().toString();
		m.name = name;
		for (ImpactCategory impact : impacts) {
			m.impactCategories.add(impact);
		}
		ImpactMethodDao dao = new ImpactMethodDao(Tests.getDb());
		return dao.insert(m);
	}

	public static class TestImpact {

		private final ImpactCategory impact;

		private TestImpact(ImpactCategory impact) {
			this.impact = impact;
		}

		public TestImpact factor(String flow, double factor, String unit) {
			ImpactFactor f = new ImpactFactor();
			f.flow = TestData.flow(flow, unit, FlowType.ELEMENTARY_FLOW);
			f.flowPropertyFactor = f.flow.getReferenceFactor();
			f.unit = f.flow.getReferenceUnit();
			f.value = factor;
			impact.impactFactors.add(f);
			return this;
		}

		public TestImpact factor(String flow, String formula, String unit) {
			ImpactFactor f = new ImpactFactor();
			f.flow = TestData.flow(flow, unit, FlowType.ELEMENTARY_FLOW);
			f.flowPropertyFactor = f.flow.getReferenceFactor();
			f.unit = f.flow.getReferenceUnit();
			f.formula = formula;
			impact.impactFactors.add(f);
			return this;
		}

		public TestImpact parameter(String name, Uncertainty u) {
			Parameter param = new Parameter();
			param.name = name;
			param.refId = UUID.randomUUID().toString();
			param.isInputParameter = true;
			param.uncertainty = u;
			param.scope = ParameterScope.IMPACT;
			impact.parameters.add(param);
			if (u.distributionType != null) {
				switch (u.distributionType) {
					case UNIFORM:
						param.value = (u.parameter1 + u.parameter2) / 2;
						break;
					case TRIANGLE:
						param.value = u.parameter3;
						break;
					default:
						param.value = u.parameter1;
				}
			}
			return this;
		}

		public ImpactCategory get() {
			ImpactCategoryDao dao = new ImpactCategoryDao(Tests.getDb());
			return impact.id == 0
					? dao.insert(impact)
					: dao.update(impact);
		}

	}

}
