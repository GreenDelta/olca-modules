package org.openlca.core.library;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
import org.openlca.core.matrix.ImpactIndex;
import org.openlca.core.matrix.MatrixConfig;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.io.MatrixExport;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.core.model.AllocationMethod;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.Databases;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibraryExport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase db;
	private final File folder;
	private MatrixSolver solver;
	private LibraryInfo info;

	private AllocationMethod allocation;
	private boolean withImpacts;
	private boolean withUncertainties;

	public LibraryExport(IDatabase db, File folder) {
		this.db = db;
		this.folder = folder;
	}

	public LibraryExport solver(MatrixSolver solver) {
		this.solver = solver;
		return this;
	}

	public LibraryExport withAllocation(AllocationMethod method) {
		this.allocation = method;
		return this;
	}

	public LibraryExport withImpacts(boolean b) {
		this.withImpacts = b;
		return this;
	}

	public LibraryExport withUncertainties(boolean b) {
		this.withUncertainties = b;
		return this;
	}

	/**
	 * Optionally set meta-data and configurations of the library that should
	 * be created.
	 */
	public LibraryExport as(LibraryInfo info) {
		this.info = info;
		return this;
	}

	@Override
	public void run() {
		log.info("start library export of database {}", db.getName());
		// create the folder if it does not exist
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				throw new RuntimeException("failed to create folder " + folder);
			}
		}

		if (info == null) {
			info = new LibraryInfo();
			info.name = db.getName();
			info.version = "0.0.1";
			info.hasUncertaintyData = Databases.hasUncertaintyData(db);
		}

		// create a thread pool and start writing the meta-data
		var threadPool = Executors.newFixedThreadPool(8);
		threadPool.execute(this::writeMeta);

		// create matrices and write them
		var data = buildMatrices();
		if (data.isEmpty()) {
			log.warn("could not build matrices of database");
		} else {
			var d = data.get();
			threadPool.execute(() -> {
				log.info("write matrices");
				MatrixExport.toNpy(db, folder, d)
					.writeMatrices();
				log.info("finished with matrices");
				log.info("write matrix indices");
				new IndexWriter(folder, d, db).run();
				log.info("finished with matrix indices");
			});

			if (solver != null) {
				threadPool.execute(() -> {
					log.info("create matrix INV");
					var inv = solver.invert(d.techMatrix);
					MatrixExport.toNpy(folder, inv, "INV");
					log.info("create matrix M");
					var m = solver.multiply(d.flowMatrix, inv);
					MatrixExport.toNpy(folder, m, "M");
					log.info("finished with INV and M");
				});
			}
		}

		// write library meta-data
		Json.write(info.toJson(), new File(folder, "library.json"));

		try {
			threadPool.shutdown();
			threadPool.awaitTermination(1, TimeUnit.DAYS);
		} catch (Exception e) {
			throw new RuntimeException("failed to wait for export to finish", e);
		}
	}

	private Optional<MatrixData> buildMatrices() {
		log.info("start building matrices");

		// TODO: this currently fails if the user wants
		// to build an LCIA library
		// create the configuration options
		var techIndex = TechIndex.unlinkedOf(db);
		var config = MatrixConfig.of(db, techIndex)
			.withUncertainties(withUncertainties)
			.withRegionalization(info.isRegionalized)
			.withAllocation(allocation);
		if (withImpacts) {
			config.withImpacts(ImpactIndex.of(db));
		}
		var data = config.build();
		log.info("finished with building matrices");

		// normalize the columns to 1 | -1
		log.info("normalize matrices to 1 | -1");
		var matrixA = data.techMatrix.asMutable();
		data.techMatrix = matrixA;
		var matrixB = data.flowMatrix.asMutable();
		data.flowMatrix = matrixB;
		int n = matrixA.columns();
		for (int j = 0; j < n; j++) {
			double f = Math.abs(matrixA.get(j, j));
			if (f == 1)
				continue;

			// normalize column j in matrix A
			for (int i = 0; i < n; i++) {
				double val = matrixA.get(i, j);
				if (val == 0)
					continue;
				matrixA.set(i, j, val / f);
			}

			// normalize column j in matrix B
			if (matrixB == null)
				continue;
			int m = matrixB.rows();
			for (int i = 0; i < m; i++) {
				double val = matrixB.get(i, j);
				if (val == 0)
					continue;
				matrixB.set(i, j, val / f);
			}
		}
		log.info("finished matrix normalization");

		return Optional.of(data);
	}

	private void writeMeta() {
		log.info("start writing meta-data");
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
			log.info("finished writing meta-data");
		} catch (Exception e) {
			throw new RuntimeException("failed to write meta data", e);
		}
	}
}
