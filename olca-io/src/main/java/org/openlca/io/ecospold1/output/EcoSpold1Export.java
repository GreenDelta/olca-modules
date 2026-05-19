package org.openlca.io.ecospold1.output;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openlca.commons.Res;
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
	private final CategoryFile categoryFile;

	public EcoSpold1Export(File outDir) {
		this(outDir, ExportConfig.getDefault());
	}

	public EcoSpold1Export(File outDir, ExportConfig config) {
		File categoryFile = new File(outDir, "categories.xml");
		this.categoryFile = new CategoryFile(categoryFile);
		File dir = new File(outDir, "EcoSpold01");
		if (!dir.exists())
			dir.mkdirs();
		this.outDir = dir;
		this.config = config;
	}

	public Res<Void> export(ImpactMethod method) {
		var spold = MethodConverter.convert(method, config);
		var fileName = "lcia_method_" + method.refId + ".xml";
		var file = new File(outDir, fileName);
		return EcoSpold.write(file, spold);
	}

	public void export(Process process) {
		categoryFile.addCategoriesOf(process);
		var dataSet = ProcessConverter.convert(process, config);
		if (config.isSingleFile()) {
			append(dataSet);
		} else {
			var factory = DataSetType.PROCESS.getFactory();
			var spold = factory.createEcoSpold();
			spold.getDataset().add(dataSet);
			var fileName = "process_" + process.refId + ".xml";
			var file = new File(outDir, fileName);
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

	/// It is important to always close the export. The category file and a
	/// possible single output file (if configured) is only written when `close`
	/// is called.
	@Override
	public void close() throws IOException {
			categoryFile.close();
			if (singleSpold == null)
				return;

			var format = new SimpleDateFormat("yyyy-MM-dd'T'hh-mm-ss");
			var time = format.format(new Date());
			int size = singleSpold.getDataset().size();
			var fileName = "EcoSpold_" + size + "_processes_" + time + ".xml";
			var file = new File(outDir, fileName);
			var res = EcoSpold.write(file, singleSpold);
			if (res.isError()) {
				throw new IOException(
					"Failed to write file: " + file + "\n  ->" + res.error());
			}
	}
}
