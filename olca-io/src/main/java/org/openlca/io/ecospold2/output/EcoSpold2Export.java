package org.openlca.io.ecospold2.output;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.ecospold2.Activity;
import org.openlca.ecospold2.DataSet;
import org.slf4j.Logger;

/**
 * Exports a set of processes to the EcoSpold 2 data format to a directory. The
 * process data sets are converted to EcoSpold 2 activity data sets and written
 * to the sub-folder 'Activities' in a given export directory. Additionally,
 * EcoSpold 2 master data are created for the exported processes and written to
 * the 'MasterData' sub-folder.
 * 
 */
public class EcoSpold2Export implements Runnable {

	private Logger log;
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
		try {
			File activityDir = new File(dir, "Activities");
			if (!activityDir.exists())
				activityDir.mkdirs();
			exportProcesses(activityDir);
		} catch (Exception e) {
			log.error("EcoSpold 2 export failed", e);
		}
	}

	private void exportProcesses(File activityDir) {
		for (ProcessDescriptor descriptor : descriptors) {
			String fileName = descriptor.getRefId() == null ? UUID.randomUUID()
					.toString() : descriptor.getRefId();
			File file = new File(activityDir, fileName + ".spold");
			ProcessDao dao = new ProcessDao(database);
			Process process = dao.getForId(descriptor.getId());
			DataSet dataSet = new DataSet();
			Activity activity = createActivity(process);

		}
	}

	private Activity createActivity(Process process) {
		Activity activity = new Activity();
		activity.setName(process.getName());
		activity.setId(process.getRefId());
		activity.setActivityNameId(process.getRefId());
		int type = process.getProcessType() == ProcessType.LCI_RESULT ? 2 : 1;
		activity.setType(type);
		activity.setSpecialActivityType(0); // default
		activity.setGeneralComment(process.getDescription());
		return activity;
	}
}
