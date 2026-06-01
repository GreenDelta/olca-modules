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
import org.openlca.ecospold.model.IDataSet;
import org.openlca.ecospold.model.IEcoSpold;
import org.openlca.ecospold.EcoSpold;
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
		this.config = config;
		this.categoryFile = config.withCategoryFile
			? new CategoryFile(new File(config.dir, "categories.xml"))
			: null;
		this.flowNames = new FlowNameFormatter(config);
	}

	public static EcoSpold1Config of(IDatabase db) {
		return new EcoSpold1Config(db);
	}

	public static EcoSpold1Config of(IDatabase db, File outDir) {
		return new EcoSpold1Config(db).withDir(outDir);
	}

	public Res<Void> export(ImpactMethod method) {
		var spold = MethodConverter.convert(method, config);
		var fileName = "lcia_method_" + method.refId + ".xml";
		var file = new File(config.dir, fileName);
		return EcoSpold.write(file, spold);
	}

	public void export(Process process) {
		if (categoryFile != null) {
			categoryFile.addCategoriesOf(process);
		}
		var ds = ProcessConverter.convert(process, config, flowNames);
		if (config.singleFile) {
			append(ds);
		} else {
			var spold = EcoSpold.newProcess();
			spold.getDataSets().add(ds);
			var fileName = "process_" + process.refId + ".xml";
			var file = new File(config.dir, fileName);
			EcoSpold.write(file, spold);
			log.trace("wrote {} to {}", process, file);
		}
	}

	private void append(IDataSet ds) {
		if (ds == null) return;
		if (singleSpold == null) {
			singleSpold = EcoSpold.newProcess();
		}
		singleSpold.getDataSets().add(ds);
	}

	/// It is important to always close the export. The category file and a
	/// possible single output file (if configured) is only written when `close`
	/// is called.
	@Override
	public void close() throws IOException {
		if (categoryFile != null) {
			categoryFile.close();
		}
		if (singleSpold == null)
			return;

		var format = new SimpleDateFormat("yyyy-MM-dd'T'hh-mm-ss");
		var time = format.format(new Date());
		int size = singleSpold.getDataSets().size();
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
		File dir;

		boolean singleFile;
		boolean withDefaults;
		boolean withRefIdInfo;
		boolean withCategoryFile;

		// config for product names
		boolean withLocationSuffixes;
		boolean withTypeSuffixes;
		boolean withProcessSuffixes;

		EcoSpold1Config(IDatabase db) {
			this.db = db;
		}

		public EcoSpold1Config withDir(File dir) {
			this.dir = dir;
			return this;
		}

		public File dir() {
			return dir;
		}

		/// If set to `true`, the export will write all process data sets into a
		/// single file.
		public EcoSpold1Config writeSingleFile(boolean b) {
			this.singleFile = b;
			return this;
		}

		public boolean isWithSingleFile() {
			return singleFile;
		}

		/// If set to `true`, the export will write default values for fields that
		/// are required by the schema but cannot be filled by the actual data set.
		public EcoSpold1Config writeDefaultValues(boolean b) {
			this.withDefaults = b;
			return this;
		}

		public boolean isWithDefaultValues() {
			return withDefaults;
		}

		/// If set to `true`, the export appends the openLCA reference ID of the
		/// exported model to the data set general comment.
		public EcoSpold1Config writeRefIdInfo(boolean b) {
			this.withRefIdInfo = b;
			return this;
		}

		public boolean isWithRefIdInfo() {
			return withRefIdInfo;
		}

		public EcoSpold1Config writeCategoryFile(boolean b) {
			this.withCategoryFile = b;
			return this;
		}

		public boolean isWithCategoryFile() {
			return withCategoryFile;
		}

		public EcoSpold1Config withLocationSuffixes(boolean b) {
			this.withLocationSuffixes = b;
			return this;
		}

		public boolean isWithLocationSuffixes() {
			return withLocationSuffixes;
		}

		public EcoSpold1Config withTypeSuffixes(boolean b) {
			this.withTypeSuffixes = b;
			return this;
		}

		public boolean isWithTypeSuffixes() {
			return withTypeSuffixes;
		}

		public EcoSpold1Config withProcessSuffixes(boolean b) {
			this.withProcessSuffixes = b;
			return this;
		}

		public boolean isWithProcessSuffixes() {
			return withProcessSuffixes;
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
