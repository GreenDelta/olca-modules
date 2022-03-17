package org.openlca.core.database.references;

import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class ProductSystemReferenceSearchTest extends BaseReferenceSearchTest {

	@Override
	protected ModelType getModelType() {
		return ModelType.PRODUCT_SYSTEM;
	}

	@Override
	protected ProductSystem createModel() {
		ProductSystem system = new ProductSystem();
		system.category = insertAndAddExpected("category", new Category());
		system.referenceProcess = createProcess();
		system.referenceExchange = system.referenceProcess.exchanges.get(0);
		system.targetFlowPropertyFactor = system.referenceExchange.flowPropertyFactor;
		system.targetUnit = system.targetFlowPropertyFactor.flowProperty.unitGroup.units.get(0);
		system.processes.add(system.referenceProcess.id);
		Process p1 = insertAndAddExpected("processes", new Process());
		Process p2 = insertAndAddExpected("processes", new Process());
		Process p3 = insertAndAddExpected("processes", new Process());
		system.processes.add(p1.id);
		system.processes.add(p2.id);
		system.processes.add(p3.id);
		system.processLinks.add(createLink(p1, p2));
		system.processLinks.add(createLink(p2, p3));
		String n1 = generateName();
		String n2 = generateName();
		String n3 = generateName();

		// TODO: find parameter references
		// system.parameterRedefs.add(createParameterRedef(n1, p1.id));
		// formula with parameter to see if added as reference (unexpected)
		// system.parameterRedefs.add(createParameterRedef(n2, n3 + "*5"));

		Parameter globalUnreferenced = createParameter(n1, "3*3", true);
		Parameter globalUnreferenced2 = createParameter(n3, "3*3", true);
		// must be inserted manually
		globalUnreferenced = db.insert(globalUnreferenced);
		globalUnreferenced2 = db.insert(globalUnreferenced2);
		return db.insert(system);
	}

	private Process createProcess() {
		Process process = new Process();
		Exchange exchange = createExchange(process);
		process = insertAndAddExpected("processes", process);
		addExpected("referenceExchange", exchange);
		return process;
	}

	private Exchange createExchange(Process process) {
		Flow flow = createFlow(true);
		return process.output(flow, 1.0);
	}

	private Flow createFlow(boolean reference) {
		if (!reference)
			return insertAndAddExpected("flowId", new Flow());
		Flow flow = new Flow();
		FlowProperty property = new FlowProperty();
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.flowProperty = property;
		UnitGroup group = new UnitGroup();
		Unit unit = new Unit();
		unit.name = "unit";
		group.units.add(unit);
		property.unitGroup = group;
		flow.flowPropertyFactors.add(factor);
		group = db.insert(group);
		addExpected("targetUnit", group.getUnit(unit.name));
		property = db.insert(property);
		flow = db.insert(flow);
		addExpected("targetFlowPropertyFactor", flow.getFactor(property));
		return flow;
	}

	private ProcessLink createLink(Process p1, Process p2) {
		ProcessLink link = new ProcessLink();
		Flow flow = createFlow(false);
		Exchange e1 = new Exchange();
		e1.flow = flow;
		p1.exchanges.add(e1);
		Exchange e2 = new Exchange();
		e2.flow = flow;
		p2.exchanges.add(e2);
		link.processId = p1.id;
		link.providerId = p2.id;
		link.flowId = flow.id;
		addExpected("processId", p1);
		addExpected("providerId", p2);
		addExpected("exchangeId", e1);
		return link;
	}

	private ParameterRedef createParameterRedef(String name, Object contextOrValue) {
		ParameterRedef redef = new ParameterRedef();
		redef.name = name;
		redef.value = 1d;
		if (contextOrValue instanceof Long) {
			redef.contextType = ModelType.PROCESS;
			redef.contextId = (long) contextOrValue;
		} else if (contextOrValue instanceof String) {
			insertAndAddExpected(name, createParameter(name, contextOrValue.toString(), true));
		}
		return redef;
	}

	private Parameter createParameter(String name, Object value, boolean global) {
		Parameter parameter = new Parameter();
		parameter.name = name;
		boolean formula = value instanceof String;
		parameter.isInputParameter = !formula;
		if (formula)
			parameter.formula = value.toString();
		else
			parameter.value = (double) value;
		if (global)
			parameter.scope = ParameterScope.GLOBAL;
		else
			parameter.scope = ParameterScope.PROCESS;
		return parameter;
	}
}
