package org.openlca.core.database.references;

import org.openlca.core.Tests;
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
		system.setCategory(insertAndAddExpected("category", new Category()));
		system.setReferenceProcess(createProcess());
		system.setReferenceExchange(system.getReferenceProcess().getExchanges()
				.get(0));
		system.setTargetFlowPropertyFactor(system.getReferenceExchange().flowPropertyFactor);
		system.setTargetUnit(system.getTargetFlowPropertyFactor()
				.getFlowProperty().getUnitGroup().getUnits().get(0));
		system.getProcesses().add(system.getReferenceProcess().getId());
		Process p1 = insertAndAddExpected("processes", new Process());
		Process p2 = insertAndAddExpected("processes", new Process());
		Process p3 = insertAndAddExpected("processes", new Process());
		system.getProcesses().add(p1.getId());
		system.getProcesses().add(p2.getId());
		system.getProcesses().add(p3.getId());
		system.getProcessLinks().add(createLink(p1, p2));
		system.getProcessLinks().add(createLink(p2, p3));
		String n1 = generateName();
		String n2 = generateName();
		String n3 = generateName();
		system.getParameterRedefs().add(createParameterRedef(n1, p1.getId()));
		// formula with parameter to see if added as reference (unexpected)
		system.getParameterRedefs().add(createParameterRedef(n2, n3 + "*5"));
		Parameter globalUnreferenced = createParameter(n1, "3*3", true);
		Parameter globalUnreferenced2 = createParameter(n3, "3*3", true);
		// must be inserted manually
		globalUnreferenced = Tests.insert(globalUnreferenced);
		globalUnreferenced2 = Tests.insert(globalUnreferenced2);
		return Tests.insert(system);
	}

	private Process createProcess() {
		Process process = new Process();
		process.getExchanges().add(createExchange());
		process = insertAndAddExpected("processes", process);
		addExpected("referenceExchange", process.getExchanges().get(0));
		return process;
	}

	private Exchange createExchange() {
		Exchange exchange = new Exchange();
		exchange.flow = createFlow(true);
		exchange.flowPropertyFactor = exchange.flow
		.getFlowPropertyFactors().get(0);
		exchange.unit = exchange.flowPropertyFactor.getFlowProperty()
		.getUnitGroup().getUnits().get(0);
		return exchange;
	}

	private Flow createFlow(boolean reference) {
		if (!reference)
			return insertAndAddExpected("flow", new Flow());
		Flow flow = new Flow();
		FlowProperty property = new FlowProperty();
		FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setFlowProperty(property);
		UnitGroup group = new UnitGroup();
		Unit unit = new Unit();
		unit.setName("unit");
		group.getUnits().add(unit);
		property.setUnitGroup(group);
		flow.getFlowPropertyFactors().add(factor);
		group = Tests.insert(group);
		addExpected("targetUnit", group.getUnit(unit.getName()));
		property = Tests.insert(property);
		flow = Tests.insert(flow);
		addExpected("targetFlowPropertyFactor", flow.getFactor(property));
		return flow;
	}

	private ProcessLink createLink(Process p1, Process p2) {
		ProcessLink link = new ProcessLink();
		Flow flow = createFlow(false);
		Exchange e1 = new Exchange();
		final Flow flow1 = flow;
		e1.flow = flow1;
		p1.getExchanges().add(e1);
		Exchange e2 = new Exchange();
		final Flow flow2 = flow;
		e2.flow = flow2;
		p2.getExchanges().add(e2);
		link.processId = p1.getId();
		link.providerId = p2.getId();
		link.flowId = flow.getId();
		return link;
	}

	private ParameterRedef createParameterRedef(String name,
			Object contextOrValue) {
		ParameterRedef redef = new ParameterRedef();
		redef.setName(name);
		redef.setValue(1d);
		if (contextOrValue instanceof Long) {
			redef.setContextType(ModelType.PROCESS);
			redef.setContextId((long) contextOrValue);
		} else if (contextOrValue instanceof String) {
			insertAndAddExpected("parameterRedefs",
					createParameter(name, contextOrValue.toString(), true));
		}
		return redef;
	}

	private Parameter createParameter(String name, Object value, boolean global) {
		Parameter parameter = new Parameter();
		parameter.setName(name);
		boolean formula = value instanceof String;
		parameter.setInputParameter(!formula);
		if (formula)
			parameter.setFormula(value.toString());
		else
			parameter.setValue((double) value);
		if (global)
			parameter.setScope(ParameterScope.GLOBAL);
		else
			parameter.setScope(ParameterScope.PROCESS);
		return parameter;
	}
}
