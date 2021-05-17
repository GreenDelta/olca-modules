package org.openlca.core.library;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.ImpactBuilder;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.MatrixConfig;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.TechIndex;
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
	MatrixData data;

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

	public LibraryExport withData(MatrixData data) {
		if (data == null)
			return this;
		this.data = data;
		withInventory = data.techIndex != null;
		withImpacts = data.impactIndex != null;
		withUncertainties = data.techUncertainties != null
												|| data.enviUncertainties != null
												|| data.impactUncertainties != null;
		info.isRegionalized = data.enviIndex != null
													&& data.enviIndex.isRegionalized();
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

		// prepare the matrix data
		if (data == null) {
			data = buildMatrices();
		}
		if (data == null) {
			log.warn("could not build matrices of database");
			return;
		}
		if (withInventory) {
			normalizeInventory(data);
		}

		// write the matrices
		threadPool.execute(() -> {
			log.info("write matrices");
			MatrixExport.toNpy(db, folder, data)
				.writeMatrices();
			new IndexWriter(folder, data, db).run();
		});
		threadPool.execute(this::preCalculate);

		// write library meta-data
		Json.write(info.toJson(), new File(folder, "library.json"));

		try {
			threadPool.shutdown();
			while (true) {
				var finished = threadPool.awaitTermination(1, TimeUnit.HOURS);
				if (finished)
					break;
			}
		} catch (Exception e) {
			throw new RuntimeException("failed to wait for export to finish", e);
		}
	}

	private MatrixData buildMatrices() {
		if (!withInventory && !withImpacts)
			return null;

		// check if we only need impact matrices
		if (!withInventory) {
			var impacts = ImpactIndex.of(db);
			var flowIndex = info.isRegionalized
				? EnviIndex.createRegionalized(db, impacts)
				: EnviIndex.create(db, impacts);
			var data = new MatrixData();
			data.impactIndex = impacts;
			data.enviIndex = flowIndex;
			ImpactBuilder.of(db, flowIndex)
				.withUncertainties(withUncertainties)
				.build()
				.addTo(data);
			return data;
		}

		// build matrices with inventory
		var techIndex = TechIndex.of(db);
		var config = MatrixConfig.of(db, techIndex)
			.withUncertainties(withUncertainties)
			.withRegionalization(info.isRegionalized)
			.withAllocation(allocation);
		if (withImpacts) {
			config.withImpacts(ImpactIndex.of(db));
		}
		return config.build();
	}

	/**
	 * Normalize the columns of the technology matrix A and intervention matrix B to one
	 * unit of output or input for each product or waste flow.
	 */
	private void normalizeInventory(MatrixData data) {
		if (data.techMatrix == null)
			return;
		log.info("normalize matrices to 1 | -1");
		var matrixA = data.techMatrix.asMutable();
		data.techMatrix = matrixA;
		var matrixB = data.enviMatrix.asMutable();
		data.enviMatrix = matrixB;
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
	}

	private void preCalculate() {
		if (!withInventory || data.techMatrix == null)
			return;
		// we only pre-calculate the inverse etc. if the
		// native can be loaded
		if (!Julia.isLoaded()) {
			if (!Julia.load())
				return;
		}
		log.info("create matrix INV");
		var solver = new JuliaSolver();
		var inv = solver.invert(data.techMatrix);
		MatrixExport.toNpy(folder, inv, "INV");
		if (data.enviMatrix == null)
			return;
		log.info("create matrix M");
		var m = solver.multiply(data.enviMatrix, inv);
		MatrixExport.toNpy(folder, m, "M");
	}
}
