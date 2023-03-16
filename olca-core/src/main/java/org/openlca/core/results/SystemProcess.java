package org.openlca.core.results;

import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;

public class SystemProcess {

	public static Process create(
			IDatabase db, CalculationSetup setup, LcaResult result, String name
	) {
		return new SystemProcess(db, setup, result, name).create(false);
	}

	public static Process createWithMetaData(
			IDatabase db, CalculationSetup setup, LcaResult result, String name
	) {
		return new SystemProcess(db, setup, result, name).create(true);
	}

	private final IDatabase db;
	private final CalculationSetup setup;
	private final LcaResult result;
	private final String name;

	private SystemProcess(
			IDatabase db, CalculationSetup setup, LcaResult result, String name
	) {
		this.db = db;
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
			double amount = result.getTotalFlowValueOf(f);
			if (amount == 0 || f.flow() == null || f.isVirtual())
				return;
			var flow = db.get(Flow.class, f.flow().id);
			if (flow == null)
				return;
			var exchange = f.isInput()
					? p.input(flow, amount)
					: p.output(flow, amount);
			if (f.location() != null) {
				exchange.location = db.get(Location.class, f.location().id);
			}
		});
	}

	private void copyMetaData(Process p) {
		var refProc = setup.process();
		if (refProc == null)
			return;
		for (var sa : refProc.socialAspects) {
			p.socialAspects.add(sa.copy());
		}
		p.socialDqSystem = refProc.socialDqSystem;
		p.category = refProc.category;
		p.defaultAllocationMethod = refProc.defaultAllocationMethod;
		p.description = refProc.description;
		if (refProc.documentation != null) {
			p.documentation = refProc.documentation.copy();
		}
		p.infrastructureProcess = refProc.infrastructureProcess;
		p.lastChange = System.currentTimeMillis();
		p.location = refProc.location;
	}
}
