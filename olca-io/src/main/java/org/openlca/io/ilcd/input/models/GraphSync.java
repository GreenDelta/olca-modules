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
		Exchange qRef = refProc.getQuantitativeReference();
		if (qRef != null && qRef.flow != null) {
			Flow flow = qRef.flow;
			flow.setRefId(UUID.randomUUID().toString());
			category(flow);
			qRef.flow = new FlowDao(db).insert(flow);
			FlowPropertyFactor factor = flow.getReferenceFactor();
			qRef.flowPropertyFactor = factor;
		}

		g.eachLink(this::syncFlows);
		syncProcesses(g);
		ProductSystem system = new ProductSystem();
		IO.mapMetaData(model, system);
		Category c = Categories.createRoot(db,
				ModelType.PRODUCT_SYSTEM, "eILCD models");
		system.setCategory(c);
		mapGraph(g, system);
		mapQRef(g, system);
		ProductSystemDao dao = new ProductSystemDao(db);
		return dao.insert(system);
	}

	private void syncProcesses(Graph g) {
		ProcessDao dao = new ProcessDao(db);
		g.eachNode(node -> {
			Process p = node.process;
			p.setRefId(UUID.randomUUID().toString());
			category(p);
			dao.insert(p);
		});
	}

	private void syncFlows(Link link) {
		Flow flow = link.input.flow;
		flow.setRefId(UUID.randomUUID().toString());
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
		if (e == null || e.getCategory() == null)
			return;
		Category c = e.getCategory();
		ModelType type = c.getModelType();
		ArrayList<String> names = new ArrayList<>();
		while (c != null) {
			names.add(0, c.getName());
			c = c.getCategory();
		}
		names.add(0, "eILCD models");
		String[] path = names.toArray(new String[names.size()]);
		c = Categories.findOrAdd(db, type, path);
		e.setCategory(c);
	}

	private void mapGraph(Graph g, ProductSystem system) {
		g.eachLink(link -> {
			system.getProcesses().add(link.provider.process.getId());
			system.getProcesses().add(link.recipient.process.getId());
			ProcessLink pLink = new ProcessLink();
			Flow flow = link.input.flow;
			pLink.flowId = flow.getId();
			if (flow.getFlowType() == FlowType.PRODUCT_FLOW) {
				pLink.providerId = link.provider.process.getId();
				pLink.processId = link.recipient.process.getId();
				pLink.exchangeId = link.input.getId();
			} else {
				pLink.providerId = link.recipient.process.getId();
				pLink.processId = link.provider.process.getId();
				pLink.exchangeId = link.output.getId();
			}
			system.getProcessLinks().add(pLink);
		});
	}

	private void mapQRef(Graph g, ProductSystem system) {
		Process refProc = g.root.process;
		system.setReferenceProcess(refProc);
		Exchange qRef = refProc.getQuantitativeReference();
		system.setReferenceExchange(qRef);
		if (qRef != null) {
			system.setTargetFlowPropertyFactor(qRef.flowPropertyFactor);
			system.setTargetUnit(qRef.unit);
			double amount = qRef.amount;
			if (g.root.scalingFactor != null) {
				amount *= g.root.scalingFactor;
			}
			system.setTargetAmount(amount);
		}
	}
}
