package org.openlca.core.library;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.database.ActorDao;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.EntityCache;
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
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.matrix.FastMatrixBuilder;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.format.CSCMatrix;
import org.openlca.core.matrix.format.HashPointMatrix;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.matrix.io.npy.Npy;
import org.openlca.core.matrix.io.npy.Npz;
import org.openlca.core.matrix.solvers.IMatrixSolver;
import org.openlca.core.model.ProductSystem;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.ZipStore;
import org.openlca.jsonld.output.JsonExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibraryExport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final IDatabase db;
	private final File folder;
	private IMatrixSolver solver;
	private LibraryInfo info;

	public LibraryExport(IDatabase db, File folder) {
		this.db = db;
		this.folder = folder;
	}

	public LibraryExport solver(IMatrixSolver solver) {
		this.solver = solver;
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
				log.info("write matrices A and B");
				writeMatrix("A", d.techMatrix);
				writeMatrix("B", d.enviMatrix);
				log.info("finished with A and B");
				writeIndices(d);
			});

			if (solver != null) {
				threadPool.execute(() -> {
					log.info("create matrix INV");
					var inv = solver.invert(d.techMatrix);
					writeMatrix("INV", inv);
					log.info("create matrix M");
					var m = solver.multiply(d.enviMatrix, inv);
					writeMatrix("M", m);
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
			return Optional.empty();
		var system = ProductSystem.of(process.get());
		system.withoutNetwork = true;
		var setup = new CalculationSetup(system);
		setup.withRegionalization = info.isRegionalized;
		var data = new FastMatrixBuilder(db, setup).build();
		log.info("finished with building matrices");
		return Optional.of(data);
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

	private void writeIndices(MatrixData data) {
		log.info("write matrix indices");
		new IndexWriter(folder, data, db).run();
		var ecache = EntityCache.create(db);

		var techArray = new JsonArray();
		data.techIndex.each((index, product) -> {
			var idxObj = new JsonObject();
			idxObj.addProperty("index", index);
			idxObj.add("process", Json.asRef(product.process, ecache));
			idxObj.add("flow", Json.asRef(product.flow, ecache));
			techArray.add(idxObj);
		});
		Json.write(techArray, new File(folder, "index_A.json"));
		log.info("wrote index A");

		var enviArray = new JsonArray();
		data.flowIndex.each((index, iFlow) -> {
			var idxObj = new JsonObject();
			idxObj.addProperty("index", index);
			idxObj.add("flow", Json.asRef(iFlow.flow, ecache));
			idxObj.addProperty("isInput", iFlow.isInput);
			if (iFlow.location != null) {
				idxObj.add("location", Json.asRef(iFlow.location, ecache));
			}
			enviArray.add(idxObj);
		});
		Json.write(enviArray, new File(folder, "index_B.json"));
		log.info("wrote index B");
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
