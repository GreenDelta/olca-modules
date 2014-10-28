package org.openlca.io.ecospold1.output;

import java.io.File;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.ecospold.io.EcoSpoldIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcoSpold1Export {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private File outDir;

	public EcoSpold1Export(File outDir) {
		File dir = new File(outDir, "EcoSpold01");
		if (!dir.exists())
			dir.mkdirs();
		this.outDir = dir;
	}

	public void export(ImpactMethod method) throws Exception {
		IEcoSpold spold = MethodConverter.convert(method);
		String fileName = "lcia_method_" + method.getRefId() + ".xml";
		File file = new File(outDir, fileName);
		EcoSpoldIO.writeTo(file, spold, DataSetType.IMPACT_METHOD);
		log.trace("wrote {} to {}", method, file);
	}

	public void export(Process process) throws Exception {
		IEcoSpold spold = ProcessConverter.convert(process);
		String fileName = "process_" + process.getRefId() + ".xml";
		File file = new File(outDir, fileName);
		EcoSpoldIO.writeTo(file, spold, DataSetType.PROCESS);
		log.trace("wrote {} to {}", process, file);
	}

}
