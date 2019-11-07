package org.openlca.core.results;

import java.util.Date;
import java.util.UUID;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.SocialAspect;

public class SystemProcess {

	public static Process create(
			IDatabase database,
			CalculationSetup setup,
			SimpleResult result,
			String name) {
		return new SystemProcess(database, setup, result, name).create(false);
	}

	public static Process createWithMetaData(IDatabase database,
			CalculationSetup setup,
			SimpleResult result, String name) {
		return new SystemProcess(database, setup, result, name).create(true);
	}

	private final FlowDao flowDao;
	private final CalculationSetup setup;
	private final SimpleResult result;
	private final String name;

	private SystemProcess(IDatabase database, CalculationSetup setup,
			SimpleResult result, String name) {
		this.flowDao = new FlowDao(database);
		this.setup = setup;
		this.result = result;
		this.name = name;
	}

	private Process create(boolean withMetaData) {
		Process p = new Process();
		p.name = name;
		p.refId = UUID.randomUUID().toString();
		p.processType = ProcessType.LCI_RESULT;
		addRefFlow(p);
		addElemFlows(p);
		if (withMetaData)
			copyMetaData(p);
		return p;
	}

	private void addRefFlow(Process p) {
		Flow product = getProduct();
		Exchange qRef = p.exchange(product);
		qRef.amount = Math.abs(setup.getDemandValue());
		p.quantitativeReference = qRef;
	}

	private Flow getProduct() {
		if (setup == null || setup.productSystem == null)
			return null;
		Exchange ref = setup.productSystem.referenceExchange;
		return ref == null ? null : ref.flow;
	}

	private void addElemFlows(Process p) {
		result.flowIndex.each((i, d) -> {
			double amount = result.getTotalFlowResult(d);
			if (amount == 0)
				return;
			Flow flow = flowDao.getForId(d.id);
			if (flow == null)
				return;
			Exchange e = p.exchange(flow);
			e.isInput = result.flowIndex.isInput(d);
			e.amount = amount;
		});
	}

	private void copyMetaData(Process p) {
		Process refProc = setup.productSystem.referenceProcess;
		if (refProc == null)
			return;
		for (SocialAspect sa : refProc.socialAspects)
			p.socialAspects.add(sa.clone());
		p.socialDqSystem = refProc.socialDqSystem;
		p.category = refProc.category;
		p.defaultAllocationMethod = refProc.defaultAllocationMethod;
		p.description = refProc.description;
		if (refProc.documentation != null)
			p.documentation = refProc.documentation.clone();
		p.infrastructureProcess = refProc.infrastructureProcess;
		p.lastChange = new Date().getTime();
		p.location = refProc.location;
	}
}
