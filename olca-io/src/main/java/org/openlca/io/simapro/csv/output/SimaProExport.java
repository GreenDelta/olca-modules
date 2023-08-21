package org.openlca.io.simapro.csv.output;

import java.io.File;
import java.util.Collection;

import org.openlca.core.database.IDatabase;
import org.openlca.core.io.maps.FlowMap;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class SimaProExport {

	final IDatabase db;
	final Collection<ProcessDescriptor> processes;
	FlowMap flowMap;

	// config for product names
	boolean withLocationSuffixes;
	boolean withTypeSuffixes;
	boolean withProcessSuffixes;

	private SimaProExport(IDatabase db, Collection<ProcessDescriptor> processes) {
		this.db = db;
		this.processes = processes;
	}

	public static SimaProExport of(IDatabase db, Collection<ProcessDescriptor> processes) {
		return new SimaProExport(db, processes);
	}

	public SimaProExport withFlowMap(FlowMap flowMap) {
		this.flowMap = flowMap;
		return this;
	}

	public SimaProExport withLocationSuffixes(boolean b) {
		this.withLocationSuffixes = b;
		return this;
	}

	public SimaProExport withTypeSuffixes(boolean b) {
		this.withTypeSuffixes = b;
		return this;
	}

	public SimaProExport withProcessSuffixes(boolean b) {
		this.withProcessSuffixes = b;
		return this;
	}

	public void writeTo(File file) {
		if (db == null
				|| file == null
				|| processes == null
				|| processes.isEmpty())
			return;
		try (var csv = CsvWriter.on(file)) {
			csv.writerHeader(db.getName());
			new ProcessWriter(this, csv).write();
		}
	}
}
