package org.openlca.io.ilcd.output;

import org.openlca.core.model.Epd;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;

public class EpdExport {

	private final Export exp;

	public EpdExport(Export exp) {
		this.exp = exp;
	}

	public Process run(Epd epd) {
		var p = new Process();
		if (epd == null)
			return p;

		var info = Processes.forceDataSetInfo(p);
		info.uuid = epd.refId;
		var name = Processes.forceProcessName(p);
		exp.add(name.name, epd.name);
		exp.add(info.comment, epd.description);
		Categories.toClassification(epd.category)
				.ifPresent(info.classifications::add);

		exp.store.put(p);
		return p;
	}

}
