package org.openlca.core;

import java.util.UUID;

import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.UnitDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.util.KeyGen;
import org.openlca.util.Strings;

public class TestProcess {

	private IDatabase db = Tests.getDb();
	private Process process;

	private TestProcess() {
	}

	public static TestProcess forOutput(String flow, double amount, String unit) {
		TestProcess tp = new TestProcess();
		tp.process = new Process();
		tp.process.setRefId(UUID.randomUUID().toString());
		tp.process.setName(flow);
		tp.prodOut(flow, amount, unit);
		tp.process.setQuantitativeReference(tp.process.getExchanges().get(0));
		return tp;
	}

	public Process get() {
		ProcessDao dao = new ProcessDao(db);
		return dao.insert(process);
	}

	public TestProcess addCosts(String flow, double amount, String currency) {
		for (Exchange e : process.getExchanges()) {
			Flow f = e.getFlow();
			if (f == null || !Strings.nullOrEqual(f.getName(), flow))
				continue;
			e.currency = currency(currency);
			e.costValue = amount;
			break;
		}
		return this;
	}

	public TestProcess prodOut(String flow, double amount, String unit) {
		Exchange e = prepareExchange(FlowType.PRODUCT_FLOW, flow, amount, unit);
		e.setInput(false);
		return this;
	}

	public TestProcess prodIn(String flow, double amount, String unit) {
		Exchange e = prepareExchange(FlowType.PRODUCT_FLOW, flow, amount, unit);
		e.setInput(true);
		return this;
	}

	public TestProcess elemOut(String flow, double amount, String unit) {
		Exchange e = prepareExchange(FlowType.ELEMENTARY_FLOW, flow, amount, unit);
		e.setInput(false);
		return this;
	}

	public TestProcess elemIn(String flow, double amount, String unit) {
		Exchange e = prepareExchange(FlowType.ELEMENTARY_FLOW, flow, amount, unit);
		e.setInput(true);
		return this;
	}

	private Exchange prepareExchange(FlowType flowType, String flow,
			double amount, String unit) {
		Exchange e = new Exchange();
		Flow f = flow(flow, unit, flowType);
		e.setFlow(f);
		e.setFlowPropertyFactor(f.getReferenceFactor());
		e.setUnit(unit(unit));
		e.setAmountValue(amount);
		process.getExchanges().add(e);
		return e;
	}

	private Flow flow(String name, String unit, FlowType type) {
		String flowId = KeyGen.get("elementary-flow", name, unit);
		FlowDao dao = new FlowDao(db);
		Flow flow = dao.getForRefId(flowId);
		if (flow != null)
			return flow;
		flow = new Flow();
		flow.setName(name);
		flow.setRefId(flowId);
		flow.setFlowType(type);
		FlowProperty property = propertyForUnit(unit);
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(property);
		factor.setConversionFactor(1);
		flow.getFlowPropertyFactors().add(factor);
		flow.setReferenceFlowProperty(property);
		return dao.insert(flow);
	}

	private FlowProperty propertyForUnit(String unit) {
		String refId = KeyGen.get("property", unit);
		FlowPropertyDao dao = new FlowPropertyDao(db);
		FlowProperty prop = dao.getForRefId(refId);
		if (prop != null)
			return prop;
		prop = new FlowProperty();
		prop.setName("Flow property for " + unit);
		prop.setFlowPropertyType(FlowPropertyType.PHYSICAL);
		prop.setRefId(refId);
		UnitGroup group = groupForUnit(unit);
		prop.setUnitGroup(group);
		return dao.insert(prop);
	}

	private Unit unit(String name) {
		String unitId = KeyGen.get("unit", name);
		UnitDao dao = new UnitDao(db);
		Unit unit = dao.getForRefId(unitId);
		if (unit != null)
			return unit;
		UnitGroup group = groupForUnit(name);
		return group.getReferenceUnit();
	}

	private UnitGroup groupForUnit(String unit) {
		String groupId = KeyGen.get("group", unit);
		UnitGroupDao dao = new UnitGroupDao(db);
		UnitGroup group = dao.getForRefId(groupId);
		if (group != null)
			return group;
		group = new UnitGroup();
		group.setName("Unit group of " + unit);
		group.setRefId(groupId);
		String unitId = KeyGen.get("unit", unit);
		Unit refUnit = new Unit();
		refUnit.setRefId(unitId);
		refUnit.setName(unit);
		refUnit.setConversionFactor(1.0);
		group.getUnits().add(refUnit);
		group.setReferenceUnit(refUnit);
		return dao.insert(group);
	}

	private Currency currency(String name) {
		String currencyId = KeyGen.get("currency", name);
		CurrencyDao dao = new CurrencyDao(db);
		Currency currency = dao.getForRefId(currencyId);
		if (currency != null)
			return currency;
		currency = new Currency();
		currency.setRefId(currencyId);
		currency.setName(name);
		currency.conversionFactor = 1;
		Currency ref = dao.getReferenceCurrency();
		if (ref != null)
			currency.referenceCurrency = ref;
		else
			currency.referenceCurrency = currency;
		return dao.insert(currency);
	}
}
