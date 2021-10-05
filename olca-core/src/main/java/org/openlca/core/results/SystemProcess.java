package org.openlca.core.results;

import java.util.Date;
import java.util.UUID;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
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
		if (setup == null)
			return;
		var flow = setup.flow();
		if (flow == null)
			return;
		double amount = Math.abs(setup.demand());
		p.quantitativeReference = flow.flowType == FlowType.WASTE_FLOW
			? p.input(flow, amount)
			: p.output(flow, amount);
	}

	private void addElemFlows(Process p) {
		result.enviIndex().each((i, f) -> {
			double amount = result.getTotalFlowResult(f);
			if (amount == 0)
				return;
			Flow flow = flowDao.getForId(f.flow().id);
			if (flow == null)
				return;
			if (f.isInput()) {
				p.input(flow, amount);
			} else {
				p.output(flow, amount);
			}
		});
	}

	private void copyMetaData(Process p) {
		Process refProc = setup.process();
		if (refProc == null)
			return;
		for (SocialAspect sa : refProc.socialAspects)
			p.socialAspects.add(sa.copy());
		p.socialDqSystem = refProc.socialDqSystem;
		p.category = refProc.category;
		p.defaultAllocationMethod = refProc.defaultAllocationMethod;
		p.description = refProc.description;
		if (refProc.documentation != null)
			p.documentation = refProc.documentation.copy();
		p.infrastructureProcess = refProc.infrastructureProcess;
		p.lastChange = new Date().getTime();
		p.location = refProc.location;
	}
}
