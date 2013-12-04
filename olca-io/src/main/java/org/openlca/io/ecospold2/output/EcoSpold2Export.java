package org.openlca.io.ecospold2.output;

import java.io.File;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * Exports a set of processes to the EcoSpold 2 data format to a directory. The
 * process data sets are converted to EcoSpold 2 activity data sets and written
 * to the sub-folder 'Activities' in a given export directory. Additionally,
 * EcoSpold 2 master data are created for the exported processes and written to
 * the 'MasterData' sub-folder.
 * 
 */
public class EcoSpold2Export implements Runnable {

	private File dir;
	private IDatabase database;
	private List<ProcessDescriptor> descriptors;

	public EcoSpold2Export(File dir, IDatabase database,
			List<ProcessDescriptor> descriptors) {
		this.dir = dir;
		this.database = database;
		this.descriptors = descriptors;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
