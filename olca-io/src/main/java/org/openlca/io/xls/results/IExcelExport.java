package org.openlca.io.xls.results;

import java.io.File;

public interface IExcelExport extends Runnable {

	boolean doneWithSuccess();
	
	void setFile(File file);

}
