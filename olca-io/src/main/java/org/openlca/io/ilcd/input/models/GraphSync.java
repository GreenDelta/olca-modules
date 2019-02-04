package org.openlca.io.ilcd.input.models;

import java.util.ArrayList;
import java.util.UUID;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.ilcd.models.Model;
import org.openlca.io.Categories;

/**
 * Synchronizes a transformed graph with a database. It assumes that all
 * processes and link flows are created new.
 */
class GraphSync {

	private final IDatabase db;

	GraphSync(IDatabase db) {
		this.db = db;
	}

	ProductSystem sync(Model model, Graph g) {

		// first, create and link the reference flow
		Process refProc = g.root.process;
		Exchange qRef = refProc.quantitativeReference;
		if (qRef != null && qRef.flow != null) {
			Flow flow = qRef.flow;
			flow.refId = UUID.randomUUID().toString();
			category(flow);
			qRef.flow = new FlowDao(db).insert(flow);
			FlowPropertyFactor factor = flow.getReferenceFactor();
			qRef.flowPropertyFactor = factor;
		}

		g.eachLink(this::syncFlows);
		syncProcesses(g);
		ProductSystem system = new ProductSystem();
		IO.mapMetaData(model, system);
		Category c = Categories.findOrCreateRoot(db,
				ModelType.PRODUCT_SYSTEM, "eILCD models");
		system.category = c;
		mapGraph(g, system);
		mapQRef(g, system);
		mapParams(g, system);
		ProductSystemDao dao = new ProductSystemDao(db);
		return dao.insert(system);
	}

	private void syncProcesses(Graph g) {
		ProcessDao dao = new ProcessDao(db);
		g.eachNode(node -> {
			Process p = node.process;
			p.refId = UUID.randomUUID().toString();
			category(p);
			node.process = dao.insert(p);
		});
	}

	private void syncFlows(Link link) {
		Flow flow = link.input.flow;
		flow.refId = UUID.randomUUID().toString();
		category(flow);
		flow = new FlowDao(db).insert(flow);
		link.input.flow = flow;
		link.output.flow = flow;
		FlowPropertyFactor factor = flow.getReferenceFactor();
		if (factor == null)
			return;
		link.input.flowPropertyFactor = factor;
		link.output.flowPropertyFactor = factor;
	}

	/**
	 * Creates a copy of the category of the given entity under the `eILCD
	 * models` tree.
	 */
	private void category(CategorizedEntity e) {
		if (e == null || e.category == null)
			return;
		Category c = e.category;
		ModelType type = c.getModelType();
		ArrayList<String> names = new ArrayList<>();
		while (c != null) {
			names.add(0, c.name);
			c = c.category;
		}
		names.add(0, "eILCD models");
		String[] path = names.toArray(new String[names.size()]);
		c = Categories.findOrAdd(db, type, path);
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
		Process refProc = g.root.process;
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
		g.eachNode(node -> {
			node.params.forEach((name, value) -> {
				if (name == null || value == null) {
					return;
				}
				ParameterRedef redef = new ParameterRedef();
				redef.name = name;
				redef.value = value;
				redef.contextId = node.process.id;
				redef.contextType = ModelType.PROCESS;
				system.parameterRedefs.add(redef);
			});
		});
	}
}
