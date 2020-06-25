package org.openlca.core.library;

import java.io.File;
import java.util.List;

import org.openlca.core.database.ActorDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.SocialIndicatorDao;
import org.openlca.core.database.SourceDao;
import org.openlca.core.database.UnitGroupDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.matrix.FastMatrixBuilder;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.io.npy.Npy;
import org.openlca.core.matrix.io.npy.Npz;
import org.openlca.core.model.ProductSystem;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.output.JsonExport;

public class LibraryExport implements Runnable {

	private final IDatabase db;
	private final File folder;

	public LibraryExport(IDatabase db, File folder) {
		this.db = db;
		this.folder = folder;
	}

	@Override
	public void run() {

		// create the folder if it does not exist
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				throw new RuntimeException("failed to create folder " + folder);
			}
		}

		writeMeta();
		// writeMatrixData();

	}

	private void writeMatrixData() {
		// create an arbitrary product system for the fast matrix builder
		// TODO this does not work when the process of that system has no
		// quantitative reference flow which is a valid provider flow
		var procDao = new ProcessDao(db);
		var process = procDao.getDescriptors()
				.stream()
				.map(d -> procDao.getForId(d.id))
				// .filter(p -> !Processes.getProviderFlows(p).isEmpty())
				.findFirst();
		if (process.isEmpty())
			return;
		var system = ProductSystem.of(process.get());
		system.withoutNetwork = true;
		var setup = new CalculationSetup(system);
		var data = new FastMatrixBuilder(db, setup).build();
		writeMatrix("A", data.techMatrix);
		writeMatrix("B", data.enviMatrix);
	}

	private void writeMatrix(String name, IMatrix matrix) {
		var m = matrix;
		if (m instanceof HashPointMatrix) {
			m = CSCMatrix.of(m);
		}
		if (m instanceof CSCMatrix) {
			var csc = (CSCMatrix) m;
			Npz.save(new File(folder, name + ".npz"), csc);
		} else {
			Npy.save(new File(folder, name + ".npy"), m);
		}
	}

	private void writeMeta() {
		try (var zip = ZipStore.open(new File(folder, "meta.zip"))) {
			var exp = new JsonExport(db, zip);
			exp.setLibraryExport(true);
			exp.setExportReferences(false);
			exp.setExportDefaultProviders(false);
			List.of(new ActorDao(db),
					new CategoryDao(db),
					new CurrencyDao(db),
					new DQSystemDao(db),
					new FlowDao(db),
					new FlowPropertyDao(db),
					new ImpactCategoryDao(db),
					new ImpactMethodDao(db),
					new LocationDao(db),
					new ParameterDao(db),
					new ProcessDao(db),
					new SocialIndicatorDao(db),
					new SourceDao(db),
					new UnitGroupDao(db))
					.forEach(dao -> dao.getDescriptors()
							.forEach(d -> exp.write(dao.getForId(d.id))));
		} catch (Exception e) {
			throw new RuntimeException("failed to write meta data", e);
		}
	}

	public static void main(String[] args) throws Exception {
		var dbPath = "C:/Users/Win10/openLCA-data-1.4/databases/ecoinvent_36_cutoff_unit_20191212";
		var db = new DerbyDatabase(new File(dbPath));
		System.out.println("Start export");
		long start = System.currentTimeMillis();
		new LibraryExport(db, new File("C:/Users/Win10/Desktop/rems/ei2")).run();
		double time = (System.currentTimeMillis() - start) / 1000d;
		System.out.println("Done, it took " + String.format("%.0f seconds", time));
		db.close();
	}
}
