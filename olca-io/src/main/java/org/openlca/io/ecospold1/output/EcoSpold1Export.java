package org.openlca.io.ecospold1.output;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.IEcoSpoldFactory;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.ecospold.io.EcoSpold;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcoSpold1Export implements Closeable {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final File outDir;
	private final ExportConfig config;
	private IEcoSpold singleSpold;
	private final CategoryWriter categoryWriter;

	public EcoSpold1Export(File outDir) {
		this(outDir, ExportConfig.getDefault());
	}

	public EcoSpold1Export(File outDir, ExportConfig config) {
		File categoryFile = new File(outDir, "categories.xml");
		categoryWriter = new CategoryWriter(categoryFile, config);
		File dir = new File(outDir, "EcoSpold01");
		if (!dir.exists())
			dir.mkdirs();
		this.outDir = dir;
		this.config = config;
	}

	public void export(ImpactMethod method) {
		IEcoSpold spold = MethodConverter.convert(method, config);
		String fileName = "lcia_method_" + method.refId + ".xml";
		File file = new File(outDir, fileName);
		EcoSpold.write(file, spold);
		log.trace("wrote {} to {}", method, file);
	}

	public void export(Process process) {
		categoryWriter.takeFrom(process);
		IDataSet dataSet = ProcessConverter.convert(process, config);
		if (config.isSingleFile())
			append(dataSet);
		else {
			IEcoSpoldFactory factory = DataSetType.PROCESS.getFactory();
			IEcoSpold spold = factory.createEcoSpold();
			spold.getDataset().add(dataSet);
			String fileName = "process_" + process.refId + ".xml";
			File file = new File(outDir, fileName);
			EcoSpold.write(file, spold);
			log.trace("wrote {} to {}", process, file);
		}
	}

	private void append(IDataSet dataSet) {
		if (dataSet == null)
			return;
		if (singleSpold == null) {
			IEcoSpoldFactory factory = DataSetType.PROCESS.getFactory();
			singleSpold = factory.createEcoSpold();
		}
		singleSpold.getDataset().add(dataSet);
	}

	/**
	 * It is very important to call this method if multiple processes should be
	 * exported into a single file as this single file is written when this
	 * method is called.
	 */
	@Override
	public void close() throws IOException {
		if (singleSpold == null)
			return;
		try {
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd'T'hh-mm-ss");
			String time = format.format(new Date());
			int size = singleSpold.getDataset().size();
			String fileName = "EcoSpold_" + size + "_processes_" + time
					+ ".xml";
			File file = new File(outDir, fileName);
			EcoSpold.write(file, singleSpold);
			log.trace("wrote {} processes to {}", size, file);
			categoryWriter.close();
		} catch (Exception e) {
			log.error("export failed", e);
			throw new IOException(e);
		}
	}

}
