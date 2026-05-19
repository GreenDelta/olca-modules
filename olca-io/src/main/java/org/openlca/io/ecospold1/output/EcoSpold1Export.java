package org.openlca.io.ecospold1.output;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openlca.commons.Res;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Process;
import org.openlca.ecospold.IDataSet;
import org.openlca.ecospold.IEcoSpold;
import org.openlca.ecospold.io.DataSetType;
import org.openlca.ecospold.io.EcoSpold;
import org.openlca.util.Dirs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcoSpold1Export implements Closeable {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final EcoSpold1Config config;
	private IEcoSpold singleSpold;
	private final CategoryFile categoryFile;
	private final FlowNameFormatter flowNames;

	private EcoSpold1Export(EcoSpold1Config config) {
		File categoryFile = new File(config.dir, "categories.xml");
		this.categoryFile = new CategoryFile(categoryFile);
		this.config = config;
		this.flowNames = new FlowNameFormatter(config);
	}

	public static EcoSpold1Config of(IDatabase db, File outDir) {
		return new EcoSpold1Config(db, outDir);
	}

	public Res<Void> export(ImpactMethod method) {
		var spold = MethodConverter.convert(method, config);
		var fileName = "lcia_method_" + method.refId + ".xml";
		var file = new File(config.dir, fileName);
		return EcoSpold.write(file, spold);
	}

	public void export(Process process) {
		categoryFile.addCategoriesOf(process);
		var ds = ProcessConverter.convert(process, config, flowNames);
		if (config.singleFile) {
			append(ds);
		} else {
			var factory = DataSetType.PROCESS.getFactory();
			var spold = factory.createEcoSpold();
			spold.getDataset().add(ds);
			var fileName = "process_" + process.refId + ".xml";
			var file = new File(config.dir, fileName);
			EcoSpold.write(file, spold);
			log.trace("wrote {} to {}", process, file);
		}
	}

	private void append(IDataSet ds) {
		if (ds == null) return;
		if (singleSpold == null) {
			var factory = DataSetType.PROCESS.getFactory();
			singleSpold = factory.createEcoSpold();
		}
		singleSpold.getDataset().add(ds);
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
			var file = new File(config.dir, fileName);
			var res = EcoSpold.write(file, singleSpold);
			if (res.isError()) {
				throw new IOException(
					"Failed to write file: " + file + "\n  ->" + res.error());
			}
	}

	public static class EcoSpold1Config {

		final IDatabase db;
		final File dir;

		boolean singleFile = false;
		boolean withDefaults = false;
		boolean writeRefIdInfo = false;

		// config for product names
		boolean withLocationSuffixes;
		boolean withTypeSuffixes;
		boolean withProcessSuffixes;

		EcoSpold1Config(IDatabase db, File dir) {
			this.db = db;
			this.dir = dir;
		}

		/// If set to `true`, the export will write all process data sets into a
		/// single file.
		public EcoSpold1Config writeSingleFile(boolean singleFile) {
			this.singleFile = singleFile;
			return this;
		}

		/// If set to `true`, the export will write default values for fields that
		/// are required by the schema but cannot be filled by the actual data set.
		public EcoSpold1Config writeDefaultValues(boolean createDefaults) {
			this.withDefaults = createDefaults;
			return this;
		}

		/// If set to `true`, the export appends the openLCA reference ID of the
		/// exported model to the data set general comment.
		public EcoSpold1Config writeRefIdInfo(boolean writeRefIdInfo) {
			this.writeRefIdInfo = writeRefIdInfo;
			return this;
		}

		public EcoSpold1Config withLocationSuffixes(boolean withLocationSuffixes) {
			this.withLocationSuffixes = withLocationSuffixes;
			return this;
		}

		public EcoSpold1Config withTypeSuffixes(boolean withTypeSuffixes) {
			this.withTypeSuffixes = withTypeSuffixes;
			return this;
		}

		public EcoSpold1Config withProcessSuffixes(boolean withProcessSuffixes) {
			this.withProcessSuffixes = withProcessSuffixes;
			return this;
		}

		public Res<EcoSpold1Export> create() {
			if (db == null)
				return Res.error("No valid database provided");
			if (dir == null)
				return Res.error("No valid export folder provided");
			try {
				Dirs.createIfAbsent(dir);
				var export = new EcoSpold1Export(this);
				return Res.ok(export);
			} catch (Exception e) {
				return Res.error("Failed to create export folder", e);
			}
		}
	}
}
