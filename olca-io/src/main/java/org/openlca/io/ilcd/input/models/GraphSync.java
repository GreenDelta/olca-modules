package org.openlca.io.ilcd.input.models;

import java.util.ArrayList;
import java.util.UUID;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.ilcd.models.Model;
import org.openlca.io.ilcd.input.ImportConfig;

/**
 * Synchronizes a transformed graph with a database. It assumes that all
 * processes and link flows are created new.
 */
record GraphSync(ImportConfig config) {

	ProductSystem sync(Model model, Graph g) {

		// first, create and link the reference flow
		var refProc = g.root.process;
		Exchange qRef = refProc.quantitativeReference;
		if (qRef != null && qRef.flow != null) {
			Flow flow = qRef.flow;
			flow.refId = UUID.randomUUID().toString();
			category(flow);
			qRef.flow = insert(flow);
			qRef.flowPropertyFactor = flow.getReferenceFactor();
		}

		g.eachLink(this::syncFlows);
		syncProcesses(g);
		ProductSystem system = new ProductSystem();
		IO.mapMetaData(model, system);
		system.category = new CategoryDao(config.db())
			.sync(ModelType.PRODUCT_SYSTEM, "eILCD models");
		mapGraph(g, system);
		mapQRef(g, system);
		mapParams(g, system);
		return insert(system);
	}

	private void syncProcesses(Graph g) {
		g.eachNode(node -> {
			var process = node.process;
			process.refId = UUID.randomUUID().toString();
			category(process);
			node.process = insert(process);
		});
	}

	private void syncFlows(Link link) {
		Flow flow = link.input.flow;
		flow.refId = UUID.randomUUID().toString();
		category(flow);
		flow = insert(flow);
		link.input.flow = flow;
		link.output.flow = flow;
		var factor = flow.getReferenceFactor();
		if (factor == null)
			return;
		link.input.flowPropertyFactor = factor;
		link.output.flowPropertyFactor = factor;
	}

	/**
	 * Creates a copy of the category of the given entity under the `eILCD
	 * models` tree.
	 */
	private void category(RootEntity e) {
		if (e == null || e.category == null)
			return;
		Category c = e.category;
		ModelType type = c.modelType;
		ArrayList<String> names = new ArrayList<>();
		while (c != null) {
			names.add(0, c.name);
			c = c.category;
		}
		names.add(0, "eILCD models");
		String[] path = names.toArray(new String[0]);
		c = new CategoryDao(config.db()).sync(type, path);
		e.category = c;
	}

	private void mapGraph(Graph g, ProductSystem system) {
		g.eachLink(link -> {
			system.processes.add(link.provider.process.id);
			system.processes.add(link.recipient.process.id);
			ProcessLink pLink = new ProcessLink();
			Flow flow = link.input.flow;
			pLink.flowId = flow.id;
			if (flow.flowType == FlowType.PRODUCT_FLOW) {
				pLink.providerId = link.provider.process.id;
				pLink.processId = link.recipient.process.id;
				pLink.exchangeId = link.input.id;
			} else {
				pLink.providerId = link.recipient.process.id;
				pLink.processId = link.provider.process.id;
				pLink.exchangeId = link.output.id;
			}
			system.processLinks.add(pLink);
		});
	}

	private void mapQRef(Graph g, ProductSystem system) {
		var refProc = g.root.process;
		system.referenceProcess = refProc;
		Exchange qRef = refProc.quantitativeReference;
		system.referenceExchange = qRef;
		if (qRef != null) {
			system.targetFlowPropertyFactor = qRef.flowPropertyFactor;
			system.targetUnit = qRef.unit;
			double amount = qRef.amount;
			if (g.root.scalingFactor != null) {
				amount *= g.root.scalingFactor;
			}
			system.targetAmount = amount;
		}
	}

	private void mapParams(Graph g, ProductSystem system) {
		g.eachNode(node -> node.params.forEach((name, value) -> {
			if (name == null || value == null) {
				return;
			}
			var redef = new ParameterRedef();
			redef.name = name;
			redef.value = value;
			redef.contextId = node.process.id;
			redef.contextType = ModelType.PROCESS;
			IO.parametersSetOf(system).add(redef);
		}));
	}

	private <T extends RootEntity> T insert(T e) {
		var r = config.db().insert(e);
		if (r instanceof ProductSystem) {
			config.log().imported(r);
		} else {
			config.log().warn(r, "created copy for eILCD model");
		}
		return r;
	}
}
