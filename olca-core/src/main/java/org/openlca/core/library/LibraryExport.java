package org.openlca.core.library;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.ImpactBuilder;
import org.openlca.core.matrix.ImpactIndex;
import org.openlca.core.matrix.MatrixConfig;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.io.MatrixExport;
import org.openlca.core.model.AllocationMethod;
import org.openlca.jsonld.Json;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibraryExport implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	final IDatabase db;
	final File folder;
	final LibraryInfo info;

	AllocationMethod allocation;
	boolean withInventory = true;
	boolean withImpacts;
	boolean withUncertainties;

	public LibraryExport(IDatabase db, File folder) {
		this.db = db;
		this.folder = folder;
		this.info = new LibraryInfo();
		info.name = db.getName();
		info.version = "0.0.1";
	}

	public LibraryExport withAllocation(AllocationMethod method) {
		this.allocation = method;
		return this;
	}

	public LibraryExport withInventory(boolean b) {
		this.withInventory = b;
		return this;
	}

	public LibraryExport withImpacts(boolean b) {
		this.withImpacts = b;
		return this;
	}

	public LibraryExport withConfig(LibraryInfo info) {
		if (info == null)
			return this;
		this.info.name = info.name;
		this.info.version = info.version;
		this.info.isRegionalized = info.isRegionalized;
		this.info.description = info.description;
		this.info.dependencies.clear();
		this.info.dependencies.addAll(info.dependencies);
		return this;
	}

	public LibraryExport withUncertainties(boolean b) {
		this.withUncertainties = b;
		return this;
	}

	@Override
	public void run() {
		log.info("start library export of database {}", db.getName());
		// create the folder if it does not exist
		if (!folder.exists()) {
			try {
				Files.createDirectories(folder.toPath());
			} catch (Exception e) {
				throw new RuntimeException("failed to create folder " + folder, e);
			}
		}

		// create a thread pool and start writing the meta-data
		var threadPool = Executors.newFixedThreadPool(4);
		threadPool.execute(new MetaDataExport(this));

		// create matrices and write them
		var data = buildMatrices();
		if (data == null) {
			log.warn("could not build matrices of database");
		} else {
			threadPool.execute(() -> {
				log.info("write matrices");
				MatrixExport.toNpy(db, folder, data)
					.writeMatrices();
				log.info("finished with matrices");
				log.info("write matrix indices");
				new IndexWriter(folder, data, db).run();
				log.info("finished with matrix indices");
			});

			if (withInventory && Julia.isLoaded()) {
				threadPool.execute(() -> {
					log.info("create matrix INV");
					var solver = new JuliaSolver();
					var inv = solver.invert(data.techMatrix);
					MatrixExport.toNpy(folder, inv, "INV");
					log.info("create matrix M");
					var m = solver.multiply(data.flowMatrix, inv);
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

	private MatrixData buildMatrices() {
		if (!withInventory && !withImpacts)
			return null;

		log.info("start building matrices");

		if (!withInventory) {
			// only build impact matrices
			var impacts = ImpactIndex.of(db);
			var flowIndex = info.isRegionalized
				? FlowIndex.createRegionalized(db, impacts)
				: FlowIndex.create(db, impacts);
			var data = new MatrixData();
			data.impactIndex = impacts;
			data.flowIndex = flowIndex;
			ImpactBuilder.of(db, flowIndex)
				.withUncertainties(withUncertainties)
				.build()
				.addTo(data);
			return data;
		}

		// TODO: this currently fails if the user wants
		// to build an LCIA library
		// create the configuration options
		var techIndex = TechIndex.of(db);
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

		return data;
	}

}
