package org.openlca.io.ecospold2.output;

import org.openlca.core.model.Process;
import org.openlca.ecospold2.ActivityName;
import org.openlca.ecospold2.DataSet;
import org.openlca.ecospold2.UserMasterData;

class MasterData {

	private Process process;
	private DataSet dataSet;

	private MasterData(Process process, DataSet dataSet) {
		this.process = process;
		this.dataSet = dataSet;
	}

	public static void map(Process process, DataSet dataSet) {
		new MasterData(process, dataSet).map();
	}

	private void map() {
		UserMasterData masterData = new UserMasterData();
		dataSet.setMasterData(masterData);

		ActivityName activityName = new ActivityName();
		masterData.getActivityNames().add(activityName);
		activityName.setId(process.getRefId());
		activityName.setName(process.getName());

	}

}
