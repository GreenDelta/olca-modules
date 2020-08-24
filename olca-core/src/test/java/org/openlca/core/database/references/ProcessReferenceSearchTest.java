package org.openlca.core.database.references;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.Tests;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.SocialAspect;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class ProcessReferenceSearchTest extends BaseReferenceSearchTest {

	private Map<Long, Process> processes = new HashMap<>();

	@Override
	protected ModelType getModelType() {
		return ModelType.PROCESS;
	}

	@Override
	protected Process createModel() {
		Process process = new Process();
		process.category = insertAndAddExpected("category", new Category());
		process.location = insertAndAddExpected("location", new Location());
		process.dqSystem = insertAndAddExpected("dqSystem", new DQSystem());
		process.exchangeDqSystem = insertAndAddExpected("exchangeDqSystem", new DQSystem());
		process.socialDqSystem = insertAndAddExpected("socialDqSystem", new DQSystem());
		String n1 = generateName();
		String n2 = generateName();
		String n3 = generateName();
		String n4 = generateName();
		String n5 = generateName();
		createExchange(process, 3d, true);
		createExchange(process, "2*" + n4, false);
		process.parameters.add(createParameter(n1, 3d, false));
		process.parameters.add(createParameter(n2, n1 + "*2*" + n3, false));
		process.socialAspects.add(createSocialAspect());
		process.socialAspects.add(createSocialAspect());
		process.documentation = createDocumentation();
		insertAndAddExpected(n3, createParameter(n3, "5*5", true));
		// formula with parameter to see if added as reference (unexpected)
		insertAndAddExpected(n4, createParameter(n4, "3*" + n5, true));
		Parameter globalUnreferenced = createParameter(n1, "3*3", true);
		Parameter globalUnreferenced2 = createParameter(n5, "3*3", true);
		// must be inserted manually
		globalUnreferenced = Tests.insert(globalUnreferenced);
		globalUnreferenced2 = Tests.insert(globalUnreferenced2);
		process = Tests.insert(process);
		for (Exchange e : process.exchanges) {
			addExpected("flow", e.flow, "exchanges", Exchange.class, e.id);
			addExpected("flowPropertyFactor", e.flowPropertyFactor, "exchanges", Exchange.class, e.id);
			addExpected("flowProperty", e.flowPropertyFactor.flowProperty, "flowPropertyFactor",
					FlowPropertyFactor.class, e.flowPropertyFactor.id);
			addExpected("unit", e.unit, "exchanges", Exchange.class, e.id);
			Process provider = processes.get(e.defaultProviderId);
			if (provider != null)
				addExpected("defaultProviderId", provider, "exchanges", Exchange.class, e.id);
		}
		for (SocialAspect a : process.socialAspects) {
			addExpected("indicator", a.indicator, "socialAspects", SocialAspect.class, a.id);
			addExpected("source", a.source, "socialAspects", SocialAspect.class, a.id);
		}
		ProcessDocumentation doc = process.documentation;
		addExpected("dataDocumentor", doc.dataDocumentor, "documentation", ProcessDocumentation.class, doc.id);
		addExpected("dataGenerator", doc.dataGenerator, "documentation", ProcessDocumentation.class, doc.id);
		addExpected("dataSetOwner", doc.dataSetOwner, "documentation", ProcessDocumentation.class, doc.id);
		addExpected("reviewer", doc.reviewer, "documentation", ProcessDocumentation.class, doc.id);
		addExpected("publication", doc.publication, "documentation", ProcessDocumentation.class, doc.id);
		for (Source s : process.documentation.sources)
			addExpected("sources", s, "documentation", ProcessDocumentation.class, doc.id);
		return process;
	}

	private Exchange createExchange(Process process, Object value, boolean provider) {
		Flow flow = createFlow();
		Exchange exchange = process.output(flow, 1);
		boolean formula = value instanceof String;
		if (formula)
			exchange.formula = value.toString();
		else
			exchange.amount = (double) value;
		if (provider) {
			Process pProcess = Tests.insert(new Process());
			processes.put(pProcess.id, pProcess);
			exchange.defaultProviderId = pProcess.id;
		}
		return exchange;
	}

	private Flow createFlow() {
		var massUnits = Tests.insert(
				UnitGroup.of("Units of mass", Unit.of("kg")));
		var mass = Tests.insert(
				FlowProperty.of("Mass", massUnits));
		return Tests.insert(
				Flow.product("a product", mass));
	}

	private SocialAspect createSocialAspect() {
		SocialAspect aspect = new SocialAspect();
		aspect.indicator = Tests.insert(new SocialIndicator());
		aspect.source = Tests.insert(new Source());
		return aspect;
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

	private ProcessDocumentation createDocumentation() {
		ProcessDocumentation doc = new ProcessDocumentation();
		doc.dataDocumentor = Tests.insert(new Actor());
		doc.dataGenerator = Tests.insert(new Actor());
		doc.dataSetOwner = Tests.insert(new Actor());
		doc.reviewer = Tests.insert(new Actor());
		doc.publication = Tests.insert(new Source());
		doc.sources.add(Tests.insert(new Source()));
		doc.sources.add(Tests.insert(new Source()));
		return doc;
	}

}
